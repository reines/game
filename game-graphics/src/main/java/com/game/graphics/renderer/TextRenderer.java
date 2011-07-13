package com.game.graphics.renderer;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.lwjgl.opengl.GL11;

import com.game.common.util.PersistenceManager;

public class TextRenderer {

	public static final char REPLACEMENT_CHAR = '?';

	static {
		PersistenceManager.alias("FontData", FontData.class);
		PersistenceManager.alias("Glyph", Glyph.class);
	}

	protected final Glyph[] glyphs;
	protected final int fontHeight;
	protected final Sprite texture;

	public TextRenderer(Graphics graphics) {
		URL fontDataResource = TextRenderer.class.getResource("fonts/liberationsans.xml");
		if (fontDataResource == null) {
			// fatal error
			throw new RuntimeException("Unable to find fonts/liberationsans.xml resource.");
		}

		FontData data = (FontData) PersistenceManager.load(fontDataResource);
		glyphs = data.glyphs;
		fontHeight = data.fontHeight;

		URL glyphResource = TextRenderer.class.getResource("fonts/liberationsans.png");
		if (glyphResource == null) {
			// fatal error
			throw new RuntimeException("Unable to find fonts/liberationsans.png resource.");
		}

		BufferedImage image = null;

		try {
			image = ImageIO.read(glyphResource);
		}
		catch (IOException e) {
			// fatal error
			throw new RuntimeException("Error loading glyph.png: " + e);
		}

		texture = new Sprite(image, graphics);
	}

	public int getWidth(String str) {
		int width = 0;

		for (int i = 0;i < str.length();i++) {
			char currentChar = str.charAt(i);
			// If this isn't displayable, replace it
			if (currentChar >= glyphs.length || glyphs[currentChar] == null)
				currentChar = REPLACEMENT_CHAR;

			width += glyphs[currentChar].width;
		}

		return width;
	}

	public int getHeight() {
		return fontHeight;
	}

	public void begin() {
		// Bind the texture
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.textureID);

		GL11.glBegin(GL11.GL_QUADS);
	}

	public void drawString(String str, int x, int y) {
		for (int i = 0;i < str.length();i++) {
			char currentChar = str.charAt(i);
			// If this isn't displayable, skip it
			if (currentChar >= glyphs.length || glyphs[currentChar] == null)
				currentChar = REPLACEMENT_CHAR;

			Glyph glyph = glyphs[currentChar];

			float textureX = ((float) (glyph.x + glyph.width) / texture.width);
			float textureY = ((float) (glyph.y + glyph.height) / texture.height);
			float textureWidth = ((float) glyph.width / texture.width);
			float textureHeight = ((float) glyph.height / texture.height);

			GL11.glTexCoord2f(textureX, textureY);
			GL11.glVertex2f(x + glyph.width, y);
			GL11.glTexCoord2f(textureX, textureY - textureHeight);
			GL11.glVertex2f(x + glyph.width, y - glyph.height);
			GL11.glTexCoord2f(textureX - textureWidth, textureY - textureHeight);
			GL11.glVertex2f(x, y - glyph.height);
			GL11.glTexCoord2f(textureX - textureWidth, textureY);
			GL11.glVertex2f(x, y);

			x += glyph.width;
		}
	}

	public void end() {
		GL11.glEnd();

		// Unbind the texture
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
	}
}
