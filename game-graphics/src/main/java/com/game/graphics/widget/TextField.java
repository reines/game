package com.game.graphics.widget;

import java.awt.event.ActionEvent;

import org.lwjgl.input.Keyboard;

import com.game.graphics.renderer.Graphics2D;

public class TextField extends Widget {

	protected final StringBuilder value;
	protected boolean shadow;

	public TextField(int width, int height) {
		this (width, height, false);
	}

	public TextField(int width, int height, boolean shadow) {
		super (width, height, true);

		this.shadow = shadow;

		value = new StringBuilder();
	}

	public void clear() {
		value.setLength(0);
	}

	public String getText() {
		return value.toString();
	}

	public String getVisibleText() {
		String text = this.getText();
		if (super.hasFocus())
			text += '*';

		return text;
	}

	public void setText(String value) {
		this.clear();
		this.value.append(value);
	}

	@Override
	public void keyPressed(int keyCode, char keyChar) {
		// Handle control characters
		if (Character.isISOControl(keyChar)) {
			switch (keyCode) {
			case Keyboard.KEY_BACK:
				if (value.length() > 0)
					value.setLength(value.length() - 1);
				break;

			case Keyboard.KEY_RETURN:
				ActionEvent e = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "keyPressed");
				super.processActionEvent(e);
				break;
			}

			return;
		}

		// Filter out invalid characters
		Character.UnicodeBlock block = Character.UnicodeBlock.of(keyChar);
		if (block == null || block == Character.UnicodeBlock.SPECIALS)
			return;

		value.append(keyChar);
	}

	@Override
	protected void display(Graphics2D g, int x, int y) {
		if (super.backgroundColor != null)
			g.fillRect(x, y, width, height, super.backgroundColor);

		g.drawString(this.getVisibleText(), x + 4, y + ((super.height - g.getFontHeight()) / 2) + g.getFontHeight(), super.textColor, shadow);

		if (super.borderColor != null)
			g.drawRect(x, y, width, height, super.borderColor);
	}
}
