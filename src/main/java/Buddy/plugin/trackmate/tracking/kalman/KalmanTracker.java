package Buddy.plugin.trackmate.tracking.kalman;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import Buddy.plugin.trackmate.Logger;
import Buddy.plugin.trackmate.BCellobjectCollection;
import Buddy.plugin.trackmate.tracking.BCellobjectTracker;
import Buddy.plugin.trackmate.tracking.sparselap.costfunction.CostFunction;
import Buddy.plugin.trackmate.tracking.sparselap.costfunction.SquareDistCostFunction;
import Buddy.plugin.trackmate.tracking.sparselap.costmatrix.JaqamanLinkingCostMatrixCreator;
import Buddy.plugin.trackmate.tracking.sparselap.linker.JaqamanLinker;
import budDetector.BCellobject;
import net.imglib2.RealPoint;
import net.imglib2.algorithm.Benchmark;

public class KalmanTracker implements BCellobjectTracker, Benchmark
{

	private static final double ALTERNATIVE_COST_FACTOR = 1.05d;

	private static final double PERCENTILE = 1d;

	private static final String BASE_ERROR_MSG = "[KalmanTracker] ";

	private SimpleWeightedGraph< BCellobject, DefaultWeightedEdge > graph;

	private String errorMessage;

	private Logger logger = Logger.VOID_LOGGER;

	private final BCellobjectCollection BCellobjects;

	private final double maxSearchRadius;

	private final int maxFrameGap;

	private final double initialSearchRadius;

	private boolean savePredictions = false;

	private BCellobjectCollection predictionsCollection;

	private long processingTime;

	/*
	 * CONSTRUCTOR
	 */

	/**
	 * @param BCellobjects
	 *            the BCellobjects to track.
	 * @param maxSearchRadius
	 * @param maxFrameGap
	 * @param initialSearchRadius
	 */
	public KalmanTracker( final BCellobjectCollection BCellobjects, final double maxSearchRadius, final int maxFrameGap, final double initialSearchRadius )
	{
		this.BCellobjects = BCellobjects;
		this.maxSearchRadius = maxSearchRadius;
		this.maxFrameGap = maxFrameGap;
		this.initialSearchRadius = initialSearchRadius;
	}

	/*
	 * PUBLIC METHODS
	 */

	@Override
	public SimpleWeightedGraph< BCellobject, DefaultWeightedEdge > getResult()
	{
		return graph;
	}

	@Override
	public boolean checkInput()
	{
		return true;
	}

	@Override
	public boolean process()
	{
		final long start = System.currentTimeMillis();

		/*
		 * Outputs
		 */

		graph = new SimpleWeightedGraph< >( DefaultWeightedEdge.class );
		predictionsCollection = new BCellobjectCollection();

		/*
		 * Constants.
		 */

		// Max KF search cost.
		final double maxCost = maxSearchRadius * maxSearchRadius;
		// Cost function to nucleate KFs.
		final CostFunction< BCellobject, BCellobject > nucleatingCostFunction = new SquareDistCostFunction();
		// Max cost to nucleate KFs.
		final double maxInitialCost = initialSearchRadius * initialSearchRadius;

		// Find first and second non-empty frames.
		final NavigableSet< Integer > keySet = BCellobjects.keySet();
		final Iterator< Integer > frameIterator = keySet.iterator();

		/*
		 * Initialize. Find first links just based on square distance. We do
		 * this via the orphan BCellobjects lists.
		 */

		// BCellobjects in the PREVIOUS frame that were not part of a link.
		Collection< BCellobject > previousOrphanBCellobjects = new ArrayList<>();
		if ( !frameIterator.hasNext() )
			return true;

		int firstFrame = frameIterator.next();
		while ( true )
		{
			previousOrphanBCellobjects = generateBCellobjectList( BCellobjects, firstFrame );
			if ( !frameIterator.hasNext() )
				return true;
			if ( !previousOrphanBCellobjects.isEmpty() )
				break;

			firstFrame = frameIterator.next();
		}

		/*
		 * BCellobjects in the current frame that are not part of a new link (no
		 * parent).
		 */
		Collection< BCellobject > orphanBCellobjects = new ArrayList<>();
		int secondFrame = frameIterator.next();
		while ( true )
		{
			orphanBCellobjects = generateBCellobjectList( BCellobjects, secondFrame );
			if ( !frameIterator.hasNext() )
				return true;
			if ( !orphanBCellobjects.isEmpty() )
				break;

			secondFrame = frameIterator.next();
		}

		/*
		 * Estimate Kalman filter variances.
		 *
		 * The search radius is used to derive an estimate of the noise that
		 * affects position and velocity. The two are linked: if we need a large
		 * search radius, then the fluctuations over predicted states are
		 * large.
		 */
		final double positionProcessStd = maxSearchRadius / 3d;
		final double velocityProcessStd = maxSearchRadius / 3d;
		/*
		 * We assume the detector did a good job and that positions measured are
		 * accurate up to a fraction of the BCellobject radius
		 */

		double meanBCellobjectRadius = 0d;
		for ( final BCellobject BCellobject : orphanBCellobjects )
			meanBCellobjectRadius += BCellobject.getFeature( BCellobject.Size ).doubleValue();

		meanBCellobjectRadius /= orphanBCellobjects.size();
		final double positionMeasurementStd = meanBCellobjectRadius / 10d;

		// The master map that contains the currently active KFs.
		final Map< CVMKalmanFilter, BCellobject > kalmanFiltersMap = new HashMap< >( orphanBCellobjects.size() );

		/*
		 * Then loop over time, starting from second frame.
		 */
		int p = 1;
		for ( int frame = secondFrame; frame <= keySet.last(); frame++ )
		{
			p++;

			// Use the BCellobject in the next frame has measurements.
			final List< BCellobject > measurements = generateBCellobjectList( BCellobjects, frame );

			/*
			 * Predict for all Kalman filters, and use it to generate linking
			 * candidates.
			 */
			final Map< ComparableRealPoint, CVMKalmanFilter > predictionMap = new HashMap< >( kalmanFiltersMap.size() );
			for ( final CVMKalmanFilter kf : kalmanFiltersMap.keySet() )
			{
				final double[] X = kf.predict();
				final ComparableRealPoint point = new ComparableRealPoint( X );
				predictionMap.put( new ComparableRealPoint( X ), kf );

				if ( savePredictions )
				{
					final BCellobject pred = toBCellobject( point );
					final BCellobject s = kalmanFiltersMap.get( kf );
					pred.setName( "Pred_" + s.getName() );
					pred.putFeature( BCellobject.Size, s.getFeature( BCellobject.Size ) );
					predictionsCollection.add( pred, frame );
				}
			}
			final List< ComparableRealPoint > predictions = new ArrayList< >( predictionMap.keySet() );

			/*
			 * The KF for which we could not find a measurement in the target
			 * frame. Is updated later.
			 */
			final Collection< CVMKalmanFilter > childlessKFs = new HashSet< >( kalmanFiltersMap.keySet() );

			/*
			 * Find the global (in space) optimum for associating a prediction
			 * to a measurement.
			 */

			orphanBCellobjects = new HashSet< >( measurements );
			if ( !predictions.isEmpty() && !measurements.isEmpty() )
			{
				// Only link measurements to predictions if we have predictions.

				final JaqamanLinkingCostMatrixCreator< ComparableRealPoint, BCellobject > crm = new JaqamanLinkingCostMatrixCreator< >(
						predictions,
						measurements,
						CF,
						maxCost,
						ALTERNATIVE_COST_FACTOR,
						PERCENTILE );
				final JaqamanLinker< ComparableRealPoint, BCellobject > linker = new JaqamanLinker< >( crm );
				if ( !linker.checkInput() || !linker.process() )
				{
					errorMessage = BASE_ERROR_MSG + "Error linking candidates in frame " + frame + ": " + linker.getErrorMessage();
					return false;
				}
				final Map< ComparableRealPoint, BCellobject > agnts = linker.getResult();
				final Map< ComparableRealPoint, Double > costs = linker.getAssignmentCosts();

				// Deal with found links.
				for ( final ComparableRealPoint cm : agnts.keySet() )
				{
					final CVMKalmanFilter kf = predictionMap.get( cm );

					// Create links for found match.
					final BCellobject source = kalmanFiltersMap.get( kf );
					final BCellobject target = agnts.get( cm );

					graph.addVertex( source );
					graph.addVertex( target );
					final DefaultWeightedEdge edge = graph.addEdge( source, target );
					final double cost = costs.get( cm );
					graph.setEdgeWeight( edge, cost );

					// Update Kalman filter
					kf.update( toMeasurement( target ) );

					// Update Kalman track BCellobject
					kalmanFiltersMap.put( kf, target );

					// Remove from orphan set
					orphanBCellobjects.remove( target );

					// Remove from childless KF set
					childlessKFs.remove( kf );
				}
			}

			/*
			 * Deal with orphans from the previous frame. (We deal with orphans
			 * from previous frame only now because we want to link in priority
			 * target BCellobjects to predictions. Nucleating new KF from nearest
			 * neighbor only comes second.
			 */
			if ( !previousOrphanBCellobjects.isEmpty() && !orphanBCellobjects.isEmpty() )
			{

				/*
				 * We now deal with orphans of the previous frame. We try to
				 * find them a target from the list of BCellobjects that are not
				 * already part of a link created via KF. That is: the orphan
				 * BCellobjects of this frame.
				 */

				final JaqamanLinkingCostMatrixCreator< BCellobject, BCellobject > ic = new JaqamanLinkingCostMatrixCreator< >(
						previousOrphanBCellobjects,
						orphanBCellobjects,
						nucleatingCostFunction,
						maxInitialCost,
						ALTERNATIVE_COST_FACTOR,
						PERCENTILE );
				final JaqamanLinker< BCellobject, BCellobject > newLinker = new JaqamanLinker< >( ic );
				if ( !newLinker.checkInput() || !newLinker.process() )
				{
					errorMessage = BASE_ERROR_MSG + "Error linking BCellobjects from frame " + ( frame - 1 ) + " to frame " + frame + ": " + newLinker.getErrorMessage();
					return false;
				}
				final Map< BCellobject, BCellobject > newAssignments = newLinker.getResult();
				final Map< BCellobject, Double > assignmentCosts = newLinker.getAssignmentCosts();

				// Build links and new KFs from these links.
				for ( final BCellobject source : newAssignments.keySet() )
				{
					final BCellobject target = newAssignments.get( source );

					// Remove from orphan collection.
					orphanBCellobjects.remove( target );

					// Derive initial state and create Kalman filter.
					final double[] XP = estimateInitialState( source, target );
					final CVMKalmanFilter kt = new CVMKalmanFilter( XP, Double.MIN_NORMAL, positionProcessStd, velocityProcessStd, positionMeasurementStd );
					// We trust the initial state a lot.

					// Store filter and source
					kalmanFiltersMap.put( kt, target );

					// Add edge to the graph.
					graph.addVertex( source );
					graph.addVertex( target );
					final DefaultWeightedEdge edge = graph.addEdge( source, target );
					final double cost = assignmentCosts.get( source );
					graph.setEdgeWeight( edge, cost );
				}
			}
			previousOrphanBCellobjects = orphanBCellobjects;

			// Deal with childless KFs.
			for ( final CVMKalmanFilter kf : childlessKFs )
			{
				// Echo we missed a measurement
				kf.update( null );

				/*
				 * We can bridge a limited number of gaps. If too much, we die.
				 * If not, we will use predicted state next time.
				 */
				if ( kf.getNOcclusion() > maxFrameGap )
					kalmanFiltersMap.remove( kf );
			}

			final double progress = ( double ) p / keySet.size();
			logger.setProgress( progress );
		}

		if ( savePredictions )
			predictionsCollection.setVisible( true );

		final long end = System.currentTimeMillis();
		processingTime = end - start;

		return true;
	}

	@Override
	public String getErrorMessage()
	{
		return errorMessage;
	}

	/**
	 * Returns the saved predicted state as a {@link BCellobjectCollection}.
	 *
	 * @return the predicted states.
	 * @see #setSavePredictions(boolean)
	 */
	public BCellobjectCollection getPredictions()
	{
		return predictionsCollection;
	}

	/**
	 * Sets whether the tracker saves the predicted states.
	 *
	 * @param doSave
	 *            if <code>true</code>, the predicted states will be saved.
	 * @see #getPredictions()
	 */
	public void setSavePredictions( final boolean doSave )
	{
		this.savePredictions = doSave;
	}

	@Override
	public void setNumThreads()
	{}

	@Override
	public void setNumThreads( final int numThreads )
	{}

	@Override
	public int getNumThreads()
	{
		return 1;
	}

	@Override
	public long getProcessingTime()
	{
		return processingTime;
	}

	@Override
	public void setLogger( final Logger logger )
	{
		this.logger = logger;
	}

	private static final double[] toMeasurement( final BCellobject BCellobject )
	{
		final double[] d = new double[] {
				BCellobject.getDoublePosition( 0 ),
				BCellobject.getDoublePosition( 1 ),
				BCellobject.getDoublePosition( 2 )
		};
		return d;
	}

	private static final BCellobject toBCellobject( final ComparableRealPoint X )
	{
		final BCellobject BCellobject = new BCellobject( X);
		return BCellobject;
	}

	private static final double[] estimateInitialState( final BCellobject first, final BCellobject second )
	{
		final double[] xp = new double[] {
				second.getDoublePosition( 0 ),
				second.getDoublePosition( 1 ),
				second.getDoublePosition( 2 ),
				second.diffTo( first, BCellobject.POSITION_X ),
				second.diffTo( first, BCellobject.POSITION_Y ),
				second.diffTo( first, BCellobject.POSITION_Z )
		};
		return xp;
	}

	private static final List< BCellobject > generateBCellobjectList( final BCellobjectCollection BCellobjects, final int frame )
	{
		final List< BCellobject > list = new ArrayList< >( BCellobjects.getNBCellobjects( frame ) );
		for ( final Iterator< BCellobject > iterator = BCellobjects.iterator( frame ); iterator.hasNext(); )
			list.add( iterator.next() );

		return list;
	}

	private static final class ComparableRealPoint extends RealPoint implements Comparable< ComparableRealPoint >
	{
		public ComparableRealPoint( final double[] A )
		{
			// Wrap array.
			super( A, false );
		}

		/**
		 * Sort based on X, Y, Z
		 */
		@Override
		public int compareTo( final ComparableRealPoint o )
		{
			int i = 0;
			while ( i < n )
			{
				if ( getDoublePosition( i ) != o.getDoublePosition( i ) ) { return ( int ) Math.signum( getDoublePosition( i ) - o.getDoublePosition( i ) ); }
				i++;
			}
			return hashCode() - o.hashCode();
		}
	}

	/**
	 * Cost function that returns the square distance between a KF state and a
	 * BCellobjects.
	 */
	private static final CostFunction< ComparableRealPoint, BCellobject > CF = new CostFunction< ComparableRealPoint, BCellobject >()
	{

		@Override
		public double linkingCost( final ComparableRealPoint state, final BCellobject BCellobject )
		{
			final double dx = state.getDoublePosition( 0 ) - BCellobject.getDoublePosition( 0 );
			final double dy = state.getDoublePosition( 1 ) - BCellobject.getDoublePosition( 1 );
			final double dz = state.getDoublePosition( 2 ) - BCellobject.getDoublePosition( 2 );
			return dx * dx + dy * dy + dz * dz + Double.MIN_NORMAL;
			// So that it's never 0
		}
	};

}
