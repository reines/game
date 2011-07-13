package com.game.graphics.renderer;

import java.awt.Color;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.vector.Vector3f;

import com.game.common.model.Point;
import com.game.graphics.models.Model;

public class Graphics3D {
	public static final Point DEFAULT_CAMERA_LOCATION = new Point(0, 0);
	public static final int DEFAULT_CAMERA_ZOOM = 8;

	protected final Graphics graphics;
	protected final DisplayMode mode;
	protected final FloatBuffer projMatrix;

	protected final FloatBuffer translationModelMatrix;
	protected final FloatBuffer translationProjMatrix;

	protected Point lastTarget;
	protected float lastZoom;
	protected int lastRotation;

	protected Graphics3D(Graphics graphics, DisplayMode mode) {
		this.graphics = graphics;
		this.mode = mode;

		// Set up our 3D mode
		GL11.glViewport(graphics.viewport.get(0), graphics.viewport.get(1), graphics.viewport.get(2), graphics.viewport.get(3));
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GLU.gluPerspective(45, (float) mode.getWidth() / (float) mode.getHeight(), 1, 35);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);

		projMatrix = BufferUtils.createFloatBuffer(16);
		GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, projMatrix);

		// Set up our lighting
		ByteBuffer dummyBuffer = BufferUtils.createByteBuffer(Float.SIZE * 4);
		dummyBuffer.order(ByteOrder.nativeOrder());

		// Ambient is enough for now
		GL11.glLight(GL11.GL_LIGHT0, GL11.GL_AMBIENT, (FloatBuffer) dummyBuffer.asFloatBuffer().put(new float[]{0.5f, 0.5f, 0.5f, 1f}).flip());

		GL11.glEnable(GL11.GL_LIGHT0);

		GL11.glEnable(GL11.GL_COLOR_MATERIAL);

		// Enable some fog
		GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_LINEAR);
		GL11.glFog(GL11.GL_FOG_COLOR, (FloatBuffer) BufferUtils.createFloatBuffer(4).put(new float[]{0, 0, 0, 1}).flip());
		GL11.glFogf(GL11.GL_FOG_DENSITY, 0.3f);
		GL11.glHint(GL11.GL_FOG_HINT, GL11.GL_DONT_CARE);
		GL11.glFogf(GL11.GL_FOG_START, 25);
		GL11.glFogf(GL11.GL_FOG_END, 35);

		translationModelMatrix = BufferUtils.createFloatBuffer(16);
		translationProjMatrix = BufferUtils.createFloatBuffer(16);
	}

	public void begin(Point target, float zoom, int rotation) {
		GL11.glPushMatrix();
		graphics.setMode(Graphics.Mode.G3D);

		// Enable lighting effects
		GL11.glEnable(GL11.GL_LIGHTING);

		// Enable fog
		GL11.glEnable(GL11.GL_FOG);

		// Enable depth testing
		GL11.glEnable(GL11.GL_DEPTH_TEST);

		// Enable anti-aliasing
		GL11.glEnable(GL11.GL_LINE_SMOOTH);

		// Enable culling
//		GL11.glEnable(GL11.GL_CULL_FACE); // TODO: We want to cull what we can't see from the camera - not from the origin!

		// Change the camera position
		this.setCamera(target, zoom, rotation);
	}

	public void end() {
		GL11.glPopMatrix();
	}

	public void beginPicking() {
		this.begin(lastTarget, lastZoom, lastRotation);
		GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);

		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_FOG);
		GL11.glDisable(GL11.GL_LINE_SMOOTH);
		GL11.glDisable(GL11.GL_DITHER);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_COLOR_MATERIAL);

		Pickable.beginPicking();

		GL11.glRenderMode(GL11.GL_SELECT);
	}

	public Pickable endPicking(int x, int y) {
		FloatBuffer pixels = BufferUtils.createFloatBuffer(3);
		GL11.glReadPixels(x, graphics.viewport.get(3) - y, 1, 1, GL11.GL_RGB, GL11.GL_FLOAT, pixels);
		Pickable target = Pickable.getPickable(new Color(pixels.get(0), pixels.get(1), pixels.get(2)));

		GL11.glRenderMode(GL11.GL_RENDER);

		Pickable.endPicking();

		GL11.glPopAttrib();
		this.end();

		return target;
	}

	public synchronized Point translateCoordinates(int x, int y) {
		y = graphics.viewport.get(3) - y;

		Vector3f coords = new Vector3f();
		this.begin(lastTarget, lastZoom, lastRotation);
		{
			translationModelMatrix.clear();
			translationProjMatrix.clear();

			GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, translationModelMatrix);
			GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, translationProjMatrix);

			FloatBuffer temp = BufferUtils.createFloatBuffer(3);
			GL11.glReadPixels(x, y, 1, 1, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, temp);

			coords.x = x;
			coords.y = y;
			coords.z = temp.get(0);

			temp.clear();
			if (GLU.gluUnProject(coords.x, coords.y, coords.z, translationModelMatrix, translationProjMatrix, graphics.viewport, temp) == GLU.GLU_FALSE)
				return null;

			coords.x = temp.get(0);
			coords.y = temp.get(1);
			coords.z = temp.get(2);
		}
		this.end();

		return new Point((int) coords.x, (int) coords.z * -1);
	}

	public Graphics getGraphics() {
		return graphics;
	}

	public void setCamera(Point target, float zoom, int rotation) {
		// Note the camera position in-case we need to calculate some mouse clicks later
		lastTarget = target;
		lastZoom = zoom;
		lastRotation = rotation;

		float x = (float) (zoom * Math.cos(Math.toRadians(rotation))) + target.x;
		float y = (float) (zoom * Math.sin(Math.toRadians(rotation))) - target.y;

		GLU.gluLookAt(
			x + 0.5f, zoom, y - 0.5f,				// Our location
			target.x + 0.5f, 1, -target.y - 0.5f,	// Where to look
			0, 1, 0									// Up vector
		);
	}

	protected void setColor(Color c) {
		GL11.glColor4f(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, c.getAlpha() / 255f);
	}

	public void drawModel(float x, float y, Model model) {
		x += 0.5f;
		y += 0.5f;

		float scale = model.getScale();

		// Advance the model to the next frame
		model.update(); // TODO: Should this be in update() rather than display()?

		GL11.glPushMatrix();
		{
			GL11.glTranslatef(x, 0, -y);
			GL11.glScalef(scale, scale, scale);
			GL11.glRotatef(model.getRotation(), 0, 1, 0);

			model.display();
		}
		GL11.glPopMatrix();
	}
}
