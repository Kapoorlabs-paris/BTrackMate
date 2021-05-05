package pluginTools;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import budDetector.Budobject;
import budDetector.Budpointobject;
import budDetector.Budregionobject;
import budDetector.Cellobject;
import budDetector.Distance;
import fiji.plugin.btrackmate.Spot;
import net.imagej.ops.OpService;
import net.imglib2.Localizable;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import utility.GetNearest;
import pluginTools.TrackEach3DCell;

public class ParallelLabel implements Runnable {

	final InteractiveBud parent;
	ArrayList<Cellobject> Greencelllist;
	final String uniqueID;

	
	public ParallelLabel(InteractiveBud parent,ArrayList<Cellobject> Greencelllist , String uniqueID) {
		
		
		this.Greencelllist = Greencelllist;
		this.parent = parent;
		this.uniqueID = uniqueID;
		
		
	}
	
	
	@Override
	public void run() {
	
		  
	    Greencelllist  = GetNearest.getAllInterior3DCells(parent, parent.CurrentViewYellowInt);
	      
     	for(Cellobject currentbudcell:Greencelllist) {
			
			// Make TM spot
     		double [] calibration = {parent.calibrationX, parent.calibrationY, parent.calibrationZ};
     		final double x = calibration[0]* ( currentbudcell.Location.getDoublePosition(0) );
			final double y = calibration[1] * ( currentbudcell.Location.getDoublePosition(1) );
			final double z = calibration[2] * ( currentbudcell.Location.getDoublePosition(2) );

			double[] point = {x,y,z};
			RealPoint location = new RealPoint(point);
			
			
			double radius = 0;
			for (int i = 0; i < currentbudcell.extents.length; ++i)
				radius *=  currentbudcell.extents[i] * calibration[i];
					
			radius = radius /8;		
			final double quality = currentbudcell.cellVolume;
			
			// Make the Spot
			Spot budncell = new Spot(location,radius,quality);
			
            parent.budcells.add(budncell, parent.fourthDimension);  
            
		
	}

	
	
	}
	
}
