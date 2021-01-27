package pluginTools;

import java.util.ArrayList;
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

public class TrackEach3DCell implements Runnable {


	final InteractiveBud parent;
	int percent;
	ArrayList<Cellobject> Greencelllist = new ArrayList<Cellobject>();
	
	
	public TrackEach3DCell(final InteractiveBud parent,
			final int percent) {

		this.parent = parent;
		this.percent = percent;
	}
	


	
	
	public ArrayList<Cellobject> returnGreenlist(){
		
		
		return Greencelllist;
	}
	
	public void displayCells() {
		
		String uniqueID = Integer.toString(parent.fourthDimension);
		
		parent.overlay.clear();

		int nThreads = Runtime.getRuntime().availableProcessors();
		// set up executor service
		final ExecutorService taskExecutor = Executors.newFixedThreadPool(nThreads);
		List<Callable<Object>> tasks = new ArrayList<Callable<Object>>();
				// For each bud get the list of points

											// For each bud get the list of points
											if (parent.jpb != null)
												utility.BudProgressBar.SetProgressBar(parent.jpb,
														100 * (percent) / (parent.fourthDimensionSize + parent.pixellist.size()),
														"Collecting Cells T = " + parent.fourthDimension + "/" + parent.fourthDimensionSize );

											tasks.add(Executors.callable(new ParallelLabel(parent, Greencelllist,  uniqueID)));
	try {
			
			taskExecutor.invokeAll(tasks);
	
	} catch (InterruptedException e1) {

		
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
			
	    double minZ = Interiorimage.realMin(2);
	    double maxZ = Interiorimage.realMax(2);
	    
	    RealPoint startA = new RealPoint(new double[] {minX, minY, minZ});
	    endPoints.add(startA);
	    RealPoint startB = new RealPoint(new double[] {minX, maxY, maxZ});
	    endPoints.add(startB);
	    RealPoint startC = new RealPoint(new double[] {maxX, maxY, maxZ});
	    endPoints.add(startC);
	    RealPoint startD = new RealPoint(new double[] {maxX, minY, minZ});
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
		long[] minVal = { Intimg.max(0), Intimg.max(1), Intimg.max(2)  };
		long[] maxVal = { Intimg.min(0), Intimg.min(1), Intimg.min(2) };

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
		// Gradient image gives us the bondary points
		RandomAccessibleInterval<BitType> gradimg = GradientmagnitudeImage(outimg);
		
		
		
		Budregionobject region = new Budregionobject(gradimg, outimg, min,  size);
		return region;

	}


	@Override
	public void run() {
		displayCells();
		
	}


	


}
