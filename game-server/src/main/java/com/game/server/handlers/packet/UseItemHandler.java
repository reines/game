package com.game.server.handlers.packet;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.game.common.codec.Packet;
import com.game.common.model.Item;
import com.game.common.util.PersistenceManager;
import com.game.server.Server;
import com.game.server.WorldManager;
import com.game.server.handlers.ItemHandler;
import com.game.server.handlers.PacketHandler;
import com.game.server.handlers.item.EdibleHandler;
import com.game.server.handlers.item.EquipableHandler;
import com.game.server.model.Player;

public class UseItemHandler implements PacketHandler {
	private static final Logger log = LoggerFactory.getLogger(UseItemHandler.class);

	protected final Map<Integer, ItemHandler> itemHandlers;
	protected final ItemHandler equipableHandler;
	protected final ItemHandler edibleHandler;

	public UseItemHandler() {
		itemHandlers = this.loadItemHandlers();
		equipableHandler = new EquipableHandler();
		edibleHandler = new EdibleHandler();
	}

	private Map<Integer, ItemHandler> loadItemHandlers() {
		Map<Integer, ItemHandler> handlers = new HashMap<Integer, ItemHandler>();
		URL path = ItemHandler.class.getResource("itemhandlers.xml");
		if (path == null) {
			// fatal error
			throw new RuntimeException("Unable to find itemhandlers.xml resource");
		}

		PersistenceManager.ItemHandler[] definitions = (PersistenceManager.ItemHandler[]) PersistenceManager.load(path);
		for (PersistenceManager.ItemHandler definition : definitions) {
			try {
				ItemHandler handler = (ItemHandler) definition.handler.newInstance();
				for (int id : definition.ids)
					handlers.put(id, handler);
			}
			catch (Exception e) {
				// fatal error
				throw new RuntimeException("Error loading item handlers: " + e.getMessage());
			}
		}

		if (log.isDebugEnabled())
			log.debug("Loaded " + handlers.size() + " item handlers");

		return handlers;
	}

	@Override
	public void handlePacket(Server server, WorldManager world, Player player, Packet packet) throws Exception {
		int index = packet.getByte();
		Item item = player.getInventory().get(index);
		if (item == null) {
			log.warn("Player attempted to use null item: " + player);

			return;
		}

		ItemHandler handler = itemHandlers.get(item.getID());

		// Handle the action
		if (handler == null) {
			// If it's edible, eat it
			if (item.isEdible())
				handler = edibleHandler;
			// If it's equipable, (un)equip it
			else if (item.isEquipable())
				handler = equipableHandler;
		}

		// There wasn't any handler, so just describe the item
		if (handler == null) {
			player.sendMessage(item.getDescription());
			return;
		}

		// Handle the item
		handler.handleItem(server, world, player, item, index);
	}
}
