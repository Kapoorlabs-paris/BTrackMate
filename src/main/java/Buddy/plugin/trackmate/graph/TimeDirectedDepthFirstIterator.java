/**
 * 
 */
package Buddy.plugin.trackmate.graph;


import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;

import budDetector.BCellobject;

public class TimeDirectedDepthFirstIterator extends SortedDepthFirstIterator<BCellobject, DefaultWeightedEdge> {

	public TimeDirectedDepthFirstIterator(Graph<BCellobject, DefaultWeightedEdge> g, BCellobject startVertex) {
		super(g, startVertex, null);
	}
	
    @Override
	protected void addUnseenChildrenOf(BCellobject vertex) {
    	
    	int ts = vertex.getFeature(BCellobject.POSITION_T).intValue();
        for (DefaultWeightedEdge edge : specifics.edgesOf(vertex)) {
            if (nListeners != 0) {
                fireEdgeTraversed(createEdgeTraversalEvent(edge));
            }

            BCellobject oppositeV = Graphs.getOppositeVertex(graph, edge, vertex);
            int tt = oppositeV.getFeature(BCellobject.POSITION_T).intValue();
            if (tt <= ts) {
            	continue;
            }

            if ( seen.containsKey(oppositeV)) {
                encounterVertexAgain(oppositeV, edge);
            } else {
                encounterVertex(oppositeV, edge);
            }
        }
    }

	
	
}
