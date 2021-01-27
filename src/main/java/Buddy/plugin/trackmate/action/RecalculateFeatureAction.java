package Buddy.plugin.trackmate.action;

import java.util.List;

import javax.swing.ImageIcon;

import org.scijava.plugin.Plugin;

import Buddy.plugin.trackmate.Logger;
import Buddy.plugin.trackmate.Model;
import Buddy.plugin.trackmate.Settings;
import Buddy.plugin.trackmate.TrackMate;
import Buddy.plugin.trackmate.features.edges.EdgeAnalyzer;
import Buddy.plugin.trackmate.features.BCellobject.BCellobjectAnalyzerFactory;
import Buddy.plugin.trackmate.features.track.TrackAnalyzer;
import Buddy.plugin.trackmate.gui.TrackMateGUIController;
import Buddy.plugin.trackmate.gui.TrackMateWizard;
import Buddy.plugin.trackmate.providers.EdgeAnalyzerProvider;
import Buddy.plugin.trackmate.providers.BCellobjectAnalyzerProvider;
import Buddy.plugin.trackmate.providers.TrackAnalyzerProvider;

public class RecalculateFeatureAction extends AbstractTMAction
{

	public static final ImageIcon ICON = new ImageIcon( TrackMateWizard.class.getResource( "images/calculator.png" ) );

	public static final String NAME = "Recompute all features";

	public static final String KEY = "RECOMPUTE_FEATURES";

	public static final String INFO_TEXT = "<html>" +
			"Calling this action causes the model to recompute all the features <br>" +
			"for all BCellobjects, edges and tracks. The feature analyzers currently declared "
			+ "in TrackMate session are also calculated if not present in the data. " +
			"</html>";

	private final TrackMateGUIController controller;

	public RecalculateFeatureAction( final TrackMateGUIController controller )
	{
		this.controller = controller;
	}

	public RecalculateFeatureAction()
	{
		this( null );
	}

	@Override
	public void execute( final TrackMate trackmate )
	{
		logger.log( "Recalculating all features.\n" );
		final Model model = trackmate.getModel();
		final Logger oldLogger = model.getLogger();
		model.setLogger( logger );

		if ( null != controller )
		{
			final Settings settings = trackmate.getSettings();

			/*
			 * Configure settings object with BCellobject, edge and track analyzers as
			 * specified in the providers.
			 */

			logger.log( "Registering BCellobject analyzers:\n" );
			settings.clearBCellobjectAnalyzerFactories();
			final BCellobjectAnalyzerProvider BCellobjectAnalyzerProvider = controller.getBCellobjectAnalyzerProvider();
			final List< String > BCellobjectAnalyzerKeys = BCellobjectAnalyzerProvider.getKeys();
			for ( final String key : BCellobjectAnalyzerKeys )
			{
				final BCellobjectAnalyzerFactory< ? > BCellobjectFeatureAnalyzer = BCellobjectAnalyzerProvider.getFactory( key );
				settings.addBCellobjectAnalyzerFactory( BCellobjectFeatureAnalyzer );
				logger.log( " - " + key + "\n" );
			}

			logger.log( "Registering edge analyzers:\n" );
			settings.clearEdgeAnalyzers();
			final EdgeAnalyzerProvider edgeAnalyzerProvider = controller.getEdgeAnalyzerProvider();
			final List< String > edgeAnalyzerKeys = edgeAnalyzerProvider.getKeys();
			for ( final String key : edgeAnalyzerKeys )
			{
				final EdgeAnalyzer edgeAnalyzer = edgeAnalyzerProvider.getFactory( key );
				settings.addEdgeAnalyzer( edgeAnalyzer );
				logger.log( " - " + key + "\n" );
			}

			logger.log( "Registering track analyzers:\n" );
			settings.clearTrackAnalyzers();
			final TrackAnalyzerProvider trackAnalyzerProvider = controller.getTrackAnalyzerProvider();
			final List< String > trackAnalyzerKeys = trackAnalyzerProvider.getKeys();
			for ( final String key : trackAnalyzerKeys )
			{
				final TrackAnalyzer trackAnalyzer = trackAnalyzerProvider.getFactory( key );
				settings.addTrackAnalyzer( trackAnalyzer );
				logger.log( " - " + key + "\n" );
			}
		}

		trackmate.computeBCellobjectFeatures( true );
		trackmate.computeEdgeFeatures( true );
		trackmate.computeTrackFeatures( true );

		model.setLogger( oldLogger );
		logger.log( "Done.\n" );
	}

	@Plugin( type = TrackMateActionFactory.class )
	public static class Factory implements TrackMateActionFactory
	{

		@Override
		public String getInfoText()
		{
			return INFO_TEXT;
		}

		@Override
		public String getName()
		{
			return NAME;
		}

		@Override
		public String getKey()
		{
			return KEY;
		}

		@Override
		public ImageIcon getIcon()
		{
			return ICON;
		}

		@Override
		public TrackMateAction create( final TrackMateGUIController controller )
		{
			return new RecalculateFeatureAction( controller );
		}
	}
}
