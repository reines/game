package com.game.common.codec;

import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.security.PublicKey;
import java.util.Date;

import javax.crypto.Cipher;

import org.apache.mina.core.buffer.IoBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.game.common.model.Friend;
import com.game.common.model.Hash;
import com.game.common.model.Item;
import com.game.common.model.Path;
import com.game.common.model.Point;
import com.game.common.model.Stat;

public class PacketBuilder extends Packet {
	private static final Logger log = LoggerFactory.getLogger(PacketBuilder.class);

	public static final int DEFAULT_CAPACITY = 100;

	protected static final CharsetEncoder stringEncoder;

	static {
		stringEncoder = Charset.forName("UTF-8").newEncoder();
	}

	public PacketBuilder(Type type) {
		super (type, IoBuffer.allocate(DEFAULT_CAPACITY).setAutoExpand(true), null);
	}

	public void putEnum(Enum<?> e) {
		payload.putEnum(e);
	}

	public void putString(String s) {
		try {
			payload.putString(s + '\0', stringEncoder);
		}
		catch (CharacterCodingException e) {
			log.error("Error encoding string: " + e);
			payload.put((byte) 0); // Put a null byte to terminate the non-existant string
		}
	}

	public void putHash(Hash hash) {
		payload.put(hash.getBytes(), 0, Hash.LENGTH);
	}

	public void putPoint(Point p) {
		this.putShort((short) p.x);
		this.putShort((short) p.y);
	}

	public void putStat(Stat s) {
		this.putEnum(s.getType());
		this.putLong(s.getExp());
		this.putShort((short) s.getCurrent());
	}

	public void putItem(Item i) {
		this.putShort((short) i.getID());
		this.putLong(i.getAmount());
		this.putBoolean(i.isEquiped());
	}

	public void putFriend(Friend f) {
		this.putHash(f.getID());
		this.putString(f.getUsername());
		this.putBoolean(f.isOnline());
	}

	public void putDate(Date d) {
		this.putLong(d.getTime());
	}

	public void putPath(Path path) {
		this.putShort((short) path.length());
		for (Point step : path)
			this.putPoint(step);
	}

	public void putBoolean(boolean b) {
		payload.put((byte) (b ? 1 : 0));
	}

	public void putByte(byte b) {
		payload.put(b);
	}

	public void putShort(short s) {
		payload.putShort(s);
	}

	public void putInt(int i) {
		payload.putInt(i);
	}

	public void putLong(long l) {
		payload.putLong(l);
	}

	@Override
	public int size() {
		return payload.position();
	}

	public void encrypt(PublicKey key) {
		try {
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.ENCRYPT_MODE, key);

			byte[] decrypted = super.getBytes();
			byte[] encrypted = cipher.doFinal(decrypted);

			super.setBytes(encrypted);
			payload.position(encrypted.length);
		}
		catch (Exception e) {
			log.error("Error encrypting packet: " + e.getMessage());
		}
	}

	public void clear() {
		payload.clear();
	}

	public IoBuffer getPayload() {
		return payload.getSlice(0, this.size()).asReadOnlyBuffer().rewind();
	}
}
