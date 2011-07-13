package com.game.client.ui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.game.client.Client;
import com.game.graphics.renderer.Graphics2D;
import com.game.graphics.renderer.Sprite;
import com.game.graphics.widget.Button;
import com.game.graphics.widget.Label;
import com.game.graphics.widget.PasswordField;
import com.game.graphics.widget.TextField;
import com.game.graphics.widget.Widget;
import com.game.graphics.widget.Window;

public class LoginWindow extends Window implements ActionListener {
	private static final Logger log = LoggerFactory.getLogger(LoginWindow.class);

	public static final String WELCOME_MESSAGE = "Please enter a username and password to login.";
	public static final Color WIDGET_BACKGROUND_COLOR = new Color(1, 1, 1, 0.9f);
	public static final Color BACKGROUND_COLOR = new Color(0, 0, 0, 0.8f);

	protected final Client client;
	protected LoadingBox loadingBox;

	protected final Sprite background;
	protected final Label label;
	protected final TextField usernameField;
	protected final PasswordField passwordField;
	protected final Button loginButton;

	public LoginWindow(Client client) {
		this.client = client;

		background = client.getGraphics().loadSprite("loading_background.jpg");

		loadingBox = null;

		label = new Label(WELCOME_MESSAGE, 300, 30, true);
		label.setBackgroundColor(null);
		label.setBorderColor(null);
		label.setTextColor(Color.WHITE);
		super.add(label, (client.width - label.width) / 2, 260);

		usernameField = new TextField(300, 30);
		usernameField.setBackgroundColor(WIDGET_BACKGROUND_COLOR);
		usernameField.addActionListener(this);
		super.add(usernameField, (client.width - usernameField.width) / 2, 300);

		passwordField = new PasswordField(300, 30);
		passwordField.setBackgroundColor(WIDGET_BACKGROUND_COLOR);
		passwordField.addActionListener(this);
		super.add(passwordField, (client.width - passwordField.width) / 2, 340);

		loginButton = new Button("Login", 100, 30);
		loginButton.setBackgroundColor(WIDGET_BACKGROUND_COLOR);
		loginButton.addActionListener(this);
		super.add(loginButton, (client.width - loginButton.width) / 2, 380);

		client.setUpdateRate(ClientFrame.UPDATE_RATE_MIN);
		super.setFocus(1);
	}

	public synchronized void failed(String reason) {
		label.setText(reason);
		log.info("Failed to login: " + reason);

		// Reset the focus and loading box so the user can try again
		super.setFocus(1);
		loadingBox = null;
	}

	public void update(long now) {
		if (loadingBox != null) {
			loadingBox.update(now);
			return;
		}
	}

	@Override
	public synchronized void display(Graphics2D g) {
		// Draw the background image
		g.drawSprite(background, 0, 0, client.width, client.height);

		// Draw the widgets
		super.display(g);

		// If loading, draw the loading screen
		if (loadingBox != null) {
			g.fillRect(0, 0, client.width, client.height, BACKGROUND_COLOR);

			loadingBox.display(g);
		}
	}

	@Override
	public boolean mouseClicked(int x, int y, boolean left) {
		// If loading, ignore input
		if (loadingBox != null)
			return true;

		return super.mouseClicked(x, y, left);
	}

	@Override
	public void keyPressed(int keyCode, char keyChar) {
		// If loading, ignore input
		if (loadingBox != null)
			return;

		super.keyPressed(keyCode, keyChar);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Widget source = (Widget) e.getSource();
		if (source instanceof TextField)
			super.focusNext();
		else if (source == loginButton) {
			String username = usernameField.getText();
			String password = passwordField.getText();

			if (username.isEmpty() || password.isEmpty())
				this.failed("Please enter both a username and password.");
			else {
				loadingBox = new LoadingBox("Logging in.", (client.width / 2) - 140, (client.height / 2) - 40, 280, 80);
				label.setText(WELCOME_MESSAGE);
				client.login(username, password);
			}
		}
	}
}
