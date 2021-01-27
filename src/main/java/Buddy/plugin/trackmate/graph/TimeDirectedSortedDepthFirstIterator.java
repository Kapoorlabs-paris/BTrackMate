/**
 *
 */
package Buddy.plugin.trackmate.graph;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;

import budDetector.BCellobject;

public class TimeDirectedSortedDepthFirstIterator extends SortedDepthFirstIterator<BCellobject, DefaultWeightedEdge> {

	public TimeDirectedSortedDepthFirstIterator(final Graph<BCellobject, DefaultWeightedEdge> g, final BCellobject startVertex, final Comparator<BCellobject> comparator) {
		super(g, startVertex, comparator);
	}

    @Override
	protected void addUnseenChildrenOf(final BCellobject vertex) {

		// Retrieve target vertices, and sort them in a list
		final List< BCellobject > sortedChildren = new ArrayList< >();
    	// Keep a map of matching edges so that we can retrieve them in the same order
    	final Map<BCellobject, DefaultWeightedEdge> localEdges = new HashMap<>();

    	final int ts = vertex.getFeature(BCellobject.POSITION_T).intValue();
        for (final DefaultWeightedEdge edge : specifics.edgesOf(vertex)) {

        	final BCellobject oppositeV = Graphs.getOppositeVertex(graph, edge, vertex);
        	final int tt = oppositeV.getFeature(BCellobject.POSITION_T).intValue();
        	if (tt <= ts) {
        		continue;
        	}

        	if (!seen.containsKey(oppositeV)) {
        		sortedChildren.add(oppositeV);
        	}
        	localEdges.put(oppositeV, edge);
        }

		Collections.sort( sortedChildren, Collections.reverseOrder( comparator ) );
		final Iterator< BCellobject > it = sortedChildren.iterator();
        while (it.hasNext()) {
			final BCellobject child = it.next();

            if (nListeners != 0) {
                fireEdgeTraversed(createEdgeTraversalEvent(localEdges.get(child)));
            }

            if (seen.containsKey(child)) {
                encounterVertexAgain(child, localEdges.get(child));
            } else {
                encounterVertex(child, localEdges.get(child));
            }
        }
    }



}
