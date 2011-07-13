package com.game.tools.mapeditor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.game.common.model.Tile;

public class EditableTile extends Tile {

	public enum Fields { ELEVATION, TEXTURE, OVERLAY, HWALL, VWALL }

	protected EditableMap map;
	protected int x;
	protected int y;

	public EditableTile() {
		super();
	}

	public EditableTile(DataInputStream in) throws IOException {
		super (in);
	}

	protected void setParent(EditableMap map, int x, int y) {
		this.map = map;
		this.x = x;
		this.y = y;
	}

	protected void tileChanged(Fields field) {
		if (map == null)
			return;

		map.tileChanged(this, field);
	}

	public boolean setField(Fields field, byte value) {
		switch (field) {
		case ELEVATION: {
			return this.setElevation(value);
		}
		case TEXTURE: {
			return this.setTexture(value);
		}
		case OVERLAY: {
			return this.setOverlay(value);
		}
		case HWALL: {
			return this.setHWall(value);
		}
		case VWALL: {
			return this.setVWall(value);
		}
		default: {
			return false;
		}
		}
	}

	public boolean setElevation(byte elevation) {
		if (super.elevation == elevation)
			return false;

		super.elevation = elevation;
		this.tileChanged(Fields.ELEVATION);
		return true;
	}

	public boolean setTexture(byte texture) {
		if (super.texture == texture)
			return false;

		super.texture = texture;

		super.definition = Tile.definitions.get(texture);
		if (super.definition == null)
			super.definition = Definition.DEFAULT_DEFINITION;

		this.tileChanged(Fields.TEXTURE);
		return true;
	}

	public boolean setOverlay(byte overlay) {
		if (super.overlay == overlay)
			return false;

		super.overlay = overlay;
		this.tileChanged(Fields.OVERLAY);
		return true;
	}

	public boolean setHWall(byte hWall) {
		if (super.hWall == hWall)
			return false;

		super.hWall = hWall;
		this.tileChanged(Fields.HWALL);
		return true;
	}

	public boolean setVWall(byte vWall) {
		if (super.vWall == vWall)
			return false;

		super.vWall = vWall;
		this.tileChanged(Fields.VWALL);
		return true;
	}

	public void save(DataOutputStream out) throws IOException {
		out.write(super.elevation);
		out.write(super.texture);
		out.write(super.overlay);

		out.write(super.hWall);
		out.write(super.vWall);
	}
}
