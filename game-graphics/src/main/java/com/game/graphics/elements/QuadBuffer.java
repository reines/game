package com.game.graphics.elements;

import java.util.List;

import org.lwjgl.opengl.GL11;

import com.game.graphics.renderer.Sprite;

public class QuadBuffer extends ElementBuffer {

	public QuadBuffer(List<Quad> quads, Sprite texture) {
		super (quads.size() * 4, texture, GL11.GL_QUADS);

		for (Quad quad : quads) {
			super.putVertex(quad.bottomLeft);
			super.putVertex(quad.topLeft);
			super.putVertex(quad.topRight);
			super.putVertex(quad.bottomRight);
		}

		super.updateBuffers();
	}
}
