package com.game.graphics.elements;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.ARBVertexBufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;

import com.game.graphics.renderer.Pickable;
import com.game.graphics.renderer.Sprite;

public abstract class ElementBuffer extends Pickable {
	public static final int NO_BUFFER = -1;

	protected final int numVertices;
	protected final Sprite texture;
	protected final boolean supportsVBO;
	protected final int mode;

	protected final FloatBuffer vertexBuffer;
	protected final int vertexBufferID;

	protected final FloatBuffer textureBuffer;
	protected final int textureBufferID;

	public ElementBuffer(int numVertices, Sprite texture, int mode) {
		this.numVertices = numVertices;
		this.texture = texture;
		this.mode = mode;

		supportsVBO = GLContext.getCapabilities().GL_ARB_vertex_buffer_object;

		vertexBuffer = BufferUtils.createFloatBuffer(numVertices * 3);	// 3D coordinates
		vertexBufferID = supportsVBO ? ARBVertexBufferObject.glGenBuffersARB() : NO_BUFFER;

		textureBuffer = BufferUtils.createFloatBuffer(numVertices * 2);	// 2D coordinates
		textureBufferID = supportsVBO ? ARBVertexBufferObject.glGenBuffersARB() : NO_BUFFER;
	}

	protected void putVertex(Vertex v) {
		vertexBuffer.put(v.x);
		vertexBuffer.put(v.y);
		vertexBuffer.put(v.z);

		textureBuffer.put(v.u);
		textureBuffer.put(v.v);
	}

	protected void updateBuffers() {
		vertexBuffer.flip();
		textureBuffer.flip();

		// VBO is available, load the data into VRAM
		if (supportsVBO)
		{
			// Bind the vertex buffer
			ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, vertexBufferID);

			// Load the data into VRAM
			ARBVertexBufferObject.glBufferDataARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, vertexBuffer, ARBVertexBufferObject.GL_STATIC_DRAW_ARB);

			// Bind the texture buffer
			ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, textureBufferID);

			// Load the data into VRAM
			ARBVertexBufferObject.glBufferDataARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, textureBuffer, ARBVertexBufferObject.GL_STATIC_DRAW_ARB);

			// Unbind the buffers
			ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, 0);
		}
	}

	@Override
	protected void draw() {
		GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
		GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);

		if (supportsVBO) {
			// Bind the vertex buffer
			ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, vertexBufferID);

			// Get the vertex position data
			GL11.glVertexPointer(3, GL11.GL_FLOAT, 0, 0);

			// Bind the texture buffer
			ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, textureBufferID);

			// Get the textue position data
			GL11.glTexCoordPointer(2, GL11.GL_FLOAT, 0, 0);
		}
		else {
			// Get the vertex position data
			GL11.glVertexPointer(3, 0, vertexBuffer);

			// Get the texture position data
			GL11.glTexCoordPointer(2, 0, textureBuffer);
		}

		// Bind the texture
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.textureID);

		// Draw the stuff
		GL11.glDrawArrays(mode, 0, numVertices);

		// Unbind the texture
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

		if (supportsVBO) {
			// Unbind the buffers
			ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, 0);
		}

		GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
		GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
	}

	public void destroy() {
		// VBO is enabled, so remove the data from VRAM
		if (supportsVBO) {
			ARBVertexBufferObject.glDeleteBuffersARB(vertexBufferID);
			ARBVertexBufferObject.glDeleteBuffersARB(textureBufferID);
		}
	}
}
