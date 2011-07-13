package com.game.client.model;

import java.io.DataInputStream;
import java.io.IOException;

import com.game.common.model.Tile;

public class LocalTile extends Tile {
	public static final int WALL_HEIGHT = 3;

	public LocalTile(DataInputStream in) throws IOException {
		super (in);
	}
}
