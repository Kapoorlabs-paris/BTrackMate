package Buddy.plugin.trackmate.util;

import Buddy.plugin.trackmate.Model;
import Buddy.plugin.trackmate.SelectionModel;
import Buddy.plugin.trackmate.graph.TimeDirectedNeighborIndex;
import budDetector.BCellobject;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.jgrapht.graph.DefaultWeightedEdge;

public class TrackNavigator {

	private final Model model;
	private final SelectionModel selectionModel;
	private final TimeDirectedNeighborIndex neighborIndex;

	public TrackNavigator(final Model model, final SelectionModel selectionModel) {
		this.model = model;
		this.selectionModel = selectionModel;
		this.neighborIndex = model.getTrackModel().getDirectedNeighborIndex();
	}

	public synchronized void nextTrack() {
		final BCellobject BCellobject = getABCellobject();
		if (null == BCellobject) {
			return;
		}

		final Set<Integer> trackIDs = model.getTrackModel().trackIDs(true); // if only it was navigable...
		if (trackIDs.isEmpty()) {
			return;
		}

		Integer trackID = model.getTrackModel().trackIDOf(BCellobject);
		if (null == trackID) {
			// No track? Then move to the first one.
			trackID = model.getTrackModel().trackIDs(true).iterator().next();
		}

		final Iterator<Integer> it = trackIDs.iterator();
		Integer nextTrackID = null;
		while (it.hasNext()) {
			final Integer id = it.next();
			if (id.equals(trackID)) {
				if (it.hasNext()) {
					nextTrackID = it.next();
					break;
				}
				nextTrackID = trackIDs.iterator().next(); // loop
			}
		}

		final Set<BCellobject> BCellobjects = model.getTrackModel().trackBCellobjects(nextTrackID);
		final TreeSet<BCellobject> ring = new TreeSet<>(BCellobject.frameComparator);
		ring.addAll(BCellobjects);
		BCellobject target = ring.ceiling(BCellobject);
		if (null == target) {
			target = ring.floor(BCellobject);
		}

		selectionModel.clearSelection();
		selectionModel.addBCellobjectToSelection(target);
	}

	public synchronized void previousTrack() {
		final BCellobject BCellobject = getABCellobject();
		if (null == BCellobject) {
			return;
		}

		Integer trackID = model.getTrackModel().trackIDOf(BCellobject);
		final Set<Integer> trackIDs = model.getTrackModel().trackIDs(true); // if only it was navigable...
		if (trackIDs.isEmpty()) {
			return;
		}

		Integer lastID = null;
		for (final Integer id : trackIDs) {
			lastID = id;
		}

		if (null == trackID) {
			// No track? Then take the last one.
			trackID = lastID;
		}

		final Iterator<Integer> it = trackIDs.iterator();
		Integer previousTrackID = null;
		while (it.hasNext()) {
			final Integer id = it.next();
			if (id.equals(trackID)) {
				if (previousTrackID != null) {
					break;
				}
				previousTrackID = lastID;
				break;
			}
			previousTrackID = id;
		}

		final Set<BCellobject> BCellobjects = model.getTrackModel().trackBCellobjects(previousTrackID);
		final TreeSet<BCellobject> ring = new TreeSet<>(BCellobject.frameComparator);
		ring.addAll(BCellobjects);
		BCellobject target = ring.ceiling(BCellobject);
		if (null == target) {
			target = ring.floor(BCellobject);
		}

		selectionModel.clearSelection();
		selectionModel.addBCellobjectToSelection(target);
	}

	public synchronized void nextSibling() {
		final BCellobject BCellobject = getABCellobject();
		if (null == BCellobject) {
			return;
		}

		final Integer trackID = model.getTrackModel().trackIDOf(BCellobject);
		if (null == trackID) {
			return;
		}

		final int frame = BCellobject.getFeature(BCellobject.POSITION_T).intValue();
		final TreeSet<BCellobject> ring = new TreeSet<>(BCellobject.nameComparator);

		final Set<BCellobject> BCellobjects = model.getTrackModel().trackBCellobjects(trackID);
		for (final BCellobject s : BCellobjects) {
			final int fs = s.getFeature(BCellobject.POSITION_T).intValue();
			if (frame == fs && s != BCellobject) {
				ring.add(s);
			}
		}

		if (!ring.isEmpty()) {
			BCellobject nextSibling = ring.ceiling(BCellobject);
			if (null == nextSibling) {
				nextSibling = ring.first(); // loop
			}
			selectionModel.clearSelection();
			selectionModel.addBCellobjectToSelection(nextSibling);
		}
	}

	public synchronized void previousSibling() {
		final BCellobject BCellobject = getABCellobject();
		if (null == BCellobject) {
			return;
		}

		final Integer trackID = model.getTrackModel().trackIDOf(BCellobject);
		if (null == trackID) {
			return;
		}

		final int frame = BCellobject.getFeature(BCellobject.POSITION_T).intValue();
		final TreeSet<BCellobject> ring = new TreeSet<>(BCellobject.nameComparator);

		final Set<BCellobject> BCellobjects = model.getTrackModel().trackBCellobjects(trackID);
		for (final BCellobject s : BCellobjects) {
			final int fs = s.getFeature(BCellobject.POSITION_T).intValue();
			if (frame == fs && s != BCellobject) {
				ring.add(s);
			}
		}

		if (!ring.isEmpty()) {
			BCellobject previousSibling = ring.floor(BCellobject);
			if (null == previousSibling) {
				previousSibling = ring.last(); // loop
			}
			selectionModel.clearSelection();
			selectionModel.addBCellobjectToSelection(previousSibling);
		}
	}

	public synchronized void previousInTime() {
		final BCellobject BCellobject = getABCellobject();
		if (null == BCellobject) {
			return;
		}

		final Set<BCellobject> predecessors = neighborIndex.predecessorsOf(BCellobject);
		if (!predecessors.isEmpty()) {
			final BCellobject next = predecessors.iterator().next();
			selectionModel.clearSelection();
			selectionModel.addBCellobjectToSelection(next);
		}
	}

	public synchronized void nextInTime() {
		final BCellobject BCellobject = getABCellobject();
		if (null == BCellobject) {
			return;
		}

		final Set<BCellobject> successors = neighborIndex.successorsOf(BCellobject);
		if (!successors.isEmpty()) {
			final BCellobject next = successors.iterator().next();
			selectionModel.clearSelection();
			selectionModel.addBCellobjectToSelection(next);
		}
	}

	/*
	 * STATIC METHODS
	 */

	/**
	 * Return a meaningful BCellobject from the current selection, or <code>null</code>
	 * if the selection is empty.
	 */
	private BCellobject getABCellobject() {
		// Get it from BCellobject selection
		final Set<BCellobject> BCellobjectSelection = selectionModel.getBCellobjectSelection();
		if (!BCellobjectSelection.isEmpty()) {
			final Iterator<BCellobject> it = BCellobjectSelection.iterator();
			BCellobject BCellobject = it.next();
			int minFrame = BCellobject.getFeature(BCellobject.POSITION_T).intValue();
			while (it.hasNext()) {
				final BCellobject s = it.next();
				final int frame = s.getFeature(BCellobject.POSITION_T).intValue();
				if (frame < minFrame) {
					minFrame = frame;
					BCellobject = s;
				}
			}
			return BCellobject;
		}

		// Nope? Then get it from edges
		final Set<DefaultWeightedEdge> edgeSelection = selectionModel.getEdgeSelection();
		if (!edgeSelection.isEmpty()) {
			final Iterator<DefaultWeightedEdge> it = edgeSelection.iterator();
			final DefaultWeightedEdge edge = it.next();
			BCellobject BCellobject = model.getTrackModel().getEdgeSource(edge);
			int minFrame = BCellobject.getFeature(BCellobject.POSITION_T).intValue();
			while (it.hasNext()) {
				final DefaultWeightedEdge e = it.next();
				final BCellobject s = model.getTrackModel().getEdgeSource(e);
				final int frame = s.getFeature(BCellobject.POSITION_T).intValue();
				if (frame < minFrame) {
					minFrame = frame;
					BCellobject = s;
				}
			}
			return BCellobject;
		}

		// Still nothing? Then give up.
		return null;
	}
}
