package com.game.client.ui;

import java.awt.image.BufferedImage;

import com.game.graphics.DisplayWindow;
import com.game.graphics.input.KeyListener;
import com.game.graphics.input.MouseListener;
import com.game.graphics.renderer.Graphics;

public abstract class ClientFrame extends DisplayWindow implements Runnable, MouseListener, KeyListener {
	public static final int LOOP_DELAY = 30;
	public static final int UPDATE_RATE_MAX = 1;
	public static final int UPDATE_RATE_MIN = 10;

	protected boolean running;
	protected int currentFPS;
	protected int updateRate;

	public ClientFrame(String title, int width, int height, BufferedImage[] iconImages) {
		super (title, width, height, iconImages);

		running = false;
		currentFPS = 0;
		this.setUpdateRate(1);
	}

	public Graphics getGraphics() {
		return graphics;
	}

	public int getCurrentFPS() {
		return currentFPS;
	}

	@Override
	public void run() {
		long lastUpdate = 0;
		long lastFrame = 0;
		int frames = 0;
		running = true;

		while (running) {
			long start = System.currentTimeMillis();
			if (start - lastUpdate >= updateRate) {
				lastUpdate = start;
				this.update(start);
			}

			frames++;
			if (start - lastFrame >= 1000) {
				lastFrame = start;
				currentFPS = frames;
				frames = 0;
			}

			// Handle any keyboard input
			keyboard.update(this);

			// Handle any mouse input
			mouse.update(this);

			if (super.isCloseRequested()) {
				running = false;
				break;
			}

			if (super.isUpdateRequired()) {
				graphics.clear();
				this.display(graphics);
				graphics.flush();
			}

			super.update();
		}

		super.destroy();
		this.close();
	}

	public void setUpdateRate(int updateRate) {
		if (updateRate < UPDATE_RATE_MAX)
			updateRate = UPDATE_RATE_MAX;
		else if (updateRate > UPDATE_RATE_MIN)
			updateRate = UPDATE_RATE_MIN;

		this.updateRate = updateRate * 30;
	}

	protected abstract void update(long now);
	public abstract void display(Graphics g);
	protected abstract void close();

	@Override
	public abstract void keyPressed(int keyCode, char keyChar);

	@Override
	public abstract void mouseClicked(int x, int y, boolean left);
}
