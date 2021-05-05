package listeners;

import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import javax.swing.JScrollBar;
import javax.swing.SwingUtilities;

import fiji.plugin.btrack.gui.components.CovistoKalmanPanel;
import pluginTools.BoundaryTrack;
import pluginTools.InteractiveBud;
import pluginTools.InteractiveBud.ValueChange;
import skeleton.SkeletonizeBuds;
import utility.BudShowView;

public class BudSkeletonListener implements ActionListener {
	
	final InteractiveBud parent;
	
	public BudSkeletonListener(final InteractiveBud parent) {
		
		this.parent = parent;
		
	}
	
	
	@Override
	public void actionPerformed(final ActionEvent arg0) {

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {

				go(parent);

			}

		});

	}
	

	public static void go(final InteractiveBud parent) {

		CovistoKalmanPanel.Skeletontime.setEnabled(false);
		CovistoKalmanPanel.Timetrack.setEnabled(false);
		parent.SaveAllbutton.setEnabled(false);
		parent.Savebutton.setEnabled(false);
		parent.Checkpointbutton.setEnabled(false);
		
		SkeletonizeBuds skeleton = new SkeletonizeBuds(parent);
		skeleton.execute();
		
		

	}
}