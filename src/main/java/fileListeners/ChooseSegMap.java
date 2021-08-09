package fileListeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;

import fiji.plugin.btrack.gui.descriptors.BTMStartDialogDescriptor;
import ij.WindowManager;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.integer.IntType;
import pluginTools.BTracksimplifiedio.SimplifiedIO;

public class ChooseSegMap implements ActionListener {

	final BTMStartDialogDescriptor parent;
	final JComboBox<String> choice;

	public ChooseSegMap(final BTMStartDialogDescriptor parent, final JComboBox<String> choice) {

		this.parent = parent;
		this.choice = choice;

	}

	@Override
	public void actionPerformed(ActionEvent e) {

		String imagename = (String) choice.getSelectedItem();
		parent.impSeg = WindowManager.getImage(imagename);

		parent.imageSegA = SimplifiedIO.openImage(
				parent.impSeg.getOriginalFileInfo().directory + parent.impSeg.getOriginalFileInfo().fileName,
				new IntType());

	}

}
