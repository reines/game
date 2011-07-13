package com.game.tools.mapeditor.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.game.tools.mapeditor.EditableMap;

@SuppressWarnings("serial")
public class PropertiesDialog extends JFrame implements ActionListener, WindowListener {

	public static class Properties {
		public final int width;
		public final int height;
		public final int sectorSize;

		public Properties(int width, int height, int sectorSize) {
			this.width = width;
			this.height = height;
			this.sectorSize = sectorSize;
		}
	}

	protected boolean completed;
	protected Properties properties;

	protected final JButton submit;
	protected final JButton cancel;

	protected final JTextField widthField;
	protected final JTextField heightField;

	public PropertiesDialog(Component parent) {
		super ("Map properties");

		super.setResizable(false);
		super.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		super.addWindowListener(this);

		Container container = super.getContentPane();
		container.setLayout(new BoxLayout(container, BoxLayout.PAGE_AXIS));

		JPanel infoPanel = new JPanel();

		JLabel sectorLabel = new JLabel("Sector size is " + EditableMap.DEFAULT_SECTOR_SIZE + " tiles.");
		infoPanel.add(sectorLabel);

		container.add(infoPanel);

		JPanel fieldPanel = new JPanel();

		JPanel widthPanel = new JPanel();
		JLabel widthLabel = new JLabel("Width (in sectors):");
		widthPanel.add(widthLabel);
		widthField = new JTextField(3);
		widthPanel.add(widthField);
		fieldPanel.add(widthPanel);

		JPanel heightPanel = new JPanel();
		JLabel heightLabel = new JLabel("Height (in sectors):");
		heightPanel.add(heightLabel);
		heightField = new JTextField(3);
		heightPanel.add(heightField);
		fieldPanel.add(heightPanel);

		container.add(fieldPanel);

		JPanel buttonPanel = new JPanel();

		submit = new JButton("Submit");
		submit.addActionListener(this);
		buttonPanel.add(submit);

		cancel = new JButton("Cancel");
		cancel.addActionListener(this);
		buttonPanel.add(cancel);

		container.add(buttonPanel);

		super.pack();
		super.setLocationRelativeTo(parent);
	}

	public synchronized Properties showPropertiesDialog(int defaultWidth, int defaultHeight) {
		completed = false;
		properties = null;

		// Set the default values
		widthField.setText(String.valueOf(defaultWidth));
		heightField.setText(String.valueOf(defaultHeight));

		super.setVisible(true);

		// Wait until the dialog is closed
		while (!completed) {
			try { this.wait(); } catch (InterruptedException e) { }
		}

		super.setVisible(false);

		return properties;
	}

	@Override
	public synchronized void actionPerformed(ActionEvent e) {
		if (e.getSource() == submit) {
			try {
				int width = Integer.parseInt(widthField.getText());
				int height = Integer.parseInt(heightField.getText());
				int sectorSize = EditableMap.DEFAULT_SECTOR_SIZE;

				// If the map is stupidly small (i.e. 0) or stupidly big, show an error
				if (width < 1 || width > EditableMap.MAX_SIZE || height < 1 || height > EditableMap.MAX_SIZE)
					JOptionPane.showMessageDialog(this, "The map dimensions " + (width * sectorSize) + "x" + (height * sectorSize) + " are not sensible!");
				else {
					properties = new Properties(width, height, sectorSize);
					completed = true;
				}
			}
			catch (NumberFormatException nfe) {
				// An Invalid width or height was entered
				JOptionPane.showMessageDialog(this, "An invalid width or height was entered.");
			}
		}
		else if (e.getSource() == cancel) {
			completed = true;
		}

		// Notify the main thread that something happened
		this.notify();
	}

	@Override
	public void windowOpened(WindowEvent e) { }

	@Override
	public synchronized void windowClosing(WindowEvent e) {
		// Notify the main thread that something happened
		completed = true;
		this.notify();
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
}
