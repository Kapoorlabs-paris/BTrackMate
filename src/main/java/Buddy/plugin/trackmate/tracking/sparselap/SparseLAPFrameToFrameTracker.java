package Buddy.plugin.trackmate.tracking.sparselap;

import static Buddy.plugin.trackmate.tracking.LAPUtils.checkFeatureMap;
import static Buddy.plugin.trackmate.tracking.TrackerKeys.KEY_ALTERNATIVE_LINKING_COST_FACTOR;
import static Buddy.plugin.trackmate.tracking.TrackerKeys.KEY_LINKING_FEATURE_PENALTIES;
import static Buddy.plugin.trackmate.tracking.TrackerKeys.KEY_LINKING_MAX_DISTANCE;
import static Buddy.plugin.trackmate.util.TMUtils.checkMapKeys;
import static Buddy.plugin.trackmate.util.TMUtils.checkParameter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import Buddy.plugin.trackmate.Logger;
import Buddy.plugin.trackmate.BCellobjectCollection;
import Buddy.plugin.trackmate.tracking.BCellobjectTracker;
import Buddy.plugin.trackmate.tracking.sparselap.costfunction.CostFunction;
import Buddy.plugin.trackmate.tracking.sparselap.costfunction.FeaturePenaltyCostFunction;
import Buddy.plugin.trackmate.tracking.sparselap.costfunction.SquareDistCostFunction;
import Buddy.plugin.trackmate.tracking.sparselap.costmatrix.JaqamanLinkingCostMatrixCreator;
import Buddy.plugin.trackmate.tracking.sparselap.linker.JaqamanLinker;
import budDetector.BCellobject;
import net.imglib2.algorithm.MultiThreadedBenchmarkAlgorithm;
import net.imglib2.multithreading.SimpleMultiThreading;

@SuppressWarnings( "deprecation" )
public class SparseLAPFrameToFrameTracker extends MultiThreadedBenchmarkAlgorithm implements BCellobjectTracker
{
	private final static String BASE_ERROR_MESSAGE = "[SparseLAPFrameToFrameTracker] ";

	protected SimpleWeightedGraph< BCellobject, DefaultWeightedEdge > graph;

	protected Logger logger = Logger.VOID_LOGGER;

	protected final BCellobjectCollection BCellobjects;

	protected final Map< String, Object > settings;

	/*
	 * CONSTRUCTOR
	 */

	public SparseLAPFrameToFrameTracker( final BCellobjectCollection BCellobjects, final Map< String, Object > settings )
	{
		this.BCellobjects = BCellobjects;
		this.settings = settings;
	}

	/*
	 * METHODS
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
		/*
		 * Check input now.
		 */

		// Check that the objects list itself isn't null
		if ( null == BCellobjects )
		{
			errorMessage = BASE_ERROR_MESSAGE + "The BCellobject collection is null.";
			return false;
		}

		// Check that the objects list contains inner collections.
		if ( BCellobjects.keySet().isEmpty() )
		{
			errorMessage = BASE_ERROR_MESSAGE + "The BCellobject collection is empty.";
			return false;
		}

		// Check that at least one inner collection contains an object.
		boolean empty = true;
		for ( final int frame : BCellobjects.keySet() )
		{
			if ( BCellobjects.getNBCellobjects( frame ) > 0 )
			{
				empty = false;
				break;
			}
		}
		if ( empty )
		{
			errorMessage = BASE_ERROR_MESSAGE + "The BCellobject collection is empty.";
			return false;
		}
		// Check parameters
		final StringBuilder errorHolder = new StringBuilder();
		if ( !checkSettingsValidity( settings, errorHolder ) )
		{
			errorMessage = BASE_ERROR_MESSAGE + errorHolder.toString();
			return false;
		}

		/*
		 * Process.
		 */

		final long start = System.currentTimeMillis();

		// Prepare frame pairs in order, not necessarily separated by 1.
		final ArrayList< int[] > framePairs = new ArrayList<>( BCellobjects.keySet().size() - 1 );
		final Iterator< Integer > frameIterator = BCellobjects.keySet().iterator();
		int frame0 = frameIterator.next();
		int frame1;
		while ( frameIterator.hasNext() )
		{ // ascending order
			frame1 = frameIterator.next();
			framePairs.add( new int[] { frame0, frame1 } );
			frame0 = frame1;
		}

		// Prepare cost function
		@SuppressWarnings( "unchecked" )
		final Map< String, Double > featurePenalties = ( Map< String, Double > ) settings.get( KEY_LINKING_FEATURE_PENALTIES );
		final CostFunction< BCellobject, BCellobject > costFunction = getCostFunction( featurePenalties );
		final Double maxDist = ( Double ) settings.get( KEY_LINKING_MAX_DISTANCE );
		final double costThreshold = maxDist * maxDist;
		final double alternativeCostFactor = ( Double ) settings.get( KEY_ALTERNATIVE_LINKING_COST_FACTOR );

		// Instantiate graph
		graph = new SimpleWeightedGraph<>( DefaultWeightedEdge.class );

		// Prepare threads
		final Thread[] threads = SimpleMultiThreading.newThreads( numThreads );

		// Prepare the thread array
		final AtomicInteger ai = new AtomicInteger( 0 );
		final AtomicInteger progress = new AtomicInteger( 0 );
		final AtomicBoolean ok = new AtomicBoolean( true );
		for ( int ithread = 0; ithread < threads.length; ithread++ )
		{
			threads[ ithread ] = new Thread( BASE_ERROR_MESSAGE + " thread " + ( 1 + ithread ) + "/" + threads.length )
			{
				@Override
				public void run()
				{
					for ( int i = ai.getAndIncrement(); i < framePairs.size(); i = ai.getAndIncrement() )
					{
						if ( !ok.get() )
						{
							break;
						}

						// Get frame pairs
						final int lFrame0 = framePairs.get( i )[ 0 ];
						final int lFrame1 = framePairs.get( i )[ 1 ];

						// Get BCellobjects - we have to create a list from each
						// content.
						final List< BCellobject > sources = new ArrayList<>( BCellobjects.getNBCellobjects( lFrame0 ) );
						for ( final Iterator< BCellobject > iterator = BCellobjects.iterator( lFrame0 ); iterator.hasNext(); )
							sources.add( iterator.next() );

						final List< BCellobject > targets = new ArrayList<>( BCellobjects.getNBCellobjects( lFrame1 ) );
						for ( final Iterator< BCellobject > iterator = BCellobjects.iterator( lFrame1 ); iterator.hasNext(); )
							targets.add( iterator.next() );

						if ( sources.isEmpty() || targets.isEmpty() )
							continue;

						/*
						 * Run the linker.
						 */

						final JaqamanLinkingCostMatrixCreator< BCellobject, BCellobject > creator = new JaqamanLinkingCostMatrixCreator<>( sources, targets, costFunction, costThreshold, alternativeCostFactor, 1d );
						final JaqamanLinker< BCellobject, BCellobject > linker = new JaqamanLinker<>( creator );
						if ( !linker.checkInput() || !linker.process() )
						{
							errorMessage = "At frame " + lFrame0 + " to " + lFrame1 + ": " + linker.getErrorMessage();
							ok.set( false );
							return;
						}

						/*
						 * Update graph.
						 */

						synchronized ( graph )
						{
							final Map< BCellobject, Double > costs = linker.getAssignmentCosts();
							final Map< BCellobject, BCellobject > assignment = linker.getResult();
							for ( final BCellobject source : assignment.keySet() )
							{
								final double cost = costs.get( source );
								final BCellobject target = assignment.get( source );
								graph.addVertex( source );
								graph.addVertex( target );
								final DefaultWeightedEdge edge = graph.addEdge( source, target );
								graph.setEdgeWeight( edge, cost );
							}
						}

						logger.setProgress( progress.incrementAndGet() / framePairs.size() );

					}
				}
			};
		}

		logger.setStatus( "Frame to frame linking..." );
		SimpleMultiThreading.startAndJoin( threads );
		logger.setProgress( 1d );
		logger.setStatus( "" );

		final long end = System.currentTimeMillis();
		processingTime = end - start;

		return ok.get();
	}

	/**
	 * Creates a suitable cost function.
	 *
	 * @param featurePenalties
	 *            feature penalties to base costs on. Can be <code>null</code>.
	 * @return a new {@link CostFunction}
	 */
	protected CostFunction< BCellobject, BCellobject > getCostFunction( final Map< String, Double > featurePenalties )
	{
		if ( null == featurePenalties || featurePenalties.isEmpty() )
			return new SquareDistCostFunction();

		return new FeaturePenaltyCostFunction( featurePenalties );
	}

	@Override
	public void setLogger( final Logger logger )
	{
		this.logger = logger;
	}

	protected boolean checkSettingsValidity( final Map< String, Object > settings, final StringBuilder str )
	{
		if ( null == settings )
		{
			str.append( "Settings map is null.\n" );
			return false;
		}

		boolean ok = true;
		// Linking
		ok = ok & checkParameter( settings, KEY_LINKING_MAX_DISTANCE, Double.class, str );
		ok = ok & checkFeatureMap( settings, KEY_LINKING_FEATURE_PENALTIES, str );
		// Others
		ok = ok & checkParameter( settings, KEY_ALTERNATIVE_LINKING_COST_FACTOR, Double.class, str );

		// Check keys
		final List< String > mandatoryKeys = new ArrayList<>();
		mandatoryKeys.add( KEY_LINKING_MAX_DISTANCE );
		mandatoryKeys.add( KEY_ALTERNATIVE_LINKING_COST_FACTOR );
		final List< String > optionalKeys = new ArrayList<>();
		optionalKeys.add( KEY_LINKING_FEATURE_PENALTIES );
		ok = ok & checkMapKeys( settings, mandatoryKeys, optionalKeys, str );

		return ok;
	}
}
