package com.game.tools.mapeditor;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;

public class SpriteMap implements Iterable<Image> {

	public final int elementWidth;
	public final int elementHeight;
	protected final List<Image> sprites;

	public SpriteMap(int elementWidth, int elementHeight) {
		this.elementWidth = elementWidth;
		this.elementHeight = elementHeight;

		sprites = new ArrayList<Image>();
	}

	public SpriteMap(URL resource, int elementWidth, int elementHeight) throws IOException {
		this (elementWidth, elementHeight);

		BufferedImage image = ImageIO.read(resource);

		if (image.getWidth() % elementWidth != 0 || image.getHeight() % elementHeight != 0) {
			// fatal error
			throw new RuntimeException("Attempted to load sprite map, but the image dimensions aren't multiples of the element dimensions.");
		}

		int width = image.getWidth() / elementWidth;
		int height = image.getHeight() / elementHeight;

		for (int y = 0;y < height;y++) {
			for (int x = 0;x < width;x++) {
				Image sprite = image.getSubimage(x * elementWidth, y * elementHeight, elementWidth, elementHeight);
				this.add(sprite);
			}
		}
	}

	public void add(Image sprite) {
		sprites.add(sprite);
	}

	public Image get(int index) {
		return sprites.get(index);
	}

	public BufferedImage generateImage() {
		return null; // generate 1 image with all the sprites tiled
	}

	@Override
	public Iterator<Image> iterator() {
		return sprites.iterator();
	}
}
