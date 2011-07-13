package com.game.tools.mapeditor.ui;

import java.awt.Component;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class FileChooser {

	protected final JFileChooser chooser;

	public FileChooser() {
		chooser = new JFileChooser(System.getProperty("user.dir"));
		chooser.setMultiSelectionEnabled(false);
	}

	public synchronized File showSaveDialog(String title, Component parent) {
		chooser.setDialogTitle(title);

		File target = null;
		while (true) {
			if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION)
				return null;

			target = chooser.getSelectedFile();

			// The target file doesn't exist, so all is good
			if (!target.exists())
				break;

			int overwrite = JOptionPane.showConfirmDialog(parent, "File " + target.getAbsolutePath() + " already exists. Do you wish to overwrite it?");

			// If they want to overwrite, lets do that
			if (overwrite == JOptionPane.YES_OPTION)
				break;

			// They cancelled saving
			if (overwrite == JOptionPane.CANCEL_OPTION)
				return null;
		}

		return target;
	}

	public synchronized File showOpenDialog(String title, Component parent) {
		chooser.setDialogTitle(title);

		if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION)
			return chooser.getSelectedFile();

		return null;
	}
}
