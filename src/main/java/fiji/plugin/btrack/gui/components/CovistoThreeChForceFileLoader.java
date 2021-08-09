package fiji.plugin.btrack.gui.components;

import java.awt.Checkbox;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public class CovistoThreeChForceFileLoader {

	final String bordertitle;
	final String[] blankimageNames;

	public CovistoThreeChForceFileLoader(final String bordertitle, final String[] blankimageNames) {

		this.bordertitle = bordertitle;
		this.blankimageNames = blankimageNames;
	}

	public JPanel panelThreeChannel = new JPanel();

	public JComboBox<String> ChooseImage;
	public JComboBox<String> ChoosesecImage;
	public JComboBox<String> ChoosethirdImage;

	public JPanel ThreeChannelOption() {

		layoutManager.Setlayout.LayoutSetter(panelThreeChannel);
		ChooseImage = new JComboBox<String>(blankimageNames);
		ChoosesecImage = new JComboBox<String>(blankimageNames);
		ChoosethirdImage = new JComboBox<String>(blankimageNames);
		Border chooseoriginalfile = new CompoundBorder(new TitledBorder(bordertitle),
				new EmptyBorder(layoutManager.Setlayout.c.insets));

		panelThreeChannel.add(ChooseImage, new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));
		panelThreeChannel.add(ChoosesecImage, new GridBagConstraints(3, 0, 3, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));
		panelThreeChannel.add(ChoosethirdImage, new GridBagConstraints(0, 1, 3, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));
		panelThreeChannel.setBorder(chooseoriginalfile);

		return panelThreeChannel;

	}

}
