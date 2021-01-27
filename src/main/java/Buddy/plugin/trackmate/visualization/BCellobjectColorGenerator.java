package Buddy.plugin.trackmate.visualization;

import java.awt.Color;
import java.util.Set;

import org.jfree.chart.renderer.InterpolatePaintScale;

import Buddy.plugin.trackmate.Model;
import Buddy.plugin.trackmate.ModelChangeEvent;
import Buddy.plugin.trackmate.ModelChangeListener;
import Buddy.plugin.trackmate.TrackMateOptionUtils;
import budDetector.BCellobject;

public class BCellobjectColorGenerator implements FeatureColorGenerator< BCellobject >, ModelChangeListener
{

	private final Model model;

	private String feature = null;

	private double min;

	private double max;

	private boolean autoMode = true;

	private final InterpolatePaintScale generator;

	public BCellobjectColorGenerator( final Model model )
	{
		this.model = model;
		model.addModelChangeListener( this );
		generator = TrackMateOptionUtils.getOptions().getPaintScale();
	}

	@Override
	public Color color( final BCellobject BCellobject )
	{
		if ( null == feature )
			return TrackMateModelView.DEFAULT_BCellobject_COLOR;

		final Double feat = BCellobject.getFeature( feature );
		if ( null == feat )
			return TrackMateModelView.DEFAULT_UNASSIGNED_FEATURE_COLOR;

		final double val = feat.doubleValue();
		if ( Double.isNaN( val ) )
			return TrackMateModelView.DEFAULT_UNDEFINED_FEATURE_COLOR;

		return generator.getPaint( ( val - min ) / ( max - min ) );
	}

	@Override
	public String getFeature()
	{
		return feature;
	}

	@Override
	public void terminate()
	{
		model.removeModelChangeListener( this );
	}

	@Override
	public void activate()
	{
		model.addModelChangeListener( this );
	}

	@Override
	public void modelChanged( final ModelChangeEvent event )
	{
		if ( !autoMode || null == feature )
		{ return; }
		if ( event.getEventID() == ModelChangeEvent.MODEL_MODIFIED )
		{
			final Set< BCellobject > BCellobjects = event.getBCellobject();
			if ( BCellobjects.size() > 0 )
				computeBCellobjectColors();

		}
		else if ( event.getEventID() == ModelChangeEvent.BCellobject_COMPUTED )
		{
			computeBCellobjectColors();
		}
	}

	/**
	 * Sets the feature that will be used to color BCellobjects. <code>null</code> is
	 * accepted; it will color all the BCellobject with the same default color.
	 *
	 * @param feature
	 *            the feature to color BCellobjects with.
	 */
	@Override
	public void setFeature( final String feature )
	{
		if ( null != feature )
		{
			if ( feature.equals( this.feature ) )
				return;

			this.feature = feature;
			computeBCellobjectColors();
		}
		else
		{
			this.feature = null;
		}
	}

	/*
	 * PRIVATE METHODS
	 */

	private void computeBCellobjectColors()
	{
		if ( null == feature )
		{ return; }

		// Get min & max
		min = Float.POSITIVE_INFINITY;
		max = Float.NEGATIVE_INFINITY;
		Double val;
		for ( final int ikey : model.getBCellobjects().keySet() )
		{
			for ( final BCellobject BCellobject : model.getBCellobjects().iterable( ikey, false ) )
			{
				val = BCellobject.getFeature( feature );
				if ( null == val || Double.isNaN( val.doubleValue() ) )
					continue;

				if ( val > max )
					max = val.doubleValue();

				if ( val < min )
					min = val.doubleValue();
			}
		}
	}

	/*
	 * MINMAXADJUSTABLE
	 */

	@Override
	public double getMin()
	{
		return min;
	}

	@Override
	public double getMax()
	{
		return max;
	}

	@Override
	public void setMinMax( final double min, final double max )
	{
		this.min = min;
		this.max = max;
	}

	@Override
	public void autoMinMax()
	{
		computeBCellobjectColors();
	}

	@Override
	public void setAutoMinMaxMode( final boolean autoMode )
	{
		this.autoMode = autoMode;
		if ( autoMode )
		{
			activate();
		}
		else
		{
			terminate();
		}
	}

	@Override
	public boolean isAutoMinMaxMode()
	{
		return autoMode;
	}

	@Override
	public void setFrom( final MinMaxAdjustable minMaxAdjustable )
	{
		setAutoMinMaxMode( minMaxAdjustable.isAutoMinMaxMode() );
		if ( !minMaxAdjustable.isAutoMinMaxMode() )
			setMinMax( minMaxAdjustable.getMin(), minMaxAdjustable.getMax() );
	}
}
