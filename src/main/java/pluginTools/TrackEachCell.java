package pluginTools;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import budDetector.BCellobject;
import budDetector.Budobject;
import budDetector.Budpointobject;
import budDetector.Budregionobject;
import budDetector.Cellobject;
import budDetector.Distance;
import ij.gui.OvalRoi;
import net.imagej.ops.OpService;
import net.imglib2.Cursor;
import net.imglib2.Localizable;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.view.Views;
import utility.GetNearest;
import displayBud.DisplayListOverlay;

public class TrackEachCell {


	final InteractiveBud parent;
	int percent;
	final ArrayList<Budobject> Budlist;
	final ArrayList<Budpointobject> Budpointlist;
	final ArrayList<BCellobject> Budcelllist;
    ArrayList<Cellobject> celllist = new ArrayList<Cellobject>();
    
	public TrackEachCell(final InteractiveBud parent, 
			ArrayList<Budobject> Budlist, ArrayList<Budpointobject> Budpointlist, 
			final int percent) {

		this.parent = parent;
		this.percent = percent;
		this.Budlist = Budlist;
		this.Budpointlist = Budpointlist;
        this.Budcelllist = null;
	}
	
	public TrackEachCell(final InteractiveBud parent, 
			ArrayList<Budobject> Budlist, ArrayList<Budpointobject> Budpointlist, ArrayList<BCellobject> Budcelllist,
			final int percent) {

		this.parent = parent;
		this.percent = percent;
		this.Budlist = Budlist;
		this.Budpointlist = Budpointlist;
        this.Budcelllist = Budcelllist;
	}

	public TrackEachCell(final InteractiveBud parent, 
			final int percent) {

		this.parent = parent;
		this.percent = percent;
		this.Budlist = null;
		this.Budpointlist = null;
        this.Budcelllist = null;

	}
	

	
	
	public ArrayList<Budobject> returnBudlist(){
		
		
		return Budlist;
	}
	
    public ArrayList<Budpointobject> returnBudpointlist(){
		
		
		return Budpointlist;
	}
	
    public ArrayList<BCellobject> returnBCellobjectlist(){
		
		
		return Budcelllist;
	}
	
	
	public void displayCells() {

		
		
		String uniqueID = Integer.toString(parent.thirdDimension);
		
		
		Iterator<Integer> setiter = parent.pixellist.iterator();
		parent.overlay.clear();
		int nThreads = Runtime.getRuntime().availableProcessors();
		// set up executor service
		final ExecutorService taskExecutor = Executors.newFixedThreadPool(nThreads);
		List<Callable<Object>> tasks = new ArrayList<Callable<Object>>();
	

		while (setiter.hasNext()) {

			percent++;
			int label = setiter.next();

			if (label > 0) {

		

				// Input the integer image of bud with the label and output the binary border
				// for that label
				Budregionobject PairCurrentViewBit = BudCurrentLabelBinaryImage(
						parent.CurrentViewInt, label);
                
				// For each bud get the list of points
				List<RealLocalizable> truths = DisplayListOverlay.GetCoordinatesBit(PairCurrentViewBit.Boundaryimage);


				
				Color displayColor; 
				
					displayColor = Color.GREEN;
				
				
				for (int i = 0; i < truths.size() ; i += 1) {

					double X = truths.get(i).getDoublePosition(0);
					double Y = truths.get(i).getDoublePosition(1);


					OvalRoi points =  new OvalRoi((int) X, (int) Y,
							2, 2);
					points.setStrokeColor(displayColor);
					parent.overlay.add(points);
				
				}
				parent.imp.updateAndDraw();


											PairCurrentViewBit = BudCurrentLabelBinaryImage(parent.CurrentViewInt, label);
											// For each bud get the list of points
											truths = DisplayListOverlay.GetCoordinatesBit(PairCurrentViewBit.Boundaryimage);
											RealLocalizable centerpoint = budDetector.Listordering.getMeanCord(truths);
                                            
											if (parent.jpb != null)
												utility.BudProgressBar.SetProgressBar(parent.jpb,
														100 * (percent) / (parent.thirdDimensionSize + parent.pixellist.size()),
														"Collecting Cells = " + parent.thirdDimension + "/" + parent.thirdDimensionSize );

											Common(PairCurrentViewBit, truths,  centerpoint, uniqueID, label);

											tasks.add(Executors.callable(new ParallelLabel2D(parent,Budlist , celllist,  Budpointlist,  PairCurrentViewBit,
													truths, centerpoint,uniqueID,
													label)));

					}

      		}
	try {
			
			taskExecutor.invokeAll(tasks);
	
	} catch (InterruptedException e1) {

		System.out.println(e1 + " Task not executed");
		
	}

		

	}
	
	
	

	
	
	

	public void Common(Budregionobject  PairCurrentViewBit,
			List<RealLocalizable> truths, RealLocalizable centerpoint, String uniqueID,
			int label) {

		// Corner points of region
		OpService ops = parent.ij.op();

		List<RealLocalizable> skeletonEndPoints = GetCorner(PairCurrentViewBit, ops);
	
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



	public static ArrayList<RealLocalizable> GetCorner(Budregionobject PairCurrentViewBit,
			OpService ops) {

		ArrayList<RealLocalizable> endPoints = new ArrayList<RealLocalizable>();

		
		 RandomAccessibleInterval<BitType> Interiorimage =  PairCurrentViewBit.Interiorimage;
			
	    double minX = Interiorimage.realMin(0);
	    double maxX = Interiorimage.realMax(0);
			
	    double minY = Interiorimage.realMin(1);
	    double maxY = Interiorimage.realMax(1);	
			
	    
	    RealPoint startA = new RealPoint(new double[] {minX, minY});
	    endPoints.add(startA);
	    RealPoint startB = new RealPoint(new double[] {minX, maxY});
	    endPoints.add(startB);
	    RealPoint startC = new RealPoint(new double[] {maxX, maxY});
	    endPoints.add(startC);
	    RealPoint startD = new RealPoint(new double[] {maxX, minY});
	    endPoints.add(startD);
	    
	    
	    
		return endPoints;

	}

	public static RandomAccessibleInterval<BitType> GradientmagnitudeImage(RandomAccessibleInterval<BitType> inputimg) {

		RandomAccessibleInterval<BitType> gradientimg = new ArrayImgFactory<BitType>().create(inputimg, new BitType());
		Cursor<BitType> cursor = Views.iterable(gradientimg).localizingCursor();
		RandomAccessible<BitType> view = Views.extendBorder(inputimg);
		RandomAccess<BitType> randomAccess = view.randomAccess();

		// iterate over all pixels
		while (cursor.hasNext()) {
			// move the cursor to the next pixel
			cursor.fwd();

			// compute gradient and its direction in each dimension
			double gradient = 0;

			for (int d = 0; d < inputimg.numDimensions(); ++d) {
				// set the randomaccess to the location of the cursor
				randomAccess.setPosition(cursor);

				// move one pixel back in dimension d
				randomAccess.bck(d);

				// get the value
				double Back = randomAccess.get().getRealDouble();

				// move twice forward in dimension d, i.e.
				// one pixel above the location of the cursor
				randomAccess.fwd(d);
				randomAccess.fwd(d);

				// get the value
				double Fwd = randomAccess.get().getRealDouble();

				gradient += ((Fwd - Back) * (Fwd - Back)) / 4;

			}

			cursor.get().setReal(Math.sqrt(gradient));

		}

		return gradientimg;
	}

	public static Budregionobject BudCurrentLabelBinaryImage(
			RandomAccessibleInterval<IntType> Intimg, int currentLabel) {
		int n = Intimg.numDimensions();
		long[] position = new long[n];
		Cursor<IntType> intCursor = Views.iterable(Intimg).cursor();

		RandomAccessibleInterval<BitType> outimg = new ArrayImgFactory<BitType>().create(Intimg, new BitType());
		RandomAccess<BitType> imageRA = outimg.randomAccess();

		// Go through the whole image and add every pixel, that belongs to
		// the currently processed label
		long[] minVal = { Intimg.max(0), Intimg.max(1) };
		long[] maxVal = { Intimg.min(0), Intimg.min(1) };

		while (intCursor.hasNext()) {
			intCursor.fwd();
			imageRA.setPosition(intCursor);
			int i = intCursor.get().get();
			if (i == currentLabel) {

				intCursor.localize(position);
				for (int d = 0; d < n; ++d) {
					if (position[d] < minVal[d]) {
						minVal[d] = position[d];
					}
					if (position[d] > maxVal[d]) {
						maxVal[d] = position[d];
					}

				}

				imageRA.get().setOne();
			} else
				imageRA.get().setZero();

		}
		
		double size = Math.sqrt(Distance.DistanceSq(minVal, maxVal));

	
		Point min = new Point(minVal.length);
		// Gradient image gives us the boundary points
		RandomAccessibleInterval<BitType> gradimg = GradientmagnitudeImage(outimg);
		
		Budregionobject region = new Budregionobject(gradimg, outimg, min,  size);
		return region;

	}
	

}
