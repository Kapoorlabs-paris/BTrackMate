package pluginTools;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import budDetector.BCellobject;
import budDetector.Budobject;
import budDetector.Budpointobject;
import budDetector.Budregionobject;
import budDetector.Cellobject;
import budDetector.Distance;
import net.imagej.ops.OpService;
import net.imglib2.Localizable;
import net.imglib2.RealLocalizable;
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
	

		

		Budobject Curreentbud = new Budobject(null, null, null, parent.fourthDimension, 1,
				0);
		  
	      Greencelllist  = GetNearest.getAllInterior3DCells(parent, parent.CurrentViewYellowInt);
	      
     	for(Cellobject currentbudcell:Greencelllist) {
			
			// and the distance
			double closestBudPoint = 0;
			// Make the bud n cell object, each cell has all information about the bud n itself 
			BCellobject budncell = new BCellobject(Curreentbud, new ArrayList<Budpointobject>(), currentbudcell, closestBudPoint, closestBudPoint, parent.fourthDimension);
            parent.budcells.add(budncell, parent.fourthDimension);  
            
		
	}

	
	
	}
	
}
