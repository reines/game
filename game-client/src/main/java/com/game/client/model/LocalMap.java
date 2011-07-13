package com.game.client.model;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.game.common.model.Map;
import com.game.common.model.Point;
import com.game.graphics.elements.Quad;
import com.game.graphics.elements.QuadBuffer;
import com.game.graphics.elements.Vertex;
import com.game.graphics.math.Dimension;
import com.game.graphics.renderer.Graphics;
import com.game.graphics.renderer.Pickable;
import com.game.graphics.renderer.SpriteMap;

public class LocalMap extends Map {
	private static final Logger log = LoggerFactory.getLogger(LocalMap.class);

	public static LocalMap load(Graphics graphics) {
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
				in.close();

				log.error("Attempted to read invalid map file.");
				System.exit(1); // fatal error
				return null;
			}

			int width = in.readInt();		// width in sectors
			int height = in.readInt();		// height in sectors
			int sectorSize = in.readInt();	// tiles (squared) per sector

			in.close();

			return new LocalMap(path, width, height, sectorSize, graphics);
		}
		catch (IOException ioe) {
			log.error("Error loading map: " + ioe.getMessage());
			System.exit(1); // fatal error
			return null;
		}
	}

	protected final URL mapResource;
	protected final Graphics graphics;
	protected final LocalMapSector[][] sectors;

	protected final SpriteMap tileTextures;
	protected final SpriteMap overlayTextures;
	protected final SpriteMap wallTextures;

	protected QuadBuffer tileBuffer;
	protected QuadBuffer overlayBuffer;
	protected QuadBuffer wallBuffer;

	public LocalMap(URL mapResource, int width, int height, int sectorSize, Graphics graphics) {
		super (width, height, sectorSize);

		this.mapResource = mapResource;
		this.graphics = graphics;

		sectors = new LocalMapSector[3][3];

		tileTextures = new SpriteMap(graphics.loadSprite("tiles.png"), 64, 64);
		overlayTextures = new SpriteMap(graphics.loadSprite("overlays.png"), 64, 64);
		wallTextures = new SpriteMap(graphics.loadSprite("walls.png"), 64, 192);

		tileBuffer = null;
		overlayBuffer = null;
		wallBuffer = null;
	}

	private Point calcGlobalSector(int x, int y) {
		return new Point(
			x - (x % super.sectorSize),
			y - (y % super.sectorSize)
		);
	}

	private Point calcLocalSector(int x, int y) {
		if (sectors[0][0] == null)
			return null;

		return new Point(
			(x - sectors[0][0].offset.x) / super.sectorSize,
			(y - sectors[0][0].offset.y) / super.sectorSize
		);
	}

	public boolean setLocation(Point location) {
		Point sector = this.calcLocalSector(location.x, location.y);

		// No sectors are loaded yet, or the loaded ones are out-of-date
		if (sector == null || sector.x < 0 || sector.x >= 3 || sector.y < 0 || sector.y >= 3) {
			// Load all 9 sectors, around the current sector
			sector = this.calcGlobalSector(location.x, location.y);

			for (int y = 0;y < 3;y++) {
				for (int x = 0;x < 3;x++) {
					Point offset = new Point(sector.x + ((x - 1) * super.sectorSize), sector.y + ((y - 1) * super.sectorSize));
					sectors[x][y] = LocalMapSector.load(mapResource, offset);
				}
			}

			this.updateBuffers();
			return true;
		}

		// The sectors are loaded, and we're still in the middle
		if (sector.x == 1 && sector.y == 1)
			return false;

		// TODO: we've moved, load/swap some sectors...

		this.updateBuffers();
		return true;
	}

	private void updateBuffers() {
		// We've nothing loaded
		if (sectors[0][0] == null)
			return;

		int mapSize = super.sectorSize * 3;
		Point start = sectors[0][0].offset;
		Point end = new Point(start.x + mapSize , start.y + mapSize);

		// Generate a list of tiles, walls, and roofs to be drawn
		List<Quad> tiles = new LinkedList<Quad>();
		List<Quad> overlays = new LinkedList<Quad>();
		List<Quad> walls = new LinkedList<Quad>();

		// For each tile, decide what elements should be drawn
		for (int y = start.y;y < end.y;y++) {
			for (int x = start.x;x < end.x;x++) {
				LocalTile tile = this.getTile(x, y);
				Dimension texture = tileTextures.getSprite(tile == null ? 0 : tile.getTexture());

				// TODO: Elevation

				tiles.add(new Quad(
					new Vertex(x, 0, -y, texture.x1, texture.y2),			// front left
					new Vertex(x, 0, -y - 1, texture.x1, texture.y1),		// back left
					new Vertex(x + 1, 0, -y - 1, texture.x2, texture.y1),	// back right
					new Vertex(x + 1, 0, -y, texture.x2, texture.y2)		// front right
				));

				if (tile == null)
					continue;

				if (tile.hasOverlay()) {
					texture = overlayTextures.getSprite(tile.getOverlay());

					overlays.add(new Quad(
						new Vertex(x, 0, -y, texture.x1, texture.y2),			// front left
						new Vertex(x, 0, -y - 1, texture.x1, texture.y1),		// back left
						new Vertex(x + 1, 0, -y - 1, texture.x2, texture.y1),	// back right
						new Vertex(x + 1, 0, -y, texture.x2, texture.y2)		// front right
					));
				}

				if (tile.hasHWall()) {
					texture = wallTextures.getSprite(tile.getHWall());

					walls.add(new Quad(
						new Vertex(x, 0, -y - 1, texture.x1, texture.y2),							// bottom left
						new Vertex(x, LocalTile.WALL_HEIGHT, -y - 1, texture.x1, texture.y1),		// top left
						new Vertex(x + 1, LocalTile.WALL_HEIGHT, -y - 1, texture.x2, texture.y1),	// top right
						new Vertex(x + 1, 0, -y - 1, texture.x2, texture.y2)						// bottom right
					));
				}

				if (tile.hasVWall()) {
					texture = wallTextures.getSprite(tile.getVWall());

					walls.add(new Quad(
						new Vertex(x, 0, -y, texture.x1, texture.y2),								// bottom left
						new Vertex(x, LocalTile.WALL_HEIGHT, -y, texture.x1, texture.y1),			// top left
						new Vertex(x, LocalTile.WALL_HEIGHT, -y - 1, texture.x2, texture.y1),		// top right
						new Vertex(x, 0, -y - 1, texture.x2, texture.y2)							// bottom right
					));
				}
			}
		}

		if (tileBuffer != null)
			tileBuffer.destroy();

		tileBuffer = tiles.isEmpty() ? null : new QuadBuffer(tiles, tileTextures.sprite);

		if (overlayBuffer != null)
			overlayBuffer.destroy();

		overlayBuffer = overlays.isEmpty() ? null : new QuadBuffer(overlays, overlayTextures.sprite);

		if (wallBuffer != null)
			wallBuffer.destroy();

		wallBuffer = walls.isEmpty() ? null : new QuadBuffer(walls, wallTextures.sprite);
	}

	public void draw() {
		if (tileBuffer != null)
			tileBuffer.display();

		if (overlayBuffer != null)
			overlayBuffer.display();

		if (wallBuffer != null)
			wallBuffer.display();
	}

	public boolean isTileBuffer(Pickable item) {
		return item == tileBuffer || item == overlayBuffer;
	}

	public Point getOffset() {
		if (sectors[0][0] == null)
			return null;

		return sectors[0][0].offset;
	}

	@Override
	public LocalTile getTile(int x, int y) {
		Point sector = this.calcLocalSector(x, y);
		if (sector == null || sector.x < 0 || sector.x >= 3 || sector.y < 0 || sector.y >= 3)
			return null;

		return sectors[sector.x][sector.y].getTile(x, y);
	}

	@Override
	public LocalTile getTile(Point p) {
		return this.getTile(p.x, p.y);
	}
}
