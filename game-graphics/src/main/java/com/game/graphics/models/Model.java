package com.game.graphics.models;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

import com.game.graphics.renderer.Graphics;
import com.game.graphics.renderer.Pickable;

public abstract class Model extends Pickable {

	protected final Graphics graphics;
	protected final boolean animated;

	protected float scale;
	protected int rotation;
	protected Mesh[] meshes;

	public Model(Graphics graphics, boolean animated) {
		this.graphics = graphics;
		this.animated = animated;

		this.setScale(1);
		this.setRotation(0);

		meshes = new Mesh[0];
	}
	
	public boolean isAnimated() {
		return animated;
	}

	public float getScale() {
		return scale;
	}

	public void setScale(float scale) {
		this.scale = scale;
	}

	public int getRotation() {
		return rotation;
	}

	public void setRotation(int rotation) {
		rotation += 180;
		this.rotation = 360 - (rotation % 360);
	}

	protected void init() {
		// TODO: Load this model into a vertex buffer, so the only way to draw is via the buffer
	}

	public abstract void update();

	@Override
	protected void draw() {
		for (Mesh mesh : meshes) {
			// Set the texture for this mesh
			Material material = mesh.getMaterial();
			if (material != null) {
				GL11.glMaterial(GL11.GL_FRONT, GL11.GL_AMBIENT, material.ambient);
				GL11.glMaterial(GL11.GL_FRONT, GL11.GL_DIFFUSE, material.diffuse);
				GL11.glMaterial(GL11.GL_FRONT, GL11.GL_SPECULAR, material.specular);
				GL11.glMaterial(GL11.GL_FRONT, GL11.GL_EMISSION, material.emissive);
				GL11.glMaterialf(GL11.GL_FRONT, GL11.GL_SHININESS, material.shininess);

				// If we have a texture, bind it
				if (material.colorMap != null)
					GL11.glBindTexture(GL11.GL_TEXTURE_2D, material.colorMap.textureID);
			}

			GL11.glBegin(GL11.GL_TRIANGLES);
			{
				for (Triangle triangle : mesh.triangles) {
					for (Triangle.Point point : triangle.points) {
						GL11.glNormal3f(point.normal.x, point.normal.y, point.normal.z);
						GL11.glTexCoord2f(point.vertex.texturePosition.x, point.vertex.texturePosition.y);

						// If there is no bone, just draw the vertex
						Joint joint = point.vertex.getJoint();
						if (joint == null || !animated)
							GL11.glVertex3f(point.vertex.position.x, point.vertex.position.y, point.vertex.position.z);
						// Otherwise calculate the animation and draw
						else {
							Vector3f animationVector = joint.finalMatrix.vectorRotate(point.vertex.position); // TODO: or joint.position?

							animationVector.x += joint.finalMatrix.m03;
							animationVector.y += joint.finalMatrix.m13;
							animationVector.z += joint.finalMatrix.m23;

							GL11.glVertex3f(animationVector.x, animationVector.y, animationVector.z);
						}
					}
				}
			}
			GL11.glEnd();

			// Unbind the texture
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		}
	}

	@Override
	public String toString() {
		return "model[meshes = " + meshes.length + "]";
	}
}
