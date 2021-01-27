package Buddy.plugin.trackmate.features.BCellobject;

import Buddy.plugin.trackmate.Model;
import Buddy.plugin.trackmate.features.FeatureAnalyzer;
import net.imagej.ImgPlus;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

/**
 * Interface for factories that can generate a {@link BCellobjectAnalyzer} configured
 * to operate on a specific frame of a model.
 * <p>
 * Concrete implementation should declare what features they can compute
 * numerically, and make this info available in the
 * {@link Buddy.plugin.trackmate.providers.BCellobjectAnalyzerProvider} that returns
 * them.
 * <p>
 * Feature key names are for historical reason all capitalized in an enum
 * manner. For instance: POSITION_X, MAX_INTENSITY, etc... They must be suitable
 * to be used as a attribute key in an xml file.
 *
 * @author Jean-Yves Tinevez - 2012
 */
public interface BCellobjectAnalyzerFactory< T extends RealType< T > & NativeType< T > > extends FeatureAnalyzer
{

	/**
	 * Returns a configured {@link BCellobjectAnalyzer} ready to operate on the given
	 * frame (0-based) and given channel (0-based). The target frame image and
	 * the target BCellobjects are retrieved from the {@link Model} thanks to the given
	 * frame and channel index.
	 *
	 * @param model
	 *            the {@link Model} to take the BCellobjects from.
	 * @param img
	 *            the 5D (X, Y, Z, C, T) source image.
	 * @param frame
	 *            the target frame to operate on.
	 * @param channel
	 *            the target channel to operate on.
	 */
	public BCellobjectAnalyzer< T > getAnalyzer( final Model model, ImgPlus< T > img, int frame, int channel );

}
