package com.game.graphics.widget;

public class PasswordField extends TextField {

	public PasswordField(int width, int height) {
		super (width, height);
	}

	public String getVisibleText() {
		int length = super.getVisibleText().length();

		StringBuilder builder = new StringBuilder(length);
		for (int i = 0;i < length;i++)
			builder.append('*');

		return builder.toString();
	}
}
