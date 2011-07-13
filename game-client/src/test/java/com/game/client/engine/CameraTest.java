package com.game.client.engine;

import junit.framework.TestCase;

public class CameraTest extends TestCase {

	public void testGetRotation() {
		Camera camera = new Camera(null);

		// Check getRotation() gives us what we expect
		camera.setRotation(45);
		assertTrue(camera.getRotation() == 45);
	}

	public void testSetRotation() {
		Camera camera = new Camera(null);

		// Test lower boundary
		camera.setRotation(0);
		assertTrue(camera.getRotation() == 0);

		// Test upper boundary
		camera.setRotation(360);
		assertTrue(camera.getRotation() == 0);

		// Test normal case
		camera.setRotation(45);
		assertTrue(camera.getRotation() == 45);

		// Test outside case
		camera.setRotation(405);
		assertTrue(camera.getRotation() == 45);
	}

}
