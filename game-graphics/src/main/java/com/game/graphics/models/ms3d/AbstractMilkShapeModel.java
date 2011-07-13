package com.game.graphics.models.ms3d;

import java.io.IOException;

import org.lwjgl.util.vector.Vector3f;

import com.game.graphics.math.Matrix34f;
import com.game.graphics.models.Joint;
import com.game.graphics.models.Keyframe;
import com.game.graphics.models.Material;
import com.game.graphics.models.Mesh;
import com.game.graphics.models.Model;
import com.game.graphics.models.Triangle;
import com.game.graphics.models.Triangle.Point;
import com.game.graphics.renderer.Graphics;

public class AbstractMilkShapeModel extends Model {
	public static final boolean ANIMATED = false;
	
	protected int numFrames;

	protected Joint[] joints;
	protected Material[] materials;
	
	public AbstractMilkShapeModel(Graphics graphics) throws IOException {
		super (graphics, ANIMATED);

		numFrames = 0;

		joints = null;
		materials = null;
	}

	@Override
	protected void init() {
		// Update the material and joint reference in all meshes
		for (Mesh mesh : super.meshes) {
			mesh.setMaterial(materials[mesh.getMaterialIndex()]);

			for (Triangle triangle : mesh.triangles)
				for (Point point : triangle.points)
					point.vertex.setJoint(joints[point.vertex.getJointIndex()], super.isAnimated());
		}

		super.init();
	}

	@Override
	public void update() {
		// If we aren't animated, do nothing
		if (!super.isAnimated())
			return;
		
		float currentTime = 0; // TODO: Calculate the current frame/time

		for (Joint joint : joints) {
			if (joint.positionKeyframes.length == 0 && joint.rotationKeyframes.length == 0) {
				joint.finalMatrix.copy(joint.absoluteMatrix);
				continue;
			}

			Matrix34f slerpedMatrix = new Matrix34f(); // TODO: wtf is this?

			Keyframe lastPositionFrame = null;
			Keyframe currentPositionFrame = null;
			for (Keyframe frame : joint.positionKeyframes) {
				if (frame.time >= currentTime) {
					currentPositionFrame = frame;
					break;
				}
				lastPositionFrame = frame;
			}

			Vector3f position = new Vector3f();

			if (lastPositionFrame == null) {
				currentPositionFrame.position.set(position);
			}
			else if (currentPositionFrame == null) {
				lastPositionFrame.position.set(position);
			}
			else {
				float timeDifference = currentPositionFrame.time - lastPositionFrame.time;
				float s = (currentTime - lastPositionFrame.time) / timeDifference; // TODO: What is this?

				position.x = lastPositionFrame.position.x + (currentPositionFrame.position.x - lastPositionFrame.position.x) * s;
				position.y = lastPositionFrame.position.y + (currentPositionFrame.position.y - lastPositionFrame.position.y) * s;
				position.z = lastPositionFrame.position.z + (currentPositionFrame.position.z - lastPositionFrame.position.z) * s;
			}

			Keyframe lastRotationFrame = null;
			Keyframe currentRotationFrame = null;
			for (Keyframe frame : joint.rotationKeyframes) {
				if (frame.time >= currentTime) {
					currentRotationFrame = frame;
					break;
				}
				lastRotationFrame = frame;
			}

			Vector3f rotation = new Vector3f();

			if (lastRotationFrame == null) {
				rotation.x = currentRotationFrame.position.x * 180 / (float) Math.PI;
				rotation.y = currentRotationFrame.position.y * 180 / (float) Math.PI;
				rotation.z = currentRotationFrame.position.z * 180 / (float) Math.PI;

				slerpedMatrix.angleMatrix(rotation);
			}
			else if (currentRotationFrame == null) {
				rotation.x = lastRotationFrame.position.x * 180 / (float) Math.PI;
				rotation.y = lastRotationFrame.position.y * 180 / (float) Math.PI;
				rotation.z = lastRotationFrame.position.z * 180 / (float) Math.PI;

				slerpedMatrix.angleMatrix(rotation);
			}
			else {
				float timeDifference = currentRotationFrame.time - lastRotationFrame.time;
				float s = (currentTime - lastRotationFrame.time) / timeDifference; // TODO: What is this?

				// TODO: slerp de derp
			}

			slerpedMatrix.m03 = position.x;
			slerpedMatrix.m13 = position.y;
			slerpedMatrix.m23 = position.z;

			slerpedMatrix.concat(joint.relativeMatrix, slerpedMatrix);

			if (joint.parent != null)
				joint.finalMatrix.concat(slerpedMatrix);
			else
				joint.finalMatrix.copy(slerpedMatrix);
		}
	}
}
