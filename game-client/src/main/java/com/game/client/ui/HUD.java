package com.game.client.ui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.game.client.Client;
import com.game.client.WorldManager;
import com.game.client.ui.menu.Friends;
import com.game.client.ui.menu.Inventory;
import com.game.client.ui.menu.Magic;
import com.game.client.ui.menu.MiniMap;
import com.game.client.ui.menu.Options;
import com.game.client.ui.menu.PlayerStats;
import com.game.common.model.Stat;
import com.game.graphics.input.Keyboard;
import com.game.graphics.renderer.Graphics2D;
import com.game.graphics.widget.Icon;
import com.game.graphics.widget.ProgressBar;
import com.game.graphics.widget.TextField;
import com.game.graphics.widget.Widget;
import com.game.graphics.widget.Window;

public class HUD extends Window implements ActionListener {
	private static class MenuIcon {
		public final Icon icon;
		public final Menu menu;

		public MenuIcon(Icon icon, Menu menu) {
			this.icon = icon;
			this.menu = menu;
		}
	};

	private static class FilterIcon {
		public final Icon icon;
		public final ChatMessages.Type type;

		public FilterIcon(Icon icon, ChatMessages.Type type) {
			this.icon = icon;
			this.type = type;
		}
	}

	private static final Color BACKGROUND_COLOR = Color.DARK_GRAY;
	private static final Color BACKGROUND_INPUT_OVERLAY = new Color(0, 0, 0, 0.8f);
	private static final Color BORDER_COLOR = Color.BLACK;
	private static final int SIZE = 32;

	protected final Client client;
	protected final WorldManager world;
	protected final ChatMessages chatMessages;
	protected final TextField chatField;

	protected final ProgressBar healthBar;

	protected final MenuIcon[] menus;
	protected MenuIcon activeMenu;

	protected final FilterIcon[] filters;
	protected FilterIcon activeFilter;

	protected TextField inputBox;
	protected boolean showFPS;

	public HUD(Client client) {
		super (false);

		int x, y;

		this.client = client;
		world = client.getWorldManager();

		menus = new MenuIcon[6];

		// settings
		menus[0] = new MenuIcon(
			new Icon(client.getGraphics().loadSprite("icons/settings.png"), SIZE, SIZE),
			new Options(world)
		);

		// stats
		menus[1] = new MenuIcon(
			new Icon(client.getGraphics().loadSprite("icons/stats.png"), SIZE, SIZE),
			new PlayerStats(world, client.getGraphics(), client.getMouse())
		);

		// map
		menus[2] = new MenuIcon(
			new Icon(client.getGraphics().loadSprite("icons/map.png"), SIZE, SIZE),
			new MiniMap(world, client.getGraphics())
		);

		// inventory
		menus[3] = new MenuIcon(
			new Icon(client.getGraphics().loadSprite("icons/inventory.png"), SIZE, SIZE),
			new Inventory(world, client.getGraphics())
		);

		// friends
		menus[4] = new MenuIcon(
			new Icon(client.getGraphics().loadSprite("icons/friends.png"), SIZE, SIZE),
			new Friends(world, this, client.getMouse())
		);

		// magic
		menus[5] = new MenuIcon(
			new Icon(client.getGraphics().loadSprite("icons/wand.png"), SIZE, SIZE),
			new Magic(world)
		);

		x = 0;
		y = 0;
		for (MenuIcon menuIcon : menus) {
			menuIcon.icon.setBackgroundColor(null);
			menuIcon.icon.setBorderColor(null);
			menuIcon.icon.addActionListener(this);

			super.add(menuIcon.icon, x, y);
			y += SIZE;
		}

		filters = new FilterIcon[4];

		// all
		filters[0] = new FilterIcon(
			new Icon(client.getGraphics().loadSprite("icons/star.png"), SIZE / 2, SIZE / 2),
			null
		);

		// messages
		filters[1] = new FilterIcon(
			new Icon(client.getGraphics().loadSprite("icons/information.png"), SIZE / 2, SIZE / 2),
			ChatMessages.Type.MESSAGE
		);

		// local chat
		filters[2] = new FilterIcon(
			new Icon(client.getGraphics().loadSprite("icons/comment.png"), SIZE / 2, SIZE / 2),
			ChatMessages.Type.LOCAL_CHAT
		);

		// private messages
		filters[3] = new FilterIcon(
			new Icon(client.getGraphics().loadSprite("icons/comment_delete.png"), SIZE / 2, SIZE / 2),
			ChatMessages.Type.PRIVATE_CHAT
		);

		x = 0;
		y = client.height - (SIZE / 2);
		for (int i = 0;i < filters.length;i++) {
			filters[i].icon.setBackgroundColor(null);
			filters[i].icon.setBorderColor(null);
			filters[i].icon.addActionListener(this);

			super.add(filters[i].icon, x, y);
			if (i % 2 != 0) {
				x = 0;
				y -= (SIZE / 2);
			}
			else
				x += (SIZE / 2);
		}

		chatField = new TextField(client.width - SIZE, client.getGraphics().get2D().getFontHeight(), true);

		// For the chat field we want white bold text with no border or background
		chatField.setTextColor(Color.WHITE);
		chatField.setBackgroundColor(null);
		chatField.setBorderColor(null);
		chatField.setHighlightFocus(false);
		chatField.addActionListener(this);
		chatField.setFocus(true);

		super.add(chatField, SIZE, client.height - chatField.height - 4);

		chatMessages = new ChatMessages(client, client.width - SIZE, client.getGraphics().get2D().getFontHeight() * 6);

		// For the chat messages we want white bold text with no border or background
		chatMessages.setTextColor(Color.WHITE);
		chatMessages.setBackgroundColor(null);
		chatMessages.setBorderColor(null);
		chatMessages.setHighlightFocus(false);

		super.add(chatMessages, SIZE, client.height - chatField.height - chatMessages.height - 4);

		final Color HEALTH_GOOD = new Color(0f, 0.4f, 0f);
		final Color HEALTH_OKAY = Color.ORANGE;
		final Color HEALTH_BAD = Color.RED;

		healthBar = new ProgressBar(10, 100) {
			@Override
			public int getValue() {
				Stat stat = world.getStats().get(Stat.Type.HITPOINTS);
				double fraction = (double)stat.getCurrent() / (double)stat.getLevel();
				// Ensure we don't shoot over the top
				if (fraction > 1)
					fraction = 1D;

				// Set the correct colour
				if (fraction > 0.7D)
					this.color = HEALTH_GOOD;
				else if (fraction > 0.2D)
					this.color = HEALTH_OKAY;
				else
					this.color = HEALTH_BAD;

				return (int) (fraction * 100);
			}
		};
		healthBar.setBackgroundColor(null);

		super.add(healthBar, 3, (menus.length * SIZE) + 10);

		this.setActiveMenu(null);
		this.setActiveFilter(filters[0]); // The "all chat" filter is enabled by default

		inputBox = null;
		showFPS = false;
	}

	public TextField getInputBox(final String prefix) {
		inputBox = new TextField(600, 30) {
			@Override
			public String getVisibleText() {
				return prefix + ": " + super.getVisibleText();
			}
		};

		inputBox.setBackgroundColor(Color.BLACK);
		inputBox.setTextColor(Color.WHITE);
		inputBox.setBorderColor(Color.WHITE);
		inputBox.setHighlightFocus(false);
		inputBox.setFocus(true);

		return inputBox;
	}

	public boolean hasInputBox() {
		return inputBox != null;
	}

	public void destroyInputBox() {
		inputBox = null;
	}

	public void addMessage(String message, ChatMessages.Type type) {
		chatMessages.add(message, type);
	}

	public void update(long now) {
		chatMessages.update(now);

		// Update the active menu, if there is one
		if (activeMenu != null)
			activeMenu.menu.update(now);
	}

	@Override
	public void display(Graphics2D g) {
		g.fillRect(0, 0, SIZE, client.height, BACKGROUND_COLOR);
		g.drawLine(0, 0, 0, client.height, BORDER_COLOR);
		g.drawLine(SIZE, 0, SIZE, client.height, BORDER_COLOR);

		super.display(g);

		// Draw the active menu, if there is one
		if (activeMenu != null)
			activeMenu.menu.display(g, SIZE, 0);

		// If the input box is active, draw it ontop
		if (inputBox != null) {
			g.fillRect(0, 0, client.width, client.height, BACKGROUND_INPUT_OVERLAY);

			inputBox.displayWidget(g, (client.width / 2) - 300, (client.height / 2) - 15);
		}

		// If enabled, show the current FPS
		if (showFPS) {
			String currentFPS = "FPS: " + client.getCurrentFPS();
			g.drawString(currentFPS, client.width - g.getFontWidth(currentFPS) - 4, g.getFontHeight(), Color.YELLOW);
		}
	}

	private void setActiveFilter(FilterIcon activeFilter) {
		if (this.activeFilter != null) {
			this.activeFilter.icon.setBackgroundColor(null);
			this.activeFilter.icon.setBorderColor(null);
		}

		this.activeFilter = activeFilter;

		if (this.activeFilter != null) {
			this.activeFilter.icon.setBackgroundColor(Color.BLUE);
			this.activeFilter.icon.setBorderColor(BORDER_COLOR);
			chatMessages.setFilterType(this.activeFilter.type);
		}
	}

	private void setActiveMenu(MenuIcon activeMenu) {
		if (this.activeMenu != null) {
			this.activeMenu.icon.setBackgroundColor(null);
			this.activeMenu.icon.setBorderColor(null);
		}

		this.activeMenu = activeMenu;

		if (this.activeMenu != null) {
			this.activeMenu.icon.setBackgroundColor(Color.BLUE);
			this.activeMenu.icon.setBorderColor(BORDER_COLOR);
		}
	}

	@Override
	public boolean mouseClicked(int x, int y, boolean left) {
		// The input box has focus
		if (inputBox != null) {
			// If we clicked outside the box, cancel it
			if (x < (client.width / 2) - 300 || x > (client.width / 2) + 300 || y < (client.height / 2) - 15 || y > (client.height / 2) + 15) {
				this.destroyInputBox();
			}

			return true;
		}

		// If there is an active menu, and it was clicked
		if (activeMenu != null && x >= SIZE && x < (SIZE + activeMenu.menu.width) && y >= 0 && y < activeMenu.menu.height)
			return activeMenu.menu.mouseClicked(x - SIZE, y, left);

		// If the click was outside the HUD
		if (x > SIZE) {
			this.setActiveMenu(null);
			return false;
		}

		// If an added widget wasn't clicked
		if (!super.mouseClicked(x, y, left))
			this.setActiveMenu(null);

		return true;
	}

	@Override
	public void keyPressed(int keyCode, char keyChar) {
		switch (keyCode) {
		case Keyboard.KEY_F12: {
			showFPS = !showFPS;
			return;
		}
		}

		if (inputBox != null) {
			inputBox.keyPressed(keyCode, keyChar);
			return;
		}

		chatField.keyPressed(keyCode, keyChar);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Widget source = (Widget) e.getSource();
		if (source == chatField) {
			String message = chatField.getText();
			// If there is no message we don't care
			if (message.isEmpty())
				return;

			chatField.clear();

			chatMessages.add(world.getUsername() + ": " + message, ChatMessages.Type.LOCAL_CHAT);
			world.sendChat(message);
			return;
		}

		for (MenuIcon menuIcon : menus) {
			if (source == menuIcon.icon) {
				this.setActiveMenu(activeMenu == menuIcon ? null : menuIcon);
				return;
			}
		}

		for (FilterIcon filterIcon : filters) {
			if (source == filterIcon.icon) {
				this.setActiveFilter(filterIcon);
				return;
			}
		}
	}
}
