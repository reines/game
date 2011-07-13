package com.game.common.codec;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import junit.framework.TestCase;

import com.game.common.model.Hash;
import com.game.common.model.Point;
import com.game.common.model.Stat;

public class PacketTest extends TestCase {

	public void testGetType() {
		PacketBuilder packet = new PacketBuilder(Packet.Type.PING_SEND);

		// check the packet type is correct
		assertTrue(packet.getType() == Packet.Type.PING_SEND);
	}

	public void testGetBytes() {
		PacketBuilder packet = new PacketBuilder(Packet.Type.PING_SEND);

		byte[] d1 = packet.getBytes();

		// the payload is empty
		assertTrue(d1.length == 0);

		// stick a string in - total of 6 bytes (5 chars + null terminated)
		packet.putString("hello");

		byte[] d2 = packet.getBytes();

		// the packet should be 6 bytes
		assertTrue(d2.length == 6);

		byte[] d3 = packet.getBytes();

		// called again, it should return the same
		assertTrue(d3.length == d2.length);
	}

	public void testDecrypt() throws NoSuchAlgorithmException {
		PacketBuilder packet = new PacketBuilder(Packet.Type.PING_SEND);
		packet.putString("hello");

		KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
		generator.initialize(1048);

		KeyPair key = generator.generateKeyPair();

		byte[] original = packet.getBytes();

		packet.encrypt(key.getPublic());

		byte[] encrypted = packet.getBytes();

		packet.decrypt(key.getPrivate());

		// work around the fact we are working with a PacketBuilder not a Packet, this
		//  is needed because getSize() (and hence getBytes()) behaves differently for each
		packet.payload.position(packet.payload.limit());

		byte[] decrypted = packet.getBytes();

		// after encrypted it should be different
		assertFalse(Arrays.equals(original, encrypted));

		// after decryption it should be the same again
		assertTrue(Arrays.equals(original, decrypted));
	}

	public void testGetEnum() {
		PacketBuilder packet = new PacketBuilder(Packet.Type.PING_SEND);
		packet.putEnum(Packet.Type.CHAT_SEND);
		packet.putEnum(Packet.Type.CHAT_RESPONSE);

		// rewind the packet for reading
		packet.rewind();

		// check we can get the enum back from the packet
		assertTrue(packet.getEnum(Packet.Type.class) == Packet.Type.CHAT_SEND);
		assertTrue(packet.getEnum(Packet.Type.class) == Packet.Type.CHAT_RESPONSE);
	}

	public void testGetString() {
		PacketBuilder packet = new PacketBuilder(Packet.Type.PING_SEND);
		packet.putString("hello");
		packet.putString("good bye");

		// rewind the packet for reading
		packet.rewind();

		// check we can get the string back from the packet
		assertTrue(packet.getString().equals("hello"));
		assertTrue(packet.getString().equals("good bye"));
	}

	public void testGetHash() {
		Hash h1 = new Hash("hello");
		Hash h2 = new Hash("good bye");

		PacketBuilder packet = new PacketBuilder(Packet.Type.PING_SEND);
		packet.putHash(h1);
		packet.putHash(h2);

		// rewind the packet for reading
		packet.rewind();

		// check we can get the hash back from the packet
		assertTrue(packet.getHash().equals(h1));
		assertTrue(packet.getHash().equals(h2));
	}

	public void testGetPoint() {
		Point p1 = new Point(47, 291);
		Point p2 = new Point(89, 272);

		PacketBuilder packet = new PacketBuilder(Packet.Type.PING_SEND);
		packet.putPoint(p1);
		packet.putPoint(p2);

		// rewind the packet for reading
		packet.rewind();

		// check we can get the point back from the packet
		assertTrue(packet.getPoint().equals(p1));
		assertTrue(packet.getPoint().equals(p2));
	}

	public void testGetStat() {
		Stat s1 = new Stat(Stat.Type.ATTACK, 1000000, 55);
		Stat s2 = new Stat(Stat.Type.STRENGTH, 0, 1);

		PacketBuilder packet = new PacketBuilder(Packet.Type.PING_SEND);
		packet.putStat(s1);
		packet.putStat(s2);

		// rewind the packet for reading
		packet.rewind();

		// check we can get the stat back from the packet
		assertTrue(packet.getStat().equals(s1));
		assertTrue(packet.getStat().equals(s2));
	}

	public void testGetBoolean() {
		PacketBuilder packet = new PacketBuilder(Packet.Type.PING_SEND);
		packet.putBoolean(true);
		packet.putBoolean(false);

		// rewind the packet for reading
		packet.rewind();

		// check we can get the boolean back from the packet
		assertTrue(packet.getBoolean() == true);
		assertTrue(packet.getBoolean() == false);
	}

	public void testGetByte() {
		PacketBuilder packet = new PacketBuilder(Packet.Type.PING_SEND);
		packet.putByte((byte) 7);
		packet.putByte((byte) 42);

		// rewind the packet for reading
		packet.rewind();

		// check we can get the byte back from the packet
		assertTrue(packet.getByte() == 7);
		assertTrue(packet.getByte() == 42);
	}

	public void testGetShort() {
		PacketBuilder packet = new PacketBuilder(Packet.Type.PING_SEND);
		packet.putShort((short) 7);
		packet.putShort((short) 42);

		// rewind the packet for reading
		packet.rewind();

		// check we can get the short back from the packet
		assertTrue(packet.getShort() == 7);
		assertTrue(packet.getShort() == 42);
	}

	public void testGetInt() {
		PacketBuilder packet = new PacketBuilder(Packet.Type.PING_SEND);
		packet.putInt(7);
		packet.putInt(42);

		// rewind the packet for reading
		packet.rewind();

		// check we can get the int back from the packet
		assertTrue(packet.getInt() == 7);
		assertTrue(packet.getInt() == 42);
	}

	public void testGetLong() {
		PacketBuilder packet = new PacketBuilder(Packet.Type.PING_SEND);
		packet.putLong(7);
		packet.putLong(42);

		// rewind the packet for reading
		packet.rewind();

		// check we can get the int back from the packet
		assertTrue(packet.getLong() == 7);
		assertTrue(packet.getLong() == 42);
	}
}
