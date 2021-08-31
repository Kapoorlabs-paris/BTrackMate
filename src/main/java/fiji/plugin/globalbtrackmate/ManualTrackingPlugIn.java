package fiji.plugin.globalbtrackmate;

import fiji.plugin.globalbtrackmate.detection.ManualDetectorFactory;
import fiji.plugin.globalbtrackmate.gui.displaysettings.DisplaySettings;
import fiji.plugin.globalbtrackmate.gui.wizard.WizardSequence;
import fiji.plugin.globalbtrackmate.gui.wizard.descriptors.ConfigureViewsDescriptor;
import fiji.plugin.globalbtrackmate.tracking.ManualTrackerFactory;
import ij.ImageJ;
import ij.ImagePlus;
import pluginTools.InteractiveBud;

public class ManualTrackingPlugIn extends TrackMatePlugIn {

	@Override
	protected WizardSequence createSequence(final TrackMate trackmate, final SelectionModel selectionModel,
			final DisplaySettings displaySettings, final Boolean secondrun) {
		final WizardSequence sequence = super.createSequence(trackmate, selectionModel, displaySettings, secondrun);
		sequence.setCurrent(ConfigureViewsDescriptor.KEY);
		return sequence;
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected Settings createSettings(final ImagePlus imp) {
		final Settings lSettings = super.createSettings(imp);
		// Manual detection
		lSettings.detectorFactory = new ManualDetectorFactory();
		lSettings.detectorSettings = lSettings.detectorFactory.getDefaultSettings();
		// Manual tracker
		lSettings.trackerFactory = new ManualTrackerFactory();
		lSettings.trackerSettings = lSettings.trackerFactory.getDefaultSettings();
		return lSettings;
	}

	public static void main(final String[] args) {
		ImageJ.main(args);
		new ManualTrackingPlugIn().run("samples/Merged.tif");
	}
}