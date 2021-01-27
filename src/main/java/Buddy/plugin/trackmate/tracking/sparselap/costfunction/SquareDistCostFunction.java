package Buddy.plugin.trackmate.tracking.sparselap.costfunction;

import budDetector.BCellobject;

/**
 * A cost function that returns cost equal to the square distance. Suited to
 * Brownian motion.
 *
 * @author Jean-Yves Tinevez - 2014
 *
 */
public class SquareDistCostFunction implements CostFunction< BCellobject, BCellobject >
{

	@Override
	public double linkingCost( final BCellobject source, final BCellobject target )
	{
		final double d2 = source.squareDistanceTo( target );
		return ( d2 == 0 ) ? Double.MIN_NORMAL : d2;
	}

}
