package Buddy.plugin.trackmate.features.edges;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import javax.swing.ImageIcon;

import net.imglib2.multithreading.SimpleMultiThreading;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.scijava.plugin.Plugin;

import Buddy.plugin.trackmate.Dimension;
import Buddy.plugin.trackmate.FeatureModel;
import Buddy.plugin.trackmate.Model;
import budDetector.BCellobject;

@SuppressWarnings( "deprecation" )
@Plugin( type = EdgeAnalyzer.class )
public class EdgeVelocityAnalyzer implements EdgeAnalyzer
{

	public static final String KEY = "Edge velocity";

	/*
	 * FEATURE NAMES
	 */
	public static final String VELOCITY = "VELOCITY";

	public static final String DISPLACEMENT = "DISPLACEMENT";

	public static final List< String > FEATURES = new ArrayList< >( 2 );

	public static final Map< String, String > FEATURE_NAMES = new HashMap< >( 2 );

	public static final Map< String, String > FEATURE_SHORT_NAMES = new HashMap< >( 2 );

	public static final Map< String, Dimension > FEATURE_DIMENSIONS = new HashMap< >( 2 );

	public static final Map< String, Boolean > IS_INT = new HashMap< >( 2 );

	static
	{
		FEATURES.add( VELOCITY );
		FEATURES.add( DISPLACEMENT );

		FEATURE_NAMES.put( VELOCITY, "Velocity" );
		FEATURE_NAMES.put( DISPLACEMENT, "Displacement" );

		FEATURE_SHORT_NAMES.put( VELOCITY, "V" );
		FEATURE_SHORT_NAMES.put( DISPLACEMENT, "D" );

		FEATURE_DIMENSIONS.put( VELOCITY, Dimension.VELOCITY );
		FEATURE_DIMENSIONS.put( DISPLACEMENT, Dimension.LENGTH );

		IS_INT.put( VELOCITY, Boolean.FALSE );
		IS_INT.put( DISPLACEMENT, Boolean.FALSE );
	}

	private int numThreads;

	private long processingTime;

	/*
	 * CONSTRUCTOR
	 */

	public EdgeVelocityAnalyzer()
	{
		setNumThreads();
	}

	@Override
	public boolean isLocal()
	{
		return true;
	}

	@Override
	public void process( final Collection< DefaultWeightedEdge > edges, final Model model )
	{

		if ( edges.isEmpty() ) { return; }

		final FeatureModel featureModel = model.getFeatureModel();

		final ArrayBlockingQueue< DefaultWeightedEdge > queue = new ArrayBlockingQueue< >( edges.size(), false, edges );

		final Thread[] threads = SimpleMultiThreading.newThreads( numThreads );
		for ( int i = 0; i < threads.length; i++ )
		{
			threads[ i ] = new Thread( "EdgeVelocityAnalyzer thread " + i )
			{
				@Override
				public void run()
				{
					DefaultWeightedEdge edge;
					while ( ( edge = queue.poll() ) != null )
					{
						final BCellobject source = model.getTrackModel().getEdgeSource( edge );
						final BCellobject target = model.getTrackModel().getEdgeTarget( edge );

						final double dx = target.diffTo( source, BCellobject.POSITION_X );
						final double dy = target.diffTo( source, BCellobject.POSITION_Y );
						final double dz = target.diffTo( source, BCellobject.POSITION_Z );
						final double dt = target.diffTo( source, BCellobject.POSITION_T );
						final double D = Math.sqrt( dx * dx + dy * dy + dz * dz );
						final double V = D / Math.abs( dt );

						featureModel.putEdgeFeature( edge, VELOCITY, V );
						featureModel.putEdgeFeature( edge, DISPLACEMENT, D );
					}

				}
			};
		}

		final long start = System.currentTimeMillis();
		SimpleMultiThreading.startAndJoin( threads );
		final long end = System.currentTimeMillis();
		processingTime = end - start;
	}

	@Override
	public String getKey()
	{
		return KEY;
	}

	@Override
	public int getNumThreads()
	{
		return numThreads;
	}

	@Override
	public void setNumThreads()
	{
		this.numThreads = Runtime.getRuntime().availableProcessors();
	}

	@Override
	public void setNumThreads( final int numThreads )
	{
		this.numThreads = numThreads;

	}

	@Override
	public long getProcessingTime()
	{
		return processingTime;
	}

	@Override
	public List< String > getFeatures()
	{
		return FEATURES;
	}

	@Override
	public Map< String, String > getFeatureShortNames()
	{
		return FEATURE_SHORT_NAMES;
	}

	@Override
	public Map< String, String > getFeatureNames()
	{
		return FEATURE_NAMES;
	}

	@Override
	public Map< String, Dimension > getFeatureDimensions()
	{
		return FEATURE_DIMENSIONS;
	}

	@Override
	public String getInfoText()
	{
		return null;
	}

	@Override
	public ImageIcon getIcon()
	{
		return null;
	}

	@Override
	public String getName()
	{
		return KEY;
	}

	@Override
	public Map< String, Boolean > getIsIntFeature()
	{
		return IS_INT;
	}

	@Override
	public boolean isManualFeature()
	{
		return false;
	}
}
