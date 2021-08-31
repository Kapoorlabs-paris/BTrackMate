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

public class LoadDualImage {

	final String bordertitle;
	final String[] blankimageNames;
	final GridBagConstraints gbcSeg;
	final GridBagConstraints gbcMask;

	public LoadDualImage(final String bordertitle, final String[] blankimageNames, final GridBagConstraints gbcSeg,
			final GridBagConstraints gbcMask) {

		this.bordertitle = bordertitle;
		this.blankimageNames = blankimageNames;
		this.gbcSeg = gbcSeg;
		this.gbcMask = gbcMask;
	}

	public JPanel panelTwoChannel = new JPanel();

	public JComboBox<String> ChooseImage;
	public JComboBox<String> ChoosesecImage;

	public JPanel TwoChannelOption() {

		layoutManager.Setlayout.LayoutSetter(panelTwoChannel);
		ChooseImage = new JComboBox<String>(blankimageNames);
		ChoosesecImage = new JComboBox<String>(blankimageNames);
		Border chooseoriginalfile = new CompoundBorder(new TitledBorder(bordertitle),
				new EmptyBorder(layoutManager.Setlayout.c.insets));

		panelTwoChannel.add(ChooseImage, gbcSeg);
		panelTwoChannel.add(ChoosesecImage, gbcMask);
		panelTwoChannel.setBorder(chooseoriginalfile);

		return panelTwoChannel;

	}

}
