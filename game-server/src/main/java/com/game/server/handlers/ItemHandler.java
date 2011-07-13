package com.game.server.handlers;

import com.game.common.model.Item;
import com.game.server.Server;
import com.game.server.WorldManager;
import com.game.server.model.Player;

public interface ItemHandler {

	public void handleItem(Server server, WorldManager world, Player player, Item item, int index);

}
