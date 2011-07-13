package com.game.common.codec;

import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.security.PrivateKey;
import java.util.Date;

import javax.crypto.Cipher;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.game.common.model.Friend;
import com.game.common.model.Hash;
import com.game.common.model.Item;
import com.game.common.model.Path;
import com.game.common.model.Point;
import com.game.common.model.Stat;

public class Packet {
	private static final Logger log = LoggerFactory.getLogger(Packet.class);

	public static final int HEADER_SIZE = 4 + 4; // type + size

	protected static final CharsetDecoder stringDecoder;

	static {
		stringDecoder = Charset.forName("UTF-8").newDecoder();
	}

	// X_SEND are packets from the client -> server
	// X_RESPONSE are from the server -> client
	public enum Type {
		LOGIN_SEND,				LOGIN_RESPONSE,
		PING_SEND,
		CHAT_SEND,				CHAT_RESPONSE,
								MESSAGE_RESPONSE,
		USE_ITEM_SEND,
								FRIEND_LOGIN_RESPONSE,
		FRIEND_ADD_SEND,		FRIEND_ADD_RESPONSE,
		FRIEND_MESSAGE_SEND,	FRIEND_MESSAGE_RESPONSE,
		FRIEND_REMOVE_SEND,		FRIEND_REMOVE_RESPONSE,
								PLAYERS_ADD_RESPONSE,
								PLAYERS_REMOVE_RESPONSE,
								PLAYERS_UPDATE_RESPONSE,
								INVENTORY_ADD_RESPONSE,
								INVENTORY_REMOVE_RESPONSE,
								INVENTORY_UPDATE_RESPONSE,
		WALK_TO_SEND,
		STAT_UPDATE_SEND,
	}

	protected final Type type;
	protected IoBuffer payload;
	protected final IoSession session;

	public Packet(Type type, IoBuffer payload, IoSession session) {
		this.type = type;
		this.payload = payload;
		this.session = session;
	}

	public IoSession getSession() {
		return session;
	}

	public Type getType() {
		return type;
	}

	protected byte[] getBytes() {
		byte[] bytes = new byte[this.size()];

		payload.rewind();
		payload.get(bytes, 0, bytes.length);

		return bytes;
	}

	protected void setBytes(byte[] bytes) {
		payload = IoBuffer.wrap(bytes).asReadOnlyBuffer();
	}

	public void decrypt(PrivateKey key) {
		try {
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.DECRYPT_MODE, key);

			byte[] encrypted = this.getBytes();
			byte[] decrypted = cipher.doFinal(encrypted);

			this.setBytes(decrypted);
		}
		catch (Exception e) {
			log.error("Error decrpyting packet: " + e.getMessage());
		}
	}

	public <E extends Enum<E>> E getEnum(Class<E> enumClass) {
		return payload.getEnum(enumClass);
	}

	public String getString() {
		try {
			return payload.getString(stringDecoder);
		}
		catch (CharacterCodingException e) {
			return null;
		}
	}

	public Hash getHash() {
		byte[] data = new byte[Hash.LENGTH];
		payload.get(data, 0, Hash.LENGTH);

		return Hash.fromBytes(data);
	}

	public Point getPoint() {
		int x = this.getShort();
		int y = this.getShort();

		return new Point(x, y);
	}

	public Stat getStat() {
		Stat.Type type = this.getEnum(Stat.Type.class);
		long exp = this.getLong();
		int current = this.getShort();

		return new Stat(type, exp, current);
	}

	public Item getItem() {
		int id = this.getShort();
		long amount = this.getLong();
		boolean equiped = this.getBoolean();

		return new Item(id, amount, equiped);
	}

	public Friend getFriend() {
		Hash id = this.getHash();
		String username = this.getString();
		boolean online = this.getBoolean();

		return new Friend(id, username, online);
	}

	public Date getDate() {
		long time = this.getLong();

		return new Date(time);
	}

	public Path getPath() {
		Path path = new Path();

		int stepCount = this.getShort();
		for (int i = 0;i < stepCount;i++)
			path.append(this.getPoint());

		return path;
	}

	public boolean getBoolean() {
		return payload.get() == 1;
	}

	public byte getByte() {
		return payload.get();
	}

	public short getShort() {
		return payload.getShort();
	}

	public int getInt() {
		return payload.getInt();
	}

	public long getLong() {
		return payload.getLong();
	}

	public void rewind() {
		payload.rewind();
	}

	public int size() {
		return payload.limit();
	}

	@Override
	public String toString() {
		return "packet[" + type + "].length = " + this.size();
	}
}
