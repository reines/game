package com.game.graphics.renderer;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;

import com.game.graphics.models.Model;

public class Graphics {

	public enum Mode { G2D, G3D }

	protected final Graphics2D graphics2D;
	protected final Graphics3D graphics3D;

	protected final DisplayMode display;
	protected Mode mode;

	protected final IntBuffer viewport;

	public Graphics(DisplayMode display) {
		this.display = display;

		viewport = (IntBuffer) BufferUtils.createIntBuffer(4).put(new int[]{0, 0, display.getWidth(), display.getHeight()}).flip();

		// Enable 2D textures
		GL11.glEnable(GL11.GL_TEXTURE_2D);

		// Set up depth testing
		GL11.glDepthFunc(GL11.GL_LEQUAL);
		GL11.glShadeModel(GL11.GL_SMOOTH);

		// Enable nicest correction
		GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);

		// Set the background to black and with a depth of 1
		GL11.glClearColor(0, 0, 0, 0);
		GL11.glClearDepth(1);

		// Enable transparency
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		mode = Mode.G2D;
		graphics2D = new Graphics2D(this, display);

		mode = Mode.G3D;
		graphics3D = new Graphics3D(this, display);

		// Draw a loading screen
		graphics2D.begin();
		this.clear();

		String message = "Loading. Please wait...";
		graphics2D.drawString(message, (display.getWidth() - graphics2D.getFontWidth(message)) / 2, (display.getHeight() - graphics2D.getFontHeight()) / 2, Color.WHITE);

		this.flush();
		graphics2D.end();
		Display.update();
	}

	protected void setMode(Mode mode) {
		// We are already in the correct mode
		if (mode == this.mode)
			return;

		GL11.glMatrixMode(GL11.GL_PROJECTION);

		switch (mode) {
		case G2D: {
			GL11.glLoadMatrix(graphics2D.projMatrix);
			break;
		}
		case G3D: {
			GL11.glLoadMatrix(graphics3D.projMatrix);
			break;
		}
		}

		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		this.mode = mode;
	}

	public Graphics2D get2D() {
		return graphics2D;
	}

	public Graphics3D get3D() {
		return graphics3D;
	}

	public Map<String, Sprite> loadSpritePack(String path) {
		InputStream resource = Sprite.class.getResourceAsStream("sprites/" + path);
		if (resource == null) {
			// fatal error
			throw new RuntimeException("Unable to find resource: " + path);
		}

		try {
			Map<String, Sprite> sprites = new HashMap<String, Sprite>();
			ZipInputStream in = new ZipInputStream(resource);

			for (ZipEntry entry;(entry = in.getNextEntry()) != null;) {
				// Skip directories
				if (entry.isDirectory())
					continue;

				sprites.put(entry.getName(), new Sprite(in, this));
			}

			in.close();
			return sprites;
		}
		catch (IOException e) {
			// fatal error
			throw new RuntimeException("Unable to load sprite pack: " + e);
		}
	}

	public Sprite loadSprite(String path) {
		URL resource = Sprite.class.getResource("sprites/" + path);
		if (resource == null) {
			// fatal error
			throw new RuntimeException("Unable to find resource: " + path);
		}

		return this.loadSprite(resource);
	}

	public Sprite loadSprite(URL resource) {
		try {
			InputStream in = resource.openStream();
			Sprite sprite = new Sprite(in, this);
			in.close();

			return sprite;
		}
		catch (IOException e) {
			// fatal error
			throw new RuntimeException("Unable to load sprite: " + e);
		}
	}

	public Model loadModel(String path, Class<? extends Model> type) {
		URL resource = Model.class.getResource(path);
		if (resource == null) {
			// fatal error
			throw new RuntimeException("Unable to find resource: " + path);
		}

		return this.loadModel(resource, type);
	}

	public Model loadModel(URL resource, Class<? extends Model> type) {
		try {
			InputStream in = resource.openStream();
			Model model = type.getConstructor(InputStream.class, Graphics.class).newInstance(in, this);
			in.close();

			return model;
		}
		catch (Exception e) {
			// fatal error
			throw new RuntimeException("Unable to load model: " + e);
		}
	}

	protected int generateTexture(Sprite sprite) {
		int textureID = GL11.glGenTextures();

		// Bind the texture
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);

		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, sprite.width, sprite.height, 0, sprite.hasAlpha ? GL11.GL_RGBA : GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, sprite.getDataBuffer());

		// Unbind the texture
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

		return textureID;
	}

	protected void updateTexture(Sprite sprite) {
		// Bind the texture
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, sprite.textureID);

		GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, sprite.width, sprite.height, sprite.hasAlpha ? GL11.GL_RGBA : GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, sprite.getDataBuffer());

		// Unbind the texture
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
	}

	public void clear() {
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
	}

	public void flush() {
		GL11.glFlush();
	}
}
