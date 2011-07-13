package com.game.tools.mapeditor.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.game.tools.mapeditor.EditableTile;
import com.game.tools.mapeditor.MapEditor;

@SuppressWarnings("serial")
public class EditorFrame extends JFrame implements ActionListener, WindowListener, ItemListener {

	protected final MapEditor editor;

	protected final JMenuItem saveItem;
	protected final JMenuItem exitItem;

	protected final MapWindow mapWindow;
	protected final JLabel statusLabel;
	protected final SpriteBox tileSpriteList;
	protected final SpriteBox overlaySpriteList;
	protected final SpriteBox hWallSpriteList;
	protected final SpriteBox vWallSpriteList;

	protected final JLabel mapInfo;
	protected final JLabel currentTileInfo;
	protected final JLabel hoverTileInfo;

	protected final JCheckBox showGrid;

	public EditorFrame(MapEditor editor) {
		super ("MapEditor");

		super.setResizable(true);
		super.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		super.addWindowListener(this);

		this.editor = editor;

		JMenuBar menuBar = new JMenuBar();

		// Add the file menu
		JMenu fileMenu = new JMenu("File");

		saveItem = new JMenuItem("Save As");
		saveItem.addActionListener(this);
		fileMenu.add(saveItem);

		exitItem = new JMenuItem("Exit");
		exitItem.addActionListener(this);
		fileMenu.add(exitItem);

		menuBar.add(fileMenu);

		super.setJMenuBar(menuBar);

		Container container = super.getContentPane();
		container.setLayout(new BorderLayout());

		JPanel sidePanel = new JPanel();
		sidePanel.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
		sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.PAGE_AXIS));

		JPanel infoPanel = new JPanel();
		infoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.PAGE_AXIS));

		mapInfo = new JLabel(editor.getMap().toString());
		infoPanel.add(mapInfo);

		currentTileInfo = new JLabel("");
		infoPanel.add(currentTileInfo);

		hoverTileInfo = new JLabel("");
		infoPanel.add(hoverTileInfo);

		sidePanel.add(infoPanel);

		JPanel optionsPanel = new JPanel();
		optionsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.PAGE_AXIS));

		showGrid = new JCheckBox("Show grid", false);
		showGrid.addItemListener(this);
		optionsPanel.add(showGrid);

		sidePanel.add(optionsPanel);

		JPanel spritePanel = new JPanel();
		spritePanel.setPreferredSize(new Dimension(200, 200));
		spritePanel.setAlignmentX(Component.CENTER_ALIGNMENT);

		tileSpriteList = new SpriteBox(editor.getTileSpriteMap(), false);
		tileSpriteList.addItemListener(this);
		spritePanel.add(tileSpriteList);

		overlaySpriteList = new SpriteBox(editor.getOverlaySpriteMap(), true);
		overlaySpriteList.addItemListener(this);
		spritePanel.add(overlaySpriteList);

		hWallSpriteList = new SpriteBox(editor.getWallSpriteMap(), true);
		hWallSpriteList.addItemListener(this);
		spritePanel.add(hWallSpriteList);

		vWallSpriteList = new SpriteBox(editor.getWallSpriteMap(), true);
		vWallSpriteList.addItemListener(this);
		spritePanel.add(vWallSpriteList);

		sidePanel.add(spritePanel);

		container.add(sidePanel, BorderLayout.LINE_START);

		mapWindow = new MapWindow(editor, this);
		container.add(mapWindow, BorderLayout.CENTER);

		statusLabel = new JLabel();
		statusLabel.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
		statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
		container.add(statusLabel, BorderLayout.PAGE_END);

		super.pack();

		super.setVisible(true);
		super.setExtendedState(JFrame.MAXIMIZED_BOTH);
		mapWindow.init();
	}

	public byte getCurrentTileSprite() {
		return (byte) tileSpriteList.getSelectedIndex();
	}

	public byte getCurrentOverlaySprite() {
		return (byte) overlaySpriteList.getSelectedIndex();
	}

	public byte getCurrentHWallSprite() {
		return (byte) hWallSpriteList.getSelectedIndex();
	}

	public byte getCurrentVWallSprite() {
		return (byte) vWallSpriteList.getSelectedIndex();
	}

	public void setCurrentTile(EditableTile tile) {
		tileSpriteList.setSelectedIndex(tile.getTexture());
		overlaySpriteList.setSelectedIndex(tile.getOverlay() + 1);
		hWallSpriteList.setSelectedIndex(tile.getHWall() + 1);
		vWallSpriteList.setSelectedIndex(tile.getVWall() + 1);
	}

	@Override
	public void dispose() {
		mapWindow.dispose();
		super.dispose();
	}

	public void setCurrentInfo(String s) {
		currentTileInfo.setText("Selected tile: " + s);
	}

	public void setHoverInfo(String s) {
		hoverTileInfo.setText("Hover tile: " + s);
	}

	public void setStatus(String status) {
		statusLabel.setText(status);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == saveItem)
			editor.save();
		else if (e.getSource() == exitItem)
			editor.exit();
	}

	@Override
	public void windowOpened(WindowEvent e) { }

	@Override
	public void windowClosing(WindowEvent e) {
		editor.exit();
	}

	@Override
	public void windowClosed(WindowEvent e) { }

	@Override
	public void windowIconified(WindowEvent e) { }

	@Override
	public void windowDeiconified(WindowEvent e) { }

	@Override
	public void windowActivated(WindowEvent e) { }

	@Override
	public void windowDeactivated(WindowEvent e) { }

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getSource() == showGrid)
			mapWindow.setGrid(showGrid.isSelected());
		else if (e.getSource() == tileSpriteList)
			mapWindow.setCurrentTile(EditableTile.Fields.TEXTURE, tileSpriteList.getSelectedIndex());
		else if (e.getSource() == overlaySpriteList)
			mapWindow.setCurrentTile(EditableTile.Fields.OVERLAY, overlaySpriteList.getSelectedIndex());
		else if (e.getSource() == hWallSpriteList)
			mapWindow.setCurrentTile(EditableTile.Fields.HWALL, hWallSpriteList.getSelectedIndex());
		else if (e.getSource() == vWallSpriteList)
			mapWindow.setCurrentTile(EditableTile.Fields.VWALL, vWallSpriteList.getSelectedIndex());
	}
}
