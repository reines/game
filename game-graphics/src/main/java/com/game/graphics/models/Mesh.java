package com.game.graphics.models;

public class Mesh {

	protected final String name;

	protected final int flags;

	protected int materialIndex;
	protected Material material;

	public final Triangle[] triangles;

	public Mesh(String name, int flags, int materialIndex, Triangle[] triangles) {
		this.name = name;

		this.flags = flags;

		this.materialIndex = materialIndex;
		material = null;

		this.triangles = triangles;
	}

	public int getMaterialIndex() {
		return materialIndex;
	}

	public void setMaterial(Material material) {
		this.material = material;
	}

	public Material getMaterial() {
		return material;
	}
}
