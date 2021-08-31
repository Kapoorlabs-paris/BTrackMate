package fiji.plugin.globalbtrack.gui.components;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public class LoadLeftRightWingDir {

	final File[] leftwings;
	final File[] rightwings;
	final String bordertitle;

	public LoadLeftRightWingDir(final String bordertitle, final File[] leftwings, final File[] rightwings) {

		this.bordertitle = bordertitle;

		this.leftwings = leftwings;

		this.rightwings = rightwings;

	}

	public JPanel DirectoryLoader = new JPanel();
	public JButton LoadLeftWingDirectory;
	public JButton LoadRightWingDirectory;

	public JPanel LoadDirectory() {

		layoutManager.Setlayout.LayoutSetter(DirectoryLoader);

		LoadLeftWingDirectory = new JButton("Load Left Wing Directory");
		LoadRightWingDirectory = new JButton("Load Right Wing Directory");

		Border directorychooser = new CompoundBorder(new TitledBorder(bordertitle),
				new EmptyBorder(layoutManager.Setlayout.c.insets));

		DirectoryLoader.add(LoadLeftWingDirectory, new GridBagConstraints(0, 0, 3, 1, 0, 0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));

		DirectoryLoader.add(LoadRightWingDirectory, new GridBagConstraints(3, 0, 3, 1, 0, 0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));

		DirectoryLoader.setBorder(directorychooser);

		return DirectoryLoader;

	}

}
