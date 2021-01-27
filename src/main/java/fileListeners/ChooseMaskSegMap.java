package fileListeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;

import ij.WindowManager;
import pluginTools.ThreeDTimeCellFileChooser;

public class ChooseMaskSegMap implements ActionListener {
	
	
	final ThreeDTimeCellFileChooser parent;
	final JComboBox<String> choice;
	
	
	public ChooseMaskSegMap(final ThreeDTimeCellFileChooser parent, final JComboBox<String> choice ) {
		
		
		this.parent = parent;
		this.choice = choice;
		
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		
		String imagename = (String) choice.getSelectedItem();
      	parent.impMask = WindowManager.getImage(imagename);
    	

		
	}
	

}
