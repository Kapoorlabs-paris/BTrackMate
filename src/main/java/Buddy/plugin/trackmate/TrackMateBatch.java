package Buddy.plugin.trackmate;

import static Buddy.plugin.trackmate.tracking.TrackerKeys.KEY_GAP_CLOSING_MAX_DISTANCE;
import static Buddy.plugin.trackmate.tracking.TrackerKeys.KEY_GAP_CLOSING_MAX_FRAME_GAP;
import static Buddy.plugin.trackmate.tracking.TrackerKeys.KEY_LINKING_MAX_DISTANCE;
import Buddy.plugin.trackmate.io.TmXmlReader;
import Buddy.plugin.trackmate.providers.EdgeAnalyzerProvider;
import Buddy.plugin.trackmate.providers.BCellobjectAnalyzerProvider;
import Buddy.plugin.trackmate.providers.TrackAnalyzerProvider;
import Buddy.plugin.trackmate.providers.TrackerProvider;
import Buddy.plugin.trackmate.tracking.BCellobjectTrackerFactory;
import Buddy.plugin.trackmate.tracking.sparselap.SimpleSparseLAPTrackerFactory;
import pluginTools.InteractiveBud;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Map;


public class TrackMateBatch
{

	public static void main( final String[] args )
	{
		final Logger logger = Logger.DEFAULT_LOGGER;

		final File rootFolder = new File( "/Users/tinevez/Projects/JYTinevez/ISBI/GroundTruth/MICROTUBULE" );

		final File exportFolder = new File( "/Users/tinevez/Projects/JYTinevez/ISBI/ISBI_scoring/TrackMate" );

		final FilenameFilter filter = new FilenameFilter()
		{
			@Override
			public boolean accept( final File dir, final String name )
			{
				return name.toLowerCase().startsWith( "microtubule" ) & name.toLowerCase().endsWith( "tm.xml" );
			}
		};

		final String[] trackmateFiles = rootFolder.list( filter );

		final TrackerProvider trackerProvider = new TrackerProvider();
		final EdgeAnalyzerProvider edgeAnalyzerProvider = new EdgeAnalyzerProvider();
		final TrackAnalyzerProvider trackAnalyzerProvider = new TrackAnalyzerProvider();

		for ( final String trackmateFile : trackmateFiles )
		{
			// Open file
			final File path = new File( rootFolder.getAbsolutePath(), trackmateFile );
			logger.log( "\n" + path + '\n' );

			// Check if already done.
			final File exportPath = new File( exportFolder, trackmateFile );
			if ( exportPath.exists() )
			{
				logger.flush();
				logger.log( "Exported file exists. Skipping.\n" );
				continue;
			}


			final TmXmlReader reader = new TmXmlReader( path );
			if ( !reader.isReadingOk() )
			{
				logger.flush();
				logger.error( reader.getErrorMessage() );
				continue;
			}

			// Read model
			final Model model = reader.getModel();
			final InteractiveBud parent = reader.getParent();
			if ( !reader.isReadingOk() )
			{
				logger.flush();
				logger.error( reader.getErrorMessage() );
				continue;
			}
			model.setLogger( logger );

			// Read settings
			final Settings settings = new Settings();
			reader.readSettings( settings, trackerProvider,  edgeAnalyzerProvider, trackAnalyzerProvider );
			if ( !reader.isReadingOk() )
			{
				logger.flush();
				logger.error( reader.getErrorMessage() );
				continue;
			}

			// Edit settings
			final BCellobjectTrackerFactory tf = new SimpleSparseLAPTrackerFactory();
			final Map< String, Object > ts = tf.getDefaultSettings();
			ts.put( KEY_LINKING_MAX_DISTANCE, 10d );
			ts.put( KEY_GAP_CLOSING_MAX_DISTANCE, 15d );
			ts.put( KEY_GAP_CLOSING_MAX_FRAME_GAP, 2 );

			settings.trackerFactory = tf;
			settings.trackerSettings = ts;

			// Re-run TrackMate for the tracking part
			final TrackMate trackmate = new TrackMate(parent, model, settings );
			final boolean trackingOk = trackmate.execTracking();
			if ( !trackingOk )
			{
				logger.flush();
				logger.error( trackmate.getErrorMessage() );
			}

		

		}

		logger.log( "\nDone." );

	}

}
