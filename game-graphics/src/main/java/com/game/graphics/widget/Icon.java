package com.game.graphics.widget;

import java.awt.event.ActionEvent;

import org.lwjgl.input.Keyboard;

import com.game.graphics.renderer.Graphics2D;
import com.game.graphics.renderer.Sprite;

public class Icon extends Widget {

	protected final Sprite sprite;

	public Icon(Sprite sprite, int width, int height) {
		super (width, height, true);

		this.sprite = sprite;
	}

	@Override
	public boolean mouseClicked(int x, int y, boolean left) {
		ActionEvent e = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "mouseClicked");
		super.processActionEvent(e);

		return true;
	}

	@Override
	public void keyPressed(int keyCode, char keyChar) {
		switch (keyCode) {
		case Keyboard.KEY_RETURN:
			ActionEvent e = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "keyPressed");
			super.processActionEvent(e);
			break;
		}
	}

	@Override
	protected void display(Graphics2D g, int x, int y) {
		if (super.backgroundColor != null)
			g.fillRect(x, y, super.width, super.height, super.backgroundColor);

		g.drawSprite(sprite, x, y, super.width, super.height);

		if (super.borderColor != null)
			g.drawRect(x, y, super.width, super.height, super.borderColor);
	}
}
