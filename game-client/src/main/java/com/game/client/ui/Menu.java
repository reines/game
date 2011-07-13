package com.game.client.ui;

import java.awt.Color;

import com.game.graphics.renderer.Graphics2D;
import com.game.graphics.widget.Label;
import com.game.graphics.widget.Widget;
import com.game.graphics.widget.Window;

public abstract class Menu extends Widget {
	public static final int MENU_HEIGHT = 16;

	private final Window window;
	private final Label title;

	public Menu(String label, int width, int height) {
		super(width, height + MENU_HEIGHT, true);

		super.setBackgroundColor(new Color(1, 1, 1, 0.6f));

		window = new Window(false);

		// A title label, which consumes mouse clicks
		title = new Label(label, super.width, MENU_HEIGHT, false) {
			@Override
			public boolean mouseClicked(int x, int y, boolean left) {
				return true;
			}
		};

		title.setTextColor(Color.WHITE);
		title.setBackgroundColor(new Color(0, 0, 1, 0.8f));

		this.add(title, 0, 0);
	}

	public void add(Widget widget, int x, int y) {
		window.add(widget, x, y);
	}

	protected abstract void onUpdate(long now);

	public final void update(long now) {
		this.onUpdate(now);
	}

	protected abstract void onMouseClicked(int x, int y, boolean left);

	@Override
	public final boolean mouseClicked(int x, int y, boolean left) {
		if (!window.mouseClicked(x, y, left))
			this.onMouseClicked(x, y - title.height, left);

		return true;
	}

	protected abstract void onDisplay(Graphics2D g, int x, int y);

	@Override
	protected final void display(Graphics2D g, int x, int y) {
		if (super.backgroundColor != null)
			g.fillRect(x, y, width, height, super.backgroundColor);

		window.display(g, x, y);
		this.onDisplay(g, x, y + title.height);

		if (super.borderColor != null)
			g.drawRect(x, y, width, height, super.borderColor);
	}
}
