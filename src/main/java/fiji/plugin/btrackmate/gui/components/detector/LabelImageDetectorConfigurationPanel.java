package fiji.plugin.btrackmate.gui.components.detector;

import static fiji.plugin.btrackmate.detection.DetectorKeys.KEY_TARGET_CHANNEL;

import java.util.Map;

import fiji.plugin.btrackmate.Model;
import fiji.plugin.btrackmate.Settings;
import fiji.plugin.btrackmate.detection.LabeImageDetectorFactory;
import fiji.plugin.btrackmate.detection.SpotDetectorFactory;
import fiji.plugin.btrackmate.detection.ThresholdDetectorFactory;

/**
 * Configuration panel for spot detectors based on label images.
 * 
 * @author Jean-Yves Tinevez, 2021
 */
public class LabelImageDetectorConfigurationPanel extends ThresholdDetectorConfigurationPanel
{

	private static final long serialVersionUID = 1L;

	/*
	 * CONSTRUCTOR
	 */

	public LabelImageDetectorConfigurationPanel(
			final Settings settings,
			final Model model )
	{
		super( settings, model, LabeImageDetectorFactory.INFO_TEXT, LabeImageDetectorFactory.NAME );
		ftfIntensityThreshold.setVisible( false );
		btnAutoThreshold.setVisible( false );
		lblIntensityThreshold.setVisible( false );
	}

	@Override
	public Map< String, Object > getSettings()
	{
		final Map< String, Object > lSettings = super.getSettings();
		lSettings.remove( ThresholdDetectorFactory.KEY_INTENSITY_THRESHOLD );
		return lSettings;
	}

	@Override
	public void setSettings( final Map< String, Object > settings )
	{
		sliderChannel.setValue( ( Integer ) settings.get( KEY_TARGET_CHANNEL ) );
		chkboxSimplify.setSelected( ( Boolean ) settings.get( ThresholdDetectorFactory.KEY_SIMPLIFY_CONTOURS ) );
	}

	/**
	 * Returns a new instance of the {@link SpotDetectorFactory} that this
	 * configuration panels configures. The new instance will in turn be used
	 * for the preview mechanism. Therefore, classes extending this class are
	 * advised to return a suitable implementation of the factory.
	 * 
	 * @return a new {@link SpotDetectorFactory}.
	 */
	@Override
	@SuppressWarnings( "rawtypes" )
	protected SpotDetectorFactory< ? > getDetectorFactory()
	{
		return new LabeImageDetectorFactory();
	}
}
