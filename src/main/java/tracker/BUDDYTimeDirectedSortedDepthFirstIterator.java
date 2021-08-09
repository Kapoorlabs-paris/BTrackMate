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

import budDetector.Budpointobject;

public class BUDDYTimeDirectedSortedDepthFirstIterator
		extends BUDDYSortedDepthFirstIterator<Budpointobject, DefaultWeightedEdge> {

	public BUDDYTimeDirectedSortedDepthFirstIterator(final Graph<Budpointobject, DefaultWeightedEdge> g,
			final Budpointobject startVertex, final Comparator<Budpointobject> comparator) {
		super(g, startVertex, comparator);
	}

	@Override
	protected void addUnseenChildrenOf(final Budpointobject vertex) {

		// Retrieve target vertices, and sort them in a list
		final List<Budpointobject> sortedChildren = new ArrayList<Budpointobject>();
		// Keep a map of matching edges so that we can retrieve them in the same order
		final Map<Budpointobject, DefaultWeightedEdge> localEdges = new HashMap<Budpointobject, DefaultWeightedEdge>();

		final int ts = vertex.getFeature(Budpointobject.POSITION_T).intValue();
		for (final DefaultWeightedEdge edge : specifics.edgesOf(vertex)) {

			final Budpointobject oppositeV = Graphs.getOppositeVertex(graph, edge, vertex);
			final int tt = oppositeV.getFeature(Budpointobject.POSITION_T).intValue();
			if (tt <= ts) {
				continue;
			}

			if (!seen.containsKey(oppositeV)) {
				sortedChildren.add(oppositeV);
			}
			localEdges.put(oppositeV, edge);
		}

		Collections.sort(sortedChildren, Collections.reverseOrder(comparator));
		final Iterator<Budpointobject> it = sortedChildren.iterator();
		while (it.hasNext()) {
			final Budpointobject child = it.next();

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
