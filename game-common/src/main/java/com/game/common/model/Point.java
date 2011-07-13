package com.game.common.model;

import java.io.Serializable;

public class Point implements Serializable {
	private static final long serialVersionUID = 1L;

	public static final Point ZERO = new Point(0, 0);

	public int x;
	public int y;

	@SuppressWarnings("unused")
	private Point() { } // For hibernate

	public Point(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public void set(Point location) {
		this.x = location.x;
		this.y = location.y;
	}

	public double distanceTo(Point p) {
		// Euclidean distance
		return Math.sqrt(Math.pow(this.x - p.x, 2) + Math.pow(this.y - p.y, 2));
	}

	@Override
	public int hashCode() {
		return x << 16 | y;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Point))
			return false;

		Point p = (Point) o;
		return x == p.x && y == p.y;
	}

	@Override
	public String toString() {
		return "(" + x + ", " + y + ")";
	}
}
