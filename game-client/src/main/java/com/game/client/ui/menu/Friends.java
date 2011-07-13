package com.game.client.ui.menu;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.game.client.WorldManager;
import com.game.client.ui.ChatMessages;
import com.game.client.ui.HUD;
import com.game.client.ui.Menu;
import com.game.common.model.Friend;
import com.game.common.util.FriendList;
import com.game.graphics.input.Mouse;
import com.game.graphics.renderer.Graphics2D;
import com.game.graphics.widget.TextField;

public class Friends extends Menu implements ActionListener {
	public static final Color FRIEND_ONLINE_COLOR = Color.GREEN;
	public static final Color FRIEND_OFFLINE_COLOR = Color.WHITE;

	protected final WorldManager world;
	protected final HUD hud;
	protected final Mouse mouse;
	protected TextField inputField;
	protected Friend activeFriend;

	public Friends(WorldManager world, HUD hud, Mouse mouse) {
		super("Friends", 140, 200);

		this.world = world;
		this.hud = hud;
		this.mouse = mouse;

		inputField = null;
		activeFriend = null;
	}

	@Override
	public void onUpdate(long now) {
		if (inputField != null && !hud.hasInputBox())
			inputField = null;
	}

	@Override
	public void onMouseClicked(int x, int y, boolean left) {
		// If no friend is active, or the message Input is overriding
		if (activeFriend == null || inputField != null)
			return;

		if (left) {
			if (!activeFriend.isOnline()) {
				hud.addMessage(activeFriend.getUsername() + " is not currently online.", ChatMessages.Type.MESSAGE);
				return;
			}

			// Pop up an input box for the PM
			inputField = hud.getInputBox("Send to " + activeFriend.getUsername());
			inputField.addActionListener(this);

			return;
		}

		// TODO: Menu with remove from friends option
		System.out.println("Show menu for: " + activeFriend);
	}

	@Override
	public void onDisplay(Graphics2D g, int x, int y) {
		y += 4;

		this.setActiveFriend(null);

		FriendList friends = world.getFriends();
		for (Friend friend : friends) {
			Color color = friend.isOnline() ? FRIEND_ONLINE_COLOR : FRIEND_OFFLINE_COLOR;

			if (mouse.getX() > x && mouse.getX() < (x + super.width) && mouse.getY() >= y && mouse.getY() < (y + g.getFontHeight())) {
				this.setActiveFriend(friend);
				color = Color.RED;
			}

			y += g.getFontHeight();

			g.drawString(friend.getUsername(), x + 6, y, color, true);
		}
	}

	private void setActiveFriend(Friend activeFriend) {
		if (inputField != null)
			return;

		this.activeFriend = activeFriend;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == inputField) {
			String message = inputField.getText();
			if (!message.isEmpty()) {
				world.sendFriendMessage(activeFriend, message);
				hud.addMessage(world.getUsername() + ": " + message, ChatMessages.Type.PRIVATE_CHAT);
			}

			hud.destroyInputBox();
		}
	}
}
