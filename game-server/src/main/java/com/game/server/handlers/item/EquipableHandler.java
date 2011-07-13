package com.game.server.handlers.item;

import com.game.common.model.Inventory;
import com.game.common.model.Item;
import com.game.server.Server;
import com.game.server.WorldManager;
import com.game.server.handlers.ItemHandler;
import com.game.server.model.Player;

public class EquipableHandler implements ItemHandler {

	@Override
	public void handleItem(Server server, WorldManager world, Player player, Item item, int index) {
		boolean equiped = !item.isEquiped();
		// If we are equiping this item, un-equip anything else that uses the same equipable position
		if (equiped)
			this.removeEquipedItem(player, item.getEquipable());

		item.setEquiped(equiped);
		player.sendInventoryUpdate(index, item);
	}

	private void removeEquipedItem(Player player, Item.Definition.Equipable position) {
		Inventory inventory = player.getInventory();

		// Un-equip anything else that is equiped in this position
		for (int index = 0;index < inventory.size();index++) {
			Item item = inventory.get(index);
			if (item.isEquiped() && item.getEquipable() == position) {
				item.setEquiped(false);
				player.sendInventoryUpdate(index, item);
				break;
			}
		}
	}
}
