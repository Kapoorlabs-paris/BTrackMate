package fiji.plugin.btrack;

import javax.swing.JFrame;

import fiji.plugin.btrack.gui.descriptors.BTStartDialogDescriptor;
import ij.ImagePlus;
import ij.io.Opener;
import ij.ImageJ;

public class LoadBTrackPlugIn {

	public static void main(String[] args) {

		JFrame frame = new JFrame("");

		new ImageJ();

		ImagePlus impA = new Opener().openImage(
				"/Users/aimachine/Downloads/113B_TRL_Mask/mask_133B_TOM_day4_BeadsEGF_each30min_10x_FINAL_Stitch.tif");

		// ImagePlus impA = new Opener()
		// .openImage("/Users/aimachine/Downloads/113B_TRL_Mask/mask_98C_p1.tif");
		impA.show();

		BTStartDialogDescriptor panel = new BTStartDialogDescriptor();

		frame.getContentPane().add(panel, "Center");
		frame.setSize(panel.getPreferredSize());

	}

}
