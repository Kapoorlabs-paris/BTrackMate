package fiji.plugin.globalbtrackmate.gui.wizard.descriptors;

import java.util.Map;

import fiji.plugin.globalbtrackmate.TrackMate;
import fiji.plugin.globalbtrackmate.detection.LabeImageDetectorFactory;
import fiji.plugin.globalbtrackmate.detection.SpotDetectorFactoryBase;
import fiji.plugin.globalbtrackmate.gui.components.ModuleChooserPanel;
import fiji.plugin.globalbtrackmate.gui.wizard.WizardPanelDescriptor;
import fiji.plugin.globalbtrackmate.providers.DetectorProvider;

public class ChooseDetectorDescriptor extends WizardPanelDescriptor {

	private static final String KEY = "ChooseDetector";

	private final TrackMate btrackmate;

	private final DetectorProvider detectorProvider;

	public ChooseDetectorDescriptor(final DetectorProvider detectorProvider, final TrackMate btrackmate) {
		super(KEY);
		this.btrackmate = btrackmate;
		this.detectorProvider = detectorProvider;
		
		System.out.println(this.btrackmate.getSettings());
		String selectedDetector = LabeImageDetectorFactory.DETECTOR_KEY; // default
		if (null != btrackmate.getSettings().detectorFactory)
			selectedDetector = btrackmate.getSettings().detectorFactory.getKey();

		this.targetPanel = new ModuleChooserPanel<>(detectorProvider, "detector", selectedDetector);
		this.targetPanel.setEnabled(false);
	}

	private void setCurrentChoiceFromPlugin() {
		String key = LabeImageDetectorFactory.DETECTOR_KEY; // back to default
		if (null != btrackmate.getSettings().detectorFactory)
			key = btrackmate.getSettings().detectorFactory.getKey();

		
		System.out.println(key);
		@SuppressWarnings({ "rawtypes", "unchecked" })
		final ModuleChooserPanel<SpotDetectorFactoryBase> component = (fiji.plugin.globalbtrackmate.gui.components.ModuleChooserPanel<SpotDetectorFactoryBase>) targetPanel;
		component.setSelectedModuleKey(key);
	}

	@Override
	public void displayingPanel() {
		setCurrentChoiceFromPlugin();
	}

	@Override
	public void aboutToHidePanel() {
		// Configure the detector provider with choice made in panel
		@SuppressWarnings({ "rawtypes", "unchecked" })
		final ModuleChooserPanel<SpotDetectorFactoryBase> component = (fiji.plugin.globalbtrackmate.gui.components.ModuleChooserPanel<SpotDetectorFactoryBase>) targetPanel;
		final String detectorKey = component.getSelectedModuleKey();

		// Configure btrackmate settings with selected detector
		final SpotDetectorFactoryBase<?> factory = detectorProvider.getFactory(detectorKey);

		if (null == factory) {
			btrackmate.getModel().getLogger().error("[ChooseDetectorDescriptor] Cannot find detector named "
					+ detectorKey + " in current TrackMate modules.");
			return;
		}
		btrackmate.getSettings().detectorFactory = factory;

		/*
		 * Compare current settings with default ones, and substitute default ones only
		 * if the old ones are absent or not compatible with it.
		 */
		System.out.println("before hide" +btrackmate.getSettings().detectorSettings);
		System.out.println(factory.getDefaultSettings());
		
		final Map<String, Object> currentSettings = btrackmate.getSettings().detectorSettings;
		System.out.println(factory.checkSettings(currentSettings));
		
		if (!factory.checkSettings(currentSettings)) {
			final Map<String, Object> defaultSettings = factory.getDefaultSettings();
			btrackmate.getSettings().detectorSettings = defaultSettings;
		}
		
		System.out.println(btrackmate.getSettings().detectorSettings);
	}

	@Override
	public Runnable getBackwardRunnable() {
		// Delete tracks.
		return () -> btrackmate.getModel().clearSpots(true);
	}
}
