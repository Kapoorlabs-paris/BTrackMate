package skeleton;

import net.imagej.ops.OpService;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

import java.util.ArrayList;

public class SkeletonAnalyzer<R extends RealType<R>> {

	final RandomAccessibleInterval<BitType> skeleton;
	final OpService opService;

	double totalSkeletonLength;
	long numBranchPoints;
	long numEndPoints;
	private RandomAccessibleInterval<BitType> branchpoints;
	private RandomAccessibleInterval<BitType> endpoints;
	private long longestBranchLength;
	private ArrayList<Long> branchLengths;

	public SkeletonAnalyzer(RandomAccessibleInterval<BitType> skeleton, OpService opService) {
		this.skeleton = skeleton;
		this.opService = opService;

		run();
	}

	private void run() {
		totalSkeletonLength = measureSum(skeleton);

		branchpoints = detectBranchpoints();

		endpoints = detectEndpoints();

		branchLengths = Skeletons.branchLengths(skeleton);
	}

	private RandomAccessibleInterval<BitType> detectBranchpoints() {
		RandomAccessibleInterval<BitType> branchpoints = Skeletons.branchPoints(skeleton);

		numBranchPoints = measureSum(branchpoints);

		return branchpoints;
	}

	private RandomAccessibleInterval<BitType> detectEndpoints() {
		RandomAccessibleInterval<BitType> endpoints = Skeletons.endPoints(skeleton);

		numEndPoints = measureSum(endpoints);

		return endpoints;
	}

	public long getNumBranches() {
		return branchLengths.size();
	}

	public double getAverageBranchLength() {

		if (branchLengths.size() == 0)
			return 0;

		double avg = 0;

		for (long length : branchLengths)
			avg += length;

		avg /= branchLengths.size();

		return avg;
	}

	public long getLongestBranchLength() {
		if (branchLengths.size() == 0)
			return 0;

		return branchLengths.get(0);
	}

	public long getNumBranchPoints() {
		return numBranchPoints;
	}

	public RandomAccessibleInterval<BitType> getBranchpoints() {
		return branchpoints;
	}

	public RandomAccessibleInterval<BitType> getEndpoints() {
		return endpoints;
	}

	public double getTotalSkeletonLength() {
		return totalSkeletonLength;
	}

	public static long measureSum(RandomAccessibleInterval<BitType> rai) {
		final Cursor<BitType> cursor = Views.iterable(rai).cursor();

		long sum = 0;

		while (cursor.hasNext())
			sum += cursor.next().getRealDouble();

		return sum;
	}

}
