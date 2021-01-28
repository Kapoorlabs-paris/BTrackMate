package pluginTools;

import javax.swing.JFrame;

import ij.ImagePlus;
import ij.io.Opener;

import net.imagej.ImageJ;

public class PanelZero3DCell {

	public static void main(String[] args) {
		
		JFrame frame = new JFrame("");

		
		
		ThreeDTimeCellFileChooser panel = new ThreeDTimeCellFileChooser();

		frame.getContentPane().add(panel, "Center");
		frame.setSize(panel.getPreferredSize());
		
		
	}
	
	
}
