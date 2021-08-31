package fiji.plugin.globalbtrack.gui.components;

import java.awt.Checkbox;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public class LoadSingleImage {

	final String bordertitle;
	final String[] blankimageNames;
	final GridBagConstraints gbc;

	public LoadSingleImage(final String bordertitle, final String[] blankimageNames, final GridBagConstraints gbc) {

		this.bordertitle = bordertitle;
		this.blankimageNames = blankimageNames;
		this.gbc = gbc;
	}

	public JPanel panelSingleChannel = new JPanel();

	public JComboBox<String> ChooseImage;

	public JPanel SingleChannelOption() {

		layoutManager.Setlayout.LayoutSetter(panelSingleChannel);
		ChooseImage = new JComboBox<String>(blankimageNames);
		Border chooseoriginalfile = new CompoundBorder(new TitledBorder(bordertitle),
				new EmptyBorder(layoutManager.Setlayout.c.insets));

		panelSingleChannel.add(ChooseImage, gbc);

		panelSingleChannel.setBorder(chooseoriginalfile);

		return panelSingleChannel;

	}

}
