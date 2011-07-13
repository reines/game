package com.game.graphics.elements;

public class Quad {

	public final Vertex bottomLeft;
	public final Vertex topLeft;
	public final Vertex topRight;
	public final Vertex bottomRight;

	public Quad(Vertex bottomLeft, Vertex topLeft, Vertex topRight, Vertex bottomRight) {
		this.bottomLeft = bottomLeft;
		this.topLeft = topLeft;
		this.topRight = topRight;
		this.bottomRight = bottomRight;
	}
}
