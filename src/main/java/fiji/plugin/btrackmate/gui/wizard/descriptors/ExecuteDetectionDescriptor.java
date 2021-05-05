package fiji.plugin.btrackmate.gui.wizard.descriptors;

import org.scijava.Cancelable;

import fiji.plugin.btrackmate.TrackMate;
import fiji.plugin.btrackmate.gui.components.LogPanel;
import fiji.plugin.btrackmate.gui.wizard.WizardPanelDescriptor;
import fiji.plugin.btrackmate.util.TMUtils;

public class ExecuteDetectionDescriptor extends WizardPanelDescriptor
{

	public static final String KEY = "ExecuteDetection";

	private final TrackMate btrackmate;
	

	public ExecuteDetectionDescriptor( final TrackMate btrackmate, final LogPanel logPanel )
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
			btrackmate.execDetection();
			final long end = System.currentTimeMillis();
			btrackmate.getModel().getLogger().log( String.format( "Detection done in %.1f s.\n", ( end - start ) / 1e3f ) );
		};
		
	}

	@Override
	public Runnable getBackwardRunnable()
	{
		return () -> btrackmate.getModel().clearSpots( true );
	}
	@Override
	public Cancelable getCancelable()
	{
		return btrackmate;
	}
}
