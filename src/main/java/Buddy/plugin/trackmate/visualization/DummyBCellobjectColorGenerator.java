package Buddy.plugin.trackmate.visualization;

import static Buddy.plugin.trackmate.visualization.TrackMateModelView.DEFAULT_BCellobject_COLOR;
import Buddy.plugin.trackmate.gui.panels.components.ColorByFeatureGUIPanel;
import budDetector.BCellobject;

import java.awt.Color;

/**
 * A dummy BCellobject color generator that always return the default color.
 *
 * @author Jean-Yves Tinevez - 2013
 */
public class DummyBCellobjectColorGenerator implements FeatureColorGenerator< BCellobject >
{

	@Override
	public Color color( final BCellobject obj )
	{
		return DEFAULT_BCellobject_COLOR;
	}

	@Override
	public void setFeature( final String feature )
	{}

	@Override
	public void terminate()
	{}

	@Override
	public void activate()
	{}

	@Override
	public String getFeature()
	{
		return ColorByFeatureGUIPanel.UNIFORM_KEY;
	}

	@Override
	public double getMin()
	{
		return Double.NaN;
	}

	@Override
	public double getMax()
	{
		return Double.NaN;
	}

	@Override
	public void setMinMax( final double min, final double max )
	{}

	@Override
	public void autoMinMax()
	{}

	@Override
	public void setAutoMinMaxMode( final boolean autoMode )
	{}

	@Override
	public boolean isAutoMinMaxMode()
	{
		return false;
	}

	@Override
	public void setFrom( final MinMaxAdjustable minMaxAdjustable )
	{}

}
