package com.game.server.model;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.game.common.model.Map;
import com.game.common.model.Point;

public class WorldMap extends Map {
	private static final Logger log = LoggerFactory.getLogger(WorldMap.class);

	public static WorldMap load() {
		URL path = Map.class.getResource("world.map");
		if (path == null) {
			log.error("Unable to find map resource");
			System.exit(1); // fatal error
			return null;
		}

		try {
			DataInputStream in = new DataInputStream(new GZIPInputStream (path.openStream()));

			// Confirm we are reading the correct map file
			if (in.readInt() != Map.MAGIC_NUMBER) {
				log.error("Attempted to read invalid map file.");
				System.exit(1); // fatal error
				return null;
			}

			int width = in.readInt();		// width in sectors
			int height = in.readInt();		// height in sectors
			int sectorSize = in.readInt();	// tiles (squared) per sector

			WorldTile[][] tiles = new WorldTile[width * sectorSize][height * sectorSize];

			// for each sector
			for (int y = 0;y < height;y++) {
				for (int x = 0;x < width;x++) {
					// for each tile in this sector
					for (int ty = 0;ty < sectorSize;ty++) {
						for (int tx = 0;tx < sectorSize;tx++) {
							tiles[tx + (x * sectorSize)][ty + (y * sectorSize)] = new WorldTile(in);
						}
					}
				}
			}

			in.close();

			return new WorldMap(width, height, sectorSize, tiles);
		}
		catch (IOException ioe) {
			log.error("Error loading map: " + ioe.getMessage());
			System.exit(1); // fatal error
			return null;
		}
	}

	protected final WorldTile[][] tiles;

	public WorldMap(int width, int height, int sectorSize, WorldTile[][] tiles) {
		super (width, height, sectorSize);

		this.tiles = tiles;

		if (log.isDebugEnabled())
			log.debug("Loaded WorldMap: " + this);
	}

	@Override
	public WorldTile getTile(int x, int y) {
		return tiles[x][y];
	}

	@Override
	public WorldTile getTile(Point p) {
		return this.getTile(p.x, p.y);
	}
}
