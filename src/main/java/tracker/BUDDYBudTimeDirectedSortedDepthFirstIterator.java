package tracker;

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

import budDetector.Budobject;






public class BUDDYBudTimeDirectedSortedDepthFirstIterator extends BUDDYSortedDepthFirstIterator<Budobject, DefaultWeightedEdge> {

	public BUDDYBudTimeDirectedSortedDepthFirstIterator(final Graph<Budobject, DefaultWeightedEdge> g, final Budobject startVertex, final Comparator<Budobject> comparator) {
		super(g, startVertex, comparator);
	}



    @Override
	protected void addUnseenChildrenOf(final Budobject vertex) {

		// Retrieve target vertices, and sort them in a list
		final List< Budobject > sortedChildren = new ArrayList< Budobject >();
    	// Keep a map of matching edges so that we can retrieve them in the same order
    	final Map<Budobject, DefaultWeightedEdge> localEdges = new HashMap<Budobject, DefaultWeightedEdge>();

    	final int ts = vertex.getFeature(Budobject.TIME).intValue();
        for (final DefaultWeightedEdge edge : specifics.edgesOf(vertex)) {

        	final Budobject oppositeV = Graphs.getOppositeVertex(graph, edge, vertex);
        	final int tt = oppositeV.getFeature(Budobject.TIME).intValue();
        	if (tt <= ts) {
        		continue;
        	}

        	if (!seen.containsKey(oppositeV)) {
        		sortedChildren.add(oppositeV);
        	}
        	localEdges.put(oppositeV, edge);
        }

		Collections.sort( sortedChildren, Collections.reverseOrder( comparator ) );
		final Iterator< Budobject > it = sortedChildren.iterator();
        while (it.hasNext()) {
			final Budobject child = it.next();

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
