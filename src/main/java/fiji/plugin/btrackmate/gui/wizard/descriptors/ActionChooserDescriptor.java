package fiji.plugin.btrackmate.gui.wizard.descriptors;

import fiji.plugin.btrackmate.SelectionModel;
import fiji.plugin.btrackmate.TrackMate;
import fiji.plugin.btrackmate.gui.components.ActionChooserPanel;
import fiji.plugin.btrackmate.gui.displaysettings.DisplaySettings;
import fiji.plugin.btrackmate.gui.wizard.WizardPanelDescriptor;
import fiji.plugin.btrackmate.providers.ActionProvider;

public class ActionChooserDescriptor extends WizardPanelDescriptor {

	private static final String KEY = "Actions";

	public ActionChooserDescriptor(final ActionProvider actionProvider, final TrackMate btrackmate,
			final SelectionModel selectionModel, final DisplaySettings displaySettings) {
		super(KEY);
		this.targetPanel = new ActionChooserPanel(actionProvider, btrackmate, selectionModel, displaySettings);
	}
}
