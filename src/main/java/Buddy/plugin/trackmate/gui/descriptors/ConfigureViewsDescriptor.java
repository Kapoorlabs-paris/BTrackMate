package Buddy.plugin.trackmate.gui.descriptors;

import Buddy.plugin.trackmate.TrackMate;
import Buddy.plugin.trackmate.gui.TrackMateGUIController;
import Buddy.plugin.trackmate.gui.panels.ConfigureViewsPanel;
import Buddy.plugin.trackmate.visualization.FeatureColorGenerator;
import Buddy.plugin.trackmate.visualization.ManualBCellobjectColorGenerator;
import Buddy.plugin.trackmate.visualization.ManualEdgeColorGenerator;
import Buddy.plugin.trackmate.visualization.PerEdgeFeatureColorGenerator;
import Buddy.plugin.trackmate.visualization.PerTrackFeatureColorGenerator;
import budDetector.BCellobject;

public class ConfigureViewsDescriptor implements WizardPanelDescriptor
{

	public static final String KEY = "ConfigureViews";

	private final ConfigureViewsPanel panel;

	private final TrackMateGUIController controller;

	public ConfigureViewsDescriptor( final TrackMate trackmate, final FeatureColorGenerator< BCellobject > BCellobjectColorGenerator, final PerEdgeFeatureColorGenerator edgeColorGenerator, final PerTrackFeatureColorGenerator trackColorGenerator, final FeatureColorGenerator< BCellobject > BCellobjectColorGeneratorPerTrackFeature, final ManualBCellobjectColorGenerator manualBCellobjectColorGenerator, final ManualEdgeColorGenerator manualEdgeColorGenerator, final TrackMateGUIController controller )
	{
		this.controller = controller;
		this.panel = new ConfigureViewsPanel( trackmate.getModel() );
		panel.setBCellobjectColorGenerator( BCellobjectColorGenerator );
		panel.setEdgeColorGenerator( edgeColorGenerator );
		panel.setTrackColorGenerator( trackColorGenerator );
		panel.setManualBCellobjectColorGenerator( manualBCellobjectColorGenerator );
		panel.setManualEdgeColorGenerator( manualEdgeColorGenerator );
		panel.setBCellobjectColorGeneratorPerTrackFeature( BCellobjectColorGeneratorPerTrackFeature );
	}

	@Override
	public ConfigureViewsPanel getComponent()
	{
		return panel;
	}

	@Override
	public void aboutToDisplayPanel()
	{
		panel.refreshGUI();
		controller.getGUI().setNextButtonEnabled( true );
	}

	@Override
	public void displayingPanel()
	{
		panel.refreshColorFeatures();
	}

	@Override
	public void aboutToHidePanel()
	{}

	@Override
	public void comingBackToPanel()
	{
		panel.refreshGUI();
		panel.refreshColorFeatures();
	}

	@Override
	public String getKey()
	{
		return KEY;
	}
}
