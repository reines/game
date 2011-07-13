package com.game.graphics.input;

import org.lwjgl.LWJGLException;

public class Keyboard {

	public static final int KEY_UP = 200;
	public static final int KEY_LEFT = 203;
	public static final int KEY_RIGHT = 205;
	public static final int KEY_DOWN = 208;

	public static final int KEY_F1 = 59;
	public static final int KEY_F2 = 60;
	public static final int KEY_F3 = 61;
	public static final int KEY_F4 = 62;
	public static final int KEY_F5 = 63;
	public static final int KEY_F6 = 64;
	public static final int KEY_F7 = 65;
	public static final int KEY_F8 = 66;
	public static final int KEY_F9 = 67;
	public static final int KEY_F10 = 68;
	public static final int KEY_F11 = 87;
	public static final int KEY_F12 = 88;
	public static final int KEY_F13 = 100;
	public static final int KEY_F14 = 101;
	public static final int KEY_F15 = 102;

	public Keyboard() {
		try {
			org.lwjgl.input.Keyboard.enableRepeatEvents(true);
			org.lwjgl.input.Keyboard.create();
		}
		catch (LWJGLException e) {
			// fatal error
			throw new RuntimeException("Error setting up keyboard: " + e);
		}
	}

	public boolean isKeyDown(int keyCode) {
		return org.lwjgl.input.Keyboard.isKeyDown(keyCode);
	}

	public void update(KeyListener listener) {
		while (org.lwjgl.input.Keyboard.next()) {
			// If it wasn't a press, ignore it
			if (!org.lwjgl.input.Keyboard.getEventKeyState())
				continue;

			listener.keyPressed(org.lwjgl.input.Keyboard.getEventKey(), org.lwjgl.input.Keyboard.getEventCharacter());
		}
	}
}
