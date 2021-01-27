package Buddy.plugin.trackmate.tracking;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import Buddy.plugin.trackmate.Logger;
import budDetector.BCellobject;
import net.imglib2.algorithm.MultiThreaded;
import net.imglib2.algorithm.OutputAlgorithm;

/**
 * This interface should be used when creating algorithms for linking objects
 * across multiple frames in time-lapse images.
 * <p>
 * A BCellobjectTracker algorithm is simply expected to <b>create</b> a new
 * {@link SimpleWeightedGraph} from the BCellobject collection help in the
 * {@link Buddy.plugin.trackmate.Model} that is given to it. We use a simple
 * weighted graph:
 * <ul>
 * <li>Though the weights themselves are not used for subsequent steps, it is
 * suggested to use edge weight to report the cost of a link.
 * <li>The graph is undirected, however, some link direction can be retrieved
 * later on using the {@link BCellobject#FRAME} feature. The {@link BCellobjectTracker}
 * implementation does not have to deal with this; only undirected edges are
 * created.
 * <li>Several links between two BCellobjects are not permitted.
 * <li>A link with the same BCellobject for source and target is not allowed.
 * <li>A link with the source BCellobject and the target BCellobject in the same frame is not
 * allowed. This must be enforced by implementations.
 * </ul>
 * <p>
 * A {@link BCellobjectTracker} implements {@link MultiThreaded}. If concrete
 * implementations are not multithreaded, they can safely ignore the associated
 * methods.
 */
public interface BCellobjectTracker extends OutputAlgorithm< SimpleWeightedGraph< BCellobject, DefaultWeightedEdge > >, MultiThreaded
{
	/**
	 * Sets the {@link Logger} instance that will receive messages from this
	 * {@link BCellobjectTracker}.
	 *
	 * @param logger
	 *            the logger to echo messages to.
	 */
	public void setLogger( final Logger logger );
}
