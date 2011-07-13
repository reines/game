package com.game.graphics.renderer;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.GL11;

public abstract class Pickable {
	private static boolean picking;
	private static int currentColor;
	protected static final Map<Color, Pickable> items;

	static {
		currentColor = 1;
		items = new HashMap<Color, Pickable>();
	}

	protected static void beginPicking() {
		picking = true;
	}

	protected static void endPicking() {
		picking = false;
		GL11.glColor3f(1, 1, 1);
	}

	protected static Pickable getPickable(Color color) {
		return items.get(color);
	}

	protected final Color pickingColor;

	public Pickable() {
		pickingColor = new Color(Pickable.currentColor);

		items.put(pickingColor, this);
		Pickable.currentColor++; // TODO: If we're using under 24-bit color do we need to handle this differently?
	}

	public final void display() {
		if (picking)
			GL11.glColor3f(pickingColor.getRed() / 255f, pickingColor.getGreen() / 255f, pickingColor.getBlue() / 255f);

		this.draw();

		if (picking)
			GL11.glColor3f(1, 1, 1);
	}

	protected abstract void draw();
}
