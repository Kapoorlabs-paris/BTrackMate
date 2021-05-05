package fiji.plugin.btrackmate.gui.wizard.descriptors;

import javax.swing.Action;

import fiji.plugin.btrackmate.gui.components.ConfigureViewsPanel;
import fiji.plugin.btrackmate.gui.components.FeatureDisplaySelector;
import fiji.plugin.btrackmate.gui.displaysettings.DisplaySettings;
import fiji.plugin.btrackmate.gui.wizard.WizardPanelDescriptor;

public class ConfigureViewsDescriptor extends WizardPanelDescriptor
{

	public static final String KEY = "ConfigureViews";

	public ConfigureViewsDescriptor(
			final DisplaySettings ds,
			final FeatureDisplaySelector featureSelector,
			final Action launchTrackSchemeAction,
			final Action showTrackTablesAction,
			final Action showSpotTableAction,
			final String spaceUnits )
	{
		super( KEY );
		this.targetPanel = new ConfigureViewsPanel(
				ds, 
				featureSelector, 
				spaceUnits,
				launchTrackSchemeAction,
				showTrackTablesAction,
				showSpotTableAction );
	}
}
