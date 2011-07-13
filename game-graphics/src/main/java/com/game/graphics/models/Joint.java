package com.game.graphics.models;

import org.lwjgl.util.vector.Vector3f;

import com.game.graphics.math.Matrix34f;

public class Joint {

	public final String name;
	public final Joint parent;

	protected final int flags;

	public final Vector3f position;
	public final Vector3f rotation;

	public final Keyframe[] positionKeyframes;
	public final Keyframe[] rotationKeyframes;

	public final Matrix34f relativeMatrix;
	public final Matrix34f absoluteMatrix;
	public final Matrix34f finalMatrix;

	public Joint(String name, Joint parent, int flags, Vector3f position, Vector3f rotation, Keyframe[] positionKeyframes, Keyframe[] rotationKeyframes) {
		this.name = name;
		this.parent = parent;

		this.flags = flags;

		this.position = position;
		this.rotation = rotation;

		this.positionKeyframes = positionKeyframes;
		this.rotationKeyframes = rotationKeyframes;

		relativeMatrix = new Matrix34f();
		absoluteMatrix = new Matrix34f();

		Vector3f rotationVector = new Vector3f(
			rotation.x * 180f / (float) Math.PI,
			rotation.y * 180f / (float) Math.PI,
			rotation.z * 180f / (float) Math.PI
		);

		relativeMatrix.angleMatrix(rotationVector);

		relativeMatrix.m03 = position.x;
		relativeMatrix.m13 = position.y;
		relativeMatrix.m23 = position.z;

		if (parent != null)
			absoluteMatrix.concat(relativeMatrix);
		else
			absoluteMatrix.copy(relativeMatrix);

		finalMatrix = new Matrix34f(absoluteMatrix);
	}
}
