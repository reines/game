package com.game.client.ui.menu;

import com.game.client.WorldManager;
import com.game.client.ui.Menu;
import com.game.graphics.renderer.Graphics2D;

public class Magic extends Menu {

	protected final WorldManager world;

	public Magic(WorldManager world) {
		super("Magic", 140, 200);

		this.world = world;
	}

	@Override
	public void onUpdate(long now) { }

	@Override
	public void onMouseClicked(int x, int y, boolean left) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onDisplay(Graphics2D g, int x, int y) {
		// TODO: display stuff
	}
}
