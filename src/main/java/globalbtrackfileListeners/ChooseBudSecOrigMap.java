package globalbtrackfileListeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;

import fiji.plugin.globalbtrack.gui.descriptors.BTStartDialogDescriptor;
import ij.WindowManager;

public class ChooseBudSecOrigMap implements ActionListener {

	final BTStartDialogDescriptor parent;
	final JComboBox<String> choice;

	public ChooseBudSecOrigMap(final BTStartDialogDescriptor parent, final JComboBox<String> choice) {

		this.parent = parent;
		this.choice = choice;

	}

	@Override
	public void actionPerformed(ActionEvent e) {

		String imagename = (String) choice.getSelectedItem();

		parent.impOrigRGB = WindowManager.getImage(imagename);

	}

}
