package com.game.tools.mapeditor.ui;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.JScrollBar;

import com.game.common.model.Point;
import com.game.tools.mapeditor.EditableMap;
import com.game.tools.mapeditor.EditableTile;
import com.game.tools.mapeditor.MapEditor;
import com.game.tools.mapeditor.SpriteMap;

@SuppressWarnings("serial")
public class MapWindow extends JPanel implements ComponentListener, MouseListener, MouseMotionListener, MouseWheelListener, AdjustmentListener, Runnable {
	public static final int OUTSIDE_WINDOW = -1;
	public static final int TILE_SIZE = 32;

	public static final Color GRID_COLOR = new Color(1, 1, 1, 0.3f);
	public static final Color HOVER_COLOR = new Color(1, 1, 0, 0.5f);
	public static final Color SELECTED_COLOR = new Color(1, 0, 0, 0.5f);
	public static final Color SELECTED_WALKABLE_COLOR = new Color(0, 1, 0, 0.5f);

	protected final JScrollBar hScroll;
	protected final JScrollBar vScroll;
	protected final Canvas canvas;
	protected final MapEditor editor;
	protected final EditorFrame frame;
	protected final EditableMap map;
	protected final int yOffset;
	protected final Point mouse;
	protected final Dimension mouseSize;
	protected final Point offset;
	protected final SpriteMap tileSprites;
	protected final SpriteMap overlaySprites;
	protected final Point selectedTile;
	protected BufferedImage background;
	protected Thread thread;
	protected boolean running;
	protected boolean drawGrid;
	protected int width;
	protected int height;

	public MapWindow(MapEditor editor, EditorFrame frame) {
		this.editor = editor;
		this.frame = frame;

		map = editor.getMap();
		tileSprites = editor.getTileSpriteMap();
		overlaySprites = editor.getOverlaySpriteMap();

		super.setLayout(new BorderLayout());

		hScroll = new JScrollBar(JScrollBar.HORIZONTAL, 0, 40, 0, map.getWidth() + 1);
		hScroll.addAdjustmentListener(this);
		super.add(hScroll, BorderLayout.PAGE_END);

		vScroll = new JScrollBar(JScrollBar.VERTICAL, 0, 20, 0, map.getHeight() + 1);
		vScroll.addAdjustmentListener(this);
		super.add(vScroll, BorderLayout.LINE_END);

		canvas = new Canvas();
		canvas.setBackground(Color.BLACK);

		canvas.addComponentListener(this);
		canvas.addMouseListener(this);
		canvas.addMouseMotionListener(this);
		canvas.addMouseWheelListener(this);

		super.add(canvas, BorderLayout.CENTER);

		super.setPreferredSize(new Dimension(TILE_SIZE * 40, TILE_SIZE * 20));

		yOffset = map.getHeight() - 1;
		mouse = new Point(OUTSIDE_WINDOW, OUTSIDE_WINDOW);
		mouseSize = new Dimension(1, 1);
		offset = new Point(0, 0);
		drawGrid = false;
		selectedTile = new Point(OUTSIDE_WINDOW, OUTSIDE_WINDOW);

		width = 0;
		height = 0;
		background = null;

		frame.setCurrentInfo("none");
		frame.setHoverInfo("none");
	}

	public void setGrid(boolean drawGrid) {
		this.drawGrid = drawGrid;
	}

	public void init() {
		if (thread != null)
			return;

		canvas.setIgnoreRepaint(true);
		canvas.createBufferStrategy(2);

		thread = new Thread(this);
		thread.start();
	}

	public void dispose() {
		running = false;

		try { thread.join(); } catch (InterruptedException e) { }
	}

	public void setCurrentTile(EditableTile.Fields field, int value) {
		if (selectedTile.x == OUTSIDE_WINDOW || selectedTile.y == OUTSIDE_WINDOW)
			return;

		EditableTile tile = map.getTile(selectedTile.x, yOffset - selectedTile.y);
		if (tile == null)
			return;

		if (tile.setField(field, (byte) value))
			this.setChanged();
	}

	protected void setChanged() {
		this.updateTiles();
		editor.setChanged();
	}

	protected void updateTiles() {
		if (super.getWidth() < 1 || super.getHeight() < 1) {
			background = null;
			return;
		}

		if (background == null || background.getWidth() != super.getWidth() || background.getHeight() != super.getHeight())
			background = new BufferedImage(super.getWidth(), super.getHeight(), BufferedImage.TYPE_INT_ARGB);

		Graphics2D g = background.createGraphics();
		g.clearRect(0, 0, background.getWidth(), background.getHeight());

		for (int y = 0;y < height;y++) {
			for (int x = 0;x < width;x++) {
				EditableTile tile = map.getTile(offset.x + x, yOffset - (offset.y + y));
				if (tile == null)
					continue;

				Image texture = tileSprites.get(tile.getTexture());
				if (texture != null)
					g.drawImage(texture, x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE, null);

				if (tile.hasOverlay()) {
					texture = overlaySprites.get(tile.getOverlay());
					if (texture != null)
						g.drawImage(texture, x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE, null);
				}

				if (tile.hasHWall()) {
					g.setColor(Color.BLACK);
					g.drawLine(x * TILE_SIZE, y * TILE_SIZE, (x + 1) * TILE_SIZE, y * TILE_SIZE);
				}

				if (tile.hasVWall()) {
					g.setColor(Color.BLACK);
					g.drawLine(x * TILE_SIZE, y * TILE_SIZE, x * TILE_SIZE, (y + 1) * TILE_SIZE);
				}
			}
		}

		g.dispose();
	}

	public void draw(Graphics2D g) {
		if (background != null)
			g.drawImage(background, 0, 0, null);

		if (drawGrid) {
			g.setColor(GRID_COLOR);

			for (int y = 0;y < height;y++)
				for (int x = 0;x < width;x++)
					g.drawRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
		}

		// If a tile is selected, highlight it
		if (selectedTile.x != OUTSIDE_WINDOW && selectedTile.y != OUTSIDE_WINDOW) {
			EditableTile tile = map.getTile(selectedTile.x, yOffset - selectedTile.y);
			if (tile != null) {
				g.setColor(tile.isWalkable() ? SELECTED_WALKABLE_COLOR : SELECTED_COLOR);
				g.drawRect((selectedTile.x - offset.x) * TILE_SIZE, (selectedTile.y - offset.y) * TILE_SIZE, TILE_SIZE, TILE_SIZE);
			}
		}

		// If the mouse is over the grid, highlight the current tile
		if (mouse.x != OUTSIDE_WINDOW && mouse.y != OUTSIDE_WINDOW) {
			g.setColor(HOVER_COLOR);

			int x = mouse.x - offset.x;
			int y = mouse.y - offset.y;
			int width = mouseSize.width;
			int height = mouseSize.height;
			if (width < 0) {
				x += width;
				width = (width - 1) * -1;
			}
			if (height < 0) {
				y += height;
				height = (height - 1) * -1;
			}

			g.drawRect(x * TILE_SIZE, y * TILE_SIZE, width * TILE_SIZE, height * TILE_SIZE);
		}
	}

	@Override
	public void run() {
		running = true;

		BufferStrategy strategy = canvas.getBufferStrategy();
		while (running) {
			if (!super.isVisible()) {
				try { Thread.sleep(100); } catch (InterruptedException e) { }
				continue;
			}

			Graphics2D g = (Graphics2D) strategy.getDrawGraphics();

			g.clearRect(0, 0, super.getWidth(), super.getHeight());
			this.draw(g);

			g.dispose();
			strategy.show();

			try { Thread.sleep(30); } catch (InterruptedException e) { }
		}
	}

	@Override
	public void componentResized(ComponentEvent e) {
		width = (super.getWidth() / TILE_SIZE) + 1;
		height = (super.getHeight() / TILE_SIZE) + 1;

		hScroll.setVisibleAmount(width);
		vScroll.setVisibleAmount(height);

		this.updateTiles();
	}

	@Override
	public void componentMoved(ComponentEvent e) { }

	@Override
	public void componentShown(ComponentEvent e) { }

	@Override
	public void componentHidden(ComponentEvent e) { }

	@Override
	public void mouseClicked(MouseEvent e) { }

	@Override
	public void mousePressed(MouseEvent e) {
		this.setMouse(e.getX() / TILE_SIZE, e.getY() / TILE_SIZE);
		mouseSize.width = mouseSize.height = 1;
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// Ignore events outside the window
		if (mouse.x == OUTSIDE_WINDOW || mouse.y == OUTSIDE_WINDOW)
			return;

		this.setMouse(e.getX() / TILE_SIZE, e.getY() / TILE_SIZE);

		// Handle the click
		switch (e.getButton()) {
		case MouseEvent.BUTTON1: {
			selectedTile.x = mouse.x;
			selectedTile.y = mouse.y;

			frame.setCurrentInfo("(" + selectedTile.x + ", " + (yOffset - selectedTile.y) + ")");

			EditableTile tile = map.getTile(selectedTile.x, yOffset - selectedTile.y);
			if (tile != null)
				frame.setCurrentTile(tile);

			break;
		}
		case MouseEvent.BUTTON3: {
			selectedTile.x = mouse.x;
			selectedTile.y = mouse.y;

			frame.setCurrentInfo("(" + selectedTile.x + ", " + (yOffset - selectedTile.y) + ")");

			int xIncrement = 1;
			int yIncrement = 1;
			int width = mouseSize.width;
			int height = mouseSize.height;

			if (width < 0) {
				xIncrement = -1;
				width = (width - 1) * -1;
			}
			if (height < 0) {
				yIncrement = -1;
				height = (height - 1) * -1;
			}

			for (int y = 0;y < height;y++) {
				for (int x = 0;x < width;x++) {
					EditableTile tile = map.getTile(selectedTile.x - (x * xIncrement), yOffset - (selectedTile.y - (y * yIncrement)));
					if (tile != null) {
						tile.setTexture(frame.getCurrentTileSprite());
						tile.setOverlay(frame.getCurrentOverlaySprite());
						tile.setHWall(frame.getCurrentHWallSprite());
						tile.setVWall(frame.getCurrentVWallSprite());
					}
				}
			}

			this.setChanged();

			break;
		}
		}

		mouseSize.width = mouseSize.height = 1;
	}

	protected void setMouse(int x, int y) {
		mouse.x = offset.x + x;
		mouse.y = offset.y + y;

		if (x == OUTSIDE_WINDOW || y == OUTSIDE_WINDOW)
			frame.setHoverInfo("none");
		else
			frame.setHoverInfo("(" + mouse.x + ", " + (yOffset - mouse.y) + ")");
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		this.setMouse(e.getX() / TILE_SIZE, e.getY() / TILE_SIZE);
		mouseSize.width = mouseSize.height = 1;
	}

	@Override
	public void mouseExited(MouseEvent e) {
		this.setMouse(OUTSIDE_WINDOW, OUTSIDE_WINDOW);
		mouseSize.width = mouseSize.height = 1;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		mouseSize.width = (e.getX() / TILE_SIZE) - (mouse.x - offset.x);
		mouseSize.height = (e.getY() / TILE_SIZE) - (mouse.y - offset.y);

		if (mouseSize.width >= 0)
			mouseSize.width++;

		if (mouseSize.height >= 0)
			mouseSize.height++;
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		this.setMouse(e.getX() / TILE_SIZE, e.getY() / TILE_SIZE);
		mouseSize.width = mouseSize.height = 1;
	}

	@Override
	public void adjustmentValueChanged(AdjustmentEvent e) {
		if (e.getSource() == hScroll)
			offset.x = e.getValue();
		else if (e.getSource() == vScroll)
			offset.y = e.getValue();

		this.updateTiles();
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		int units = e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL ? vScroll.getUnitIncrement() : vScroll.getBlockIncrement();
		vScroll.setValue(vScroll.getValue() + (units * e.getWheelRotation()));
	}
}
