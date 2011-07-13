package com.game.graphics.renderer;

import com.game.graphics.math.Dimension;

public class SpriteMap {

	public final Sprite sprite;
	public final int elementWidth;
	public final int elementHeight;

	// The amount of elements stored in this sprite
	public final int width;
	public final int height;

	public SpriteMap(Sprite sprite, int elementWidth, int elementHeight) {
		this.sprite = sprite;
		this.elementWidth = elementWidth;
		this.elementHeight = elementHeight;

		if (sprite.originalWidth % elementWidth != 0 || sprite.originalHeight % elementHeight != 0) {
			// fatal error
			throw new RuntimeException("Attempted to load sprite map, but the sprites dimensions aren't multiples of the element dimensions.");
		}

		width = sprite.originalWidth / elementWidth;
		height = sprite.originalHeight / elementHeight;
	}

	public Dimension getSprite(int index) {
		int x = index % width;
		int y = index / width;

		if (y >= height)
			return null;

		x *= elementWidth;
		y *= elementHeight;

		return new Dimension(
			(float) (x + 1) / sprite.originalWidth,
			(float) (y + 1) / sprite.originalHeight,
			(float) (x + elementWidth - 2) / sprite.originalWidth,
			(float) (y + elementHeight - 2) / sprite.originalHeight
		);
	}
}
