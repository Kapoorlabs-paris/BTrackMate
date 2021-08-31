package fiji.plugin.globalbtrackmate.gui.wizard.descriptors;

import fiji.plugin.globalbtrackmate.Logger;
import fiji.plugin.globalbtrackmate.Settings;
import fiji.plugin.globalbtrackmate.gui.components.ConfigurationPanel;
import fiji.plugin.globalbtrackmate.gui.wizard.WizardPanelDescriptor;
import fiji.plugin.globalbtrackmate.util.TMUtils;

public class SpotDetectorDescriptor extends WizardPanelDescriptor {

	public static final String KEY = "ConfigureDetector";

	private final Settings settings;

	private final Logger logger;

	public SpotDetectorDescriptor(final Settings settings, final ConfigurationPanel configurationPanel,
			final Logger logger) {
		super(KEY);
		this.settings = settings;
		this.targetPanel = configurationPanel;
		this.logger = logger;
	}

	@Override
	public void aboutToHidePanel() {
		final ConfigurationPanel configurationPanel = (ConfigurationPanel) targetPanel;
		settings.detectorSettings = configurationPanel.getSettings();
		if (logger != null) {
			logger.log("\nConfigured detector ");
			logger.log(settings.detectorFactory.getName(), Logger.BLUE_COLOR);
			logger.log(" with settings:\n");
			logger.log(TMUtils.echoMap(settings.detectorSettings, 2) + "\n");
		}
	}
}
