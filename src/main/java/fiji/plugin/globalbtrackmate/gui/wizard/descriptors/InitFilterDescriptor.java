package fiji.plugin.globalbtrackmate.gui.wizard.descriptors;

import java.util.function.Function;

import fiji.plugin.globalbtrackmate.Logger;
import fiji.plugin.globalbtrackmate.Spot;
import fiji.plugin.globalbtrackmate.TrackMate;
import fiji.plugin.globalbtrackmate.features.FeatureFilter;
import fiji.plugin.globalbtrackmate.features.FeatureUtils;
import fiji.plugin.globalbtrackmate.gui.components.InitFilterPanel;
import fiji.plugin.globalbtrackmate.gui.displaysettings.DisplaySettings.TrackMateObject;
import fiji.plugin.globalbtrackmate.gui.wizard.WizardPanelDescriptor;

public class InitFilterDescriptor extends WizardPanelDescriptor {

	public static final String KEY = "InitialFiltering";

	private final TrackMate btrackmate;

	public InitFilterDescriptor(final TrackMate btrackmate, final FeatureFilter filter) {
		super(KEY);
		this.btrackmate = btrackmate;
		final Function<String, double[]> valuesCollector = key -> FeatureUtils.collectFeatureValues(Spot.QUALITY,
				TrackMateObject.SPOTS, btrackmate.getModel(), btrackmate.getSettings(), false);
		this.targetPanel = new InitFilterPanel(filter, valuesCollector);
	}

	@Override
	public Runnable getForwardRunnable() {
		return new Runnable() {

			@Override
			public void run() {
				btrackmate.getModel().getLogger().log("\nComputing spot quality histogram...\n", Logger.BLUE_COLOR);
				final InitFilterPanel component = (InitFilterPanel) targetPanel;
				component.refresh();
			}
		};
	}

	@Override
	public void aboutToHidePanel() {
		final InitFilterPanel component = (InitFilterPanel) targetPanel;
		btrackmate.getSettings().initialSpotFilterValue = component.getFeatureThreshold().value;
	}
}
