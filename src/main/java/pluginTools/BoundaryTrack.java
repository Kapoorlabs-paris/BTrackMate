package pluginTools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.JProgressBar;

import budDetector.Budobject;
import budDetector.Budpointobject;
import fiji.plugin.btrack.gui.components.CovistoKalmanPanel;
import fiji.plugin.btrackmate.Spot;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealLocalizable;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.view.Views;
import pluginTools.InteractiveBud.ValueChange;
import tracker.BUDDYDisplaySelectedTrack;


public class BoundaryTrack {
	
	
	final InteractiveBud parent;
	final JProgressBar jpb;
	
	
	public BoundaryTrack(final InteractiveBud parent, final JProgressBar jpb) {
		
		this.parent = parent;
		this.jpb = jpb;
		
	}
	
	
	public void ShowBoundary() {
		
		int percent = 0;
		

		Common(percent);
	}
	
	
	public void ShowBoundaryTime() {
		
		int percent = 0;
		for(int t = parent.AutostartTime; t <= parent.AutoendTime; ++t) {
			
			
			if (parent.mvl != null) {
				parent.imp.getCanvas().removeMouseListener(parent.mvl);
			    parent.mouseremoved = true;	
			}
			
			
			if(parent.EscapePressed) {

				if(parent.jpb!=null ) 
					utility.BudProgressBar.SetProgressBar(parent.jpb, 100 ,
							"You pressed Escape to stop calculation, press restart to start again" );
				parent.EscapePressed = false;
				CovistoKalmanPanel.Skeletontime.setEnabled(true);
				break;
			}
			
		parent.thirdDimension = t;
		parent.updatePreview(ValueChange.THIRDDIMmouse);
		
		parent.inputFieldT.setText(Integer.toString((int)parent.thirdDimension));
		
		parent.timeslider.setValue(utility.BudSlicer.computeScrollbarPositionFromValue(parent.thirdDimension, parent.thirdDimensionsliderInit, parent.thirdDimensionSize, parent.scrollbarSize));
		parent.timeslider.repaint();
		parent.timeslider.validate();
		parent.panelFirst.validate();
		parent.panelFirst.repaint();
		
		Common(percent);
			
		
		percent++;
		
		}
		
		BUDDYDisplaySelectedTrack.Select(parent, null);
		BUDDYDisplaySelectedTrack.Mark(parent, null);
		
		if(parent.jpb!=null && parent.thirdDimension == parent.AutoendTime) {
			CovistoKalmanPanel.Skeletontime.setEnabled(true);
			CovistoKalmanPanel.Timetrack.setEnabled(true);
			parent.SaveAllbutton.setEnabled(true);
			parent.Savebutton.setEnabled(true);
			parent.Checkpointbutton.setEnabled(true);
			utility.BudProgressBar.SetProgressBar(parent.jpb, 100 ,
					"Skeletons Created, Push Track Buddies Button" );
			parent.AllRefcords = new HashMap<String, RealLocalizable>();
			parent.AllBudcenter = new ArrayList<RealLocalizable>();
			parent.ChosenBudcenter = new ArrayList<RealLocalizable>();	
			
		}
	}
	
	public void Common(int percent) {

	
		

	
	RandomAccessibleInterval<IntType> BudSeg = utility.BudSlicer.getCurrentBudView(parent.Segoriginalimg,(int) parent.thirdDimension,
			(int)parent.thirdDimensionSize);
	GetPixelList(BudSeg);
	IntType min = new IntType();
	IntType max = new IntType();
	computeMinMax(Views.iterable(BudSeg), min, max);
	ArrayList<Budobject> Budlist = new ArrayList<Budobject>();
	ArrayList<Budpointobject>Budpointlist = new ArrayList<Budpointobject>();
	ArrayList<Spot> Budcelllist = new ArrayList<Spot>();
	if(parent.SegYelloworiginalimg!=null) {
		
	    TrackEachBud compute = new TrackEachBud(parent,  Budlist,Budpointlist, Budcelllist,max.get(), percent);
	    compute.displayBuds();
	}
	
	else {
	  TrackEachBud compute = new TrackEachBud(parent, Budlist,Budpointlist, max.get(), percent);
		
	  compute.displayBuds();
	}
	
		parent.AllBuds.put(Integer.toString(parent.thirdDimension), Budlist);
		

		parent.AllBudpoints.put(Integer.toString(parent.thirdDimension), Budpointlist);
		
		
		
	}
	public  void GetPixelList(RandomAccessibleInterval<IntType> intimg) {

		IntType min = new IntType();
		IntType max = new IntType();
		computeMinMax(Views.iterable(intimg), min, max);
		Cursor<IntType> intCursor = Views.iterable(intimg).cursor();
		// Neglect the background class label
		parent.pixellist.clear();
		
		
		while (intCursor.hasNext()) {
			intCursor.fwd();
			int i = intCursor.get().get();

			if(!parent.pixellist.contains(i) && i > 0)
				parent.pixellist.add(i);



		}

	}
	
	public <T extends Comparable<T> & Type<T>> void computeMinMax(final Iterable<T> input, final T min, final T max) {
		// create a cursor for the image (the order does not matter)
		final Iterator<T> iterator = input.iterator();

		// initialize min and max with the first image value
		T type = iterator.next();

		min.set(type);
		max.set(type);

		// loop over the rest of the data and determine min and max value
		while (iterator.hasNext()) {
			// we need this type more than once
			type = iterator.next();

			if (type.compareTo(min) < 0)
				min.set(type);

			if (type.compareTo(max) > 0)
				max.set(type);
		}
	}


}