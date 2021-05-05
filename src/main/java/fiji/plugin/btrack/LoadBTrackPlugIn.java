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
	
		ImagePlus impA = new Opener()
				.openImage("/home/kapoorlab/TestSmartSeedResults.tif");
		impA.show();
		
	
	
		

		BTStartDialogDescriptor panel = new BTStartDialogDescriptor();

		frame.getContentPane().add(panel, "Center");
		frame.setSize(panel.getPreferredSize());
		
		
	}
	
	
}
