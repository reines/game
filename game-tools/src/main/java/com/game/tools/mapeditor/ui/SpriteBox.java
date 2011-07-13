package com.game.tools.mapeditor.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import com.game.tools.mapeditor.SpriteMap;

@SuppressWarnings("serial")
public class SpriteBox extends JComboBox {

	protected static class SpriteBoxRenderer extends JLabel implements ListCellRenderer {
		protected final SpriteBox box;

		public SpriteBoxRenderer(SpriteBox box) {
			this.box = box;

			super.setBackground(Color.BLACK);
			super.setOpaque(true);
			super.setHorizontalAlignment(SwingConstants.CENTER);
			super.setVerticalAlignment(SwingConstants.CENTER);
		}

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			super.setIcon((ImageIcon) value);
			super.setBorder(isSelected ? BorderFactory.createLineBorder(Color.RED) : null);

			return this;
		}
	}

	// Width needs to account for the scrollbar/dropdown arrow
	public SpriteBox(SpriteMap sprites, boolean addBlank) {
		super.setRenderer(new SpriteBoxRenderer(this));

		// Add a blank image at position 0 - 0 means no sprite
		if (addBlank) {
			BufferedImage blank = new BufferedImage(sprites.elementWidth, sprites.elementHeight, BufferedImage.TYPE_INT_ARGB);
			this.addItem(new ImageIcon(blank));
		}

		for (Image sprite : sprites)
			super.addItem(new ImageIcon(sprite));

		super.setPreferredSize(new Dimension(sprites.elementWidth + 2 + UIManager.getInt("ScrollBar.width"), sprites.elementHeight));
		super.setSelectedIndex(0);
	}
}
