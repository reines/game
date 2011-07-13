package com.game.graphics.models;

import org.lwjgl.util.vector.Vector3f;

public class Keyframe {

	public final float time;
	public final Vector3f position;

	public Keyframe(float time, Vector3f position) {
		this.time = time;
		this.position = position;
	}
}
