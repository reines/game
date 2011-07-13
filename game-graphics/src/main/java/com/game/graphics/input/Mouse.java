package com.game.graphics.input;

import org.lwjgl.LWJGLException;

public class Mouse {

	protected final int width;
	protected final int height;

	public Mouse(int width, int height) {
		this.width = width;
		this.height = height;

		try {
			org.lwjgl.input.Mouse.create();
		}
		catch (LWJGLException e) {
			// fatal error
			throw new RuntimeException("Error setting up mouse: " + e);
		}
	}

	public int getX() {
		return org.lwjgl.input.Mouse.getX();
	}

	public int getY() {
		return height - org.lwjgl.input.Mouse.getY();
	}

	public void update(MouseListener listener) {
		while (org.lwjgl.input.Mouse.next()) {
			// If it wasn't a press, ignore it
			if (!org.lwjgl.input.Mouse.getEventButtonState())
				continue;

			int button = org.lwjgl.input.Mouse.getEventButton();
			if (button < 0)
				continue;

			switch (org.lwjgl.input.Mouse.getEventButton()) {
			// Left button
			case 0: {
				listener.mouseClicked(org.lwjgl.input.Mouse.getEventX(), height - org.lwjgl.input.Mouse.getEventY(), true);
				break;
			}

			// Right button
			case 1: {
				listener.mouseClicked(org.lwjgl.input.Mouse.getEventX(), height - org.lwjgl.input.Mouse.getEventY(), false);
				break;
			}
			}
		}
	}
}
