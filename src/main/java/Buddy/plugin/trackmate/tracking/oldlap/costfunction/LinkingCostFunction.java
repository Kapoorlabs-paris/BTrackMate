package Buddy.plugin.trackmate.tracking.oldlap.costfunction;

import static Buddy.plugin.trackmate.tracking.TrackerKeys.KEY_BLOCKING_VALUE;
import static Buddy.plugin.trackmate.tracking.TrackerKeys.KEY_LINKING_FEATURE_PENALTIES;
import static Buddy.plugin.trackmate.tracking.TrackerKeys.KEY_LINKING_MAX_DISTANCE;

import java.util.List;
import java.util.Map;

import Jama.Matrix;
import budDetector.BCellobject;
import Buddy.plugin.trackmate.tracking.LAPUtils;

/**
 * <p>
 * Linking cost function used with
 * {@link Buddy.plugin.trackmate.tracking.oldlap.LAPTracker}.
 * 
 * <p>
 * The <b>cost function</b> is determined by the default equation in the
 * TrackMate trackmate, see below.
 * <p>
 * It slightly differs from the Jaqaman article, see equation (3) in the paper.
 * 
 * @see LAPUtils#computeLinkingCostFor(BCellobject, BCellobject, double, double,
 *      java.util.Map)
 * 
 * @author Nicholas Perry
 * @author Jean-Yves Tinevez
 *
 */
public class LinkingCostFunction implements CostFunctions
{

	protected final double maxDist;

	protected final Map< String, Double > featurePenalties;

	protected final double blockingValue;

	@SuppressWarnings( "unchecked" )
	public LinkingCostFunction( final Map< String, Object > settings )
	{
		this.maxDist = ( Double ) settings.get( KEY_LINKING_MAX_DISTANCE );
		this.featurePenalties = ( Map< String, Double > ) settings.get( KEY_LINKING_FEATURE_PENALTIES );
		this.blockingValue = ( Double ) settings.get( KEY_BLOCKING_VALUE );
	}

	@Override
	public Matrix getCostFunction( final List< BCellobject > t0, final List< BCellobject > t1 )
	{
		BCellobject s0 = null; // BCellobject in t0
		BCellobject s1 = null; // BCellobject in t1
		final Matrix m = new Matrix( t0.size(), t1.size() );

		for ( int i = 0; i < t0.size(); i++ )
		{

			s0 = t0.get( i );

			for ( int j = 0; j < t1.size(); j++ )
			{

				s1 = t1.get( j );
				final double cost = LAPUtils.computeLinkingCostFor( s0, s1, maxDist, blockingValue, featurePenalties );
				m.set( i, j, cost );
			}
		}

		return m;
	}

}
