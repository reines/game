package com.game.client;

import com.game.client.engine.GameEngine;
import com.game.client.model.Player;
import com.game.client.ui.ClientFrame;
import com.game.common.codec.Packet;
import com.game.common.codec.PacketBuilder;
import com.game.common.model.Friend;
import com.game.common.model.Hash;
import com.game.common.model.Inventory;
import com.game.common.model.Path;
import com.game.common.model.PlayerProfile;
import com.game.common.util.FriendList;
import com.game.common.util.StatList;
import com.game.graphics.models.Model;
import com.game.graphics.models.ms3d.MilkShapeModel;

public class WorldManager extends GameEngine {

	protected final Connection connection;

	protected PlayerProfile profile;

	public WorldManager(Client client) {
		super (client);

		connection = client.getConnection();
	}

	public Hash getID() {
		return profile.id;
	}

	public String getUsername() {
		return profile.username;
	}

	public FriendList getFriends() {
		return profile.friends;
	}

	public StatList getStats() {
		return profile.stats;
	}

	public Inventory getInventory() {
		return profile.inventory;
	}

	public void init(PlayerProfile profile) {
		this.profile = profile;

		client.setUpdateRate(ClientFrame.UPDATE_RATE_MAX);

		Model model = client.getGraphics().loadModel("test.ms3d", MilkShapeModel.class);
		model.setScale(0.03f);

		super.init(new Player(profile.id, profile.username, profile.location, model));
	}

	public void sendFriendAdd(String username) {
		PacketBuilder packet = new PacketBuilder(Packet.Type.FRIEND_ADD_SEND);

		packet.putString(username);

		connection.write(packet);
	}

	public void sendFriendRemove(Friend friend) {
		PacketBuilder packet = new PacketBuilder(Packet.Type.FRIEND_REMOVE_SEND);

		packet.putHash(friend.getID());

		connection.write(packet);
	}

	public void sendFriendMessage(Friend friend, String message) {
		PacketBuilder packet = new PacketBuilder(Packet.Type.FRIEND_MESSAGE_SEND);

		packet.putHash(friend.getID());
		packet.putString(message);

		connection.write(packet);
	}

	public void sendUseItem(int index) {
		PacketBuilder packet = new PacketBuilder(Packet.Type.USE_ITEM_SEND);

		packet.putByte((byte) index);

		connection.write(packet);
	}

	public void sendChat(String message) {
		PacketBuilder packet = new PacketBuilder(Packet.Type.CHAT_SEND);

		packet.putString(message);

		connection.write(packet);
	}

	public void sendWalkTo(Path path) {
		PacketBuilder packet = new PacketBuilder(Packet.Type.WALK_TO_SEND);

		packet.putPath(path);

		connection.write(packet);
	}
}
