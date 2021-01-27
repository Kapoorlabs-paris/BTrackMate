package Buddy.plugin.trackmate.gui.descriptors;

import java.util.Map;

import Buddy.plugin.trackmate.Logger;
import Buddy.plugin.trackmate.TrackMate;
import Buddy.plugin.trackmate.gui.ConfigurationPanel;
import Buddy.plugin.trackmate.gui.TrackMateGUIController;
import Buddy.plugin.trackmate.providers.TrackerProvider;
import Buddy.plugin.trackmate.tracking.BCellobjectTrackerFactory;

public class TrackerConfigurationDescriptor implements WizardPanelDescriptor
{

	private static final String KEY = "ConfigureTracker";

	private final TrackMate trackmate;

	private ConfigurationPanel configPanel;

	private final TrackMateGUIController controller;

	/**
	 * @param trackerProvider
	 */
	public TrackerConfigurationDescriptor( final TrackerProvider trackerProvider, final TrackMate trackmate, final TrackMateGUIController controller )
	{
		this.trackmate = trackmate;
		this.controller = controller;
	}

	/*
	 * METHODS
	 */

	/**
	 * Regenerate the config panel to reflect current settings stored in the
	 * trackmate.
	 */
	private void updateComponent()
	{
		final BCellobjectTrackerFactory trackerFactory = trackmate.getSettings().trackerFactory;
		// Regenerate panel
		configPanel = trackerFactory.getTrackerConfigurationPanel( trackmate.getModel() );
		Map< String, Object > settings = trackmate.getSettings().trackerSettings;
		// Bulletproof null
		if ( null == settings || !trackerFactory.checkSettingsValidity( settings ) )
		{
			settings = trackerFactory.getDefaultSettings();
			trackmate.getSettings().trackerSettings = settings;
		}
		configPanel.setSettings( settings );
	}

	@Override
	public ConfigurationPanel getComponent()
	{
		if ( null == configPanel )
		{
			updateComponent();
		}
		return configPanel;
	}

	@Override
	public void aboutToDisplayPanel()
	{
		updateComponent();
	}

	@Override
	public void displayingPanel()
	{
		if ( null == configPanel )
		{
			// happens after loading.
			aboutToDisplayPanel();
		}
		controller.getGUI().setNextButtonEnabled( true );
	}

	@Override
	public void aboutToHidePanel()
	{
		final BCellobjectTrackerFactory trackerFactory = trackmate.getSettings().trackerFactory;
		Map< String, Object > settings = configPanel.getSettings();
		final boolean settingsOk = trackerFactory.checkSettingsValidity( settings );
		if ( !settingsOk )
		{
			final Logger logger = trackmate.getModel().getLogger();
			logger.error( "Config panel returned bad settings map:\n" + trackerFactory.getErrorMessage() + "Using defaults settings.\n" );
			settings = trackerFactory.getDefaultSettings();
		}
		trackmate.getSettings().trackerSettings = settings;
	}

	@Override
	public void comingBackToPanel()
	{}

	@Override
	public String getKey()
	{
		return KEY;
	}
}
