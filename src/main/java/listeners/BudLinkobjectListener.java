package listeners;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import ij.gui.Roi;
import net.imglib2.RealLocalizable;
import pluginTools.BoundaryTrack;
import pluginTools.InteractiveBud;
import tracker.BUDDYTrackResult;
import utility.SavePink;


public class BudLinkobjectListener implements ActionListener {
	
	final InteractiveBud parent;
	
	public BudLinkobjectListener(final InteractiveBud parent) {
		
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

		
		parent.Tracklist.clear();
		parent.Finalresult.clear();
		parent.PanelSelectFile.removeAll();
		parent.scrollPane.removeAll();
		parent.panelFirst.repaint();
		parent.panelFirst.validate();
		parent.table.removeAll();
		parent.scrollPane = new JScrollPane(parent.table);
		parent.scrollPane.getViewport().add(parent.table);
		parent.scrollPane.setAutoscrolls(true);
		parent.scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		parent.scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		parent.PanelSelectFile.add(parent.scrollPane, BorderLayout.CENTER);

		if (parent.imp.getOverlay()!=null) {
		Roi[] rois = parent.imp.getOverlay().toArray();
		
		for(int i = 0; i<rois.length;++i) {
			
			Color roicolor = rois[i].getStrokeColor();
			
			if(roicolor == parent.Drawcolor)
	        parent.imp.getOverlay().remove(rois[i]);			
				
				
		}
		}
		parent.imp.updateAndDraw();		
		parent.PanelSelectFile.setBorder(parent.selectfile);
		int size = 100;
		parent.table.getColumnModel().getColumn(0).setPreferredWidth(size);
		parent.table.getColumnModel().getColumn(1).setPreferredWidth(size);
		parent.table.getColumnModel().getColumn(2).setPreferredWidth(size);
		parent.table.getColumnModel().getColumn(3).setPreferredWidth(size);
		parent.table.getColumnModel().getColumn(4).setPreferredWidth(size);
		parent.table.setPreferredScrollableViewportSize(parent.table.getPreferredSize());
		parent.table.setFillsViewportHeight(true);
		parent.table.isOpaque();
		parent.scrollPane.setMinimumSize(new Dimension(300, 200));
		parent.scrollPane.setPreferredSize(new Dimension(300, 200));
		
		
		
		BUDDYTrackResult track = new BUDDYTrackResult(parent);
		track.execute();
		SavePink pinkies = new SavePink(parent);
		pinkies.Saver();

		parent.Cellbutton.setEnabled(true);

	}

}