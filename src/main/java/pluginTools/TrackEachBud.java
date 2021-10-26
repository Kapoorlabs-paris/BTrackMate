package pluginTools;

import java.awt.Color;
import java.awt.Rectangle;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import budDetector.Budobject;
import budDetector.Budpointobject;
import budDetector.Budregionobject;
import budDetector.Cellobject;
import budDetector.Distance;
import budDetector.Listordering;
import budDetector.Roiobject;
import net.imagej.ops.OpService;
import net.imglib2.Cursor;
import net.imglib2.Interval;
import net.imglib2.Localizable;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.algorithm.neighborhood.DiamondShape;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;
import net.imglib2.view.Views;
import skeleton.*;
import utility.GetNearest;
import utility.SavePink;
import displayBud.DisplayListOverlay;
import fiji.plugin.globalbtrack.gui.components.CovistoKalmanPanel;
import fiji.plugin.globalbtrackmate.Spot;
import ij.IJ;
import ij.gui.OvalRoi;

public class TrackEachBud {

	final InteractiveBud parent;
	final int maxlabel;
	int percent;
	final ArrayList<Budobject> Budlist;
	final ArrayList<Budpointobject> Budpointlist;
	final ArrayList<Spot> Budcelllist;
	ArrayList<Cellobject> celllist = new ArrayList<Cellobject>();

	public TrackEachBud(final InteractiveBud parent, ArrayList<Budobject> Budlist,
			ArrayList<Budpointobject> Budpointlist, final int maxlabel, final int percent) {

		this.parent = parent;
		this.maxlabel = maxlabel;
		this.percent = percent;
		this.Budlist = Budlist;
		this.Budpointlist = Budpointlist;
		this.Budcelllist = null;
	}

	public TrackEachBud(final InteractiveBud parent, ArrayList<Budobject> Budlist,
			ArrayList<Budpointobject> Budpointlist, ArrayList<Spot> Budcelllist, final int maxlabel,
			final int percent) {

		this.parent = parent;
		this.maxlabel = maxlabel;
		this.percent = percent;
		this.Budlist = Budlist;
		this.Budpointlist = Budpointlist;
		this.Budcelllist = Budcelllist;
	}

	public ArrayList<Budobject> returnBudlist() {

		return Budlist;
	}

	public ArrayList<Budpointobject> returnBudpointlist() {

		return Budpointlist;
	}

	public ArrayList<Spot> returnSpotlist() {

		return Budcelllist;
	}

	public void displayBuds() {

		ArrayList<Roiobject> Allrois = new ArrayList<Roiobject>();
		String uniqueID = Integer.toString(parent.thirdDimension);
		Iterator<Integer> setiter = parent.pixellist.iterator();
		parent.overlay.clear();

		while (setiter.hasNext()) {

			percent++;
			int label = setiter.next();

			if (label > 0) {

				ArrayList<Roiobject> currentrois = new ArrayList<Roiobject>();
				ArrayList<Roiobject> currentbranchrois = new ArrayList<Roiobject>();
				ArrayList<Roiobject> rejrois = new ArrayList<Roiobject>();
				// Input the integer image of bud with the label and output the binary border
				// for that label
				Budregionobject PairCurrentViewBit = BudCurrentLabelBinaryImage(parent.CurrentViewInt, label);

				// For each bud get the list of points
				List<RealLocalizable> truths = DisplayListOverlay.GetCoordinatesBit(PairCurrentViewBit.Boundaryimage);
				List<RealLocalizable> bodytruths = DisplayListOverlay
						.GetCoordinatesBit(PairCurrentViewBit.Interiorimage);

				Comparator<RealLocalizable> comp = new Comparator<RealLocalizable>() {

					@Override
					public int compare(final RealLocalizable A, final RealLocalizable B) {

						int i = 0;
						while (i < A.numDimensions()) {
							if (A.getDoublePosition(i) != B.getDoublePosition(i)) {
								return (int) Math.signum(A.getDoublePosition(i) - B.getDoublePosition(i));
							}
							i++;
						}

						return A.hashCode() - B.hashCode();

					}
				};

				Collections.sort(truths, comp);
				Collections.sort(bodytruths, comp);
				// Get the center point of each bud

				DisplayListOverlay.BoundaryCenterDisplay(parent, truths);
				if (parent.jpb != null)
					utility.BudProgressBar.SetProgressBar(parent.jpb,
							100 * (percent) / (parent.thirdDimensionSize + parent.pixellist.size()),
							"Computing Skeletons = " + parent.thirdDimension + "/" + parent.thirdDimensionSize
									+ " Total Buddies = " + (parent.pixellist.size()));
				// If we did not compute the skeletons before we compute it for each label

				if (parent.BudOvalRois.get(uniqueID) == null) {

					ArrayList<Pair<RealLocalizable, RealLocalizable>> currentbranchskel = SkeletonCreator(
							PairCurrentViewBit, bodytruths);

					List<RealLocalizable> currentskel = new ArrayList<RealLocalizable>();
					List<RealLocalizable> currentbranch = new ArrayList<RealLocalizable>();
					for (int i = 0; i < currentbranchskel.size(); ++i) {

						currentskel.add(currentbranchskel.get(i).getA());

						currentbranch.add(currentbranchskel.get(i).getB());

					}

				//	DisplayListOverlay.SkeletonEndDisplay(parent, currentskel, label, parent.BudBeforeColor);

					List<RealLocalizable> newcurrentskel = NearestBoundarySkel(currentskel, currentbranch, truths);

					newcurrentskel = RemoveClose(newcurrentskel, 10);

					// Write a method to move the points to the boundary

					currentrois = DisplayListOverlay.SkeletonEndDisplay(parent, newcurrentskel, label, parent.BudColor);

					FillArrays(newcurrentskel, bodytruths, label);

				//	currentbranchrois = DisplayListOverlay.SkeletonEndDisplay(parent, currentbranch, label,
				//			parent.BudSplitColor);

					currentskel = newcurrentskel;

				}

				// If we have a pre-computation/manual marked skeleton point we load it for the
				// current label
				if (parent.BudOvalRois.get(uniqueID) != null) {

					ArrayList<Roiobject> rois = parent.BudOvalRois.get(uniqueID);
					List<RealLocalizable> currentskel = new ArrayList<RealLocalizable>();
					List<RealLocalizable> rejskel = new ArrayList<RealLocalizable>();
					for (Roiobject currentroi : rois) {

						if (currentroi.color == parent.BudColor && currentroi.Label == label) {

							double LocationX = currentroi.point.getDoublePosition(0);
							double LocationY = currentroi.point.getDoublePosition(1);

							RealPoint point = new RealPoint(new double[] { LocationX, LocationY });

							currentskel.add(point);

						}

						if (currentroi.color == parent.RemoveBudColor && currentroi.Label == label) {

							double LocationX = currentroi.point.getDoublePosition(0);
							double LocationY = currentroi.point.getDoublePosition(1);

							RealPoint point = new RealPoint(new double[] { LocationX, LocationY });

							rejskel.add(point);

						}

					}

					currentrois = DisplayListOverlay.SkeletonEndDisplay(parent, currentskel, label, parent.BudColor);
					rejrois = DisplayListOverlay.SkeletonEndDisplay(parent, rejskel, label, parent.RemoveBudColor);

					currentskel.addAll(rejskel);
					FillArrays(currentskel, truths, label);
				}

				currentrois.addAll(rejrois);

				Allrois.addAll(currentrois);

			}

		}

		parent.BudOvalRois.put(uniqueID, Allrois);

		parent.imp.updateAndDraw();

	}

	public List<RealLocalizable> NearestBoundarySkel(List<RealLocalizable> currentskel,
			List<RealLocalizable> currentbranch, List<RealLocalizable> truths) {

		List<RealLocalizable> closestskel = new ArrayList<RealLocalizable>();

		for (int i = 0; i < currentskel.size(); ++i) {

			RealLocalizable cord = currentskel.get(i);

			RealLocalizable nearestbranchpoint = currentbranch.get(i);

			// Make the line

			double slope = (cord.getDoublePosition(1) - nearestbranchpoint.getDoublePosition(1))
					/ (cord.getDoublePosition(0) - nearestbranchpoint.getDoublePosition(0) + 1.0E-30);

			double intercept = cord.getDoublePosition(1) - slope * cord.getDoublePosition(0);

			// Closest distance of point from a line

			RealLocalizable nearestborderpoint = Listordering.getClosestBoundaryPoint(truths, nearestbranchpoint, cord,
					slope, intercept);

			closestskel.add(nearestborderpoint);

		}

		return closestskel;

	}

	public ArrayList<Pair<RealLocalizable, RealLocalizable>> SkeletonCreator(Budregionobject PairCurrentViewBit,
			List<RealLocalizable> truths) {

		// Skeletonize Bud
		OpService ops = parent.ij.op();
		List<RealLocalizable> skeletonEndPoints = new ArrayList<RealLocalizable>();
		List<RealLocalizable> skeletonBranchPoints = new ArrayList<RealLocalizable>();
		ArrayList<Pair<RealLocalizable, RealLocalizable>> skelEndsBranches = new ArrayList<Pair<RealLocalizable, RealLocalizable>>();

		SkeletonCreator<BitType> skelmake = new SkeletonCreator<BitType>(PairCurrentViewBit.Interiorimage, ops);
		skelmake.setClosingRadius(2);
		skelmake.run();
		ArrayList<RandomAccessibleInterval<BitType>> Allskeletons = skelmake.getSkeletons();

		skelEndsBranches = AnalyzeSkeleton(Allskeletons, truths, ops);

		return skelEndsBranches;

	}

	public void FillArrays(List<RealLocalizable> skeletonEndPoints, List<RealLocalizable> truths,
			int label) {

		for (RealLocalizable budpoints : skeletonEndPoints) {

			Budpointobject Budpoint = new Budpointobject(skeletonEndPoints.get(0), truths, skeletonEndPoints,
					truths.size() * parent.calibrationX, label,
					new double[] { budpoints.getDoublePosition(0), budpoints.getDoublePosition(1) },
					parent.thirdDimension, 0);

			Budpointlist.add(Budpoint);

		}
		Budobject Curreentbud = new Budobject(skeletonEndPoints.get(0), truths, skeletonEndPoints, parent.thirdDimension, label,
				truths.size() * parent.calibrationX);
		Budlist.add(Curreentbud);
		if (parent.SegYelloworiginalimg != null) {
			celllist = GetNearest.getAllInteriorCells(parent, parent.CurrentViewInt, parent.CurrentViewYellowInt);

			// check over this point later
			// ArrayList<Cellobject> budcelllist = GetNearest.getLabelInteriorCells(parent,
			// CurrentViewInt, celllist, Curreentbud, label);
			for (Cellobject currentbudcell : celllist) {

				// Make TM spot
				double[] calibration = { parent.calibrationX, parent.calibrationY, parent.calibrationZ };
				final double x = calibration[0] * (currentbudcell.Location.getDoublePosition(0));
				final double y = calibration[1] * (currentbudcell.Location.getDoublePosition(1));
				final double z = calibration[2] * (currentbudcell.Location.getDoublePosition(2));

				double[] point = { x, y, z };
				RealPoint location = new RealPoint(point);

				double radius = 0;
				for (int i = 0; i < currentbudcell.extents.length; ++i)
					radius *= currentbudcell.extents[i] * calibration[i];

				radius = radius / 8;
				final double quality = currentbudcell.cellVolume;

				// Make the Spot
				Spot budncell = new Spot(location, radius, quality);
				parent.budcells.add(budncell, parent.thirdDimension);
			}

		}

	}

	public static ArrayList<Pair<RealLocalizable, RealLocalizable>> AnalyzeSkeleton(
			ArrayList<RandomAccessibleInterval<BitType>> Allskeletons, List<RealLocalizable> truths, OpService ops) {

		ArrayList<RealLocalizable> endPoints = new ArrayList<RealLocalizable>();
		ArrayList<RealLocalizable> branchPoints = new ArrayList<RealLocalizable>();
		ArrayList<RealLocalizable> whitePoints = new ArrayList<RealLocalizable>();
		ArrayList<RealLocalizable> copywhitePoints = new ArrayList<RealLocalizable>();
		ArrayList<Pair<RealLocalizable, RealLocalizable>> graphPairs = new ArrayList<Pair<RealLocalizable, RealLocalizable>>();

		for (RandomAccessibleInterval<BitType> skeleton : Allskeletons) {

			SkeletonAnalyzer<BitType> skelanalyze = new SkeletonAnalyzer<BitType>(skeleton, ops);
			RandomAccessibleInterval<BitType> Ends = skelanalyze.getEndpoints();
			RandomAccessibleInterval<BitType> Branches = skelanalyze.getBranchpoints();

			Cursor<BitType> skeletoncursor = Views.iterable(skeleton).localizingCursor();

			while (skeletoncursor.hasNext()) {

				skeletoncursor.next();
				RealPoint whitePoint = new RealPoint(skeletoncursor);
				if (skeletoncursor.get().getInteger() > 0) {

					whitePoints.add(whitePoint);
				}

			}

			Cursor<BitType> skelcursor = Views.iterable(Ends).localizingCursor();
			while (skelcursor.hasNext()) {

				skelcursor.next();

				RealPoint addPoint = new RealPoint(skelcursor);
				if (skelcursor.get().getInteger() > 0) {

					endPoints.add(addPoint);

				}

			}

			Cursor<BitType> skelbranchcursor = Views.iterable(Branches).localizingCursor();
			while (skelbranchcursor.hasNext()) {

				skelbranchcursor.next();

				RealPoint addPoint = new RealPoint(skelbranchcursor);
				if (skelbranchcursor.get().getInteger() > 0) {

					branchPoints.add(addPoint);

				}

			}

			for (RealLocalizable point : endPoints) {

				copywhitePoints = whitePoints;
				double minDistance = Double.MAX_VALUE;
				RealLocalizable pointA = point;
				RealLocalizable pointB = null;

				while (minDistance > 0) {

					copywhitePoints.remove(point);

					RealLocalizable nearest = budDetector.Listordering.getNextNearest(point, copywhitePoints);

					point = nearest;

					for (RealLocalizable branchpoint : branchPoints) {
						double distance = Distance.DistanceSqrt(nearest, branchpoint);

						if (distance <= minDistance) {

							minDistance = distance;

							pointB = branchpoint;

						}

					}

				}

				graphPairs.add(new ValuePair<RealLocalizable, RealLocalizable>(pointA, pointB));

			}

		}

		return graphPairs;

	}

	public static ArrayList<RealLocalizable> RemoveClose(List<RealLocalizable> newcurrentskel, double mindist) {

		ArrayList<RealLocalizable> farPoints = new ArrayList<RealLocalizable>(newcurrentskel);

		for (RealLocalizable addPoint : newcurrentskel) {

			farPoints.remove(addPoint);

			RealLocalizable closestdynamicskel = GetNearest.getNearestskelPoint(farPoints, addPoint);

			farPoints.add(addPoint);

			double closestGrowthPoint = Distance.DistanceSqrt(addPoint, closestdynamicskel);
			if (closestGrowthPoint < mindist && closestGrowthPoint != 0)
				farPoints.remove(closestdynamicskel);

		}

		return farPoints;

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

			cursor.get().setReal((Math.sqrt(gradient)));

		}

		return gradientimg;
	}

	public static Budregionobject BudCurrentLabelBinaryImage(RandomAccessibleInterval<IntType> Intimg,
			int currentLabel) {
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
		// Gradient image gives us the bondary points
		RandomAccessibleInterval<BitType> gradimg = GradientmagnitudeImage(outimg);

		Budregionobject region = new Budregionobject(gradimg, outimg, min, size);
		return region;

	}

	public static Pair<Budregionobject, Budregionobject> BudCurrentLabelBinaryImage3D(
			RandomAccessibleInterval<IntType> Intimg, int currentLabel) {
		int n = Intimg.numDimensions();
		long[] position = new long[n];
		Cursor<IntType> intCursor = Views.iterable(Intimg).cursor();

		RandomAccessibleInterval<BitType> outimg = new ArrayImgFactory<BitType>().create(Intimg, new BitType());
		RandomAccess<BitType> imageRA = outimg.randomAccess();

		// Go through the whole image and add every pixel, that belongs to
		// the currently processed label
		long[] minVal = { Intimg.max(0), Intimg.max(1), Intimg.max(2) };
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

		RandomAccessibleInterval<BitType> smalloutimg = Views.interval(outimg, minVal, maxVal);
		Point min = new Point(minVal.length);
		// Gradient image gives us the bondary points
		RandomAccessibleInterval<BitType> smallgradimg = GradientmagnitudeImage(smalloutimg);
		RandomAccessibleInterval<BitType> gradimg = GradientmagnitudeImage(outimg);
		Budregionobject smallregion = new Budregionobject(smallgradimg, smalloutimg, min, size);
		Budregionobject region = new Budregionobject(gradimg, outimg, min, size);
		return new ValuePair<Budregionobject, Budregionobject>(smallregion, region);

	}

	public static Budregionobject YellowCurrentLabelBinaryImage(RandomAccessibleInterval<IntType> Intimg,
			int currentLabel) {
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
		for (int d = 0; d < n; ++d) {

			minVal[d] = minVal[d] - 10;
			maxVal[d] = maxVal[d] + 10;

		}

		Point min = new Point(minVal);
		outimg = Views.offsetInterval(outimg, minVal, maxVal);
		// Gradient image gives us the bondary points
		RandomAccessibleInterval<BitType> gradimg = GradientmagnitudeImage(outimg);

		Budregionobject region = new Budregionobject(gradimg, outimg, min, size);
		return region;

	}

}
