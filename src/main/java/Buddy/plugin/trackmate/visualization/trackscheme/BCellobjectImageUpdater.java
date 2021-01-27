package Buddy.plugin.trackmate.visualization.trackscheme;

import Buddy.plugin.trackmate.Settings;
import Buddy.plugin.trackmate.util.TMUtils;
import budDetector.BCellobject;
import net.imagej.ImgPlus;

@SuppressWarnings( "deprecation" )
public class BCellobjectImageUpdater
{

	private int previousFrame;

	private int previousChannel;

	private BCellobjectIconGrabber< ? > grabber;

	private final Settings settings;

	/**
	 * Instantiates a new BCellobject image updater.
	 *
	 * @param settings
	 *            the {@link Settings} object from which we read the raw image
	 *            and the target channel.
	 */
	public BCellobjectImageUpdater( final Settings settings )
	{
		this.settings = settings;
		this.previousFrame = -1;
		this.previousChannel = -1;
	}

	/**
	 * Returns the image string of the given BCellobject, based on the raw images
	 * contained in the given model. For performance, the image at target frame
	 * is stored for subsequent calls of this method. So it is a good idea to
	 * group calls to this method for BCellobjects that belong to the same frame.
	 *
	 * @param radiusFactor
	 *            a factor that determines the size of the thumbnail. The
	 *            thumbnail will have a size equal to the BCellobject diameter times
	 *            this radius.
	 * @return the image string.
	 */
	@SuppressWarnings( { "rawtypes", "unchecked" } )
	public String getImageString( final BCellobject spot, final double radiusFactor )
	{
		final int frame = spot.getFeature( BCellobject.POSITION_T ).intValue();
		final int targetChannel = settings.imp.getC() - 1;
		if ( frame == previousFrame && targetChannel == previousChannel )
		{
			// Keep the same image than in memory
		}
		else
		{
			final ImgPlus img = TMUtils.rawWraps( settings.imp );
			final ImgPlus< ? > imgCT = TMUtils.hyperSlice( img, targetChannel, frame );

			grabber = new BCellobjectIconGrabber( imgCT );
			previousFrame = frame;
			previousChannel = targetChannel;
		}
		return grabber.getImageString( spot, radiusFactor );
	}

}
