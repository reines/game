package com.game.graphics.widget;

import java.awt.event.ActionEvent;

import org.lwjgl.input.Keyboard;

import com.game.graphics.renderer.Graphics2D;

public class Button extends Label {

	public Button(String label, int width, int height) {
		super (label, width, height, false, true);
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
		super.display(g, x, y);
	}
}
