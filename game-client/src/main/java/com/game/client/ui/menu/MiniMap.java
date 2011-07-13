package com.game.client.ui.menu;

import java.awt.Color;

import com.game.client.WorldManager;
import com.game.client.engine.Camera;
import com.game.client.model.LocalMap;
import com.game.client.model.LocalTile;
import com.game.client.model.Player;
import com.game.client.ui.Menu;
import com.game.common.model.Path;
import com.game.common.model.Point;
import com.game.graphics.renderer.EditableSprite;
import com.game.graphics.renderer.Graphics;
import com.game.graphics.renderer.Graphics2D;

// TODO:	- innerImage size should be a power of 2
//			- clicking seems a bit off
//			- camera rotation
public class MiniMap extends Menu {
	public static final int MAP_SIZE = 128;

	public static final int TRIPLE_MAP_SIZE;
	public static final int HALF_MAP_SIZE;
	public static final int TRIPLE_HALF_MAP_SIZE;

	static {
		TRIPLE_MAP_SIZE = MAP_SIZE * 3;
		HALF_MAP_SIZE = MAP_SIZE / 2;
		TRIPLE_HALF_MAP_SIZE = HALF_MAP_SIZE * 3;
	}

	protected final WorldManager world;
	protected final Camera camera;
	protected final LocalMap map;
	protected final EditableSprite mapSprite;

//	protected final BufferedImage outerImage;
//	protected final Graphics2D outerImageGraphics;

	protected Point location;
	protected int rotation;

	public MiniMap(WorldManager world, Graphics graphics) {
		super("World Map", TRIPLE_HALF_MAP_SIZE, TRIPLE_HALF_MAP_SIZE);
		super.setBackgroundColor(null);

		this.world = world;

		camera = world.getCamera();
		map = world.getMap();

		mapSprite = new EditableSprite(TRIPLE_MAP_SIZE, TRIPLE_MAP_SIZE, false, graphics);

//		outerImage = new BufferedImage(TRIPLE_HALF_MAP_SIZE, TRIPLE_HALF_MAP_SIZE, BufferedImage.TYPE_INT_ARGB);
//		outerImageGraphics = outerImage.createGraphics();
//		outerImageGraphics.setBackground(null);

		location = null;
		rotation = 0;
	}

	@Override
	public void onUpdate(long now) {
		location = world.getLocation();
		rotation = camera.getRotation();

		Point start = new Point(location.x - HALF_MAP_SIZE, location.y - HALF_MAP_SIZE);

		// draw all the map tiles
		for (int y = 0;y < MAP_SIZE;y++) {
			for (int x = 0;x < MAP_SIZE;x++) {
				LocalTile t = map.getTile(start.x + x, start.y + y);
				Color c = (t == null) ? Color.BLACK : Color.GREEN; // TODO: Tile color

				mapSprite.fillRect(x * 3, y * 3, 3, 3, c);
			}
		}

		// draw other players on the map
		for (Player player : world.getPlayers()) {
			int x = player.getLocation().x - start.x;
			int y = player.getLocation().y - start.y;

			// If the player is outside the map, skip them
			if (x < 0 || x >= MAP_SIZE || y < 0 || y >= MAP_SIZE)
				continue;

			mapSprite.fillDiamond(x * 3, y * 3, 3, world.getFriends().contains(player.getID()) ? Color.GREEN : Color.WHITE);
		}

		// draw ourself on the map
		mapSprite.fillDiamond(TRIPLE_HALF_MAP_SIZE, TRIPLE_HALF_MAP_SIZE, 5, Color.WHITE);

		// Update the texture
		mapSprite.flush();

//		AffineTransform transform = new AffineTransform();
//		transform.rotate(Math.toRadians(rotation), outerImage.getWidth() / 2, outerImage.getHeight() / 2);
//		transform.translate(-(outerImage.getWidth() / 2), -(outerImage.getHeight() / 2));

//		outerImageGraphics.clearRect(0, 0, outerImage.getWidth(), outerImage.getHeight());
//		outerImageGraphics.drawImage(innerImage.getImage(), transform, null);
	}

	@Override
	public void onMouseClicked(int x, int y, boolean left) {
		// Right clicks do nothing
		if (!left || location == null)
			return;

		Point target = this.calcClickTarget(x, y);
		// If we clicked on ourselves, do nothing
		if (target.equals(Point.ZERO))
			return;

		target.x += location.x;
		target.y += location.y;

		Path path = world.getMap().generatePath(location, target);
		// There is no path to this location
		if (path == null)
			return;

		// Send a walk request
		world.sendWalkTo(path);
	}

	private Point calcClickTarget(int x, int y) {
		x = ((x / 3) * 2) - HALF_MAP_SIZE;
		y = ((y / 3) * 2) - HALF_MAP_SIZE;

		switch (rotation) {
		// 0<b0>, no translation
		case 0: {
			return new Point(x / 2, y / 2);
		}

		// 90<b0>, swap x and y, flip y
		case 90: {
			return new Point(y / 2, -x / 2);
		}

		// 180<b0>, flip x and y
		case 180: {
			return new Point(-x / 2, -y / 2);
		}

		// 270<b0>, swap x and y, flip x
		case 270: {
			return new Point(-y / 2, x / 2);
		}

		// Not perfectly aligned - we need to calculate the target using trigonometry
		default: {
			// Calculate the x, y offset of where was clicked - this is our adjacent and opposite sides
			double adj = x;
			double opp = y;

			double angle;
			double hyp;

			// If the opposite is 0
			if (y == 0) {
				angle = 0;
				hyp = adj;
			}
			// Calculate the angle, and then hypotenuse
			else {
				angle = Math.atan(opp / adj);
				hyp = opp / Math.sin(angle);
			}

			// Add the rotation on to our angle
			angle += (2 * Math.PI) - Math.toRadians(rotation);

			// Calculate the new adjacent and opposite, these are our translated x and y offsets
			adj = hyp * Math.cos(angle);
			opp = hyp * Math.sin(angle);

			return new Point((int) Math.round(adj / 2), (int) Math.round(opp / 2));
		}
		}
	}

	@Override
	public void onDisplay(Graphics2D g, int x, int y) {
		g.drawSprite(mapSprite, x, y, TRIPLE_HALF_MAP_SIZE, TRIPLE_HALF_MAP_SIZE);
	}
}
