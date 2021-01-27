package listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import Buddy.plugin.trackmate.TrackMatePlugIn_;
import ij.IJ;
import pluginTools.InteractiveBud;

public class BudMastadonListener implements ActionListener {

	final InteractiveBud parent;
	
	public BudMastadonListener(InteractiveBud parent) {
		
		
		this.parent = parent;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {

		
		
		
	
		IJ.log(Integer.toString(parent.budcells.keySet().size()));
		TrackMatePlugIn_ plugin = new TrackMatePlugIn_(parent);
		plugin.run(null);
		
		
		
		
		
	}

}
