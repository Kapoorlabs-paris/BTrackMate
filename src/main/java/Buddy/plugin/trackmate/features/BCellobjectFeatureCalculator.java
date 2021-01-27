package Buddy.plugin.trackmate.features;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import net.imagej.ImgPlus;
import net.imglib2.algorithm.MultiThreadedBenchmarkAlgorithm;
import net.imglib2.multithreading.SimpleMultiThreading;
import Buddy.plugin.trackmate.Dimension;
import Buddy.plugin.trackmate.Logger;
import Buddy.plugin.trackmate.Model;
import Buddy.plugin.trackmate.Settings;
import Buddy.plugin.trackmate.features.BCellobject.BCellobjectAnalyzer;
import Buddy.plugin.trackmate.features.BCellobject.BCellobjectAnalyzerFactory;
import Buddy.plugin.trackmate.features.BCellobject.IndependentBCellobjectFeatureAnalyzer;
import Buddy.plugin.trackmate.BCellobjectCollection;
import Buddy.plugin.trackmate.util.TMUtils;
import budDetector.BCellobject;

/**
 * A class dedicated to centralizing the calculation of the numerical features
 * of BCellobjects, through {@link BCellobjectAnalyzer}s.
 * 
 * @author Jean-Yves Tinevez - 2013
 * 
 */
@SuppressWarnings( "deprecation" )
public class BCellobjectFeatureCalculator extends MultiThreadedBenchmarkAlgorithm
{

	private static final String BASE_ERROR_MSG = "[BCellobjectFeatureCalculator] ";

	private final Settings settings;

	private final Model model;

	public BCellobjectFeatureCalculator( final Model model, final Settings settings )
	{
		this.settings = settings;
		this.model = model;
	}

	/*
	 * METHODS
	 */

	@Override
	public boolean checkInput()
	{
		if ( null == model )
		{
			errorMessage = BASE_ERROR_MSG + "Model object is null.";
			return false;
		}
		if ( null == settings )
		{
			errorMessage = BASE_ERROR_MSG + "Settings object is null.";
			return false;
		}
		return true;
	}

	/**
	 * Calculates the BCellobject features configured in the {@link Settings} for all
	 * the BCellobjects of this model,
	 * <p>
	 * Features are calculated for each BCellobject, using their location, and the raw
	 * image. Since a {@link BCellobjectAnalyzer} can compute more than a feature at
	 * once, BCellobjects might received more data than required.
	 */
	@Override
	public boolean process()
	{
		final long start = System.currentTimeMillis();

		// Declare what you do.
		for ( final BCellobjectAnalyzerFactory< ? > factory : settings.getBCellobjectAnalyzerFactories() )
		{
			final Collection< String > features = factory.getFeatures();
			final Map< String, String > featureNames = factory.getFeatureNames();
			final Map< String, String > featureShortNames = factory.getFeatureShortNames();
			final Map< String, Dimension > featureDimensions = factory.getFeatureDimensions();
			final Map< String, Boolean > isIntFeature = factory.getIsIntFeature();
			model.getFeatureModel().declareBCellobjectFeatures( features, featureNames, featureShortNames, featureDimensions, isIntFeature );
		}

		// Do it.
		computeBCellobjectFeaturesAgent( model.getBCellobjects(), settings.getBCellobjectAnalyzerFactories(), true );

		final long end = System.currentTimeMillis();
		processingTime = end - start;
		return true;
	}

	/**
	 * Calculates all the BCellobject features configured in the {@link Settings}
	 * object for the specified BCellobject collection. Features are calculated for
	 * each BCellobject, using their location, and the raw image.
	 */
	public void computeBCellobjectFeatures( final BCellobjectCollection toCompute, final boolean doLogIt )
	{
		final List< BCellobjectAnalyzerFactory< ? >> BCellobjectFeatureAnalyzers = settings.getBCellobjectAnalyzerFactories();
		computeBCellobjectFeaturesAgent( toCompute, BCellobjectFeatureAnalyzers, doLogIt );
	}

	/**
	 * The method in charge of computing BCellobject features with the given
	 * {@link BCellobjectAnalyzer}s, for the given {@link BCellobjectCollection}.
	 * 
	 * @param toCompute
	 */
	private void computeBCellobjectFeaturesAgent( final BCellobjectCollection toCompute, final List< BCellobjectAnalyzerFactory< ? >> analyzerFactories, final boolean doLogIt )
	{

		final Logger logger;
		if ( doLogIt )
		{
			logger = model.getLogger();
		}
		else
		{
			logger = Logger.VOID_LOGGER;
		}

		// Can't compute any BCellobject feature without an image to compute on.
		if ( settings.imp == null )
			return;

		// Do it.
		final List< Integer > frameSet = new ArrayList<>( toCompute.keySet() );
		final int numFrames = frameSet.size();

		final AtomicInteger ai = new AtomicInteger( 0 );
		final AtomicInteger progress = new AtomicInteger( 0 );
		final Thread[] threads = SimpleMultiThreading.newThreads( numThreads );

		int tc = 0;
		if ( settings != null && settings.detectorSettings != null )
		{
			// Try to extract it from detector settings target channel
			final Map< String, Object > ds = settings.detectorSettings;
			final Object obj = ds.get( 1 );
			if ( null != obj && obj instanceof Integer )
			{
				tc = ( ( Integer ) obj ) - 1;
			}
		}
		final int targetChannel = tc;

		@SuppressWarnings( "rawtypes" )
		final ImgPlus img = TMUtils.rawWraps( settings.imp );

		// Prepare the thread array
		for ( int ithread = 0; ithread < threads.length; ithread++ )
		{

			threads[ ithread ] = new Thread( "TrackMate BCellobject feature calculating thread " + ( 1 + ithread ) + "/" + threads.length )
			{

				@Override
				public void run()
				{

					for ( int index = ai.getAndIncrement(); index < numFrames; index = ai.getAndIncrement() )
					{

						final int frame = frameSet.get( index );
						for ( final BCellobjectAnalyzerFactory< ? > factory : analyzerFactories )
						{
							@SuppressWarnings( "unchecked" )
							final BCellobjectAnalyzer< ? > analyzer = factory.getAnalyzer( model, img, frame, targetChannel );
							if ( analyzer instanceof IndependentBCellobjectFeatureAnalyzer )
							{
								// Independent: we can process only the BCellobject to update.
								@SuppressWarnings( "rawtypes" )
								final IndependentBCellobjectFeatureAnalyzer analyzer2 = ( IndependentBCellobjectFeatureAnalyzer ) analyzer;
								for ( final BCellobject BCellobject : toCompute.iterable( frame, false ) )
								{
									analyzer2.process( BCellobject );
								}
							}
							else
							{
								// Process all BCellobjects of the frame at once.
								analyzer.process();
							}

						}

						logger.setProgress( progress.incrementAndGet() / ( float ) numFrames );
					} // Finished looping over frames
				}
			};
		}
		logger.setStatus( "Calculating " + toCompute.getNBCellobjects( ) + " BCellobjects features..." );
		logger.setProgress( 0 );

		SimpleMultiThreading.startAndJoin( threads );

		logger.setProgress( 1 );
		logger.setStatus( "" );
	}

}
