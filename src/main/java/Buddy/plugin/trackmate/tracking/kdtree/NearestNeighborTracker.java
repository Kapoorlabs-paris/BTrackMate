package Buddy.plugin.trackmate.tracking.kdtree;

import static Buddy.plugin.trackmate.tracking.TrackerKeys.KEY_LINKING_MAX_DISTANCE;
import static Buddy.plugin.trackmate.util.TMUtils.checkMapKeys;
import static Buddy.plugin.trackmate.util.TMUtils.checkParameter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import net.imglib2.KDTree;
import net.imglib2.RealPoint;
import net.imglib2.algorithm.MultiThreadedBenchmarkAlgorithm;
import net.imglib2.multithreading.SimpleMultiThreading;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import Buddy.plugin.trackmate.Logger;
import Buddy.plugin.trackmate.BCellobjectCollection;
import Buddy.plugin.trackmate.tracking.BCellobjectTracker;
import Buddy.plugin.trackmate.util.TMUtils;
import budDetector.BCellobject;

@SuppressWarnings( "deprecation" )
public class NearestNeighborTracker extends MultiThreadedBenchmarkAlgorithm	implements BCellobjectTracker {

	/*
	 * FIELDS
	 */

	protected final BCellobjectCollection BCellobjects;

	protected final Map< String, Object > settings;

	protected Logger logger = Logger.VOID_LOGGER;

	protected SimpleWeightedGraph< BCellobject, DefaultWeightedEdge > graph;

	/*
	 * CONSTRUCTOR
	 */

	public NearestNeighborTracker( final BCellobjectCollection BCellobjects, final Map< String, Object > settings )
	{
		this.BCellobjects = BCellobjects;
		this.settings = settings;
	}

	/*
	 * PUBLIC METHODS
	 */

	@Override
	public boolean checkInput() {
		final StringBuilder errrorHolder = new StringBuilder();
		final boolean ok = checkInput(settings, errrorHolder);
		if (!ok) {
			errorMessage = errrorHolder.toString();
		}
		return ok;
	}

	@Override
	public boolean process() {
		final long start = System.currentTimeMillis();

		reset();

		final double maxLinkingDistance = (Double) settings.get(KEY_LINKING_MAX_DISTANCE);
		final double maxDistSquare = maxLinkingDistance  * maxLinkingDistance;

		final TreeSet<Integer> frames = new TreeSet<>(BCellobjects.keySet());
		final Thread[] threads = new Thread[numThreads];

		// Prepare the thread array
		final AtomicInteger ai = new AtomicInteger(frames.first());
		final AtomicInteger progress = new AtomicInteger(0);
		for (int ithread = 0; ithread < threads.length; ithread++) {

			threads[ithread] = new Thread("Nearest neighbor tracker thread "+(1+ithread)+"/"+threads.length) {

				@Override
				public void run() {

					for (int i = ai.getAndIncrement(); i < frames.last(); i = ai.getAndIncrement()) {

						// Build frame pair
						final int sourceFrame = i;
						final int targetFrame = frames.higher(i);

						final int nTargetBCellobjects = BCellobjects.getNBCellobjects(targetFrame);
						if (nTargetBCellobjects < 1) {
							continue;
						}

						final List<RealPoint> targetCoords = new ArrayList<>(nTargetBCellobjects);
						final List<FlagNode<BCellobject>> targetNodes = new ArrayList<>(nTargetBCellobjects);
						final Iterator<BCellobject> targetIt = BCellobjects.iterator(targetFrame);
						while (targetIt.hasNext()) {
							final double[] coords = new double[3];
							final BCellobject BCellobject = targetIt.next();
							TMUtils.localize(BCellobject, coords);
							targetCoords.add(new RealPoint(coords));
							targetNodes.add(new FlagNode<>(BCellobject));
						}


						final KDTree<FlagNode<BCellobject>> tree = new KDTree<>(targetNodes, targetCoords);
						final NearestNeighborFlagSearchOnKDTree<BCellobject> search = new NearestNeighborFlagSearchOnKDTree<>(tree);

						// For each BCellobject in the source frame, find its nearest neighbor in the target frame
						final Iterator<BCellobject> sourceIt = BCellobjects.iterator(sourceFrame);
						while (sourceIt.hasNext()) {
							final BCellobject source = sourceIt.next();
							final double[] coords = new double[3];
							TMUtils.localize(source, coords);
							final RealPoint sourceCoords = new RealPoint(coords);
							search.search(sourceCoords);

							final double squareDist = search.getSquareDistance();
							final FlagNode<BCellobject> targetNode = search.getSampler().get();

							if (squareDist > maxDistSquare) {
								// The closest we could find is too far. We skip this source BCellobject and do not create a link
								continue;
							}

							// Everything is ok. This mode is free and below max dist. We create a link
							// and mark this node as assigned.

							targetNode.setVisited(true);
							synchronized (graph) {
								final DefaultWeightedEdge edge = graph.addEdge(source, targetNode.getValue());
								graph.setEdgeWeight(edge, squareDist);
							}

						}
						logger.setProgress(progress.incrementAndGet() / (float)frames.size() );

					}
				}
			};


		}

		logger.setStatus("Tracking...");
		logger.setProgress(0);

		SimpleMultiThreading.startAndJoin(threads);

		logger.setProgress(1);
		logger.setStatus("");

		final long end = System.currentTimeMillis();
		processingTime = end - start;
		return true;
	}

	@Override
	public SimpleWeightedGraph<BCellobject, DefaultWeightedEdge> getResult() {
		return graph;
	}

	public void reset() {
		graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
		final Iterator<BCellobject> it = BCellobjects.iterator(true);
		while (it.hasNext()) {
			graph.addVertex(it.next());
		}
	}

	public static boolean checkInput(final Map<String, Object> settings, final StringBuilder errrorHolder) {
		boolean ok = checkParameter(settings, KEY_LINKING_MAX_DISTANCE, Double.class, errrorHolder);
		final List<String> mandatoryKeys = new ArrayList<>();
		mandatoryKeys.add(KEY_LINKING_MAX_DISTANCE);
		ok = ok & checkMapKeys(settings, mandatoryKeys, null, errrorHolder);
		return ok;
	}

	@Override
	public void setLogger(final Logger logger) {
		this.logger = logger;
	}
}
