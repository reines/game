package com.game.client.ui;

import java.awt.Color;
import java.awt.Point;

import com.game.graphics.renderer.Graphics2D;

public class LoadingBox {
	protected final int x;
	protected final int y;
	protected final int width;
	protected final int height;

	protected Point fontLocation;

	protected String[] messages;
	protected int message;

	public LoadingBox(String string, int x, int y, int width, int height) {
		this.x = x;
		this.y = y;

		this.width = width;
		this.height = height;

		fontLocation = null;

		string += " Please wait...";

		messages = new String[4];
		message = 0;

		for (int i = 0;i < 4;i++)
			messages[3 - i] = string.substring(0, string.length() - i);
	}

	public void update(long now) {
		message = (message + 1) % messages.length;
	}

	public void display(Graphics2D g) {
		g.fillRect(x, y, width, height, Color.BLACK);
		g.drawRect(x, y, width, height, Color.WHITE);

		// We need the graphics object to get font metrics, so calculate this the first time we are displayed
		if (fontLocation == null) {
			fontLocation = new Point();

			fontLocation.x = x + ((width - g.getFontWidth(messages[3])) / 2);
			fontLocation.y = y + ((height - g.getFontHeight()) / 2) + g.getFontHeight();
		}

		g.drawString(messages[message], fontLocation.x, fontLocation.y, Color.WHITE);
	}

}
