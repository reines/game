package com.game.client.handlers.packet;

import com.game.client.Client;
import com.game.client.WorldManager;
import com.game.client.handlers.PacketHandler;
import com.game.common.codec.Packet;
import com.game.common.model.Inventory;
import com.game.common.model.Item;

public class InventoryHandler implements PacketHandler {

	@Override
	public void handlePacket(Client client, WorldManager world, Packet packet) throws Exception {
		Inventory inventory = world.getInventory();

		switch (packet.getType()) {
		// Add an item
		case INVENTORY_ADD_RESPONSE: {
			inventory.add(packet.getItem());

			break;
		}

		// Remove an item
		case INVENTORY_REMOVE_RESPONSE: {
			inventory.remove(packet.getByte());

			break;
		}

		// Update an item
		case INVENTORY_UPDATE_RESPONSE: {
			Item item = inventory.get(packet.getByte());
			if (item == null)
				return;

			item.setAmount(packet.getLong());
			item.setEquiped(packet.getBoolean());

			break;
		}
		}
	}
}
