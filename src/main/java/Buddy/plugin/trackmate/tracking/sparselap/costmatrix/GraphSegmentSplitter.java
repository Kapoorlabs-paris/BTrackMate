package Buddy.plugin.trackmate.tracking.sparselap.costmatrix;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.graph.DefaultWeightedEdge;

import budDetector.BCellobject;

public class GraphSegmentSplitter
{
	private final List< BCellobject > segmentStarts;

	private final List< BCellobject > segmentEnds;

	private final List< List< BCellobject >> segmentMiddles;

	public GraphSegmentSplitter( final Graph< BCellobject, DefaultWeightedEdge > graph, final boolean findMiddlePoints )
	{
		final ConnectivityInspector< BCellobject, DefaultWeightedEdge > connectivity = new ConnectivityInspector< >( graph );
		final List< Set< BCellobject >> connectedSets = connectivity.connectedSets();
		final Comparator< BCellobject > framecomparator = BCellobject.frameComparator;

		segmentStarts = new ArrayList< >( connectedSets.size() );
		segmentEnds = new ArrayList< >( connectedSets.size() );
		if ( findMiddlePoints )
		{
			segmentMiddles = new ArrayList< >( connectedSets.size() );
		}
		else
		{
			segmentMiddles = Collections.emptyList();
		}

		for ( final Set< BCellobject > set : connectedSets )
		{
			if ( set.size() < 2 )
			{
				continue;
			}

			final List< BCellobject > list = new ArrayList< >( set );
			Collections.sort( list, framecomparator );

			segmentEnds.add( list.remove( list.size() - 1 ) );
			segmentStarts.add( list.remove( 0 ) );
			if ( findMiddlePoints )
			{
				segmentMiddles.add( list );
			}
		}
	}

	public List< BCellobject > getSegmentEnds()
	{
		return segmentEnds;
	}

	public List< List< BCellobject >> getSegmentMiddles()
	{
		return segmentMiddles;
	}

	public List< BCellobject > getSegmentStarts()
	{
		return segmentStarts;
	}

}
