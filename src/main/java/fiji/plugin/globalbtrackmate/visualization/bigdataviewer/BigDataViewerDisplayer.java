package fiji.plugin.globalbtrackmate.visualization.bigdataviewer;

import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.Roi;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.FloatType;
import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import bdv.util.BdvOverlay;
import bdv.util.BdvSource;
import fiji.plugin.globalbtrackmate.Model;
import fiji.plugin.globalbtrackmate.ModelChangeEvent;
import fiji.plugin.globalbtrackmate.SelectionChangeEvent;
import fiji.plugin.globalbtrackmate.SelectionModel;
import fiji.plugin.globalbtrackmate.Spot;
import fiji.plugin.globalbtrackmate.gui.displaysettings.DisplaySettings;
import fiji.plugin.globalbtrackmate.visualization.AbstractTrackMateModelView;
import fiji.plugin.globalbtrackmate.visualization.ViewUtils;
import fiji.plugin.globalbtrackmate.visualization.bigdataviewer.SpotEditTool;
import fiji.plugin.globalbtrackmate.visualization.bigdataviewer.SpotOverlay;
import fiji.plugin.globalbtrackmate.visualization.bigdataviewer.TrackOverlay;

public class BigDataViewerDisplayer extends AbstractTrackMateModelView {

	private static final boolean DEBUG = false;

	protected final ImagePlus imp;

	final Bdv bdv;

	protected final Img<FloatType> image;

	protected SpotOverlay spotOverlay;

	protected TrackOverlay trackOverlay;

	private SpotEditTool editTool;

	private Roi initialROI;

	public static final String KEY = "BIGDATAVIEWERDISPLAYER";

	/*
	 * CONSTRUCTORS
	 */

	public BigDataViewerDisplayer(final Model model, final SelectionModel selectionModel, final ImagePlus imp,
			final DisplaySettings displaySettings) {
		super(model, selectionModel, displaySettings);
		if (null != imp)
			this.imp = imp;
		else
			this.imp = ViewUtils.makeEmpytImagePlus(model);

		this.image = ImageJFunctions.convertFloat(imp);

		this.bdv = BdvFunctions.show(this.image, "BTM");

		this.spotOverlay = createSpotOverlay(displaySettings);
		this.trackOverlay = createTrackOverlay(displaySettings);
		displaySettings.listeners().add(() -> refresh());
	}

	public BigDataViewerDisplayer(final Model model, final SelectionModel selectionModel,
			final DisplaySettings displaySettings) {
		this(model, selectionModel, null, displaySettings);
	}

	/*
	 * PROTECTED METHODS
	 */

	/**
	 * Hook for subclassers. Instantiate here the overlay you want to use for the
	 * spots.
	 * 
	 * @param displaySettings
	 *
	 * @return the spot overlay
	 */
	protected SpotOverlay createSpotOverlay(final DisplaySettings displaySettings) {
		return new SpotOverlay(model, imp, displaySettings);
	}

	/**
	 * Hook for subclassers. Instantiate here the overlay you want to use for the
	 * spots.
	 * 
	 * @param displaySettings
	 *
	 * @return the track overlay
	 */
	protected TrackOverlay createTrackOverlay(final DisplaySettings displaySettings) {
		return new TrackOverlay(model, imp, displaySettings);
	}

	/*
	 * PUBLIC METHODS
	 */

	/**
	 * Exposes the {@link ImagePlus} on which the model is drawn by this view.
	 *
	 * @return the ImagePlus used in this view.
	 */
	public ImagePlus getImp() {
		return imp;
	}

	@Override
	public void modelChanged(final ModelChangeEvent event) {
		if (DEBUG)
			System.out.println("[BigDataViewerDisplayer] Received model changed event ID: " + event.getEventID()
					+ " from " + event.getSource());
		switch (event.getEventID()) {
		case ModelChangeEvent.MODEL_MODIFIED:
		case ModelChangeEvent.SPOTS_FILTERED:
		case ModelChangeEvent.SPOTS_COMPUTED:
		case ModelChangeEvent.TRACKS_VISIBILITY_CHANGED:
		case ModelChangeEvent.TRACKS_COMPUTED:
			refresh();
			break;
		}
	}

	@Override
	public void selectionChanged(final SelectionChangeEvent event) {
		// Highlight selection
		trackOverlay.setHighlight(selectionModel.getEdgeSelection());
		spotOverlay.setSpotSelection(selectionModel.getSpotSelection());
		// Center on last spot
		super.selectionChanged(event);
		// Redraw
		imp.updateAndDraw();
	}

	@Override
	public void centerViewOn(final Spot spot) {
		final int frame = spot.getFeature(Spot.FRAME).intValue();
		final double dz = imp.getCalibration().pixelDepth;
		final long z = Math.round(spot.getFeature(Spot.POSITION_Z) / dz) + 1;
		imp.setPosition(imp.getC(), (int) z, frame + 1);
	}

	@Override
	public void render() {
		initialROI = imp.getRoi();
		if (initialROI != null)
			imp.killRoi();

		clear();
		imp.setOpenAsHyperStack(true);
		if (!imp.isVisible())
			imp.show();

		addOverlay(spotOverlay);
		addOverlay(trackOverlay);
		imp.updateAndDraw();
		registerEditTool();
	}

	@Override
	public void refresh() {
		if (null != imp)
			imp.updateAndDraw();
	}

	@Override
	public void clear() {
		Overlay overlay = imp.getOverlay();
		if (overlay == null) {
			overlay = new Overlay();
			imp.setOverlay(overlay);
		}
		overlay.clear();

		if (initialROI != null)
			imp.getOverlay().add(initialROI);

		refresh();
	}

	public void addOverlay(final Roi overlay) {
		imp.getOverlay().add(overlay);
	}

	public SelectionModel getSelectionModel() {
		return selectionModel;
	}

	/*
	 * PRIVATE METHODS
	 */

	private void registerEditTool() {
		editTool = SpotEditTool.getInstance();
		if (!SpotEditTool.isLaunched())
			editTool.run("");

		editTool.register(imp, this);
	}

	@Override
	public String getKey() {
		return KEY;
	}

}
