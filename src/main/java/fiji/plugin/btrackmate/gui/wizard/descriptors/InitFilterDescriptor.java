package fiji.plugin.btrackmate.gui.wizard.descriptors;

import java.util.function.Function;

import fiji.plugin.btrackmate.Logger;
import fiji.plugin.btrackmate.Spot;
import fiji.plugin.btrackmate.TrackMate;
import fiji.plugin.btrackmate.features.FeatureFilter;
import fiji.plugin.btrackmate.features.FeatureUtils;
import fiji.plugin.btrackmate.gui.components.InitFilterPanel;
import fiji.plugin.btrackmate.gui.displaysettings.DisplaySettings.TrackMateObject;
import fiji.plugin.btrackmate.gui.wizard.WizardPanelDescriptor;

public class InitFilterDescriptor extends WizardPanelDescriptor
{

	public static final String KEY = "InitialFiltering";

	private final TrackMate btrackmate;

	public InitFilterDescriptor( final TrackMate btrackmate, final FeatureFilter filter )
	{
		super( KEY );
		this.btrackmate = btrackmate;
		final Function< String, double[] > valuesCollector = key -> FeatureUtils.collectFeatureValues(
				Spot.QUALITY, TrackMateObject.SPOTS, btrackmate.getModel(), btrackmate.getSettings(), false );
		this.targetPanel = new InitFilterPanel( filter, valuesCollector );
	}

	@Override
	public Runnable getForwardRunnable()
	{
		return new Runnable()
		{

			@Override
			public void run()
			{
				btrackmate.getModel().getLogger().log( "\nComputing spot quality histogram...\n", Logger.BLUE_COLOR );
				final InitFilterPanel component = ( InitFilterPanel ) targetPanel;
				component.refresh();
			}
		};
	}

	@Override
	public void aboutToHidePanel()
	{
		final InitFilterPanel component = ( InitFilterPanel ) targetPanel;
		btrackmate.getSettings().initialSpotFilterValue = component.getFeatureThreshold().value;
	}
}
