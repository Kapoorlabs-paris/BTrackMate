package tracker;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;

import budDetector.Budobject;




public class BUDDYBudTimeDirectedDepthFirstIterator extends BUDDYSortedDepthFirstIterator<Budobject, DefaultWeightedEdge> {

	public BUDDYBudTimeDirectedDepthFirstIterator(Graph<Budobject, DefaultWeightedEdge> g, Budobject startVertex) {
		super(g, startVertex, null);
	}
	
	
	
    protected void addUnseenChildrenOf(Budobject vertex) {
    	
    	int ts = vertex.getFeature(Budobject.TIME).intValue();
        for (DefaultWeightedEdge edge : specifics.edgesOf(vertex)) {
            if (nListeners != 0) {
                fireEdgeTraversed(createEdgeTraversalEvent(edge));
            }

            Budobject oppositeV = Graphs.getOppositeVertex(graph, edge, vertex);
            int tt = oppositeV.getFeature(Budobject.TIME).intValue();
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
