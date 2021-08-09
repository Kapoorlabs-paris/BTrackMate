package fiji.plugin.btrackmate.gui.wizard.descriptors;

import fiji.plugin.btrackmate.TrackMate;
import fiji.plugin.btrackmate.gui.components.GrapherPanel;
import fiji.plugin.btrackmate.gui.displaysettings.DisplaySettings;
import fiji.plugin.btrackmate.gui.wizard.WizardPanelDescriptor;

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
