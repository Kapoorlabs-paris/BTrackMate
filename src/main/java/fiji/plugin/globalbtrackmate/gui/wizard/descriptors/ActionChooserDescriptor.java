package fiji.plugin.globalbtrackmate.gui.wizard.descriptors;

import fiji.plugin.globalbtrackmate.SelectionModel;
import fiji.plugin.globalbtrackmate.TrackMate;
import fiji.plugin.globalbtrackmate.gui.components.ActionChooserPanel;
import fiji.plugin.globalbtrackmate.gui.displaysettings.DisplaySettings;
import fiji.plugin.globalbtrackmate.gui.wizard.WizardPanelDescriptor;
import fiji.plugin.globalbtrackmate.providers.ActionProvider;

public class ActionChooserDescriptor extends WizardPanelDescriptor {

	private static final String KEY = "Actions";

	public ActionChooserDescriptor(final ActionProvider actionProvider, final TrackMate btrackmate,
			final SelectionModel selectionModel, final DisplaySettings displaySettings) {
		super(KEY);
		this.targetPanel = new ActionChooserPanel(actionProvider, btrackmate, selectionModel, displaySettings);
	}
}
