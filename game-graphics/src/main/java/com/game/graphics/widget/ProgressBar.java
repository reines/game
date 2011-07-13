package com.game.graphics.widget;

import java.awt.Color;

import com.game.graphics.renderer.Graphics2D;

public class ProgressBar extends Widget {

	protected int value;
	protected Color color;


	public ProgressBar(int width, int height) {
		super(width, height, false);

		// Default to not started
		color = new Color(0f, 0.5f, 0f);
		value = 0;
	}

	public int getValue() {
		return value;
	}

	public Color getColor() {
		return color;
	}

	@Override
	protected void display(Graphics2D g, int x, int y) {
		if (super.backgroundColor != null)
			g.fillRect(x, y, width, height, super.backgroundColor);

		int offset = (height / 100) * (100 - this.getValue());
		g.fillRect(x, y + offset, width, height - offset, this.getColor());

		if (super.borderColor != null)
			g.drawRect(x, y, width, height, super.borderColor);
	}
}
