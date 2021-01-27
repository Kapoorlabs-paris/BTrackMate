package Buddy.plugin.trackmate.visualization;

import Buddy.plugin.trackmate.Model;
import budDetector.BCellobject;

import java.awt.Color;

public class BCellobjectColorGeneratorPerTrackFeature implements FeatureColorGenerator< BCellobject >
{

	private final PerTrackFeatureColorGenerator trackColorGenerator;

	private final Model model;

	public BCellobjectColorGeneratorPerTrackFeature( final Model model, final String feature )
	{
		this.model = model;
		this.trackColorGenerator = new PerTrackFeatureColorGenerator( model, feature );
	}

	@Override
	public Color color( final BCellobject BCellobject )
	{
		final Integer trackID = model.getTrackModel().trackIDOf( BCellobject );
		if ( null == trackID )
			return TrackMateModelView.DEFAULT_BCellobject_COLOR;

		return trackColorGenerator.colorOf( trackID );
	}

	@Override
	public void setFeature( final String feature )
	{
		trackColorGenerator.setFeature( feature );
	}

	@Override
	public String getFeature()
	{
		return trackColorGenerator.getFeature();
	}

	@Override
	public void terminate()
	{
		trackColorGenerator.terminate();
	}

	@Override
	public void activate()
	{
		trackColorGenerator.activate();
	}

	/*
	 * MINMAXADJUSTABLE
	 */

	@Override
	public double getMin()
	{
		return trackColorGenerator.getMin();
	}

	@Override
	public double getMax()
	{
		return trackColorGenerator.getMax();
	}

	@Override
	public void setMinMax( final double min, final double max )
	{
		trackColorGenerator.setMinMax( min, max );
	}

	@Override
	public void autoMinMax()
	{
		trackColorGenerator.autoMinMax();
	}

	@Override
	public void setAutoMinMaxMode( final boolean autoMode )
	{
		trackColorGenerator.setAutoMinMaxMode( autoMode );
	}

	@Override
	public boolean isAutoMinMaxMode()
	{
		return trackColorGenerator.isAutoMinMaxMode();
	}

	@Override
	public void setFrom( final MinMaxAdjustable minMaxAdjustable )
	{
		setAutoMinMaxMode( minMaxAdjustable.isAutoMinMaxMode() );
		if ( !minMaxAdjustable.isAutoMinMaxMode() )
		{
			setMinMax( minMaxAdjustable.getMin(), minMaxAdjustable.getMax() );
		}
	}
}
