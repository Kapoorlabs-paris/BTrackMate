package fiji.plugin.globalbtrackmate.gui.wizard.descriptors;

import javax.swing.Action;

import fiji.plugin.globalbtrackmate.gui.components.ConfigureViewsPanel;
import fiji.plugin.globalbtrackmate.gui.components.FeatureDisplaySelector;
import fiji.plugin.globalbtrackmate.gui.displaysettings.DisplaySettings;
import fiji.plugin.globalbtrackmate.gui.wizard.WizardPanelDescriptor;

public class ConfigureViewsDescriptor extends WizardPanelDescriptor {

	public static final String KEY = "ConfigureViews";

	public ConfigureViewsDescriptor(final DisplaySettings ds, final FeatureDisplaySelector featureSelector,
			final Action launchTrackSchemeAction, final Action showTrackTablesAction, final Action showSpotTableAction,
			final String spaceUnits) {
		super(KEY);
		this.targetPanel = new ConfigureViewsPanel(ds, featureSelector, spaceUnits, launchTrackSchemeAction,
				showTrackTablesAction, showSpotTableAction);
	}
}
