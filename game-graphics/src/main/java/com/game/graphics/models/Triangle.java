package com.game.graphics.models;

import org.lwjgl.util.vector.Vector3f;

public class Triangle {

	public static class Point {
		public final Vertex vertex;
		public final Vector3f normal;

		public Point(Vertex vertex, Vector3f normal) {
			this.vertex = vertex;
			this.normal = normal;
		}
	}

	protected final int flags;

	public final Point[] points;
	public final int smoothingGroup;

	public Triangle(int flags, Point[] points, int smoothingGroup) {
		this.flags = flags;

		this.points = points;
		this.smoothingGroup = smoothingGroup;
	}
}
