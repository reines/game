package com.game.common.model;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Hash implements Comparable<Hash>, Serializable {
	private static final Logger log = LoggerFactory.getLogger(Hash.class);
	private static final long serialVersionUID = 1L;

	public static final int LENGTH = 20; // in bytes
	public static final String ALGORITHM = "SHA-1";

	protected static MessageDigest digest;

	static {
		try {
			digest = MessageDigest.getInstance(ALGORITHM);
		}
		catch (NoSuchAlgorithmException e) {
			log.error("No such hashing algorithm: " + ALGORITHM);
			System.exit(1); // fatal error
		}
	}

	public static Hash fromString(String hex) {
		Hash hash = new Hash();
		hash.setHex(hex);

		return hash;
	}

	public static Hash fromBytes(byte[] bytes) {
		Hash hash = new Hash();
		hash.setBytes(bytes);

		return hash;
	}

	protected String hex;

	public Hash(String str) {
		try {
			digest.update(str.getBytes("UTF-8"));
		}
		catch (UnsupportedEncodingException e) { }

		this.setBytes(digest.digest());
	}

	private Hash() {
		hex = null;
	}

	private void setHex(String hex) {
		this.hex = hex;
	}

	private void setBytes(byte[] bytes) {
		Formatter foramtter = new Formatter();

		for (byte b : bytes)
			foramtter.format("%02x", b);

		this.hex = foramtter.toString();
	}

	public byte[] getBytes() {
		byte[] bytes = new byte[LENGTH];

		for (int i = 0;i < LENGTH;i++)
			bytes[i] = (byte) Short.parseShort(hex.substring(2 * i, 2 * (i + 1)), 16);

		return bytes;
	}

	@Override
	public String toString() {
		return hex;
	}

	@Override
	public int hashCode() {
		return hex.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Hash))
			return false;

		Hash h = (Hash) o;
		return hex.equals(h.hex);
	}

	@Override
	public int compareTo(Hash h) {
		return hex.compareTo(h.hex);
	}
}
