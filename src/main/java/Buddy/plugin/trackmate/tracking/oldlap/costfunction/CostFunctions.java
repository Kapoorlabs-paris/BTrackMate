package Buddy.plugin.trackmate.tracking.oldlap.costfunction;

import Jama.Matrix;
import budDetector.BCellobject;

import java.util.List;

/**
 * Interface for cost function classes, which take a {@link Matrix} and
 * fill in values according to the cost function.
 * 
 * @author Nicholas Perry
 *
 */
public interface CostFunctions {

	/** 
	 * Return a cost matrix using the information given at construction for
	 * the two BCellobject lists given here.
	 * <p>
	 * We need to use a list, for the matrix index will reflect the BCellobject position
	 * in the lists.
	 */
	public Matrix getCostFunction(final List<BCellobject> t0, final List<BCellobject> t1);
	
}
