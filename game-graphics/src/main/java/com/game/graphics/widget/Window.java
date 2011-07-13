package com.game.graphics.widget;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;

import com.game.graphics.renderer.Graphics2D;

public class Window {

	private static class WindowWidget {
		public final Widget widget;
		public final int x;
		public final int y;

		public WindowWidget(Widget widget, int x, int y) {
			this.widget = widget;
			this.x = x;
			this.y = y;
		}
	}

	protected final List<WindowWidget> widgets;
	protected final boolean handleFocus;
	private int focus;

	public Window() {
		this (true);
	}

	public Window(boolean handleFocus) {
		this.handleFocus = handleFocus;

		widgets = new ArrayList<WindowWidget>();
		focus = -1;
	}

	public void add(Widget widget, int x, int y) {
		WindowWidget container = new WindowWidget(widget, x, y);
		widgets.add(container);
	}

	public void display(Graphics2D g, int x, int y) {
		for (WindowWidget container : widgets)
			container.widget.displayWidget(g, x + container.x, y + container.y);
	}

	public void display(Graphics2D g) {
		this.display(g, 0 , 0);
	}

	public boolean mouseClicked(int x, int y, boolean left) {
		if (widgets.isEmpty())
			return false;

		boolean hit = false;
		for (int i = 0;i < widgets.size();i++) {
			WindowWidget container = widgets.get(i);
			int xOff = x - container.x;
			int yOff = y - container.y;

			if (xOff >= 0 && xOff < container.widget.width && yOff >= 0 && yOff < container.widget.height) {
				hit |= container.widget.mouseClicked(xOff, yOff, left);
				if (container.widget.isFocusable())
					this.setFocus(i);
			}
		}

		return hit;
	}

	public void keyPressed(int keyCode, char keyChar) {
		if (widgets.isEmpty())
			return;

		switch (keyCode) {
		case Keyboard.KEY_TAB:
			this.focusNext();
			break;
		default:
			if (focus > -1) {
				Widget widget = widgets.get(focus).widget;
				widget.keyPressed(keyCode, keyChar);
			}
			break;
		}
	}

	public boolean setFocus(int focus) {
		if (!handleFocus || widgets.isEmpty())
			return false;
		
		// Remove focus from the existing widget
		if (this.focus > -1) {
			Widget widget = widgets.get(this.focus).widget;
			if (widget.hasFocus())
				widget.setFocus(false);
		}

		this.focus = focus;

		// Add focus to the new widget
		if (this.focus > -1) {
			Widget widget = widgets.get(this.focus).widget;
			if (widget.isFocusable()) {
				// Focus on the new widget and return success
				widget.setFocus(true);
				return true;
			}
		}
		
		return false;
	}

	public void focusNext() {
		if (!handleFocus)
			return;

		if (widgets.isEmpty())
			this.setFocus(-1);
		else {
			// This will loop infinitely if we call focusNext() and no widgets allow focus!
			while (!this.setFocus((focus + 1) % widgets.size()));
		}
	}
}
