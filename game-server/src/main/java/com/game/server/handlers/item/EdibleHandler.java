package com.game.server.handlers.item;

import com.game.common.model.Item;
import com.game.common.model.Stat;
import com.game.server.Server;
import com.game.server.WorldManager;
import com.game.server.handlers.ItemHandler;
import com.game.server.model.Player;

public class EdibleHandler implements ItemHandler {

	@Override
	public void handleItem(Server server, WorldManager world, Player player, Item item, int index) {
		Stat hitpoints = player.getStats().get(Stat.Type.HITPOINTS);
		String message = "You eat the " + item.getName().toLowerCase();

		player.getInventory().remove(index);
		player.sendInventoryRemove(index);

		// Our health isn't full so increase it
		int health = hitpoints.getCurrent();
		if (health < hitpoints.getLevel()) {
			// Increase the health by the correct amount, but not over full
			health += item.getEdible();
			if (health > hitpoints.getLevel())
				health = hitpoints.getLevel();

			hitpoints.setCurrent(health);

			message += ", it heals some health";
		}

		player.sendMessage(message + ".");
	}
}
