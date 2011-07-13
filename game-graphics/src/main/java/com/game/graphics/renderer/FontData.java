package com.game.graphics.renderer;

public class FontData {
	public final Glyph[] glyphs;
	public final int fontHeight;

	public FontData(int numGlyphs, int fontHeight) {
		glyphs = new Glyph[numGlyphs];
		this.fontHeight = fontHeight;
	}
}
