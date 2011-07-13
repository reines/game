package com.game.common.model;

import java.util.Observable;

public abstract class Entity extends Observable {

	public abstract Hash getID();
	public abstract Point getLocation();
	public abstract void setLocation(Point p);

}
