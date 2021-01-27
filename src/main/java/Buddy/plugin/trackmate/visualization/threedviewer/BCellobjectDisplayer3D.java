package Buddy.plugin.trackmate.visualization.threedviewer;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.scijava.java3d.BadTransformException;
import org.scijava.vecmath.Color3f;
import org.scijava.vecmath.Color4f;
import org.scijava.vecmath.Point4d;

import Buddy.plugin.trackmate.BCellobjectCollection;
import Buddy.plugin.trackmate.Model;
import Buddy.plugin.trackmate.ModelChangeEvent;
import Buddy.plugin.trackmate.SelectionChangeEvent;
import Buddy.plugin.trackmate.SelectionModel;
import Buddy.plugin.trackmate.util.TMUtils;
import Buddy.plugin.trackmate.visualization.AbstractTrackMateModelView;
import Buddy.plugin.trackmate.visualization.FeatureColorGenerator;
import Buddy.plugin.trackmate.visualization.TrackColorGenerator;
import Buddy.plugin.trackmate.visualization.TrackMateModelView;
import ij3d.Content;
import ij3d.ContentInstant;
import ij3d.Image3DUniverse;
import pluginTools.InteractiveBud;
import budDetector.BCellobject;

public class BCellobjectDisplayer3D extends AbstractTrackMateModelView {

	static final String KEY = "3DVIEWER";

	public static final int DEFAULT_RESAMPLING_FACTOR = 4;

	// public static final int DEFAULT_THRESHOLD = 50;

	private static final boolean DEBUG = false;

	private static final String TRACK_CONTENT_NAME = "Tracks";

	private static final String BCellobject_CONTENT_NAME = "BCellobjects";

	private TreeMap<Integer, BCellobjectGroupNode<BCellobject>> blobs;

	private TrackDisplayNode trackNode;

	private Content BCellobjectContent;

	private Content trackContent;

	private final Image3DUniverse universe;
	
	public final InteractiveBud parent;

	// For highlighting
	private ArrayList<BCellobject> previousBCellobjectHighlight;

	private HashMap<BCellobject, Color3f> previousColorHighlight;

	private HashMap<BCellobject, Integer> previousFrameHighlight;

	private TreeMap<Integer, ContentInstant> contentAllFrames;

	public BCellobjectDisplayer3D(final InteractiveBud parent, final Model model, final SelectionModel selectionModel,
			final Image3DUniverse universe) {
		super(parent, model, selectionModel);
		this.parent = parent;
		this.universe = universe;
		setModel(model);
	}

	/*
	 * OVERRIDDEN METHODS
	 */

	@Override
	public void modelChanged(final ModelChangeEvent event) {
		if (DEBUG) {
			System.out.println("[BCellobjectDisplayer3D: modelChanged() called with event ID: " + event.getEventID());
			System.out.println(event);
		}

		switch (event.getEventID()) {

		case ModelChangeEvent.BCellobject_COMPUTED:
			makeBCellobjectContent();
			break;

		case ModelChangeEvent.BCellobject_FILTERED:
			for (final int frame : blobs.keySet()) {
				final BCellobjectGroupNode<BCellobject> frameBlobs = blobs.get(frame);
				for (final Iterator<BCellobject> it = model.getBCellobjects().iterator(frame); it.hasNext();) {
					final BCellobject BCellobject = it.next();
					final boolean visible = true;
					frameBlobs.setVisible(BCellobject, visible);
				}
			}
			break;

		case ModelChangeEvent.TRACKS_COMPUTED:
			trackContent = makeTrackContent();
			universe.removeContent(TRACK_CONTENT_NAME);
			universe.addContent(trackContent);
			break;

		case ModelChangeEvent.TRACKS_VISIBILITY_CHANGED:
			updateTrackColors();
			trackNode.setTrackVisible(model.getTrackModel().trackIDs(true));
			break;

		case ModelChangeEvent.MODEL_MODIFIED: {
			/*
			 * We do not do anything. I could not find a good way to dynamically change the
			 * content of a 3D viewer content. So the 3D viewer just shows a snapshot of the
			 * TrackMate model when it was launched, and is not kept in sync with
			 * modifications afterwards.
			 */
			break;
		}

		default: {
			System.err.println("[BCellobjectDisplayer3D] Unknown event ID: " + event.getEventID());
		}
		}
	}

	@Override
	public void selectionChanged(final SelectionChangeEvent event) {
		// Highlight edges.
		trackNode.setSelection(selectionModel.getEdgeSelection());
		trackNode.refresh();
		// Highlight BCellobjects.
		displaySpotCollection((Integer) displaySettings
				.get(KEY_TRACK_DISPLAY_MODE) == TrackMateModelView.TRACK_DISPLAY_MODE_SELECTION_ONLY);
		// Center on last BCellobject
		super.selectionChanged(event);
	}

	@Override
	public void centerViewOn(final BCellobject BCellobject) {
		final int frame = BCellobject.getFeature(BCellobject.POSITION_T).intValue();
		universe.showTimepoint(frame);
	}

	@Override
	public void refresh() {
		if (null != trackNode)
			trackNode.refresh();
	}

	@Override
	public void render() {
		if (DEBUG)
			System.out.println("[BCellobjectDisplayer3D] Call to render().");

		updateRadiuses();
		updateBCellobjectColors();
		BCellobjectContent.setVisible((Boolean) displaySettings.get(KEY_BCellobjectS_VISIBLE));
		if (null != trackContent) {
			trackContent.setVisible((Boolean) displaySettings.get(KEY_TRACKS_VISIBLE));
			trackNode.setTrackDisplayMode((Integer) displaySettings.get(KEY_TRACK_DISPLAY_MODE));
			trackNode.setTrackDisplayDepth((Integer) displaySettings.get(KEY_TRACK_DISPLAY_DEPTH));
			updateTrackColors();
			trackNode.refresh();
			universe.updateStartAndEndTime(blobs.firstKey(), blobs.lastKey());
			universe.updateTimelineGUI();
		}
	}

	@Override
	public void setDisplaySettings(final String key, final Object value) {
		super.setDisplaySettings(key, value);
		// Treat change of radius
		if (key == KEY_BCellobject_RADIUS_RATIO) {
			updateRadiuses();
		} else if (key == KEY_BCellobject_COLORING) {
			updateBCellobjectColors();
		} else if (key == KEY_TRACK_COLORING) {
			updateTrackColors();
		} else if (key == KEY_DISPLAY_BCellobject_NAMES) {
			for (final int frame : blobs.keySet()) {
				blobs.get(frame).setShowLabels((Boolean) value);
			}
		} else if (key == KEY_BCellobjectS_VISIBLE) {
			BCellobjectContent.setVisible((Boolean) value);
		} else if (key == KEY_TRACKS_VISIBLE && null != trackContent) {
			trackContent.setVisible((Boolean) value);
		} else if (key == KEY_TRACK_DISPLAY_MODE && null != trackNode) {
			trackNode.setTrackDisplayMode((Integer) value);
			displaySpotCollection((Integer) value == TrackMateModelView.TRACK_DISPLAY_MODE_SELECTION_ONLY);
		} else if (key == KEY_TRACK_DISPLAY_DEPTH && null != trackNode) {
			trackNode.setTrackDisplayDepth((Integer) value);
		}
	}

	@Override
	public void clear() {
		universe.removeContent(BCellobject_CONTENT_NAME);
		universe.removeContent(TRACK_CONTENT_NAME);
	}

	/*
	 * PRIVATE METHODS
	 */

	private void setModel(final Model model) {
		if (model.getBCellobjects() != null) {
			makeBCellobjectContent();
		}
		if (model.getTrackModel().nTracks(true) > 0) {
			trackContent = makeTrackContent();
			universe.removeContent(TRACK_CONTENT_NAME);
			universe.addContentLater(trackContent);
		}
	}

	private Content makeTrackContent() {
		// Prepare tracks instant
		trackNode = new TrackDisplayNode(model);
		universe.addTimelapseListener(trackNode);

		// Pass tracks instant to all instants
		final TreeMap<Integer, ContentInstant> instants = new TreeMap<>();
		final ContentInstant trackCI = new ContentInstant("Tracks_all_frames");
		trackCI.display(trackNode);
		instants.put(0, trackCI);
		final Content tc = new Content(TRACK_CONTENT_NAME, instants);
		tc.setShowAllTimepoints(true);
		tc.showCoordinateSystem(false);
		return tc;
	}

	private void makeBCellobjectContent() {

		blobs = new TreeMap<>();
		contentAllFrames = new TreeMap<>();
		final double radiusRatio = (Double) displaySettings.get(KEY_BCellobject_RADIUS_RATIO);
		final BCellobjectCollection BCellobjects = model.getBCellobjects();
		@SuppressWarnings("unchecked")
		final FeatureColorGenerator<BCellobject> BCellobjectColorGenerator = (FeatureColorGenerator<BCellobject>) displaySettings
				.get(KEY_BCellobject_COLORING);

		for (final int frame : BCellobjects.keySet()) {
			if (BCellobjects.getNBCellobjects(frame) == 0) {
				continue; // Do not create content for empty frames
			}
			buildFrameContent(BCellobjects, frame, radiusRatio, BCellobjectColorGenerator);
		}

		BCellobjectContent = new Content(BCellobject_CONTENT_NAME, contentAllFrames);
		BCellobjectContent.showCoordinateSystem(false);
		universe.removeContent(BCellobject_CONTENT_NAME);
		universe.addContentLater(BCellobjectContent);
	}

	private void buildFrameContent(final BCellobjectCollection BCellobjects, final Integer frame,
			final double radiusRatio, final FeatureColorGenerator<BCellobject> BCellobjectColorGenerator) {
		final Map<BCellobject, Point4d> centers = new HashMap<>(BCellobjects.getNBCellobjects(frame));
		final Map<BCellobject, Color4f> colors = new HashMap<>(BCellobjects.getNBCellobjects(frame));
		final double[] coords = new double[3];

		for (final Iterator<BCellobject> it = BCellobjects.iterator(frame); it.hasNext();) {
			final BCellobject BCellobject = it.next();
			TMUtils.localize(BCellobject, coords);
			final Double radius = BCellobject.getFeature(BCellobject.Size);
			final double[] pos = new double[] { coords[0], coords[1], coords[2], radius * radiusRatio };
			centers.put(BCellobject, new Point4d(pos));
			final Color4f col = new Color4f(BCellobjectColorGenerator.color(BCellobject));
			col.w = 0f;
			colors.put(BCellobject, col);
		}
		final BCellobjectGroupNode<BCellobject> blobGroup = new BCellobjectGroupNode<>(centers, colors);
		final ContentInstant contentThisFrame = new ContentInstant("BCellobjects_frame_" + frame);

		try {
			contentThisFrame.display(blobGroup);
		} catch (final BadTransformException bte) {
			System.err.println("Bad content for frame " + frame + ". Generated an exception:\n"
					+ bte.getLocalizedMessage() + "\nContent was:\n" + blobGroup.toString());
		}

		// Set visibility:
		if (BCellobjects.getNBCellobjects(frame) > 0) {
			blobGroup.setVisible(BCellobjects.iterable(frame, true));
		}

		contentAllFrames.put(frame, contentThisFrame);
		blobs.put(frame, blobGroup);
	}

	private void updateRadiuses() {
		final double radiusRatio = (Double) displaySettings.get(KEY_BCellobject_RADIUS_RATIO);

		for (final int frame : blobs.keySet()) {
			final BCellobjectGroupNode<BCellobject> BCellobjectGroup = blobs.get(frame);
			for (final Iterator<BCellobject> iterator = model.getBCellobjects().iterator(frame); iterator.hasNext();) {
				final BCellobject BCellobject = iterator.next();
				BCellobjectGroup.setRadius(BCellobject, radiusRatio * BCellobject.getFeature(BCellobject.Size));
			}
		}
	}

	private void updateBCellobjectColors() {
		@SuppressWarnings("unchecked")
		final FeatureColorGenerator<BCellobject> BCellobjectColorGenerator = (FeatureColorGenerator<BCellobject>) displaySettings
				.get(KEY_BCellobject_COLORING);

		for (final int frame : blobs.keySet()) {
			final BCellobjectGroupNode<BCellobject> BCellobjectGroup = blobs.get(frame);
			for (final Iterator<BCellobject> iterator = model.getBCellobjects().iterator(frame); iterator.hasNext();) {
				final BCellobject BCellobject = iterator.next();
				BCellobjectGroup.setColor(BCellobject, new Color3f(BCellobjectColorGenerator.color(BCellobject)));
			}
		}
	}

	private void updateTrackColors() {
		final TrackColorGenerator colorGenerator = (TrackColorGenerator) displaySettings.get(KEY_TRACK_COLORING);

		for (final Integer trackID : model.getTrackModel().trackIDs(true)) {
			colorGenerator.setCurrentTrackID(trackID);
			for (final DefaultWeightedEdge edge : model.getTrackModel().trackEdges(trackID)) {
				final Color color = colorGenerator.color(edge);
				trackNode.setColor(edge, color);
			}
		}
	}

	private void highlightBCellobjects(final Collection<BCellobject> BCellobjects) {
		// Restore previous display settings for previously highlighted BCellobject
		if (null != previousBCellobjectHighlight)
			for (final BCellobject BCellobject : previousBCellobjectHighlight) {
				final Integer frame = previousFrameHighlight.get(BCellobject);
				if (null != frame) {
					final BCellobjectGroupNode<BCellobject> BCellobjectGroupNode = blobs.get(frame);
					if (null != BCellobjectGroupNode) {
						BCellobjectGroupNode.setColor(BCellobject, previousColorHighlight.get(BCellobject));
					}
				}
			}

		/*
		 * Don't color BCellobject selection in the highlight color if we are displaying
		 * selection only.
		 */
		final Integer trackDisplayMode = (Integer) displaySettings.get(KEY_TRACK_DISPLAY_MODE);
		if (trackDisplayMode == TrackMateModelView.TRACK_DISPLAY_MODE_SELECTION_ONLY)
			return;

		/*
		 * Store previous color value and color the BCellobject selection with the
		 * highlight color.
		 */

		previousBCellobjectHighlight = new ArrayList<>(BCellobjects.size());
		previousColorHighlight = new HashMap<>(BCellobjects.size());
		previousFrameHighlight = new HashMap<>(BCellobjects.size());

		final Color3f highlightColor = new Color3f((Color) displaySettings.get(KEY_HIGHLIGHT_COLOR));
		for (final BCellobject BCellobject : BCellobjects) {
			final int frame = BCellobject.getFeature(BCellobject.POSITION_T).intValue();
			// Store current settings
			previousBCellobjectHighlight.add(BCellobject);
			final BCellobjectGroupNode<BCellobject> BCellobjectGroupNode = blobs.get(frame);
			if (null != BCellobjectGroupNode) {
				previousColorHighlight.put(BCellobject, BCellobjectGroupNode.getColor3f(BCellobject));
				previousFrameHighlight.put(BCellobject, frame);
				// Update target BCellobject display
				blobs.get(frame).setColor(BCellobject, highlightColor);
			}
		}
	}

	/**
	 * Changes the visibility of the displayed BCellobject.
	 *
	 * @param onlySpotCollection
	 *            If <code>true</code>, we display on the BCellobjects in the
	 *            selection. Otherwise we display all BCellobjects marked as
	 *            visible.
	 */
	private void displaySpotCollection(final boolean onlySpotCollection) {
		final Set<BCellobject> SpotCollection = selectionModel.getBCellobjectSelection();
		if (onlySpotCollection) {
			if (SpotCollection.isEmpty()) {
				for (final Integer frame : blobs.keySet()) {
					blobs.get(frame).setVisible(false);
				}
				return;
			}

			// Sort BCellobjects in selection per frame.
			final HashMap<Integer, ArrayList<BCellobject>> BCellobjectsPerFrame = new HashMap<>(blobs.size());
			for (final Integer frame : blobs.keySet()) {
				final ArrayList<BCellobject> BCellobjects = new ArrayList<>();
				BCellobjectsPerFrame.put(frame, BCellobjects);
			}

			for (final BCellobject BCellobject : SpotCollection) {
				final int frame = BCellobject.getFeature(BCellobject.POSITION_T).intValue();
				final ArrayList<BCellobject> BCellobjects = BCellobjectsPerFrame.get(Integer.valueOf(frame));
				BCellobjects.add(BCellobject);
			}

			// Mark then as visible, the others as invisible.
			for (final Integer frame : BCellobjectsPerFrame.keySet()) {
				blobs.get(frame).setVisible(BCellobjectsPerFrame.get(frame));
			}

			// Restore proper color.
			updateBCellobjectColors();
			updateTrackColors();
		} else {
			// Make all visible BCellobjects visible here.
			for (final int frame : blobs.keySet()) {
				final Iterable<BCellobject> BCellobjects = model.getBCellobjects().iterable(frame, true);
				blobs.get(frame).setVisible(BCellobjects);
			}
			highlightBCellobjects(SpotCollection);
		}
	}

	@Override
	public String getKey() {
		return KEY;
	}
}