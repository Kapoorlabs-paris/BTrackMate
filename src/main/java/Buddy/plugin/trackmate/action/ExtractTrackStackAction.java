package Buddy.plugin.trackmate.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.ImageIcon;

import org.jgrapht.graph.DefaultWeightedEdge;

import Buddy.plugin.trackmate.Model;
import Buddy.plugin.trackmate.SelectionModel;
import Buddy.plugin.trackmate.Settings;
import Buddy.plugin.trackmate.TrackMate;
import Buddy.plugin.trackmate.gui.TrackMateWizard;
import Buddy.plugin.trackmate.util.TMUtils;
import Buddy.plugin.trackmate.visualization.trackscheme.BCellobjectIconGrabber;
import budDetector.BCellobject;
import ij.CompositeImage;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;
import ij.process.ImageProcessor;
import net.imagej.ImgPlus;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.view.Views;

@SuppressWarnings( "deprecation" )
public class ExtractTrackStackAction extends AbstractTMAction
{

	public static final String NAME = "Extract track stack";

	public static final String KEY = "EXTRACT_TRACK_STACK";

	public static final String INFO_TEXT = "<html> "
			+ "Generate a stack of images taken from the track "
			+ "that joins two selected BCellobjects. "
			+ "<p> "
			+ "There must be exactly 1 or 2 BCellobjects selected for this action "
			+ "to work. If only one BCellobject is selected, then the stack is extracted from "
			+ "the track it belongs to, from the first BCellobject in time to the last in time. "
			+ "If there are two BCellobjects selected, they must belong to a track that connects "
			+ "them. A path is then found that joins them and the stack is extracted "
			+ "from this path."
			+ "<p> "
			+ "A stack of images will be generated from the BCellobjects that join "
			+ "them. A GUI allows specifying the size of the extract, in units of the largest "
			+ "BCellobject in the track, and whether to capture a 2D or 3D stack over time. "
			+ "All channels are captured. " +
			"</html>";

	public static final ImageIcon ICON = new ImageIcon( TrackMateWizard.class.getResource( "images/magnifier.png" ) );

	/**
	 * By how much we resize the capture window to get a nice border around the
	 * BCellobject.
	 */
	private static final float RESIZE_FACTOR = 1.5f;

	private final SelectionModel selectionModel;

	private final double radiusRatio;

	private final boolean do3d;

	/*
	 * CONSTRUCTOR
	 */

	public ExtractTrackStackAction( final SelectionModel selectionModel, final double radiusRatio, final boolean do3d )
	{
		this.selectionModel = selectionModel;
		this.radiusRatio = radiusRatio;
		this.do3d = do3d;
	}

	/*
	 * METHODS
	 */

	@SuppressWarnings( { "unchecked", "rawtypes" } )
	@Override
	public void execute( final TrackMate trackmate )
	{
		logger.log( "Capturing " + ( do3d ? "3D" : "2D" ) + " track stack.\n" );

		final Model model = trackmate.getModel();
		final Set< BCellobject > selection = selectionModel.getBCellobjectSelection();
		int nBCellobjects = selection.size();
		if ( nBCellobjects != 2 )
		{
			if ( nBCellobjects == 1 )
			{
				final Integer trackID = model.getTrackModel().trackIDOf( selectionModel.getBCellobjectSelection().iterator().next() );
				final List< BCellobject > BCellobjects = new ArrayList<>( model.getTrackModel().trackBCellobjects( trackID ) );
				Collections.sort( BCellobjects, BCellobject.frameComparator );
				selectionModel.clearSelection();
				selectionModel.addBCellobjectToSelection( BCellobjects.get( 0 ) );
				selectionModel.addBCellobjectToSelection( BCellobjects.get( BCellobjects.size() - 1 ) );
			}
			else
			{
				logger.error( "Expected 1 or 2 BCellobjects in the selection, got " + nBCellobjects + ".\nAborting.\n" );
				return;
			}
		}

		// Get start & end
		BCellobject tmp1, tmp2, start, end;
		final Iterator< BCellobject > it = selection.iterator();
		tmp1 = it.next();
		tmp2 = it.next();
		if ( tmp1.getFeature( BCellobject.POSITION_T ) > tmp2.getFeature( BCellobject.POSITION_T ) )
		{
			end = tmp1;
			start = tmp2;
		}
		else
		{
			end = tmp2;
			start = tmp1;
		}

		// Find path
		final List< DefaultWeightedEdge > edges = model.getTrackModel().dijkstraShortestPath( start, end );
		if ( null == edges )
		{
			logger.error( "The 2 BCellobjects are not connected.\nAborting\n" );
			return;
		}
		selectionModel.clearEdgeSelection();
		selectionModel.addEdgeToSelection( edges );

		// Build BCellobject list
		// & Get largest diameter
		final List< BCellobject > path = new ArrayList<>( edges.size() );
		path.add( start );
		BCellobject previous = start;
		BCellobject current;
		double radius = Math.abs( start.getFeature( BCellobject.Size ) ) * radiusRatio;
		for ( final DefaultWeightedEdge edge : edges )
		{
			current = model.getTrackModel().getEdgeSource( edge );
			if ( current == previous )
			{
				current = model.getTrackModel().getEdgeTarget( edge ); // We have to check both in case of bad oriented edges
			}
			path.add( current );
			final double ct = Math.abs( current.getFeature( BCellobject.Size ) );
			if ( ct > radius )
			{
				radius = ct;
			}
			previous = current;
		}
		path.add( end );

		// Sort BCellobject by ascending frame number
		final TreeSet< BCellobject > sortedBCellobjects = new TreeSet<>( BCellobject.frameComparator );
		sortedBCellobjects.addAll( path );
		nBCellobjects = sortedBCellobjects.size();

		// Common coordinates
		final Settings settings = trackmate.getSettings();
		final double[] calibration = TMUtils.getSpatialCalibration( settings.imp );
		final int width = ( int ) Math.ceil( 2 * radius * RESIZE_FACTOR / calibration[ 0 ] );
		final int height = ( int ) Math.ceil( 2 * radius * RESIZE_FACTOR / calibration[ 1 ] );
		final int depth;
		if ( do3d )
			depth = ( int ) Math.ceil( 2 * radius * RESIZE_FACTOR / calibration[ 2 ] );
		else
			depth = 1;

		// Extract target channel
		final ImgPlus img = TMUtils.rawWraps( settings.imp );

		// Prepare new image holder:
		final ImageStack stack = new ImageStack( width, height );

		// Iterate over set to grab imglib image
		int progress = 0;
		final int nChannels = settings.imp.getNChannels();


		for ( final BCellobject BCellobject : sortedBCellobjects )
		{

			// Extract image for current frame
			final int frame = BCellobject.getFeature( BCellobject.POSITION_T ).intValue();

			for ( int c = 0; c < nChannels; c++ )
			{
				final ImgPlus imgCT = TMUtils.hyperSlice( img, c, frame );

				// Compute target coordinates for current BCellobject
				final int x = ( int ) ( Math.round( ( BCellobject.getFeature( BCellobject.POSITION_X ) ) / calibration[ 0 ] ) - width / 2 );
				final int y = ( int ) ( Math.round( ( BCellobject.getFeature( BCellobject.POSITION_Y ) ) / calibration[ 1 ] ) - height / 2 );
				long slice = 0;
				if ( imgCT.numDimensions() > 2 )
				{
					slice = Math.round( BCellobject.getFeature( BCellobject.POSITION_Z ) / calibration[ 2 ] );
					if ( slice < 0 )
						slice = 0;

					if ( slice >= imgCT.dimension( 2 ) )
						slice = imgCT.dimension( 2 ) - 1;
				}

				final BCellobjectIconGrabber< ? > grabber = new BCellobjectIconGrabber( imgCT );
				if ( do3d )
				{
					final Img crop = grabber.grabImage( x, y, slice, width, height, depth );
					// Copy it so stack
					for ( int i = 0; i < crop.dimension( 2 ); i++ )
					{
						final ImageProcessor processor = ImageJFunctions.wrap( Views.hyperSlice( crop, 2, i ), crop.toString() ).getProcessor();
						stack.addSlice( BCellobject.toString(), processor );
					}
				}
				else
				{
					final Img crop = grabber.grabImage( x, y, slice, width, height );
					stack.addSlice( BCellobject.toString(), ImageJFunctions.wrap( crop, crop.toString() ).getProcessor() );
				}

			}
			logger.setProgress( ( float ) ( progress + 1 ) / nBCellobjects );
			progress++;

		}

		// Convert to plain ImageJ
		final ImagePlus stackTrack = new ImagePlus( "", stack );
		stackTrack.setTitle( "Path from " + start + " to " + end );
		final Calibration impCal = stackTrack.getCalibration();
		impCal.setTimeUnit( settings.imp.getCalibration().getTimeUnit() );
		impCal.setUnit( settings.imp.getCalibration().getUnit() );
		impCal.pixelWidth = calibration[ 0 ];
		impCal.pixelHeight = calibration[ 1 ];
		impCal.pixelDepth = calibration[ 2 ];
		impCal.frameInterval = settings.dt;
		stackTrack.setDimensions( nChannels, depth, nBCellobjects );
		stackTrack.setOpenAsHyperStack( true );

		//Display it
		if ( nChannels > 1 )
		{
			final CompositeImage cmp = new CompositeImage( stackTrack, CompositeImage.COMPOSITE );
			if ( settings.imp instanceof CompositeImage )
			{
				final CompositeImage scmp = ( CompositeImage ) settings.imp;
				for ( int c = 0; c < nChannels; c++ )
					cmp.setChannelLut( scmp.getChannelLut( c+1 ), c+1 );
			}

			cmp.show();
			cmp.setZ( depth / 2 + 1 );
			cmp.resetDisplayRange();
		}
		else
		{
			stackTrack.show();
			stackTrack.setZ( depth / 2 + 1 );
			stackTrack.resetDisplayRange();
		}

		logger.log( "Done." );
	}
}
