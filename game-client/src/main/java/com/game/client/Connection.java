package com.game.client;

import java.net.InetSocketAddress;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.apache.mina.core.RuntimeIoException;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.game.client.handlers.PacketHandler;
import com.game.common.codec.Packet;
import com.game.common.codec.PacketBuilder;
import com.game.common.codec.PacketCodecFactory;
import com.game.common.util.ISAACAlgorithm;
import com.game.common.util.PersistenceManager;

public class Connection implements IoHandler {
	private static final Logger log = LoggerFactory.getLogger(Connection.class);

	public static final int PING_DELAY = 30000;
	public static final int PACKET_PROCESSING_DELAY = 100;

	protected final Client client;
	protected final NioSocketConnector connector;
	protected final Map<Packet.Type, PacketHandler> packetHandlers;
	protected final Queue<Packet> packets;
	protected IoSession session;
	protected long lastPingUpdate, lastPacketUpdate;

	public Connection(Client client) {
		this.client = client;

		packetHandlers = this.loadPacketHandlers();

		packets = new LinkedList<Packet>();

		connector = new NioSocketConnector();
		connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new PacketCodecFactory()));
		connector.setHandler(this);

		lastPingUpdate = 0;
		lastPacketUpdate = 0;
	}

	private Map<Packet.Type, PacketHandler> loadPacketHandlers() {
		Map<Packet.Type, PacketHandler> handlers = new HashMap<Packet.Type, PacketHandler>();
		URL path = PacketHandler.class.getResource("packethandlers.xml");
		if (path == null) {
			// fatal error
			throw new RuntimeException("Unable to find packethandlers.xml resource");
		}

		PersistenceManager.PacketHandler[] definitions = (PersistenceManager.PacketHandler[]) PersistenceManager.load(path);
		for (PersistenceManager.PacketHandler definition : definitions) {
			try {
				PacketHandler handler = (PacketHandler) definition.handler.newInstance();
				for (Packet.Type type : definition.types)
					handlers.put(type, handler);
			}
			catch (Exception e) {
				// fatal error
				throw new RuntimeException("Error loading packet handlers: " + e.getMessage());
			}
		}

		if (log.isDebugEnabled())
			log.debug("Loaded " + handlers.size() + " packet handlers");

		return handlers;
	}

	public void open(String hostname, int port) throws RuntimeIoException {
		if (session != null)
			return;

		InetSocketAddress address = new InetSocketAddress(hostname, port);

		ConnectFuture future = connector.connect(address);
		future.awaitUninterruptibly();
		session = future.getSession();

		log.info("Connected to server: " + address.getHostName() + ":" + address.getPort());
	}

	public void enableEncryption(long encryptionSeed, long decryptionSeed) {
		session.setAttribute("encrypter", new ISAACAlgorithm(encryptionSeed));
		session.setAttribute("decrypter", new ISAACAlgorithm(decryptionSeed));
	}

	public void close() {
		if (session == null)
			return;

		IoSession session = this.session;
		this.session = null;

		if (session.isConnected())
			session.close(true).awaitUninterruptibly();
	}

	public void destroy() {
		this.close();
		connector.dispose();
	}

	public void update(long now) {
		// Process the queued packets
		if (now - lastPacketUpdate > PACKET_PROCESSING_DELAY) {
			lastPacketUpdate = now;

			synchronized (packets) {
				for (Packet message : packets)
					this.processPacket(message);

				packets.clear();
			}
		}

		// If we aren't logged in, ignore the rest...
		if (!client.isLoggedIn())
			return;

		// Send a ping to keep the connection alive
		if (now - lastPingUpdate > PING_DELAY) {
			lastPingUpdate = now;

			PacketBuilder builder = new PacketBuilder(Packet.Type.PING_SEND);
			this.write(builder);
		}
	}

	private boolean processPacket(Packet message) {
		if (session == null)
			return true;

		if (!client.isLoggedIn() && !session.containsAttribute("pending"))
			return true;

		PacketHandler handler = packetHandlers.get(message.getType());
		// If there's no handler then close the session (forcefully)
		if (handler == null) {
			log.error("Unhandled packet: " + message);
			session.close(true);
			return false;
		}

		try {
			handler.handlePacket(client, client.getWorldManager(), message);
			return true;
		}
		// Something went wrong (malformed packet?), close the session (forcefully)
		catch (Exception e) {
			log.warn("Error decoding packet: " + e.getMessage());
			session.close(true);
			return false;
		}
	}

	public void write(PacketBuilder packet) {
		session.write(packet);
	}

	@Override
	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
		log.warn("Error from server connection: " + cause.getMessage());
		cause.printStackTrace();

		// Close the session (forcefully)
		session.close(true);
	}

	@Override
	public void messageReceived(IoSession session, Object o) throws Exception {
		Packet message = (Packet) o;
		// We should only process 1 packet from a session at a time
		synchronized (session) {
			// If logged in queue the packet for processing
			if (client.isLoggedIn() || session.containsAttribute("pending")) {
				synchronized (packets) {
					packets.add(message);
				}
			}
			// If we aren't logged in then this must be the login response
			else if (message.getType() == Packet.Type.LOGIN_RESPONSE) {
				// Mark this session as pending login
				session.setAttribute("pending");

				// Queue the packet
				synchronized (packets) {
					packets.add(message);
				}
			}
			// Otherwise this packet shouldn't be here!
			else {
				log.error("Client isn't logged in, but received a packet: " + message);
				session.close(true);
			}
		}
	}

	@Override
	public void messageSent(IoSession session, Object o) throws Exception {
		lastPingUpdate = System.currentTimeMillis();
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		if (this.session == null)
			return;

		client.handleLogout();
	}

	@Override
	public void sessionCreated(IoSession session) throws Exception { }

	@Override
	public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
		// When a session becomes idle, close it (gracefully)
		session.close(false);
	}

	@Override
	public void sessionOpened(IoSession session) throws Exception { }
}
