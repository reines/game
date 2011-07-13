package com.game.common.util;

import com.game.common.model.Hash;

import junit.framework.TestCase;

public class HashTest extends TestCase {

	public void testFromString() {
		Hash h1 = Hash.fromString("aaf4c61ddcc5e8a2dabede0f3b482cd9aea9434d");
		
		// A hash generated from a hex string, should return the same hex string from toString()
		assertEquals(h1.toString(), "aaf4c61ddcc5e8a2dabede0f3b482cd9aea9434d");
	}

	public void testFromBytes() {
		Hash h1 = new Hash("hello");
		Hash h2 = Hash.fromBytes(h1.getBytes());
		
		// A generated hash, and a hash constructed from byte[] should equal
		assertEquals(h1, h2);
	}

	public void testToString() {
		Hash h1 = new Hash("hello");
		
		// toString should return a hex representation of the SHA1 hash
		assertEquals(h1.toString(), "aaf4c61ddcc5e8a2dabede0f3b482cd9aea9434d");
	}

	public void testEqualsObject() {
		Hash h1 = new Hash("hello");
		Hash h2 = new Hash("hello");
		Hash h3 = new Hash("good bye");
		
		// must equal itself!
		assertTrue(h1.equals(h1));
		
		// must not equal null
		assertFalse(h1.equals(null));
		
		// must equal another hash generated from the same input
		assertTrue(h1.equals(h2));
		assertTrue(h2.equals(h1));
		
		// must not equal another hash generated from different input
		assertFalse(h1.equals(h3));
		assertFalse(h3.equals(h1));
	}

}
