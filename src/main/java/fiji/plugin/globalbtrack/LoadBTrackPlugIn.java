package fiji.plugin.globalbtrack;

import javax.swing.JFrame;

import fiji.plugin.globalbtrack.gui.descriptors.BTStartDialogDescriptor;
import ij.ImagePlus;
import ij.io.Opener;
import ij.ImageJ;

public class LoadBTrackPlugIn {

	public static void main(String[] args) {

		JFrame frame = new JFrame("");

		new ImageJ();

		ImagePlus impA = new Opener().openImage(
				"/Users/aimachine/148E_MaskDay4Day5.tif");

		// ImagePlus impA = new Opener()
		// .openImage("/Users/aimachine/Downloads/113B_TRL_Mask/mask_98C_p1.tif");
		impA.show();

		BTStartDialogDescriptor panel = new BTStartDialogDescriptor();

		frame.getContentPane().add(panel, "Center");
		frame.setSize(panel.getPreferredSize());

	}

}
