package com.game.graphics.models;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

public class Vertex {

	protected final int flags;

	public final Vector3f position;
	public final Vector2f texturePosition;

	protected int jointIndex;
	protected Joint joint;

	public Vertex(int flags, Vector3f position, Vector2f texturePosition, int jointIndex) {
		this.flags = flags;

		this.position = position;
		this.texturePosition = texturePosition;

		this.jointIndex = jointIndex;
		joint = null;
	}

	public int getJointIndex() {
		return jointIndex;
	}

	public void setJoint(Joint joint, boolean animated) {
		// Attempting to set the same joint
		if (this.joint == joint)
			return;

		if (this.joint != null) {
			// fatal error
			throw new RuntimeException("Attempted to set a joint on an already set vertex.");
		}

		this.joint = joint;

		// Set up the rotation stuff
		if (animated) {
			position.x -= joint.absoluteMatrix.m03;
			position.y -= joint.absoluteMatrix.m13;
			position.z -= joint.absoluteMatrix.m23;
	
			Vector3f inverseRotationVector = joint.absoluteMatrix.vectorIRotate(position);
			position.set(inverseRotationVector);
		}
	}

	public Joint getJoint() {
		return joint;
	}
}
