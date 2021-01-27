package pluginTools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JProgressBar;

import budDetector.BCellobject;
import budDetector.Budobject;
import budDetector.Budpointobject;
import kalmanGUI.CovistoKalmanPanel;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealLocalizable;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import pluginTools.InteractiveBud.ValueChange;


public class CellTrack implements Runnable {
	
	
	final InteractiveBud parent;
	final JProgressBar jpb;
	
	
	public CellTrack(final InteractiveBud parent, final JProgressBar jpb) {
		
		this.parent = parent;
		this.jpb = jpb;
		
	}
	
	

	
	public void ShowCellTime() {
		
		int percent = 0;
		for(int t = 1; t <= parent.thirdDimensionSize; ++t) {
	
		parent.updatePreview(ValueChange.THIRDDIMmouse);
	
		parent.thirdDimension = t;
		
		RandomAccessibleInterval<IntType> BudSeg = utility.BudSlicer.getCurrentBudView(parent.Segoriginalimg,(int) parent.thirdDimension,
				(int)parent.thirdDimensionSize);
		GetPixelList(BudSeg);
		IntType min = new IntType();
		IntType max = new IntType();
		computeMinMax(Views.iterable(BudSeg), min, max);
		ArrayList<Budobject> Budlist = new ArrayList<Budobject>();
		ArrayList<Budpointobject>Budpointlist = new ArrayList<Budpointobject>();
		ArrayList<BCellobject> Budcelllist = new ArrayList<BCellobject>();
		
			
		    TrackEachCell compute = new TrackEachCell(parent,  Budlist,Budpointlist, Budcelllist,  percent);
		    compute.displayCells();
		
		
			parent.AllBuds.put(Integer.toString(t), Budlist);
			

			parent.AllBudpoints.put(Integer.toString(t), Budpointlist);
			
		
		percent++;
		
		}
		
		
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




	@Override
	public void run() {
		
		ShowCellTime();
		
	}


}