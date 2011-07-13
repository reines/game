package com.game.tools.mapeditor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.game.graphics.renderer.Sprite;
import com.game.tools.mapeditor.ui.EditorFrame;
import com.game.tools.mapeditor.ui.FileChooser;
import com.game.tools.mapeditor.ui.PropertiesDialog;

public class MapEditor {
	private static final Logger log = LoggerFactory.getLogger(MapEditor.class);

	public static final void main(String[] args) throws FileNotFoundException, IOException {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e) {
			log.error("Error setting native look and feel.");
		}

		try {
			new MapEditor();
		}
		catch (RuntimeException e) {
			log.error(e.getMessage());
		}
	}

	protected final FileChooser chooser;
	protected final EditableMap map;
	protected SpriteMap tileSprites;
	protected SpriteMap overlaySprites;
	protected SpriteMap wallSprites;
	protected final EditorFrame frame;

	public MapEditor() {
		int result = JOptionPane.showOptionDialog(
			null,
			"Would you like to create a new map or load an existing map?",
			"Map Editor",
			JOptionPane.YES_NO_OPTION,
			JOptionPane.QUESTION_MESSAGE,
			null, // icon
			new String[]{"Create New", "Load Existing"},
			null
		);

		chooser = new FileChooser();

		// Create new
		if (result == 0) {
			PropertiesDialog chooser = new PropertiesDialog(null);
			PropertiesDialog.Properties properties = chooser.showPropertiesDialog(EditableMap.DEFAULT_SIZE, EditableMap.DEFAULT_SIZE);
			if (properties == null)
				throw new RuntimeException("Map creation cancelled.");

			map = new EditableMap(properties.width, properties.height, properties.sectorSize);
		}
		// Load existing
		else if (result == 1) {
			File mapFile = chooser.showOpenDialog("Load Map", null);
			if (mapFile == null)
				throw new RuntimeException("Map loading cancelled.");

			map = EditableMap.load(mapFile);
		}
		else {
			// fatal error
			throw new RuntimeException("An invalid option was chosen.");
		}

		try {
			// Load tile sprites
			URL tileResource = Sprite.class.getResource("sprites/tiles.png");
			if (tileResource == null) {
				// fatal error
				throw new RuntimeException("Unable to find tiles.png resource.");
			}

			tileSprites = new SpriteMap(tileResource, 64, 64);

			// Load overlay sprites
			URL overlayResource = Sprite.class.getResource("sprites/overlays.png");
			if (overlayResource == null) {
				// fatal error
				throw new RuntimeException("Unable to find overlays.png resource.");
			}

			overlaySprites = new SpriteMap(overlayResource, 64, 64);

			// Load wall sprites
			URL wallResource = Sprite.class.getResource("sprites/walls.png");
			if (wallResource == null) {
				// fatal error
				throw new RuntimeException("Unable to find walls.png resource.");
			}

			wallSprites = new SpriteMap(wallResource, 64, 192);
		}
		catch (IOException e) {
			// fatal error
			throw new RuntimeException("Error loading sprites: " + e);
		}

		frame = new EditorFrame(this);

		this.setDefaultStatus();
	}

	public EditorFrame getFrame() {
		return frame;
	}

	public EditableMap getMap() {
		return map;
	}

	public SpriteMap getTileSpriteMap() {
		return tileSprites;
	}

	public SpriteMap getOverlaySpriteMap() {
		return overlaySprites;
	}

	public SpriteMap getWallSpriteMap() {
		return wallSprites;
	}

	public void setChanged() {
		map.setChanged(true);
		this.setDefaultStatus();
	}

	protected void setDefaultStatus() {
		frame.setStatus("Map " + (map.isChanged() ? "modified" : "unmodified") + ". " + map + ".");
	}

	public Thread save() {
		final File target = chooser.showSaveDialog("Save Map", null);
		if (target == null)
			return null;

		frame.setStatus("Saving map...");
		// Save the map in a new thread since it takes a while
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					map.save(target);
					map.setChanged(false);
					frame.setStatus("Map saved to '" + target.getName() + "'");
				}
				catch (Exception e) {
					String message = "Error saving map file: " + e;

					frame.setStatus(message);
					log.error(message);
				}
			}
		});

		thread.start();
		return thread;
	}

	public void exit() {
		// The map has changed since the last save
		if (map.isChanged()) {
			int save = JOptionPane.showConfirmDialog(frame, "The current map has been modified. Do you want to save the map before closing it?");
			// If they canceled the close, don't close
			if (save == JOptionPane.CANCEL_OPTION)
				return;

			// If they chose to save the map first, save it
			if (save == JOptionPane.YES_OPTION) {
				try { this.save().join(); } catch (InterruptedException e) { }
			}
		}

		frame.dispose();
		System.exit(0);
	}
}
