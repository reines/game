package com.game.graphics.models;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import com.game.graphics.renderer.Sprite;

public class Material {
	private static final ByteBuffer dummyBuffer = ByteBuffer.allocateDirect(16).order(ByteOrder.nativeOrder());

	public final String name;

	public final FloatBuffer ambient;
	public final FloatBuffer diffuse;
	public final FloatBuffer specular;
	public final FloatBuffer emissive;
	public final float shininess;
	public final float transparency;

	public final Sprite colorMap;
	public final Sprite alphaMap;

	public Material(String name, float[] ambient, float[] diffuse, float[] specular, float[] emissive, float shininess, float transparency, Sprite colorMap, Sprite alphaMap) {
		this.name = name;

		this.ambient = (FloatBuffer) dummyBuffer.asFloatBuffer().put(ambient).flip();
		this.diffuse = (FloatBuffer) dummyBuffer.asFloatBuffer().put(diffuse).flip();
		this.specular = (FloatBuffer) dummyBuffer.asFloatBuffer().put(specular).flip();
		this.emissive = (FloatBuffer) dummyBuffer.asFloatBuffer().put(emissive).flip();
		this.shininess = shininess;
		this.transparency = transparency;

		this.colorMap = colorMap;
		this.alphaMap = alphaMap;
	}
}
