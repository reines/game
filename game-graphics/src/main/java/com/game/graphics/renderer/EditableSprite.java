package com.game.graphics.renderer;

import java.awt.Color;

public class EditableSprite extends Sprite {

	public EditableSprite(int width, int height, boolean hasAlpha, Graphics graphics) {
		super (width, height, hasAlpha, graphics);
	}

	public void setPixel(int x, int y, Color c) {
		int i = ((y * width) + x) * (super.hasAlpha ? 4 : 3);

		dataBuffer.put(i++, (byte) c.getRed());
		dataBuffer.put(i++, (byte) c.getGreen());
		dataBuffer.put(i++, (byte) c.getBlue());

		if (super.hasAlpha)
			dataBuffer.put(i++, (byte) c.getAlpha());
	}

	public void fillRect(int x, int y, int width, int height, Color c) {
		for (int j = 0;j < height;j++) {
			for (int i = 0;i < width;i++) {
				this.setPixel(x + i, y + j, c);
			}
		}
	}

	public void fillDiamond(int x, int y, int size, Color c) {
		int middle = (int) Math.ceil(size / 2d);
		int width = 1;
		for (int i = 0;i < size;i++) {
			int j = middle - width;
			for (;j < width;j++)
				this.setPixel(x + j, y + i, c);

			width += (i < middle ? 1 : -1);
		}
	}

	public void flush() {
		graphics.updateTexture(this);
	}
}
