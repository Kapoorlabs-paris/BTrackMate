package fileListeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import javax.swing.JComboBox;

import fiji.plugin.btrack.gui.descriptors.BTMStartDialogDescriptor;
import ij.IJ;
import ij.WindowManager;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.FloatType;
import pluginTools.simplifiedio.SimplifiedIO;

public class ChooseOrigMap implements ActionListener {
	
	
	final BTMStartDialogDescriptor parent;
	final JComboBox<String> choice;
	
	
	public ChooseOrigMap(final BTMStartDialogDescriptor parent, final JComboBox<String> choice ) {
		
		
		this.parent = parent;
		this.choice = choice;
		
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		
		String imagename = (String) choice.getSelectedItem();
		
		
		
    	parent.impOrig = WindowManager.getImage(imagename);
    	if(parent.impOrig!=null) {
    		parent.imageOrig = SimplifiedIO.openImage(
    				parent.impOrig.getOriginalFileInfo().directory + parent.impOrig.getOriginalFileInfo().fileName,
    				new FloatType());
    		
    			
			parent.TimeTotal = parent.imageOrig.dimension(3);
			
			
			DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.getDefault());
			otherSymbols.setDecimalSeparator('.');
			otherSymbols.setGroupingSeparator(','); 
			DecimalFormat df = new DecimalFormat(("#.###"), otherSymbols);
			parent.inputFieldT.setText(String.valueOf(df.format(parent.TimeTotal)));
	}
 
		
	}
	

}
