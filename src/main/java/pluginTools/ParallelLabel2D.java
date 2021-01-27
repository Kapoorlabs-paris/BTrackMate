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

	public class ParallelLabel2D implements Runnable {

		final InteractiveBud parent;
		final Budregionobject  PairCurrentViewBit;
		final List<RealLocalizable> truths;
		ArrayList<Budobject> Budlist;
		ArrayList<Cellobject> celllist;
		ArrayList<Budpointobject> Budpointlist;
		final RealLocalizable centerpoint;
		final String uniqueID;
		final int label;

		
		public ParallelLabel2D(InteractiveBud parent,ArrayList<Budobject> Budlist , ArrayList<Cellobject> celllist, ArrayList<Budpointobject> Budpointlist,  Budregionobject  PairCurrentViewBit,
				List<RealLocalizable> truths, RealLocalizable centerpoint, String uniqueID,
				int label) {
			
			
			this.Budlist = Budlist;
			this.celllist = celllist;
			this.Budpointlist = Budpointlist;
			this.parent = parent;
			this.PairCurrentViewBit = PairCurrentViewBit;
			this.truths = truths;
			this.centerpoint = centerpoint;
			this.uniqueID = uniqueID;
			this.label = label;
			
			
		}
		
		
		
		@Override
		public void run() {
		

			
			// Corner points of region
			OpService ops = parent.ij.op();

			List<RealLocalizable> skeletonEndPoints = TrackEachCell.GetCorner(PairCurrentViewBit, ops);
		
			double perimeter = GetNearest.Volume(PairCurrentViewBit.Boundaryimage);
			Budobject Curreentbud = new Budobject(centerpoint, truths, skeletonEndPoints, parent.thirdDimension, label,
					perimeter);
			Budlist.add(Curreentbud);
			if (parent.SegYelloworiginalimg != null) {
		          celllist = GetNearest.getAllInteriorCells(parent, parent.CurrentViewInt, parent.CurrentViewYellowInt);

			for(Cellobject currentbudcell:celllist) {
				
	           			
				Localizable centercell = currentbudcell.Location;
				RealLocalizable closestskel = GetNearest.getNearestskelPoint(truths, centercell);
				// and the distance
				double closestBudPoint = Distance.DistanceSqrt(centercell, closestskel);
				// Make the bud n cell object, each cell has all information about the bud n itself 
				BCellobject budncell = new BCellobject(Curreentbud, Budpointlist, currentbudcell, closestBudPoint, closestBudPoint, parent.thirdDimension);
	            parent.budcells.add(budncell, parent.thirdDimension);  
	            
			
		}

		
		
		}
		
	}

	
}
