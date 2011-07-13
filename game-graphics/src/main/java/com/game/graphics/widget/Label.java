package com.game.graphics.widget;

import java.awt.Point;

import com.game.graphics.renderer.Graphics2D;

public class Label extends Widget {

	protected String label;
	protected Point fontLocation;
	protected boolean shadow;

	public Label(String label, int width, int height, boolean shadow) {
		this (label, width, height, shadow, false);
	}
	
	public Label(String label, int width, int height, boolean shadow, boolean focusable) {
		super (width, height, focusable);

		this.shadow = shadow;

		this.setText(label);
	}

	public void setText(String label) {
		this.label = label;
		fontLocation = null;
	}

	@Override
	protected void display(Graphics2D g, int x, int y) {
		if (super.backgroundColor != null)
			g.fillRect(x, y, width, height, super.backgroundColor);

		// We need the graphics object to get font metrics, so calculate this the first time we are displayed
		if (fontLocation == null) {
			fontLocation = new Point();

			fontLocation.x = x + ((super.width - g.getFontWidth(label)) / 2);
			fontLocation.y = y + ((super.height - g.getFontHeight()) / 2) + g.getFontHeight();
		}

		g.drawString(label, fontLocation.x, fontLocation.y, super.textColor, shadow);


		if (super.borderColor != null)
			g.drawRect(x, y, width, height, super.borderColor);
	}
}
