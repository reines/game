package com.game.tools.fontconverter;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.game.common.util.PersistenceManager;
import com.game.graphics.renderer.FontData;
import com.game.graphics.renderer.Glyph;

public class FontConverter {
	private static final Logger log = LoggerFactory.getLogger(FontConverter.class);

	public static final int DEFAULT_FONT_SIZE = 11;

	protected static final Options options;

	static {
		options = new Options();

		options.addOption("h", "help", false, "Print this help.");
		options.addOption("i", "input-file", true, "The TTF font file to convert.");
		options.addOption("go", "glyph-output-file", true, "Path to the glyph output file, default <fontname>.png.");
		options.addOption("do", "data-output-file", true, "Path to the data output file, default <fontname>.xml.");
		options.addOption("s", "size", true, "The font size, default " + DEFAULT_FONT_SIZE + ".");

		PersistenceManager.alias("FontData", FontData.class);
		PersistenceManager.alias("Glyph", Glyph.class);
	}

	public static void main(String[] args) {
		try {
			CommandLineParser parser = new PosixParser();
			CommandLine config = parser.parse(options, args);

			if (config.hasOption("h") || !config.hasOption("i")) {
				HelpFormatter help = new HelpFormatter();
				help.printHelp("java " + FontConverter.class.getSimpleName(), options);
				return;
			}

			File inputFile = new File(config.getOptionValue("i"));
			if (!inputFile.exists()) {
				// fatal error
				throw new RuntimeException("Invalid input file: " + inputFile.getName());
			}

			float size = DEFAULT_FONT_SIZE;
			if (config.hasOption("s")) {
				try {
					size = Float.parseFloat(config.getOptionValue("s"));
				}
				catch (NumberFormatException e) {
					log.error("Invalid font size: " + config.getOptionValue("s") + ", trying default: " + size);
				}
			}

			// Load the custom font
			Font loadedFont = Font.createFont(Font.TRUETYPE_FONT, inputFile).deriveFont(Font.PLAIN, size);

			// Generate a font converter
			FontConverter converter = new FontConverter(loadedFont, 256);

			String fontName = inputFile.getName().substring(0, inputFile.getName().lastIndexOf('.'));

			// Save the glyph
			File glyphFile = new File(config.getOptionValue("go", fontName + ".png"));
			ImageIO.write(converter.getTexture(), "png", glyphFile);
			log.info("Font glyphs saved to: " + glyphFile.getAbsolutePath());

			// Save the data
			File dataFile = new File(config.getOptionValue("do", fontName + ".xml"));
			PersistenceManager.save(converter.getData(), dataFile);
			log.info("Font data saved to: " + dataFile.getAbsolutePath());
		}
		catch (ParseException e) {
			log.error("Error parsing command line options: " + e);
		}
		catch (FontFormatException e) {
			log.error("Error decoding font: " + e);
		}
		catch (IOException e) {
			log.error("Error loading font: " + e);
		}
		catch (RuntimeException e) {
			log.error(e.getMessage());
		}
	}

	protected final Font font;
	protected final FontMetrics metrics;
	protected final FontData data;
	protected final BufferedImage texture;

	public FontConverter(Font font, int numGlyphs) {
		this.font = font;
		metrics = this.getMetrics(font);

		data = new FontData(numGlyphs, metrics.getHeight());
		texture = this.loadGlyphs(data.glyphs);
	}

	public FontData getData() {
		return data;
	}

	public BufferedImage getTexture() {
		return texture;
	}

	private FontMetrics getMetrics(Font font) {
		BufferedImage tmp = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

		Graphics2D g = tmp.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setFont(font);

		return g.getFontMetrics();
	}

	private BufferedImage loadGlyphs(Glyph[] glyphs) {
		// Use a width of 256, the height will be dynamically calculated
		int width = 256;

		try {
			BufferedImage[] charImages = new BufferedImage[glyphs.length];
			Point position = new Point(0, 0);
			for (int i = 0;i < glyphs.length;i++) {
				char currentChar = (char) i;
				// Create an image with this character drawn in place
				BufferedImage charImage = this.getCharImage(currentChar);
				// If this character isn't displayable, skip it
				if (charImage == null)
					continue;

				// If this would make us go over the width, goto the next line
				if (position.x + charImage.getWidth() >= width) {
					position.x = 0;
					position.y += metrics.getHeight();
				}

				// Save information about this glyph
				glyphs[i] = new Glyph(charImage.getWidth(), charImage.getHeight(), position.x, position.y);
				charImages[i] = charImage;

				position.x += charImage.getWidth();
			}

			// Calculate the required height, making sure it's a power of 2
			int height = 16;
			for (int requiredHeight = position.y + data.fontHeight + 4;height < requiredHeight;height *= 2);

			BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = image.createGraphics();

			for (int i = 0;i < glyphs.length;i++) {
				if (charImages[i] == null)
					continue;

				// Draw this char
				g.drawImage(charImages[i], glyphs[i].x, glyphs[i].y, null);
			}

			g.dispose();

			return image;
		}
		catch (Exception e) {
			// fatal error
			throw new RuntimeException("Failed to create font: " + e);
		}
	}

	private BufferedImage getCharImage(char c) {
		// If we can't display this, skip it
		if (!font.canDisplay(c))
			return null;

		int width = metrics.charWidth(c);
		// If its 0 size (i.e. not displayable), skip it
		if (width < 1)
			return null;

		int height = metrics.getHeight();

		BufferedImage charImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = charImage.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g.setFont(font);
		g.setColor(Color.WHITE);
		g.drawString(String.valueOf(c), 0, metrics.getAscent());

		g.dispose();

		return charImage;
	}
}
