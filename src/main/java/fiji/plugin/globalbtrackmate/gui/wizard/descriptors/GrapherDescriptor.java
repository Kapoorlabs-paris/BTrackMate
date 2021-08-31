package fiji.plugin.globalbtrackmate.gui.wizard.descriptors;

import fiji.plugin.globalbtrackmate.TrackMate;
import fiji.plugin.globalbtrackmate.gui.components.GrapherPanel;
import fiji.plugin.globalbtrackmate.gui.displaysettings.DisplaySettings;
import fiji.plugin.globalbtrackmate.gui.wizard.WizardPanelDescriptor;

public class GrapherDescriptor extends WizardPanelDescriptor {

	private static final String KEY = "GraphFeatures";

	public GrapherDescriptor(final TrackMate btrackmate, final DisplaySettings displaySettings) {
		super(KEY);
		this.targetPanel = new GrapherPanel(btrackmate, displaySettings);
	}

	@Override
	public void aboutToDisplayPanel() {
		final GrapherPanel panel = (GrapherPanel) targetPanel;
		panel.refresh();
	}
}
