package com.game.common.util;

import junit.framework.TestCase;

public class ISAACAlgorithmTest extends TestCase {

	public void testNextInt() {
		long seed = System.currentTimeMillis();

		ISAACAlgorithm r1 = new ISAACAlgorithm(seed);
		ISAACAlgorithm r2 = new ISAACAlgorithm(seed);

		// Since we use the same seed, the generated numbers should match
		boolean same = true;
		for (int i = 0;i < 100;i++)
			same &= (r1.nextInt() == r2.nextInt());

		// all 100 should be equal
		assertTrue(same);

		seed = System.currentTimeMillis();
		ISAACAlgorithm r3 = new ISAACAlgorithm(seed);

		same = true;
		for (int i = 0;i < 100;i++)
			same &= (r1.nextInt() == r3.nextInt());

		// these use a different seed, some might match but most shouldn't
		// if all do, then something is differently wrong...
		assertFalse(same);
	}
}
