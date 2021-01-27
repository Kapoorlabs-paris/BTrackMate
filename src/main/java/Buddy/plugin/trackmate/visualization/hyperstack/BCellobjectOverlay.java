package Buddy.plugin.trackmate.visualization.hyperstack;

import static Buddy.plugin.trackmate.visualization.TrackMateModelView.KEY_BCellobject_COLORING;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import Buddy.plugin.trackmate.Model;
import Buddy.plugin.trackmate.BCellobjectCollection;
import Buddy.plugin.trackmate.util.TMUtils;
import Buddy.plugin.trackmate.visualization.FeatureColorGenerator;
import Buddy.plugin.trackmate.visualization.TrackMateModelView;
import budDetector.BCellobject;
import ij.ImagePlus;
import ij.gui.Roi;

/**
 * The overlay class in charge of drawing the BCellobject images on the hyperstack
 * window.
 *
 * @author Jean-Yves Tinevez &lt;jeanyves.tinevez@gmail.com&gt; 2010 - 2011
 */
public class BCellobjectOverlay extends Roi
{

	private static final long serialVersionUID = 1L;

	private static final Font LABEL_FONT = new Font( "Arial", Font.BOLD, 12 );

	private static final boolean DEBUG = false;

	protected BCellobject editingBCellobject;

	protected final double[] calibration;

	protected Composite composite = AlphaComposite.getInstance( AlphaComposite.SRC_OVER );

	protected FontMetrics fm;

	protected Collection< BCellobject > BCellobjectSelection = new ArrayList<>();

	protected Map< String, Object > displaySettings;

	protected final Model model;

	/*
	 * CONSTRUCTOR
	 */

	public BCellobjectOverlay( final Model model, final ImagePlus imp, final Map< String, Object > displaySettings )
	{
		super( 0, 0, imp );
		this.model = model;
		this.imp = imp;
		this.calibration = TMUtils.getSpatialCalibration( imp );
		this.displaySettings = displaySettings;
	}

	/*
	 * METHODS
	 */

	@Override
	public void drawOverlay( final Graphics g )
	{
		final int xcorner = ic.offScreenX( 0 );
		final int ycorner = ic.offScreenY( 0 );
		final double magnification = getMagnification();
		final BCellobjectCollection BCellobjects = model.getBCellobjects();

		final boolean BCellobjectVisible = ( Boolean ) displaySettings.get( TrackMateModelView.KEY_BCellobjectS_VISIBLE );
		if ( !BCellobjectVisible || BCellobjects.getNBCellobjects(  ) == 0 ) {
			return;
		}

		final boolean doLimitDrawingDepth = ( Boolean ) displaySettings.get( TrackMateModelView.KEY_LIMIT_DRAWING_DEPTH );
		final double drawingDepth = ( Double ) displaySettings.get( TrackMateModelView.KEY_DRAWING_DEPTH );
		final int trackDisplayMode = ( Integer ) displaySettings.get( TrackMateModelView.KEY_TRACK_DISPLAY_MODE );
		final boolean selectionOnly = ( trackDisplayMode == TrackMateModelView.TRACK_DISPLAY_MODE_SELECTION_ONLY );

		final Graphics2D g2d = ( Graphics2D ) g;
		// Save graphic device original settings
		final AffineTransform originalTransform = g2d.getTransform();
		final Composite originalComposite = g2d.getComposite();
		final Stroke originalStroke = g2d.getStroke();
		final Color originalColor = g2d.getColor();
		final Font originalFont = g2d.getFont();

		g2d.setComposite( composite );
		g2d.setFont( LABEL_FONT );
		g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
		fm = g2d.getFontMetrics();

		final double zslice = ( imp.getSlice() - 1 ) * calibration[ 2 ];
		final double lMag = magnification;
		final int frame = imp.getFrame() - 1;

		// Deal with normal BCellobjects.
		@SuppressWarnings( "unchecked" )
		final FeatureColorGenerator< BCellobject > colorGenerator = ( FeatureColorGenerator< BCellobject > ) displaySettings.get( KEY_BCellobject_COLORING );
		g2d.setStroke( new BasicStroke( 1.0f ) );

		if ( selectionOnly && null != BCellobjectSelection)
		{
			// Track display mode only displays selection.

			for ( final BCellobject BCellobject : BCellobjectSelection )
			{
				if ( BCellobject == editingBCellobject )
				{
					continue;
				}
				final int sFrame = BCellobject.getFeature( BCellobject.POSITION_T ).intValue();
				if ( sFrame != frame )
				{
					continue;
				}

				final double z = BCellobject.getFeature( BCellobject.POSITION_Z ).doubleValue();
				if ( doLimitDrawingDepth && Math.abs( z - zslice ) > drawingDepth )
				{
					continue;
				}
				
				final Color color = colorGenerator.color( BCellobject );
				g2d.setColor( color );
				drawBCellobject( g2d, BCellobject, zslice, xcorner, ycorner, lMag );
			}

		}
		else
		{
			// Other track displays.

			for ( final Iterator< BCellobject > iterator = BCellobjects.iterator( frame ); iterator.hasNext(); )
			{
				final BCellobject BCellobject = iterator.next();

				if ( editingBCellobject == BCellobject || ( BCellobjectSelection != null && BCellobjectSelection.contains( BCellobject ) ) )
				{
					continue;
				}

				final Color color = colorGenerator.color( BCellobject );
				g2d.setColor( color );

				final double z = BCellobject.getFeature( BCellobject.POSITION_Z ).doubleValue();
				if ( doLimitDrawingDepth && Math.abs( z - zslice ) > drawingDepth )
				{
					continue;
				}

				drawBCellobject( g2d, BCellobject, zslice, xcorner, ycorner, lMag );
			}

			// Deal with BCellobject selection
			if ( null != BCellobjectSelection )
			{
				g2d.setStroke( new BasicStroke( 2.0f ) );
				g2d.setColor( TrackMateModelView.DEFAULT_HIGHLIGHT_COLOR );
				for ( final BCellobject BCellobject : BCellobjectSelection )
				{
					if ( BCellobject == editingBCellobject )
					{
						continue;
					}
					final int sFrame = BCellobject.getFeature( BCellobject.POSITION_T ).intValue();
					if ( DEBUG )
					{
						System.out.println( "[BCellobjectOverlay] For BCellobject " + BCellobject + " in selection, found frame " + sFrame );
					}
					if ( sFrame != frame )
					{
						continue;
					}
					drawBCellobject( g2d, BCellobject, zslice, xcorner, ycorner, lMag );
				}
			}
		}

		drawExtraLayer( g2d, frame );

		// Deal with editing BCellobject - we always draw it with its center at the
		// current z, current t
		// (it moves along with the current slice)
		if ( null != editingBCellobject )
		{
			g2d.setColor( TrackMateModelView.DEFAULT_HIGHLIGHT_COLOR );
			g2d.setStroke( new BasicStroke( 1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, new float[] { 5f, 5f }, 0 ) );
			final double x = editingBCellobject.getFeature( BCellobject.POSITION_X );
			final double y = editingBCellobject.getFeature( BCellobject.POSITION_Y );
			final double radius = editingBCellobject.getFeature( BCellobject.Size ) / calibration[ 0 ] * lMag;
			// In pixel units
			final double xp = x / calibration[ 0 ] + 0.5d;
			final double yp = y / calibration[ 1 ] + 0.5d;
			// Scale to image zoom
			final double xs = ( xp - xcorner ) * lMag;
			final double ys = ( yp - ycorner ) * lMag;
			final double radiusRatio = ( Double ) displaySettings.get( TrackMateModelView.KEY_BCellobject_RADIUS_RATIO );
			g2d.drawOval( ( int ) Math.round( xs - radius * radiusRatio ), ( int ) Math.round( ys - radius * radiusRatio ), ( int ) Math.round( 2 * radius * radiusRatio ), ( int ) Math.round( 2 * radius * radiusRatio ) );
		}

		// Restore graphic device original settings
		g2d.setTransform( originalTransform );
		g2d.setComposite( originalComposite );
		g2d.setStroke( originalStroke );
		g2d.setColor( originalColor );
		g2d.setFont( originalFont );
	}

	/**
	 * @param g2d 
	 * @param frame  
	 */
	protected void drawExtraLayer( final Graphics2D g2d, final int frame )
	{}

	public void setBCellobjectSelection( final Collection< BCellobject > BCellobjects )
	{
		this.BCellobjectSelection = BCellobjects;
	}

	protected void drawBCellobject( final Graphics2D g2d, final BCellobject BCellobject, final double zslice, final int xcorner, final int ycorner, final double magnification )
	{
		final double x = BCellobject.getFeature( BCellobject.POSITION_X );
		final double y = BCellobject.getFeature( BCellobject.POSITION_Y );
		final double z = BCellobject.getFeature( BCellobject.POSITION_Z );
		final double dz2 = ( z - zslice ) * ( z - zslice );
		final double radiusRatio = ( Double ) displaySettings.get( TrackMateModelView.KEY_BCellobject_RADIUS_RATIO );
		final double radius = BCellobject.getFeature( BCellobject.Size ) * radiusRatio;
		// In pixel units
		final double xp = x / calibration[ 0 ] + 0.5f;
		final double yp = y / calibration[ 1 ] + 0.5f; // so that BCellobject centers
		// are displayed on the
		// pixel centers
		// Scale to image zoom
		final double xs = ( xp - xcorner ) * magnification;
		final double ys = ( yp - ycorner ) * magnification;

		if ( dz2 >= radius * radius )
		{
			g2d.fillOval( ( int ) Math.round( xs - 2 * magnification ), ( int ) Math.round( ys - 2 * magnification ), ( int ) Math.round( 4 * magnification ), ( int ) Math.round( 4 * magnification ) );
		}
		else
		{
			final double apparentRadius = Math.sqrt( radius * radius - dz2 ) / calibration[ 0 ] * magnification;
			g2d.drawOval( ( int ) Math.round( xs - apparentRadius ), ( int ) Math.round( ys - apparentRadius ), ( int ) Math.round( 2 * apparentRadius ), ( int ) Math.round( 2 * apparentRadius ) );
			final boolean BCellobjectNameVisible = ( Boolean ) displaySettings.get( TrackMateModelView.KEY_DISPLAY_BCellobject_NAMES );
			if ( BCellobjectNameVisible )
			{
				final String str = BCellobject.toString();

				final int xindent = fm.stringWidth( str );
				int xtext = ( int ) ( xs + apparentRadius + 5 );
				if ( xtext + xindent > imp.getWindow().getWidth() )
				{
					xtext = ( int ) ( xs - apparentRadius - 5 - xindent );
				}

				final int yindent = fm.getAscent() / 2;
				final int ytext = ( int ) ys + yindent;

				g2d.drawString( BCellobject.toString(), xtext, ytext );
			}
		}
	}
}
