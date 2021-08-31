package fiji.plugin.globalbtrackmate.gui.wizard.descriptors;

import org.scijava.Cancelable;

import fiji.plugin.globalbtrackmate.TrackMate;
import fiji.plugin.globalbtrackmate.gui.components.LogPanel;
import fiji.plugin.globalbtrackmate.gui.wizard.WizardPanelDescriptor;
import fiji.plugin.globalbtrackmate.util.TMUtils;

public class ExecuteDetectionDescriptor extends WizardPanelDescriptor {

	public static final String KEY = "ExecuteDetection";

	private final TrackMate btrackmate;

	public ExecuteDetectionDescriptor(final TrackMate btrackmate, final LogPanel logPanel) {
		super(KEY);
		this.btrackmate = btrackmate;

		this.targetPanel = logPanel;

	}

	@Override
	public Runnable getForwardRunnable() {

		return () -> {
			final long start = System.currentTimeMillis();
			btrackmate.execDetection();
			final long end = System.currentTimeMillis();
			btrackmate.getModel().getLogger().log(String.format("Detection done in %.1f s.\n", (end - start) / 1e3f));
		};

	}

	@Override
	public Runnable getBackwardRunnable() {
		return () -> btrackmate.getModel().clearSpots(true);
	}

	@Override
	public Cancelable getCancelable() {
		return btrackmate;
	}
}
