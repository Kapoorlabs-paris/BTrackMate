package Buddy.plugin.trackmate.action;

import java.awt.Component;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.scijava.plugin.Plugin;

import Buddy.plugin.trackmate.LoadTrackMatePlugIn_;
import Buddy.plugin.trackmate.Logger;
import Buddy.plugin.trackmate.Model;
import Buddy.plugin.trackmate.TrackMate;
import Buddy.plugin.trackmate.gui.TrackMateGUIController;
import Buddy.plugin.trackmate.gui.TrackMateWizard;
import Buddy.plugin.trackmate.util.BCellobjectNeighborhood;
import Buddy.plugin.trackmate.util.TMUtils;
import budDetector.BCellobject;
import ij.ImageJ;
import ij.ImagePlus;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imglib2.Dimensions;
import net.imglib2.FinalDimensions;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;
import pluginTools.InteractiveBud;

@SuppressWarnings( "deprecation" )
public class LabelImgExporter extends AbstractTMAction
{

	public static final String INFO_TEXT = "<html>"
			+ "This action creates a label image from the tracking results. "
			+ "<p> "
			+ "A new 16-bit image is generated, of same dimension and size that "
			+ "of the input image. The label image has one channel, with black baground (0 value) "
			+ "everywhere, except where there are BCellobjects. Each BCellobject is painted with "
			+ "a uniform integer value equal to the trackID it belongs to. "
			+ "BCellobjects that do not belong to tracks are painted with a unique integer "
			+ "larger than the last trackID in the dataset. "
			+ "<p> "
			+ "Only visible BCellobjects are painted. "
			+ "</html>";

	public static final String KEY = "EXPORT_LABEL_IMG";

	public static final ImageIcon ICON = new ImageIcon( TrackMateWizard.class.getResource( "images/picture_key.png" ) );

	public static final String NAME = "Export label image";

	/**
	 * Parent component to display the dialog.
	 */
	private final Component gui;

	public LabelImgExporter( final Component gui )
	{
		this.gui = gui;
	}

	@Override
	public void execute( final TrackMate trackmate )
	{
		/*
		 * Ask use for option.
		 */

		final boolean exportBCellobjectsAsDots;
		final boolean exportTracksOnly;
		if ( gui != null )
		{
			final LabelImgExporterPanel panel = new LabelImgExporterPanel();
			final int userInput = JOptionPane.showConfirmDialog(
					gui,
					panel,
					"Export to label image",
					JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE,
					TrackMateWizard.TRACKMATE_ICON );

			if ( userInput != JOptionPane.OK_OPTION )
				return;

			exportBCellobjectsAsDots = panel.isExportBCellobjectsAsDots();
			exportTracksOnly = panel.isExportTracksOnly();
		}
		else
		{
			exportBCellobjectsAsDots = false;
			exportTracksOnly = false;
		}

		/*
		 * Generate label image.
		 */

		createLabelImagePlus( trackmate, exportBCellobjectsAsDots, exportTracksOnly, logger ).show();
	}

	/**
	 * Creates a new label {@link ImagePlus} where the BCellobjects of the specified
	 * model are painted as ellipsoids taken from their shape, with their track
	 * ID as pixel value.
	 *
	 * @param trackmate
	 *            the trackmate instance from which we takes the BCellobjects to paint.
	 *            The label image will have the same calibration, name and
	 *            dimension from the input image stored in the trackmate
	 *            settings. The output label image will have the same size that
	 *            of this input image, except for the number of channels, which
	 *            will be 1.
	 * @param exportBCellobjectsAsDots
	 *            if <code>true</code>, BCellobjects will be painted as single dots
	 *            instead of ellipsoids.
	 * @param exportTracksOnly
	 *            if <code>true</code>, only the BCellobjects belonging to visible
	 *            tracks will be painted. If <code>false</code>, BCellobjects not
	 *            belonging to a track will be painted with a unique ID,
	 *            different from the track IDs and different for each BCellobject.
	 *
	 * @return a new {@link ImagePlus}.
	 */
	public static final ImagePlus createLabelImagePlus(
			final TrackMate trackmate,
			final boolean exportBCellobjectsAsDots,
			final boolean exportTracksOnly )
	{
		return createLabelImagePlus( trackmate, exportBCellobjectsAsDots, exportTracksOnly, Logger.VOID_LOGGER );
	}

	/**
	 * Creates a new label {@link ImagePlus} where the BCellobjects of the specified
	 * model are painted as ellipsoids taken from their shape, with their track
	 * ID as pixel value.
	 *
	 * @param trackmate
	 *            the trackmate instance from which we takes the BCellobjects to paint.
	 *            The label image will have the same calibration, name and
	 *            dimension from the input image stored in the trackmate
	 *            settings. The output label image will have the same size that
	 *            of this input image, except for the number of channels, which
	 *            will be 1.
	 * @param exportBCellobjectsAsDots
	 *            if <code>true</code>, BCellobjects will be painted as single dots
	 *            instead of ellipsoids.
	 * @param exportTracksOnly
	 *            if <code>true</code>, only the BCellobjects belonging to visible
	 *            tracks will be painted. If <code>false</code>, BCellobjects not
	 *            belonging to a track will be painted with a unique ID,
	 *            different from the track IDs and different for each BCellobject.
	 * @param logger
	 *            a {@link Logger} instance, to report progress of the export
	 *            process.
	 *
	 * @return a new {@link ImagePlus}.
	 */
	public static final ImagePlus createLabelImagePlus(
			final TrackMate trackmate,
			final boolean exportBCellobjectsAsDots,
			final boolean exportTracksOnly,
			final Logger logger )
	{
		return createLabelImagePlus( trackmate.getModel(), trackmate.getParent(), trackmate.getSettings().imp, exportBCellobjectsAsDots, exportTracksOnly, logger );
	}

	/**
	 * Creates a new label {@link ImagePlus} where the BCellobjects of the specified
	 * model are painted as ellipsoids taken from their shape, with their track
	 * ID as pixel value.
	 *
	 * @param model
	 *            the model from which we takes the BCellobjects to paint.
	 * @param imp
	 *            a source image to read calibration, name and dimension from.
	 *            The output label image will have the same size that of this
	 *            source image, except for the number of channels, which will be
	 *            1.
	 * @param exportBCellobjectsAsDots
	 *            if <code>true</code>, BCellobjects will be painted as single dots
	 *            instead of ellipsoids.
	 * @param exportTracksOnly
	 *            if <code>true</code>, only the BCellobjects belonging to visible
	 *            tracks will be painted. If <code>false</code>, BCellobjects not
	 *            belonging to a track will be painted with a unique ID,
	 *            different from the track IDs and different for each BCellobject.
	 *
	 * @return a new {@link ImagePlus}.
	 */
	public static final ImagePlus createLabelImagePlus(
			final Model model,
			final InteractiveBud parent,
			final ImagePlus imp,
			final boolean exportBCellobjectsAsDots,
			final boolean exportTracksOnly )
	{
		return createLabelImagePlus( model, parent, imp, exportBCellobjectsAsDots, exportTracksOnly, Logger.VOID_LOGGER );
	}

	/**
	 * Creates a new label {@link ImagePlus} where the BCellobjects of the specified
	 * model are painted as ellipsoids taken from their shape, with their track
	 * ID as pixel value.
	 *
	 * @param model
	 *            the model from which we takes the BCellobjects to paint.
	 * @param imp
	 *            a source image to read calibration, name and dimension from.
	 *            The output label image will have the same size that of this
	 *            source image, except for the number of channels, which will be
	 *            1.
	 * @param exportBCellobjectsAsDots
	 *            if <code>true</code>, BCellobjects will be painted as single dots
	 *            instead of ellipsoids.
	 * @param exportTracksOnly
	 *            if <code>true</code>, only the BCellobjects belonging to visible
	 *            tracks will be painted. If <code>false</code>, BCellobjects not
	 *            belonging to a track will be painted with a unique ID,
	 *            different from the track IDs and different for each BCellobject.
	 * @param logger
	 *            a {@link Logger} instance, to report progress of the export
	 *            process.
	 *
	 * @return a new {@link ImagePlus}.
	 */
	public static final ImagePlus createLabelImagePlus(
			final Model model,
			final InteractiveBud parent,
			final ImagePlus imp,
			final boolean exportBCellobjectsAsDots,
			final boolean exportTracksOnly,
			final Logger logger )
	{
		final int[] dimensions = imp.getDimensions();
		final int[] dims = new int[] { dimensions[ 0 ], dimensions[ 1 ], dimensions[ 3 ], dimensions[ 4 ] };

		final ImagePlus lblImp = createLabelImagePlus( model, parent, dims, exportBCellobjectsAsDots, exportTracksOnly, logger );
		lblImp.setCalibration( imp.getCalibration().copy() );
		lblImp.setTitle( "LblImg_" + imp.getTitle() );
		return lblImp;

	}

	/**
	 * Creates a new label {@link ImagePlus} where the BCellobjects of the specified
	 * model are painted as ellipsoids taken from their shape, with their track
	 * ID as pixel value.
	 *
	 * @param model
	 *            the model from which we takes the BCellobjects to paint.
	 * @param dimensions
	 *            the desired dimensions of the output image (width, height,
	 *            nZSlices, nFrames) as a 4 element int array. BCellobjects outside
	 *            these dimensions are ignored.
	 * @param exportBCellobjectsAsDots
	 *            if <code>true</code>, BCellobjects will be painted as single dots
	 *            instead of ellipsoids.
	 * @param exportTracksOnly
	 *            if <code>true</code>, only the BCellobjects belonging to visible
	 *            tracks will be painted. If <code>false</code>, BCellobjects not
	 *            belonging to a track will be painted with a unique ID,
	 *            different from the track IDs and different for each BCellobject.
	 *
	 * @return a new {@link ImagePlus}.
	 */
	public static final ImagePlus createLabelImagePlus(
			final Model model,
			final InteractiveBud parent,
			final int[] dimensions,
			final boolean exportBCellobjectsAsDots,
			final boolean exportTracksOnly )
	{
		return createLabelImagePlus( model, parent, dimensions, exportBCellobjectsAsDots, exportTracksOnly, Logger.VOID_LOGGER );
	}

	/**
	 * Creates a new label {@link ImagePlus} where the BCellobjects of the specified
	 * model are painted as ellipsoids taken from their shape, with their track
	 * ID as pixel value.
	 *
	 * @param model
	 *            the model from which we takes the BCellobjects to paint.
	 * @param dimensions
	 *            the desired dimensions of the output image (width, height,
	 *            nZSlices, nFrames) as a 4 element int array. BCellobjects outside
	 *            these dimensions are ignored.
	 * @param exportBCellobjectsAsDots
	 *            if <code>true</code>, BCellobjects will be painted as single dots
	 *            instead of ellipsoids.
	 * @param exportTracksOnly
	 *            if <code>true</code>, only the BCellobjects belonging to visible
	 *            tracks will be painted. If <code>false</code>, BCellobjects not
	 *            belonging to a track will be painted with a unique ID,
	 *            different from the track IDs and different for each BCellobject.
	 * @param logger
	 *            a {@link Logger} instance, to report progress of the export
	 *            process.
	 *
	 * @return a new {@link ImagePlus}.
	 */
	public static final ImagePlus createLabelImagePlus(
			final Model model,
			final InteractiveBud parent,
			final int[] dimensions,
			final boolean exportBCellobjectsAsDots,
			final boolean exportTracksOnly,
			final Logger logger )
	{
		final long[] dims = new long[ 4 ];
		for ( int d = 0; d < dims.length; d++ )
			dims[ d ] = dimensions[ d ];

		final ImagePlus lblImp = ImageJFunctions.wrap( createLabelImg( model, parent, dims, exportBCellobjectsAsDots, exportTracksOnly, logger ), "LblImage" );
		lblImp.setDimensions( 1, dimensions[ 2 ], dimensions[ 3 ] );
		lblImp.setOpenAsHyperStack( true );
		lblImp.resetDisplayRange();
		return lblImp;
	}

	/**
	 * Creates a new label {@link Img} of {@link UnsignedShortType} where the
	 * BCellobjects of the specified model are painted as ellipsoids taken from their
	 * shape, with their track ID as pixel value.
	 *
	 * @param model
	 *            the model from which we takes the BCellobjects to paint.
	 * @param dimensions
	 *            the desired dimensions of the output image (width, height,
	 *            nZSlices, nFrames) as a 4 element int array. BCellobjects outside
	 *            these dimensions are ignored.
	 * @param exportBCellobjectsAsDots
	 *            if <code>true</code>, BCellobjects will be painted as single dots
	 *            instead of ellipsoids.
	 * @param exportTracksOnly
	 *            if <code>true</code>, only the BCellobjects belonging to visible
	 *            tracks will be painted. If <code>false</code>, BCellobjects not
	 *            belonging to a track will be painted with a unique ID,
	 *            different from the track IDs and different for each BCellobject.
	 *
	 * @return a new {@link Img}.
	 */
	public static final Img< UnsignedShortType > createLabelImg(
			final Model model,
			final InteractiveBud parent,
			final long[] dimensions,
			final boolean exportBCellobjectsAsDots,
			final boolean exportTracksOnly )
	{
		return createLabelImg( model, parent, dimensions, exportBCellobjectsAsDots, exportTracksOnly, Logger.VOID_LOGGER );
	}

	/**
	 * Creates a new label {@link Img} of {@link UnsignedShortType} where the
	 * BCellobjects of the specified model are painted as ellipsoids taken from their
	 * shape, with their track ID as pixel value.
	 *
	 * @param model
	 *            the model from which we takes the BCellobjects to paint.
	 * @param dimensions
	 *            the desired dimensions of the output image (width, height,
	 *            nZSlices, nFrames) as a 4 element int array. BCellobjects outside
	 *            these dimensions are ignored.
	 * @param exportBCellobjectsAsDots
	 *            if <code>true</code>, BCellobjects will be painted as single dots
	 *            instead of ellipsoids.
	 * @param exportTracksOnly
	 *            if <code>true</code>, only the BCellobjects belonging to visible
	 *            tracks will be painted. If <code>false</code>, BCellobjects not
	 *            belonging to a track will be painted with a unique ID,
	 *            different from the track IDs and different for each BCellobject.
	 * @param logger
	 *            a {@link Logger} instance, to report progress of the export
	 *            process.
	 *
	 * @return a new {@link Img}.
	 */
	public static final Img< UnsignedShortType > createLabelImg(
			final Model model,
			final InteractiveBud parent,
			final long[] dimensions,
			final boolean exportBCellobjectsAsDots,
			final boolean exportTracksOnly,
			final Logger logger )
	{
		/*
		 * Create target image.
		 */
		final Dimensions targetSize = FinalDimensions.wrap( dimensions );
		final Img< UnsignedShortType > lblImg = Util.getArrayOrCellImgFactory( targetSize, new UnsignedShortType() ).create( targetSize );
		final AxisType[] axes = new AxisType[] {
				Axes.X,
				Axes.Y,
				Axes.Z,
				Axes.TIME };
		final ImgPlus< UnsignedShortType > imgPlus = new ImgPlus<>( lblImg, "LblImg", axes );

		/*
		 * Determine the starting id for BCellobjects not in tracks.
		 */

		int maxTrackID = -1;
		final Set< Integer > trackIDs = model.getTrackModel().trackIDs( false );
		if ( null != trackIDs )
			for ( final Integer trackID : trackIDs )
				if ( trackID > maxTrackID )
					maxTrackID = trackID.intValue();
		final AtomicInteger lonelyBCellobjectID = new AtomicInteger( maxTrackID + 2 );

		/*
		 * Frame by frame iteration.
		 */

		logger.log( "Writing label image.\n" );
		for ( int frame = 0; frame < dimensions[ 3 ]; frame++ )
		{
			final ImgPlus< UnsignedShortType > imgCT = TMUtils.hyperSlice( imgPlus, 0, frame );

			final BCellobjectWriter BCellobjectWriter = exportBCellobjectsAsDots
					? new BCellobjectAsDotWriter( parent, imgCT )
					: new BCellobjectSphereWriter( imgCT );

			for ( final BCellobject BCellobject : model.getBCellobjects().iterable( frame, true ) )
			{
				final int id;
				final Integer trackID = model.getTrackModel().trackIDOf( BCellobject );
				if ( null == trackID || !model.getTrackModel().isVisible( trackID ) )
				{
					if ( exportTracksOnly )
						continue;

					id = lonelyBCellobjectID.getAndIncrement();
				}
				else
				{
					id = 1 + trackID.intValue();
				}

				BCellobjectWriter.write( BCellobject, id );
			}
			logger.setProgress( ( double ) ( 1 + frame ) / dimensions[ 3 ] );
		}
		logger.log( "Done.\n" );

		return lblImg;
	}

	@Plugin( type = TrackMateActionFactory.class )
	public static class Factory implements TrackMateActionFactory
	{

		@Override
		public String getInfoText()
		{
			return INFO_TEXT;
		}

		@Override
		public String getKey()
		{
			return KEY;
		}

		@Override
		public TrackMateAction create( final TrackMateGUIController controller )
		{
			return new LabelImgExporter( controller.getGUI() );
		}

		@Override
		public ImageIcon getIcon()
		{
			return ICON;
		}

		@Override
		public String getName()
		{
			return NAME;
		}
	}

	/**
	 * Interface for classes that can 'write' a BCellobject into a label image.
	 */
	private static interface BCellobjectWriter
	{
		public void write( BCellobject BCellobject, int id );
	}

	private static final class BCellobjectSphereWriter implements BCellobjectWriter
	{

		private final ImgPlus< UnsignedShortType > img;

		public BCellobjectSphereWriter( final ImgPlus< UnsignedShortType > img )
		{
			this.img = img;
		}

		@Override
		public void write( final BCellobject BCellobject, final int id )
		{
			final BCellobjectNeighborhood< UnsignedShortType > neighborhood = new BCellobjectNeighborhood< UnsignedShortType >( BCellobject, img );
			for ( final UnsignedShortType pixel : neighborhood )
				pixel.set( id );
		}
	}

	private static final class BCellobjectAsDotWriter implements BCellobjectWriter
	{

		private final double[] calibration;

		private final long[] center;

		private final RandomAccess< UnsignedShortType > ra;

		public BCellobjectAsDotWriter( final InteractiveBud parent,final ImgPlus< UnsignedShortType > img )
		{
			this.calibration = TMUtils.getSpatialCalibration( parent, img );
			this.center = new long[ img.numDimensions() ];
			this.ra = Views.extendZero( img ).randomAccess();
		}

		@Override
		public void write( final BCellobject BCellobject, final int id )
		{
			for ( int d = 0; d < center.length; d++ )
				center[ d ] = Math.round( BCellobject.getFeature( BCellobject.POSITION_FEATURES[ d ] ).doubleValue() / calibration[ d ] );

			ra.setPosition( center );
			ra.get().set( id );
		}
	}


}
