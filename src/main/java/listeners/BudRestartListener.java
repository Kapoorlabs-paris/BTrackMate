package listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.SwingUtilities;

import fiji.plugin.btrack.gui.components.CovistoKalmanPanel;
import pluginTools.InteractiveBud;
import pluginTools.InteractiveBud.ValueChange;
import skeleton.SkeletonizeBuds;

public class BudRestartListener implements ActionListener {
	
	final InteractiveBud parent;
	
	public BudRestartListener(final InteractiveBud parent) {
		
		this.parent = parent;
		
	}
	
	
	@Override
	public void actionPerformed(final ActionEvent arg0) {

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {

				go();

			}

		});

	}
	

	public void go() {

		
		parent.thirdDimension = 1;
		parent.inputFieldT.setText(Integer.toString((int)parent.thirdDimension));
		parent.timeslider.setValue(utility.BudSlicer.computeScrollbarPositionFromValue(parent.thirdDimension, parent.thirdDimensionsliderInit, parent.thirdDimensionSize, parent.scrollbarSize));
        parent.BudOvalRois.clear();
		parent.timeslider.repaint();
		parent.timeslider.validate();
		parent.panelFirst.validate();
		parent.panelFirst.repaint();
		parent.table.removeAll();
		parent.Tracklist.clear();
		parent.table.repaint();
		parent.table.validate();
		CovistoKalmanPanel.Skeletontime.setEnabled(true);
		
		parent.updatePreview(ValueChange.THIRDDIMmouse);
		
		

	}
	
	}