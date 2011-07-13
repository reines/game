package com.game.graphics.widget;

import java.awt.AWTEventMulticaster;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.game.graphics.renderer.Graphics2D;

public abstract class Widget {
	private transient ActionListener actionListener;

	public final int width;
	public final int height;
	public final boolean focusable;

	protected Color backgroundColor;
	protected Color borderColor;
	protected Color textColor;
	protected boolean highlightFocus;

	protected boolean focused;

	public Widget(int width, int height, boolean focusable) {
		this.width = width;
		this.height = height;
		this.focusable = focusable;

		backgroundColor = Color.WHITE;
		borderColor = Color.BLACK;
		textColor = Color.BLACK;
		highlightFocus = true;

		focused = false;
	}
	
	public boolean isFocusable() {
		return focusable;
	}

	public void setTextColor(Color textColor) {
		this.textColor = textColor;
	}

	public void setBackgroundColor(Color backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public void setBorderColor(Color borderColor) {
		this.borderColor = borderColor;
	}

	public void setHighlightFocus(boolean highlightFocus) {
		this.highlightFocus = highlightFocus;
	}

	public boolean hasFocus() {
		return focused;
	}

	public void setFocus(boolean focused) {
		this.focused = focused;
	}

	public void addActionListener(ActionListener l) {
		if (l == null)
			return;

		actionListener = AWTEventMulticaster.add(actionListener, l);
	}

	public void removeActionListener(ActionListener l) {
		if (l == null)
			return;

		actionListener = AWTEventMulticaster.remove(actionListener, l);
	}

	protected void processActionEvent(ActionEvent e) {
		if (actionListener != null)
			actionListener.actionPerformed(e);
	}

	public boolean mouseClicked(int x, int y, boolean left) {
		return false;
	}

	public void keyPressed(int keyCode, char keyChar) { }

	protected abstract void display(Graphics2D g, int x, int y);

	public void displayWidget(Graphics2D g, int x, int y) {
		this.display(g, x, y);

		if (this.highlightFocus && this.focusable && this.focused)
			g.drawRect(x - 1, y - 1, width + 2, height + 2, Color.YELLOW);
	}

}
