package com.game.client.model;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.game.common.model.Map;
import com.game.common.model.Point;

public class LocalMapSector {
	private static final Logger log = LoggerFactory.getLogger(LocalMapSector.class);

	public static LocalMapSector load(URL path, Point offset) {
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

			LocalTile[][] tiles = new LocalTile[sectorSize][sectorSize];

			Point sectorOffset = new Point(offset.x / sectorSize, offset.y / sectorSize);

			// We got an offset which is outwith the map, let the sector have null tiles
			if (sectorOffset.x < 0 || sectorOffset.x >= width || sectorOffset.y < 0 || sectorOffset.y >= height) {
				return new LocalMapSector(offset, sectorSize, tiles);
			}

			// skip all the preceeding sectors
			int skip = ((sectorOffset.y * width) + sectorOffset.x) * sectorSize * sectorSize * LocalTile.DATA_LENGTH;
			for (int skipped = 0;skipped < skip;)
				skipped += in.skip(skip - skipped);

			// for each tile in this sector
			for (int ty = 0;ty < sectorSize;ty++) {
				for (int tx = 0;tx < sectorSize;tx++) {
					tiles[tx][ty] = new LocalTile(in);
				}
			}

			in.close();

			return new LocalMapSector(offset, sectorSize, tiles);
		}
		catch (IOException ioe) {
			log.error("Error loading map: " + ioe.getMessage());
			System.exit(1); // fatal error
			return null;
		}
	}

	public final Point offset;
	protected final int sectorSize;
	protected final LocalTile[][] tiles;

	public LocalMapSector(Point offset, int sectorSize, LocalTile[][] tiles) {
		this.offset = offset;
		this.sectorSize = sectorSize;
		this.tiles = tiles;
	}

	public LocalTile getTile(int x, int y) {
		x -= offset.x;
		y -= offset.y;

		if (x < 0 || x >= sectorSize || y < 0 || y >= sectorSize)
			return null;

		return tiles[x][y];
	}
}
