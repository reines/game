package com.game.graphics.renderer;

import java.awt.Color;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

public class Graphics2D {
	protected final TextRenderer textRenderer;
	protected final Graphics graphics;
	protected final DisplayMode mode;
	protected final FloatBuffer projMatrix;

	protected Graphics2D(Graphics graphics, DisplayMode mode) {
		this.graphics = graphics;
		this.mode = mode;

		textRenderer = new TextRenderer(graphics);

		// Set up our 2D mode
		GL11.glViewport(graphics.viewport.get(0), graphics.viewport.get(1), graphics.viewport.get(2), graphics.viewport.get(3));
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GLU.gluOrtho2D(0, mode.getWidth(), mode.getHeight(), 0);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);

		projMatrix = BufferUtils.createFloatBuffer(16);
		GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, projMatrix);
	}

	public Graphics getGraphics() {
		return graphics;
	}

	public void begin() {
		GL11.glPushMatrix();
		graphics.setMode(Graphics.Mode.G2D);

		// Disable lighting effects
		GL11.glDisable(GL11.GL_LIGHTING);

		// Disable fog
		GL11.glDisable(GL11.GL_FOG);

		// Disable depth testing
		GL11.glDisable(GL11.GL_DEPTH_TEST);

		// Disable anti-aliasing
		GL11.glDisable(GL11.GL_LINE_SMOOTH);

		// Disable culling
		GL11.glDisable(GL11.GL_CULL_FACE);
	}

	public void end() {
		GL11.glPopMatrix();
	}

	public int getFontWidth(String message) {
		return textRenderer.getWidth(message);
	}

	public int getFontHeight() {
		return textRenderer.getHeight();
	}

	protected void setColor(Color c) {
		GL11.glColor4f(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, c.getAlpha() / 255f);
	}

	public void drawRect(int x, int y, int width, int height, Color c) {
		this.setColor(c);
		GL11.glBegin(GL11.GL_LINE_LOOP);
		{
			GL11.glVertex2i(x, y);
			GL11.glVertex2i(x + width, y);
			GL11.glVertex2i(x + width, y + height);
			GL11.glVertex2i(x, y + height);
		}
		GL11.glEnd();
		this.setColor(Color.WHITE);
	}

	public void fillRect(int x, int y, int width, int height, Color c) {
		this.setColor(c);
		GL11.glBegin(GL11.GL_QUADS);
		{
			GL11.glVertex2i(x, y);
			GL11.glVertex2i(x + width, y);
			GL11.glVertex2i(x + width, y + height);
			GL11.glVertex2i(x, y + height);
		}
		GL11.glEnd();
		this.setColor(Color.WHITE);
	}

	public void drawLine(int x1, int y1, int x2, int y2, Color c) {
		this.setColor(c);
		GL11.glBegin(GL11.GL_LINES);
		{
			GL11.glVertex2i(x1, y1);
			GL11.glVertex2i(x2, y2);
		}
		GL11.glEnd();
		this.setColor(Color.WHITE);
	}

	public void drawString(String str, int x, int y, Color c) {
		this.drawString(str, x, y, c, false);
	}

	public void drawString(String str, int x, int y, Color c, boolean shadow) {
		textRenderer.begin();
		{
			if (shadow) {
				this.setColor(Color.BLACK);
				textRenderer.drawString(str, x + 1, y + 1);
			}

			this.setColor(c);
			textRenderer.drawString(str, x, y);
			this.setColor(Color.WHITE);
		}
		textRenderer.end();
	}

	public void drawSprite(Sprite sprite, int x, int y, int width, int height) {
		// Bind the texture
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, sprite.textureID);

		GL11.glBegin(GL11.GL_QUADS);
		{
			GL11.glTexCoord2f(0, 0);
			GL11.glVertex2i(x, y);

			GL11.glTexCoord2f(0, 1);
			GL11.glVertex2i(x, y + height);

			GL11.glTexCoord2f(1, 1);
			GL11.glVertex2i(x + width, y + height);

			GL11.glTexCoord2f(1, 0);
			GL11.glVertex2i(x + width, y);
		}
		GL11.glEnd();

		// Unbind the texture
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
	}
}
