package fiji.plugin.btrackmate.gui.wizard.descriptors;

import java.util.IntSummaryStatistics;

import org.scijava.Cancelable;

import fiji.plugin.btrackmate.Logger;
import fiji.plugin.btrackmate.TrackMate;
import fiji.plugin.btrackmate.TrackModel;
import fiji.plugin.btrackmate.gui.components.LogPanel;
import fiji.plugin.btrackmate.gui.wizard.WizardPanelDescriptor;

public class ExecuteTrackingDescriptor extends WizardPanelDescriptor
{

	public static final String KEY = "ExecuteTracking";

	private final TrackMate btrackmate;

	public ExecuteTrackingDescriptor( final TrackMate btrackmate, final LogPanel logPanel )
	{
		super( KEY );
		this.btrackmate = btrackmate;
		this.targetPanel = logPanel;
	}

	@Override
	public Runnable getForwardRunnable()
	{
		return () -> {
			final long start = System.currentTimeMillis();
			btrackmate.execTracking();
			final long end = System.currentTimeMillis();

			final Logger logger = btrackmate.getModel().getLogger();
			logger.log( String.format( "Tracking done in %.1f s.\n", ( end - start ) / 1e3f ) );
			final TrackModel trackModel = btrackmate.getModel().getTrackModel();
			final int nTracks = trackModel.nTracks( false );
			final IntSummaryStatistics stats = trackModel.unsortedTrackIDs( false ).stream()
					.mapToInt( id -> trackModel.trackSpots( id ).size() )
					.summaryStatistics();
			logger.log( "Found " + nTracks + " tracks.\n" );
			logger.log( String.format( "  - avg size: %.1f spots.\n", stats.getAverage() ) );
			logger.log( String.format( "  - min size: %d spots.\n", stats.getMin() ) );
			logger.log( String.format( "  - max size: %d spots.\n", stats.getMax() ) );
		};
	}
	@Override
	public Runnable getBackwardRunnable()
	{
		return () -> btrackmate.getModel().clearTracks( true );
	}
	@Override
	public Cancelable getCancelable()
	{
		return btrackmate;
	}
}
