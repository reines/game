package com.game.server;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.security.PrivateKey;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.game.common.codec.Packet;
import com.game.common.codec.PacketCodecFactory;
import com.game.common.model.Item;
import com.game.common.util.PersistenceManager;
import com.game.server.db.Database;
import com.game.server.handlers.PacketHandler;
import com.game.server.model.Player;

public class Server implements IoHandler, Runnable {
	private static final Logger log = LoggerFactory.getLogger(Server.class);

	public static final int DEFAULT_PORT = 36954;
	public static final int LOOP_DELAY = 100;

	protected static final Options options;

	static {
		options = new Options();

		options.addOption("h", "help", false, "print this help.");
		options.addOption("p", "port", true, "port number to listen on, default " + DEFAULT_PORT + ".");
		options.addOption("b", "bind", true, "IP address to bind to, default all.");
	}

	public static void main(String[] args) {
		try {
			CommandLineParser parser = new PosixParser();
			CommandLine config = parser.parse(options, args);

			if (config.hasOption("h")) {
				HelpFormatter help = new HelpFormatter();
				help.printHelp("java " + Server.class.getSimpleName(), options);
				return;
			}

			Server server = new Server(config);
			server.start();
		}
		catch (ParseException e) {
			log.error("Error parsing command line options: " + e);
		}
		catch (RuntimeException e) {
			log.error(e.getMessage());
		}
	}

	protected final CommandLine config;
	protected final NioSocketAcceptor acceptor;
	protected final Map<Packet.Type, PacketHandler> packetHandlers;
	protected final Database db;
	protected final WorldManager world;
	protected final PrivateKey privateKey;
	protected final Queue<Packet> packets;
	protected boolean running;
	protected long lastPacketUpdate;

	private Server(CommandLine config) {
		this.config = config;

		packetHandlers = this.loadPacketHandlers();

		// Connection to the MySQL database
		File dbConfig = new File("database.conf.xml");
		if (!dbConfig.exists()) {
			// fatal error
			throw new RuntimeException("Unable to load database config file: " + dbConfig.getAbsolutePath());
		}

		privateKey = (PrivateKey) PersistenceManager.load(Server.class.getResource("privatekey.xml"));

		// Pre-load the item definitions
		Item.load();

		db = new Database(dbConfig, this);

		acceptor = new NioSocketAcceptor();
		acceptor.setReuseAddress(true);

		acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new PacketCodecFactory()));

		// Set the idle timeout to 5 seconds - once a client has logged in this gets increased
		acceptor.getSessionConfig().setIdleTime(IdleStatus.READER_IDLE, 5);
		acceptor.setHandler(this);

		world = new WorldManager(this);

		packets = new LinkedList<Packet>();

		running = false;
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

	public Database getDatabase() {
		return db;
	}

	public WorldManager getWorldManager() {
		return world;
	}

	@Override
	public void run() {
		while (running) {
			long start = System.currentTimeMillis();
			this.update(start);

			int duration = (int) (System.currentTimeMillis() - start);
			if (duration < LOOP_DELAY) {
				try { Thread.sleep(LOOP_DELAY - duration); } catch (InterruptedException e) { }
			}
		}

		// TODO: Shut down the server
	}

	private void update(long now) {
		// Process the queued packets
		synchronized (packets) {
			for (Packet message : packets)
				this.processPacket(message);

			packets.clear();
		}

		// Update the world
		world.update(now);
	}

	private boolean processPacket(Packet message) {
		IoSession session = message.getSession();
		// Confirm the client is still connected and valid
		if (!session.isConnected() || (!session.containsAttribute("client") && !session.containsAttribute("pending")))
			return false;

		Player client = (Player) session.getAttribute("client");

		PacketHandler handler = packetHandlers.get(message.getType());
		// If there's no handler then close the session (forcefully)
		if (handler == null) {
			log.warn("Unhandled packet from: " + client);
			session.close(true);
			return false;
		}

		try {
			handler.handlePacket(this, world, client, message);
			return true;
		}
		// Something went wrong (malformed packet?), close the session (forcefully)
		catch (Exception e) {
			log.warn("Error decoding packet from: " + client);
			e.printStackTrace();
			session.close(true);
			return false;
		}
	}

	private void start() {
		if (running)
			return;

		int port = DEFAULT_PORT;
		if (config.hasOption("p")) {
			try {
				port = Integer.parseInt(config.getOptionValue("p"));
			}
			catch (NumberFormatException e) {
				// fatal error
				throw new RuntimeException("Invalid port number: " + config.getOptionValue("p"));
			}
		}

		InetSocketAddress listen = null;
		if (config.hasOption("b"))
			listen = new InetSocketAddress(config.getOptionValue("b"), port);
		else
			listen = new InetSocketAddress(port);

		running = true;
		new Thread(this).start();

		try {
			acceptor.bind(listen);
			System.out.println("Server listening on: " + listen.getHostName() + ":" + listen.getPort());
		}
		catch (IOException e) {
			// fatal error
			throw new RuntimeException("Unable to bind to: " + listen.getHostName() + ":" + listen.getPort());
		}
	}

	public void stop() {
		running = false;
	}

	@Override
	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
		log.warn("Error from " + (session.containsAttribute("client") ? session.getAttribute("client") : "new") + " connection: " + cause.getMessage());

		// Close the session (forcefully)
		session.close(true);
	}

	@Override
	public void messageReceived(IoSession session, Object o) throws Exception {
		Packet message = (Packet) o;
		// We should only process 1 packet from a session at a time
		synchronized (session) {
			// If there is a client, queue the packet for processing
			if (session.containsAttribute("client") || session.containsAttribute("pending")) {
				synchronized (packets) {
					packets.add(message);
				}
			}
			// If there isn't a client attached then this must be the login request
			else if (message.getType() == Packet.Type.LOGIN_SEND) {
				// Decrypt the login request
				message.decrypt(privateKey);

				// Mark this session as pending login
				session.setAttribute("pending");

				// Queue the packet
				synchronized (packets) {
					packets.add(message);
				}
			}
			// Otherwise this packet shouldn't be here!
			else {
				log.warn("Client isn't logged in, but sent a packet");
				session.close(true);
			}
		}
	}

	@Override
	public void messageSent(IoSession session, Object o) throws Exception { }

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		synchronized (packets) {
			// If the session has a client attached, call the session closed method
			if (session.containsAttribute("client")) {
				Player client = (Player) session.getAttribute("client");
				world.removePlayer(client);
			}
			// Otherwise they haven't logged in yet, so who cares just drop them
		}
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
