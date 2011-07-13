package com.game.common.model;

import junit.framework.TestCase;

public class PointTest extends TestCase {

	public void testDistanceTo() {
		Point p1 = new Point(100, 100);
		Point p2 = new Point(100, 110);
		
		// Distance to itself should always be 0
		assertEquals(p1.distanceTo(p1), 0.0d);
		
		// Distance should be equal either way it is calculated
		assertEquals(p1.distanceTo(p2), 10.0d);
		assertEquals(p2.distanceTo(p1), 10.0d);
	}
}
