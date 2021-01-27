package Buddy.plugin.trackmate.gui.descriptors;

import Buddy.plugin.trackmate.TrackMate;
import Buddy.plugin.trackmate.gui.GrapherPanel;
import Buddy.plugin.trackmate.gui.TrackMateGUIController;

import java.awt.Component;

public class GrapherDescriptor implements WizardPanelDescriptor
{

	private static final String KEY = "GraphFeatures";

	private final GrapherPanel panel;

	private final TrackMateGUIController controller;

	public GrapherDescriptor( final TrackMate trackmate, final TrackMateGUIController controller )
	{
		this.panel = new GrapherPanel( trackmate );
		this.controller = controller;
	}

	@Override
	public Component getComponent()
	{
		return panel;
	}

	@Override
	public void aboutToDisplayPanel()
	{
		panel.refresh();
	}

	@Override
	public void displayingPanel()
	{
		controller.getGUI().setNextButtonEnabled( true );
	}

	@Override
	public void aboutToHidePanel()
	{}

	@Override
	public void comingBackToPanel()
	{}

	@Override
	public String getKey()
	{
		return KEY;
	}
}
