package com.game.client.engine;

import com.game.graphics.input.Keyboard;

public class Camera {
	public static final int ROTATION_STEP = 10;
	public static final int ROTATION_DEFAULT = 0;

	public static final int ZOOM_STEP = 1;
	public static final int ZOOM_DEFAULT = 8;
	public static final int ZOOM_MIN = 6;
	public static final int ZOOM_MAX = 12;

	protected final Keyboard keyboard;

	protected int rotation; // in degrees (0-359)
	protected int zoom;

	public Camera(Keyboard keyboard) {
		this.keyboard = keyboard;

		rotation = ROTATION_DEFAULT;
		zoom = ZOOM_DEFAULT;
	}

	public int getZoom() {
		return zoom;
	}

	public void setZoom(int zoom) {
		if (zoom < ZOOM_MIN)
			zoom = ZOOM_MIN;
		else if (zoom > ZOOM_MAX)
			zoom = ZOOM_MAX;

		this.zoom = zoom;
	}

	public int getRotation() {
		return rotation;
	}

	public void setRotation(int rotation) {
		this.rotation = rotation % 360;
		if (this.rotation < 0)
			this.rotation = 360 + this.rotation;
	}

	public void update(long now) {
		if (keyboard.isKeyDown(Keyboard.KEY_LEFT))
			this.setRotation(rotation + ROTATION_STEP);
		if (keyboard.isKeyDown(Keyboard.KEY_RIGHT))
			this.setRotation(rotation - ROTATION_STEP);
		if (keyboard.isKeyDown(Keyboard.KEY_UP))
			this.setZoom(zoom - ZOOM_STEP);
		if (keyboard.isKeyDown(Keyboard.KEY_DOWN))
			this.setZoom(zoom + ZOOM_STEP);
	}
}
