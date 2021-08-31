package fiji.plugin.globalbtrackmate.gui.wizard.descriptors;

import fiji.plugin.globalbtrackmate.Logger;
import fiji.plugin.globalbtrackmate.Settings;
import fiji.plugin.globalbtrackmate.gui.components.ConfigurationPanel;
import fiji.plugin.globalbtrackmate.gui.wizard.WizardPanelDescriptor;
import fiji.plugin.globalbtrackmate.util.TMUtils;

public class SpotTrackerDescriptor extends WizardPanelDescriptor {

	public static final String KEY = "ConfigureTracker";

	private final Settings settings;

	private final Logger logger;

	public SpotTrackerDescriptor(final Settings settings, final ConfigurationPanel configurationPanel,
			final Logger logger) {
		super(KEY);
		this.settings = settings;
		this.targetPanel = configurationPanel;
		this.logger = logger;
	}

	@Override
	public void aboutToHidePanel() {
		final ConfigurationPanel configurationPanel = (ConfigurationPanel) targetPanel;
		settings.trackerSettings = configurationPanel.getSettings();

		logger.log("\nConfigured tracker ");
		logger.log(settings.trackerFactory.getName(), Logger.BLUE_COLOR);
		logger.log(" with settings:\n");
		logger.log(TMUtils.echoMap(settings.trackerSettings, 2) + "\n");
	}
}
