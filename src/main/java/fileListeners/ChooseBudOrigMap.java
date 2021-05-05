package fileListeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import javax.swing.JComboBox;

import fiji.plugin.btrack.gui.descriptors.BTStartDialogDescriptor;
import ij.IJ;
import ij.WindowManager;

public class ChooseBudOrigMap implements ActionListener {
	
	
	final BTStartDialogDescriptor parent;
	final JComboBox<String> choice;
	
	
	public ChooseBudOrigMap(final BTStartDialogDescriptor parent, final JComboBox<String> choice ) {
		
		
		this.parent = parent;
		this.choice = choice;
		
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		
		String imagename = (String) choice.getSelectedItem();
		
		
		
    	parent.impOrig = WindowManager.getImage(imagename);
   
    	if(parent.impOrig!=null) {
			parent.calibrationX = parent.impOrig.getCalibration().pixelWidth;
			parent.calibrationY = parent.impOrig.getCalibration().pixelHeight;
			parent.FrameInterval = parent.impOrig.getCalibration().frameInterval;
			if (parent.FrameInterval == 0)
				parent.FrameInterval = 1;
			DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.getDefault());
			otherSymbols.setDecimalSeparator('.');
			otherSymbols.setGroupingSeparator(','); 
			DecimalFormat df = new DecimalFormat(("#.###"), otherSymbols);
			parent.FieldinputLabelcalT.setText(String.valueOf(df.format(parent.FrameInterval))); 
			parent.inputFieldcalX.setText(String.valueOf(df.format(parent.calibrationX)));
			parent.inputFieldcalY.setText(String.valueOf(df.format(parent.calibrationY)));
	}
 
		
	}
	

}
