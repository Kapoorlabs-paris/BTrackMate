package listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import fiji.plugin.globalbtrackmate.TrackMatePlugIn;
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
		// TrackMatePlugIn plugin = new TrackMatePlugIn(parent);
		// plugin.run(null);

	}

}
