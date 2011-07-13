package com.game.common.model;

import com.game.common.util.PathFinder;

public abstract class Map {
	public static final int MAGIC_NUMBER = 0x4531;

	protected final int width;
	protected final int height;
	protected final int sectorSize;

	protected Map(int width, int height, int sectorSize) {
		this.width = width;
		this.height = height;
		this.sectorSize = sectorSize;
	}

	public int getSectorSize() {
		return sectorSize;
	}

	public int getWidth() {
		return width * sectorSize;
	}

	public int getHeight() {
		return height * sectorSize;
	}

	public abstract Tile getTile(int x, int y);

	public abstract Tile getTile(Point p);

	public Path generatePath(Point start, Point target) {
		PathFinder finder = new PathFinder(this, start);

		return finder.generatePath(target);
	}

	public boolean isValidStep(Point from, Point to) {
		// If the step is more than 1 tile away, it isn't valid
		if (from.distanceTo(to) >= 2)
			return false;

		Tile fromTile = this.getTile(from);
		// This shouldn't happen!
		if (fromTile == null)
			return false;

		Tile toTile = this.getTile(to);
		// If it isn't found in the map, or isn't walkable then skip it
		if (toTile == null || !toTile.isWalkable())
			return false;

		// Moving left, check our old y
		if (from.x > to.x) {
			Tile toCheck = this.getTile(from.x, from.y);
			if (toCheck == null || toCheck.isBlockedVertically())
				return false;
		}
		// Moving right, check our old y
		else if (from.x < to.x) {
			Tile toCheck = this.getTile(from.x + 1, from.y);
			if (toCheck == null || toCheck.isBlockedVertically())
				return false;
		}

		// Moving down, check our old x
		if (from.y > to.y){
			Tile toCheck = this.getTile(from.x, from.y - 1);
			if (toCheck == null || toCheck.isBlockedHorizontally())
				return false;
		}
		// Moving up, check our old x
		else if (from.y < to.y) {
			Tile toCheck = this.getTile(from.x, from.y);
			if (toCheck == null || toCheck.isBlockedHorizontally())
				return false;
		}

		// We are moving straight, and aren't blocked
		if ((from.x == to.x) || (from.y == to.y))
			return true;

		// We are moving diagonal, so we also need to check the destination tile isn't blocked

		// Moving left, check our new y
		if (from.x > to.x) {
			Tile toCheck = this.getTile(to.x + 1, to.y);
			if (toCheck == null || toCheck.isBlockedVertically())
				return false;
		}
		// Moving right, check our new y
		else if (from.x < to.x) {
			Tile toCheck = this.getTile(to.x, to.y);
			if (toCheck == null || toCheck.isBlockedVertically())
				return false;
		}

		// Moving down, check our new x
		if (from.y > to.y) {
			Tile toCheck = this.getTile(to.x, to.y);
			if (toCheck == null || toCheck.isBlockedHorizontally())
				return false;
		}
		// Moving up, check our new x
		else if (from.y < to.y) {
			Tile toCheck = this.getTile(to.x, to.y - 1);
			if (toCheck == null || toCheck.isBlockedHorizontally())
				return false;
		}

		return true;
	}

	@Override
	public String toString() {
		return "map[width = " + this.getWidth() + ", height = " + this.getHeight() + ", sectorSize = " + this.getSectorSize() + "]";
	}
}
