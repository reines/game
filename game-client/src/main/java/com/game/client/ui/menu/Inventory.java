package com.game.client.ui.menu;

import java.awt.Color;
import java.awt.Point;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Map;

import com.game.client.WorldManager;
import com.game.client.ui.Menu;
import com.game.common.model.Item;
import com.game.graphics.renderer.Graphics;
import com.game.graphics.renderer.Graphics2D;
import com.game.graphics.renderer.Sprite;

public class Inventory extends Menu {
	public static final int BOX_WIDTH = 48;
	public static final int BOX_HEIGHT = 36;

	public static final int WIDTH = 5;
	public static final int HEIGHT = 8;

	public static final Color EQUIPED_COLOR = new Color(0.7f, 0, 0, 0.6f);

	protected final WorldManager world;
	protected final NumberFormat formatter;
	protected final Map<String, Sprite> sprites;

	public Inventory(WorldManager world, Graphics g) {
		super("Inventory", BOX_WIDTH * WIDTH, BOX_HEIGHT * HEIGHT);

		this.world = world;

		formatter = new DecimalFormat("#,###,###");
		sprites = g.loadSpritePack("items.zip");
	}

	@Override
	public void onUpdate(long now) { }

	@Override
	public void onMouseClicked(int x, int y, boolean left) {
		x /= BOX_WIDTH;
		y /= BOX_HEIGHT;

		int index = (y * WIDTH) + x;

		// If we clicked an empty box, ignore it
		if (index >= world.getInventory().size())
			return;

		// If it was a left click, use that item
		if (left)
			world.sendUseItem(index);
		// If it was a right click, handle the action menu
		else {
			// TODO: Handle the action menu for item <index>
			System.out.println("Show menu for: " + world.getInventory().get(index));
		}
	}

	@Override
	public void onDisplay(Graphics2D g, int x, int y) {
		int index = 0;
		Point offset = new Point(x, y);

		for (int by = 0;by < HEIGHT;by++) {
			offset.x = x;

			for (int bx = 0;bx < WIDTH;bx++) {
				Item item = world.getInventory().get(index++);

				// If we have an item, draw it
				if (item != null) {
					// If its equiped draw the background
					if (item.isEquiped())
						g.fillRect(offset.x, offset.y, BOX_WIDTH, BOX_HEIGHT, EQUIPED_COLOR);

					// Draw the actual item
					Sprite sprite = sprites.get(item.getID() + ".png");
					if (sprite != null)
						g.drawSprite(sprite, offset.x, offset.y, BOX_WIDTH, BOX_HEIGHT);

					// If its stackable, draw the amount
					if (item.isStackable())
						g.drawString(formatter.format(item.getAmount()), offset.x + 2, offset.y + g.getFontHeight(), Color.YELLOW, true);
				}

				if (super.borderColor != null)
					g.drawRect(offset.x, offset.y, BOX_WIDTH, BOX_HEIGHT, super.borderColor);

				offset.x += BOX_WIDTH;
			}

			offset.y += BOX_HEIGHT;
		}
	}
}
