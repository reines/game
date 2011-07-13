package com.game.graphics.renderer;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Hashtable;

import javax.imageio.ImageIO;

public class Sprite {
	public static final ColorModel ALPHA_COLOR_MODEL;
	public static final ColorModel COLOR_MODEL;

	static {
		ALPHA_COLOR_MODEL = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[] {8, 8, 8, 8}, true, false, Transparency.TRANSLUCENT, DataBuffer.TYPE_BYTE);
		COLOR_MODEL = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[] {8, 8, 8, 0}, false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
	}

	@SuppressWarnings("rawtypes")
	public static ByteBuffer getDataBuffer(BufferedImage image, int width, int height, boolean hasAlpha) {
		WritableRaster textureRaster;
		BufferedImage textureImage;
		if (hasAlpha) {
			textureRaster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, width, height, 4, null);
			textureImage = new BufferedImage(ALPHA_COLOR_MODEL, textureRaster, false, new Hashtable());
		}
		else {
			textureRaster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, width, height, 3, null);
			textureImage = new BufferedImage(COLOR_MODEL, textureRaster, false, new Hashtable());
		}

		Graphics2D g = textureImage.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.drawImage(image, 0, 0, width, height, null);

		byte[] data = ((DataBufferByte) textureImage.getRaster().getDataBuffer()).getData();

		ByteBuffer dataBuffer = ByteBuffer.allocateDirect(data.length);
		dataBuffer.order(ByteOrder.nativeOrder());
		dataBuffer.put(data, 0, data.length);
		dataBuffer.flip();

		return dataBuffer;
	}

	public final Graphics graphics;
	public final int textureID;
	public final int originalWidth;
	public final int originalHeight;
	public final int width;
	public final int height;
	public final boolean hasAlpha;
	protected final ByteBuffer dataBuffer;

	protected Sprite(InputStream in, Graphics graphics) throws IOException {
		this (ImageIO.read(in), graphics);
	}

	protected Sprite(BufferedImage image, Graphics graphics) {
		this.graphics = graphics;

		int width = image.getWidth();
		int height = image.getHeight();

		originalWidth = width;
		originalHeight = height;

		// Check the width is a power of 2
		if ((width & (width - 1)) != 0) {
			width = 2;
			while (width < image.getWidth())
				width *= 2;
		}

		// Check the height is a power of 2
		if ((height & (height - 1)) != 0) {
			height = 2;
			while (height < image.getHeight())
				height *= 2;
		}

		this.width = width;
		this.height = height;

		hasAlpha = image.getColorModel().hasAlpha();
		dataBuffer = Sprite.getDataBuffer(image, width, height, hasAlpha);

		// Generate an OpenGL texture
		textureID = graphics.generateTexture(this);
	}

	protected Sprite(int width, int height, boolean hasAlpha, Graphics graphics) {
		this.width = this.originalWidth = width;
		this.height = this.originalHeight = height;

		dataBuffer = ByteBuffer.allocateDirect(width * height * (hasAlpha ? 4 : 3));
		dataBuffer.order(ByteOrder.nativeOrder());

		this.hasAlpha = hasAlpha;
		this.graphics = graphics;

		// Generate an OpenGL texture
		textureID = graphics.generateTexture(this);
	}

	public ByteBuffer getDataBuffer() {
		dataBuffer.rewind();
		return dataBuffer;
	}

	public int getDataLength() {
		return dataBuffer.capacity();
	}

	@Override
	public String toString() {
		return "sprite[width=" + width + ", height=" + height + ", hasAlpha = " + hasAlpha + ", dataBuffer.length = " + dataBuffer.capacity() + "];";
	}
}