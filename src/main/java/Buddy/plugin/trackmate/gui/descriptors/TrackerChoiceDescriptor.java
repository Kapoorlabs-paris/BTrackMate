package Buddy.plugin.trackmate.gui.descriptors;

import Buddy.plugin.trackmate.Logger;
import Buddy.plugin.trackmate.TrackMate;
import Buddy.plugin.trackmate.gui.TrackMateGUIController;
import Buddy.plugin.trackmate.gui.panels.ListChooserPanel;
import Buddy.plugin.trackmate.providers.TrackerProvider;
import Buddy.plugin.trackmate.tracking.BCellobjectTrackerFactory;
import Buddy.plugin.trackmate.tracking.ManualTrackerFactory;
import Buddy.plugin.trackmate.tracking.oldlap.LAPTrackerFactory;
import Buddy.plugin.trackmate.tracking.sparselap.SimpleSparseLAPTrackerFactory;
import Buddy.plugin.trackmate.tracking.sparselap.SparseLAPTrackerFactory;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

public class TrackerChoiceDescriptor implements WizardPanelDescriptor
{

	private static final String KEY = "ChooseTracker";

	private final ListChooserPanel component;

	private final TrackMate trackmate;

	private final TrackerProvider trackerProvider;

	private final TrackMateGUIController controller;

	public TrackerChoiceDescriptor( final TrackerProvider trackerProvider, final TrackMate trackmate, final TrackMateGUIController controller )
	{
		this.trackmate = trackmate;
		this.controller = controller;
		this.trackerProvider = trackerProvider;
		final List< String > keys = trackerProvider.getVisibleKeys();
		final List< String > trackerNames = new ArrayList< >( keys.size() );
		final List< String > infoTexts = new ArrayList< >( keys.size() );
		for ( final String key : keys )
		{
			trackerNames.add( trackerProvider.getFactory( key ).getName() );
			infoTexts.add( trackerProvider.getFactory( key ).getInfoText() );
		}
		this.component = new ListChooserPanel( trackerNames, infoTexts, "tracker" );
		setCurrentChoiceFromPlugin();
	}

	/*
	 * METHODS
	 */

	@Override
	public Component getComponent()
	{
		return component;
	}

	@Override
	public void aboutToDisplayPanel()
	{
		setCurrentChoiceFromPlugin();
	}

	@Override
	public void displayingPanel()
	{
		controller.getGUI().setNextButtonEnabled( true );
	}

	@Override
	public void aboutToHidePanel()
	{

		// Configure the detector provider with choice made in panel
		final int index = component.getChoice();
		final String key = trackerProvider.getVisibleKeys().get( index );
		final BCellobjectTrackerFactory trackerFactory = trackerProvider.getFactory( key );

		// Check
		if ( trackerFactory == null )
		{
			final Logger logger = trackmate.getModel().getLogger();
			logger.error( "Choice panel returned a tracker unkown to this trackmate: " + key + "\n" );
			return;
		}

		trackmate.getSettings().trackerFactory = trackerFactory;

		if ( trackerFactory.getKey().equals( ManualTrackerFactory.TRACKER_KEY ) )
		{
			/*
			 * Compute track and edge features now to ensure they will be
			 * available in the next descriptor.
			 */
			final Thread trackFeatureCalculationThread = new Thread( "TrackMate track feature calculation thread" )
			{
				@Override
				public void run()
				{
					trackmate.computeTrackFeatures( true );
					trackmate.computeEdgeFeatures( true );
				}
			};
			trackFeatureCalculationThread.start();
			try
			{
				trackFeatureCalculationThread.join();
			}
			catch ( final InterruptedException e )
			{
				e.printStackTrace();
			}
		}
	}

	@Override
	public void comingBackToPanel()
	{
		/*
		 * We clear the tracks here. We don't do it at the tracker configuration
		 * panel, because we want the user to be able to visually see the
		 * changes a parameter tuning cause.
		 */
		trackmate.getModel().clearTracks( true );
		controller.getSelectionModel().clearEdgeSelection();
	}

	private void setCurrentChoiceFromPlugin()
	{

		String key;
		if ( null != trackmate.getSettings().trackerFactory )
		{
			key = trackmate.getSettings().trackerFactory.getKey();
		}
		else
		{
			key = SparseLAPTrackerFactory.THIS_TRACKER_KEY;
		}
		final int index = trackerProvider.getVisibleKeys().indexOf( key );

		if ( index < 0 )
		{
			trackmate.getModel().getLogger().error( "[TrackerChoiceDescriptor] Cannot find tracker named " + key + " in Trackmate." );
			return;
		}
		component.setChoice( index );
	}

	@Override
	public String getKey()
	{
		return KEY;
	}

}
