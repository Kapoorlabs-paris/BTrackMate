package Buddy.plugin.trackmate;

import Buddy.plugin.trackmate.gui.GuiUtils;
import Buddy.plugin.trackmate.gui.TrackMateGUIController;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.plugin.PlugIn;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.FloatType;
import pluginTools.InteractiveBud;

public class TrackMatePlugIn_ implements PlugIn {

	protected TrackMate trackmate;

	protected InteractiveBud parent;

	protected Settings settings;

	protected Model model;

	public TrackMatePlugIn_(final InteractiveBud parent) {

		this.parent = parent;
	}
	public static ImagePlus Reshape(RandomAccessibleInterval<FloatType> image, String title) {
		
		int channels, frames;
		
		ImagePlus imp = ImageJFunctions.wrapFloat(image, title);
		if(imp.getNChannels() > imp.getNFrames()) {
			channels = imp.getNFrames();
		    frames = imp.getNChannels();
		    
		}
		
		else {
			
			channels = imp.getNChannels();
		    frames = imp.getNFrames();
			
		}
		
		imp.setDimensions(channels, imp.getNSlices(), frames);
		imp.show();
		
		return imp;
		
	}
	
	public static ImagePlus Reshape3D(RandomAccessibleInterval<FloatType> image, String title) {
		
		int channels, frames;
		
		ImagePlus imp = ImageJFunctions.wrapFloat(image, title);
		if(imp.getNChannels() > imp.getNFrames()) {
			channels = imp.getNFrames();
		    frames = imp.getNChannels();
		    
		}
		
		else {
			
			channels = imp.getNChannels();
		    frames = imp.getNFrames();
			
		}
		
		imp.setDimensions(channels, frames,  imp.getNSlices());
		imp.show();
		
		return imp;
		
	}
	
	/**
	 * Runs the TrackMate GUI plugin.
	 *
	 * @param imagePath
	 *            a path to an image that can be read by ImageJ. If set, the image
	 *            will be opened and TrackMate will be started set to operate on it.
	 *            If <code>null</code> or 0-length, TrackMate will be set to operate
	 *            on the image currently opened in ImageJ.
	 */
	@Override
	public void run(final String imagePath) {
		
		
		ImagePlus imp = null;
		if (imagePath==null)
		imp = Reshape(parent.originalimg, "");
		else
		imp = Reshape3D(parent.originalimg, "");
		GuiUtils.userCheckImpDimensions(imp);

		settings = createSettings(imp);
		model = createModel();
		model.setBCellobjects(parent.budcells, true);
		trackmate = createTrackMate();

		/*
		 * Launch GUI.
		 */

		final TrackMateGUIController controller = new TrackMateGUIController(parent, trackmate);
		GuiUtils.positionWindow(controller.getGUI(), imp.getWindow());
	}

	/*
	 * HOOKS
	 */

	/**
	 * Hook for subclassers: <br>
	 * Creates the {@link Model} instance that will be used to store data in the
	 * {@link TrackMate} instance.
	 *
	 * @return a new {@link Model} instance.
	 */

	public InteractiveBud getparent() {

		return parent;
	}

	protected Model createModel() {
		return new Model();
	}

	/**
	 * Hook for subclassers: <br>
	 * Creates the {@link Settings} instance that will be used to tune the
	 * {@link TrackMate} instance. It is initialized by default with values taken
	 * from the current {@link ImagePlus}.
	 * 
	 * @param imp
	 *            the {@link ImagePlus} to operate on.
	 * @return a new {@link Settings} instance.
	 */
	protected Settings createSettings(final ImagePlus imp) {
		final Settings lSettings = new Settings();
		lSettings.setFrom(imp);
		return lSettings;
	}

	/**
	 * Hook for subclassers: <br>
	 * Creates the TrackMate instance that will be controlled in the GUI.
	 *
	 * @return a new {@link TrackMate} instance.
	 */
	protected TrackMate createTrackMate() {
		/*
		 * Since we are now sure that we will be working on this model with this
		 * settings, we need to pass to the model the units from the settings.
		 */
		final String spaceUnits = "pixel";
		final String timeUnits = "pixel";

		model.setPhysicalUnits(spaceUnits, timeUnits);

		return new TrackMate(parent, model, settings);
	}

	/*
	 * MAIN METHOD
	 */

}
