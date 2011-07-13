package com.game.tools.mapeditor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.game.common.model.Map;
import com.game.common.model.Point;

public class EditableMap extends Map {
	private static final Logger log = LoggerFactory.getLogger(EditableMap.class);

	public static final int DEFAULT_SECTOR_SIZE = 32;
	public static final int DEFAULT_SIZE = 20;
	public static final int MAX_SIZE = 100;

	public static EditableMap load(File mapFile) {
		try {
			DataInputStream in = new DataInputStream(new GZIPInputStream (new FileInputStream(mapFile)));

			// Confirm we are reading the correct map file
			if (in.readInt() != Map.MAGIC_NUMBER) {
				log.error("Attempted to read invalid map file.");
				System.exit(1); // fatal error
				return null;
			}

			int width = in.readInt();		// width in sectors
			int height = in.readInt();		// height in sectors
			int sectorSize = in.readInt();	// tiles (squared) per sector

			EditableTile[][] tiles = new EditableTile[width * sectorSize][height * sectorSize];

			// for each sector
			for (int y = 0;y < height;y++) {
				for (int x = 0;x < width;x++) {
					// for each tile in this sector
					for (int ty = 0;ty < sectorSize;ty++) {
						for (int tx = 0;tx < sectorSize;tx++) {
							tiles[tx + (x * sectorSize)][ty + (y * sectorSize)] = new EditableTile(in);
						}
					}
				}
			}

			in.close();

			return new EditableMap(width, height, sectorSize, tiles);
		}
		catch (IOException ioe) {
			log.error("Error loading map: " + ioe.getMessage());
			System.exit(1); // fatal error
			return null;
		}
	}

	protected final EditableTile[][] tiles;
	protected boolean changed;

	public EditableMap(int width, int height, int sectorSize) {
		super(width, height, sectorSize);

		tiles = new EditableTile[super.getWidth()][super.getHeight()];
		for (int y = 0;y < super.getHeight();y++) {
			for (int x = 0;x < super.getWidth();x++) {
				tiles[x][y] = new EditableTile();
				tiles[x][y].setParent(this, x, y);
			}
		}

		changed = false;
	}

	protected EditableMap(int width, int height, int sectorSize, EditableTile[][] tiles) {
		super (width, height, sectorSize);

		this.tiles = tiles;

		for (int y = 0;y < super.getHeight();y++)
			for (int x = 0;x < super.getWidth();x++)
				tiles[x][y].setParent(this, x, y);

		changed = false;
	}

	public boolean isChanged() {
		return changed;
	}

	public void setChanged(boolean changed) {
		this.changed = changed;
	}

	protected void tileChanged(EditableTile tile, EditableTile.Fields field) {
		changed = true;
	}

	@Override
	public EditableTile getTile(int x, int y) {
		if (x < 0 || x >= super.getWidth() || y < 0 || y >= super.getHeight())
			return null;

		return tiles[x][y];
	}

	@Override
	public EditableTile getTile(Point p) {
		return this.getTile(p.x, p.y);
	}

	public synchronized void save(File file) throws FileNotFoundException, IOException {
		DataOutputStream out = new DataOutputStream(new GZIPOutputStream(new FileOutputStream(file)));

		out.writeInt(Map.MAGIC_NUMBER);
		out.writeInt(super.width);
		out.writeInt(super.height);
		out.writeInt(super.sectorSize);

		// for each sector
		for (int y = 0;y < super.height;y++) {
			for (int x = 0;x < super.width;x++) {
				// for each tile in this sector
				for (int ty = 0;ty < super.sectorSize;ty++) {
					for (int tx = 0;tx < super.sectorSize;tx++) {
						tiles[tx + (x * super.sectorSize)][ty + (y * super.sectorSize)].save(out);
					}
				}
			}
		}

		out.close();
	}

	@Override
	public String toString() {
		return "Sectors " + super.width + "x" + super.height + ", Tiles " + (super.width * super.sectorSize) + "x" + (super.height * super.sectorSize);
	}
}
