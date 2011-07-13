package com.game.graphics;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

import com.game.graphics.input.Keyboard;
import com.game.graphics.input.Mouse;
import com.game.graphics.renderer.Graphics;
import com.game.graphics.renderer.Sprite;

public class DisplayWindow {

	public final int width;
	public final int height;
	protected final Graphics graphics;
	protected final Keyboard keyboard;
	protected final Mouse mouse;

	public DisplayWindow(String title, int width, int height, BufferedImage[] iconImages) {
		this.width = width;
		this.height = height;

		try {
			Display.setDisplayMode(new DisplayMode(width, height));
			Display.setTitle(title);
			Display.setFullscreen(false);
			Display.setVSyncEnabled(true);
		}
		catch (LWJGLException e) {
			// fatal error
			throw new RuntimeException("Unable to initialise display: " + e);
		}

		this.setIcon(iconImages);

		try {
			Display.create();
		}
		catch (LWJGLException e) {
			// fatal error
			throw new RuntimeException("Unable to create display: " + e);
		}

		graphics = new Graphics(Display.getDisplayMode());
		keyboard = new Keyboard();
		mouse = new Mouse(width, height);
	}

	public Keyboard getKeyboard() {
		return keyboard;
	}

	public Mouse getMouse() {
		return mouse;
	}

	public void setVSync(boolean vsync) {
		Display.setVSyncEnabled(vsync);
	}

	public boolean isCloseRequested() {
		return Display.isCloseRequested();
	}

	public boolean isUpdateRequired() {
		return Display.isActive() || Display.isVisible() || Display.isDirty();
	}

	public void update() {
		Display.update();
	}

	public void setIcon(BufferedImage[] iconImages) {
		if (iconImages == null) {
			Display.setIcon(null);
			return;
		}

		ByteBuffer[] iconBuffers = new ByteBuffer[iconImages.length];
		for (int i = 0;i < iconImages.length;i++) {
			BufferedImage icon = iconImages[i];
			iconBuffers[i] = Sprite.getDataBuffer(icon, icon.getWidth(), icon.getHeight(), true);
		}

		Display.setIcon(iconBuffers);
	}

	public void destroy() {
		Display.destroy();
	}
}
