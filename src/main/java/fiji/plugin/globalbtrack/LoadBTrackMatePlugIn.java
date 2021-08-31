package fiji.plugin.globalbtrack;

import javax.swing.JFrame;

import fiji.plugin.globalbtrack.gui.descriptors.BTMStartDialogDescriptor;
import ij.ImageJ;
import ij.ImagePlus;
import ij.io.Opener;

public class LoadBTrackMatePlugIn {

	public static void main(String[] args) {

		JFrame frame = new JFrame("");

		new ImageJ();

		ImagePlus impA = new Opener().openImage("/home/sancere/Downloads/TOMCellSegmentation.tif");
		impA.show();

		BTMStartDialogDescriptor panel = new BTMStartDialogDescriptor();

		frame.setSize(panel.getPreferredSize());

	}

}
