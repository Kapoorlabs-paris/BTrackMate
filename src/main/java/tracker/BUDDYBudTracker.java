package tracker;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import budDetector.Budobject;
import budDetector.Budpointobject;
import net.imglib2.algorithm.OutputAlgorithm;

public interface BUDDYBudTracker extends OutputAlgorithm<SimpleWeightedGraph<Budobject, DefaultWeightedEdge>> {

}