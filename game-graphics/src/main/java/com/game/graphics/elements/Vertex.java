package com.game.graphics.elements;

public class Vertex {

	// Vertex coordinates
	public final float x;
	public final float y;
	public final float z;

	// Texture coordinates
	public final float u;
	public final float v;

	public Vertex(float x, float y, float z, float u, float v) {
		this.x = x;
		this.y = y;
		this.z = z;

		this.u = u;
		this.v = v;
	}

	@Override
	public String toString() {
		return "vertex[x = " + x + ", y = " + y + ",z = " + z + "]";
	}
}
