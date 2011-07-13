package com.game.client.model;

import com.game.common.model.Entity;
import com.game.common.model.Hash;
import com.game.common.model.Point;
import com.game.graphics.models.Model;

public class Player extends Entity {

	protected final Hash id;
	protected final String username;
	protected final Point location;
	protected final Model model;

	public Player(Hash id, String name, Point location, Model model) {
		this.id = id;
		this.username = name;
		this.location = location;
		this.model = model;
	}

	@Override
	public Hash getID() {
		return id;
	}

	public String getUsername() {
		return username;
	}

	@Override
	public Point getLocation() {
		return location;
	}

	public Model getModel() {
		return model;
	}

	@Override
	public void setLocation(Point location) {
		if (location.equals(this.location))
			return;

		// moving right: 45�, 90�, or 135�
		if (location.x > this.location.x) {
			// moving up: 45�
			if (location.y > this.location.y)
				model.setRotation(45);
			// moving down: 135�
			else if (location.y < this.location.y)
				model.setRotation(135);
			// else: 90�
			else
				model.setRotation(90);
		}
		// moving left: 225�, 270�, or 315�
		else if (location.x < this.location.x){
			// moving up: 315�
			if (location.y > this.location.y)
				model.setRotation(315);
			// moving down: 225�
			else if (location.y < this.location.y)
				model.setRotation(225);
			// else: 270�
			else
				model.setRotation(270);
		}
		// else: 0�, or 180�
		else {
			// moving up: 0�
			if (location.y > this.location.y)
				model.setRotation(0);
			// else: 180�
			else
				model.setRotation(180);
		}

		this.location.set(location);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Player))
			return false;

		Player p = (Player) o;
		return id.equals(p.id);
	}

	@Override
	public String toString() {
		return "player[id = " + id + ", username = '" + username + "', location = " + location + "]";
	}
}
