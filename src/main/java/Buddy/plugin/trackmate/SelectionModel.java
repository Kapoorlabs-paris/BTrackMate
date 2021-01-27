package Buddy.plugin.trackmate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.traverse.GraphIterator;

import budDetector.BCellobject;

/**
 * A component of {@link Model} that handles BCellobject and edges selection.
 * 
 * @author Jean-Yves Tinevez
 */
public class SelectionModel {

	private static final boolean DEBUG = false;

	/** The BCellobject current selection. */
	private Set<BCellobject> BCellobjectSelection = new HashSet<>();
	/** The edge current selection. */
	private Set<DefaultWeightedEdge> edgeSelection = new HashSet<>();
	/** The list of listener listening to change in selection. */
	private List<SelectionChangeListener> selectionChangeListeners = new ArrayList<>();

	private final Model model;

	/*
	 * DEFAULT VISIBILITY CONSTRUCTOR
	 */

	public SelectionModel(Model parent) {
		this.model = parent;
	}

	/*
	 * DEAL WITH SELECTION CHANGE LISTENER
	 */

	public boolean addSelectionChangeListener(SelectionChangeListener listener) {
		return selectionChangeListeners.add(listener);
	}

	public boolean removeSelectionChangeListener(SelectionChangeListener listener) {
		return selectionChangeListeners.remove(listener);
	}

	public List<SelectionChangeListener> getSelectionChangeListener() {
		return selectionChangeListeners;
	}

	/*
	 * SELECTION CHANGES
	 */

	public void clearSelection() {
		if (DEBUG)
			System.out.println("[SelectionModel] Clearing selection");
		// Prepare event
		Map<BCellobject, Boolean> BCellobjectMap = new HashMap<>(BCellobjectSelection.size());
		for (BCellobject BCellobject : BCellobjectSelection)
			BCellobjectMap.put(BCellobject, false);
		Map<DefaultWeightedEdge, Boolean> edgeMap = new HashMap<>(edgeSelection.size());
		for (DefaultWeightedEdge edge : edgeSelection)
			edgeMap.put(edge, false);
		SelectionChangeEvent event = new SelectionChangeEvent(this, BCellobjectMap, edgeMap);
		// Clear fields
		clearBCellobjectSelection();
		clearEdgeSelection();
		// Fire event
		for (SelectionChangeListener listener : selectionChangeListeners)
			listener.selectionChanged(event);
	}

	public void clearBCellobjectSelection() {
		if (DEBUG)
			System.out.println("[SelectionModel] Clearing BCellobject selection");
		// Prepare event
		Map<BCellobject, Boolean> BCellobjectMap = new HashMap<>(BCellobjectSelection.size());
		for (BCellobject BCellobject : BCellobjectSelection)
			BCellobjectMap.put(BCellobject, false);
		SelectionChangeEvent event = new SelectionChangeEvent(this, BCellobjectMap, null);
		// Clear field
		BCellobjectSelection.clear();
		// Fire event
		for (SelectionChangeListener listener : selectionChangeListeners)
			listener.selectionChanged(event);
	}

	public void clearEdgeSelection() {
		if (DEBUG)
			System.out.println("[SelectionModel] Clearing edge selection");
		// Prepare event
		Map<DefaultWeightedEdge, Boolean> edgeMap = new HashMap<>(edgeSelection.size());
		for (DefaultWeightedEdge edge : edgeSelection)
			edgeMap.put(edge, false);
		SelectionChangeEvent event = new SelectionChangeEvent(this, null, edgeMap);
		// Clear field
		edgeSelection.clear();
		// Fire event
		for (SelectionChangeListener listener : selectionChangeListeners)
			listener.selectionChanged(event);
	}

	public void addBCellobjectToSelection(final BCellobject BCellobject) {
		if (!BCellobjectSelection.add(BCellobject))
			return; // Do nothing if already present in selection
		if (DEBUG)
			System.out.println("[SelectionModel] Adding BCellobject " + BCellobject + " to selection");
		Map<BCellobject, Boolean> BCellobjectMap = new HashMap<>(1);
		BCellobjectMap.put(BCellobject, true);
		if (DEBUG)
			System.out.println("[SelectionModel] Seding event to listeners: " + selectionChangeListeners);
		SelectionChangeEvent event = new SelectionChangeEvent(this, BCellobjectMap, null);
		for (SelectionChangeListener listener : selectionChangeListeners)
			listener.selectionChanged(event);
	}

	public void removeBCellobjectFromSelection(final BCellobject BCellobject) {
		if (!BCellobjectSelection.remove(BCellobject))
			return; // Do nothing was not already present in selection
		if (DEBUG)
			System.out.println("[SelectionModel] Removing BCellobject " + BCellobject + " from selection");
		Map<BCellobject, Boolean> BCellobjectMap = new HashMap<>(1);
		BCellobjectMap.put(BCellobject, false);
		SelectionChangeEvent event = new SelectionChangeEvent(this, BCellobjectMap, null);
		for (SelectionChangeListener listener : selectionChangeListeners)
			listener.selectionChanged(event);
	}

	public void addBCellobjectToSelection(final Collection<BCellobject> BCellobjects) {
		Map<BCellobject, Boolean> BCellobjectMap = new HashMap<>(BCellobjects.size());
		for (BCellobject BCellobject : BCellobjects) {
			if (BCellobjectSelection.add(BCellobject)) {
				BCellobjectMap.put(BCellobject, true);
				if (DEBUG)
					System.out.println("[SelectionModel] Adding BCellobject " + BCellobject + " to selection");
			}
		}
		SelectionChangeEvent event = new SelectionChangeEvent(this, BCellobjectMap, null);
		if (DEBUG)
			System.out.println("[SelectionModel] Seding event " + event.hashCode() + " to "
					+ selectionChangeListeners.size() + " listeners: " + selectionChangeListeners);
		for (SelectionChangeListener listener : selectionChangeListeners)
			listener.selectionChanged(event);
	}

	public void removeBCellobjectFromSelection(final Collection<BCellobject> BCellobjects) {
		Map<BCellobject, Boolean> BCellobjectMap = new HashMap<>(BCellobjects.size());
		for (BCellobject BCellobject : BCellobjects) {
			if (BCellobjectSelection.remove(BCellobject)) {
				BCellobjectMap.put(BCellobject, false);
				if (DEBUG)
					System.out.println("[SelectionModel] Removing BCellobject " + BCellobject + " from selection");
			}
		}
		SelectionChangeEvent event = new SelectionChangeEvent(this, BCellobjectMap, null);
		for (SelectionChangeListener listener : selectionChangeListeners)
			listener.selectionChanged(event);
	}

	public void addEdgeToSelection(final DefaultWeightedEdge edge) {
		if (!edgeSelection.add(edge))
			return; // Do nothing if already present in selection
		if (DEBUG)
			System.out.println("[SelectionModel] Adding edge " + edge + " to selection");
		Map<DefaultWeightedEdge, Boolean> edgeMap = new HashMap<>(1);
		edgeMap.put(edge, true);
		SelectionChangeEvent event = new SelectionChangeEvent(this, null, edgeMap);
		for (SelectionChangeListener listener : selectionChangeListeners)
			listener.selectionChanged(event);

	}

	public void removeEdgeFromSelection(final DefaultWeightedEdge edge) {
		if (!edgeSelection.remove(edge))
			return; // Do nothing if already present in selection
		if (DEBUG)
			System.out.println("[SelectionModel] Removing edge " + edge + " from selection");
		Map<DefaultWeightedEdge, Boolean> edgeMap = new HashMap<>(1);
		edgeMap.put(edge, false);
		SelectionChangeEvent event = new SelectionChangeEvent(this, null, edgeMap);
		for (SelectionChangeListener listener : selectionChangeListeners)
			listener.selectionChanged(event);

	}

	public void addEdgeToSelection(final Collection<DefaultWeightedEdge> edges) {
		Map<DefaultWeightedEdge, Boolean> edgeMap = new HashMap<>(edges.size());
		for (DefaultWeightedEdge edge : edges) {
			if (edgeSelection.add(edge)) {
				edgeMap.put(edge, true);
				if (DEBUG)
					System.out.println("[SelectionModel] Adding edge " + edge + " to selection");
			}
		}
		SelectionChangeEvent event = new SelectionChangeEvent(this, null, edgeMap);
		for (SelectionChangeListener listener : selectionChangeListeners)
			listener.selectionChanged(event);
	}

	public void removeEdgeFromSelection(final Collection<DefaultWeightedEdge> edges) {
		Map<DefaultWeightedEdge, Boolean> edgeMap = new HashMap<>(edges.size());
		for (DefaultWeightedEdge edge : edges) {
			if (edgeSelection.remove(edge)) {
				edgeMap.put(edge, false);
				if (DEBUG)
					System.out.println("[SelectionModel] Removing edge " + edge + " from selection");
			}
		}
		SelectionChangeEvent event = new SelectionChangeEvent(this, null, edgeMap);
		for (SelectionChangeListener listener : selectionChangeListeners)
			listener.selectionChanged(event);
	}

	public Set<BCellobject> getBCellobjectSelection() {
		return BCellobjectSelection;
	}

	public Set<DefaultWeightedEdge> getEdgeSelection() {
		return edgeSelection;
	}

	/*
	 * SPECIAL METHODS
	 */

	/**
	 * Search and add all BCellobjects and links belonging to the same track(s) that
	 * of given <code>BCellobjects</code> and <code>edges</code> to current
	 * selection. A <code>direction</code> parameter allow specifying whether we
	 * should include only parts upwards in time, downwards in time or all the way
	 * through.
	 * 
	 * @param BCellobjects
	 *            the BCellobjects to include in search
	 * @param edges
	 *            the edges to include in search
	 * @param direction
	 *            the direction to go when searching. Positive integers will result
	 *            in searching upwards in time, negative integers downwards in time
	 *            and 0 all the way through.
	 */
	public void selectTrack(final Collection<BCellobject> BCellobjects, final Collection<DefaultWeightedEdge> edges,
			final int direction) {

		HashSet<BCellobject> inspectionBCellobjects = new HashSet<>(BCellobjects);

		for (DefaultWeightedEdge edge : edges) {
			// We add connected BCellobjects to the list of BCellobjects to inspect
			inspectionBCellobjects.add(model.getTrackModel().getEdgeSource(edge));
			inspectionBCellobjects.add(model.getTrackModel().getEdgeTarget(edge));
		}

		// Walk across tracks to build selection
		final HashSet<BCellobject> lBCellobjectSelection = new HashSet<>();
		final HashSet<DefaultWeightedEdge> lEdgeSelection = new HashSet<>();

		if (direction == 0) { // Unconditionally
			for (BCellobject BCellobject : inspectionBCellobjects) {
				lBCellobjectSelection.add(BCellobject);
				GraphIterator<BCellobject, DefaultWeightedEdge> walker = model.getTrackModel()
						.getDepthFirstIterator(BCellobject, false);
				while (walker.hasNext()) {
					BCellobject target = walker.next();
					lBCellobjectSelection.add(target);
					// Deal with edges
					Set<DefaultWeightedEdge> targetEdges = model.getTrackModel().edgesOf(target);
					for (DefaultWeightedEdge targetEdge : targetEdges) {
						lEdgeSelection.add(targetEdge);
					}
				}
			}

		} else { // Only upward or backward in time
			for (BCellobject BCellobject : inspectionBCellobjects) {
				lBCellobjectSelection.add(BCellobject);

				// A bit more complicated: we want to walk in only one direction,
				// when branching is occurring, we do not want to get back in time.
				Stack<BCellobject> stack = new Stack<>();
				stack.add(BCellobject);
				while (!stack.isEmpty()) {
					BCellobject inspected = stack.pop();
					Set<DefaultWeightedEdge> targetEdges = model.getTrackModel().edgesOf(inspected);
					for (DefaultWeightedEdge targetEdge : targetEdges) {
						BCellobject other;
						if (direction > 0) {
							// Upward in time: we just have to search through edges using their source
							// BCellobjects
							other = model.getTrackModel().getEdgeSource(targetEdge);
						} else {
							other = model.getTrackModel().getEdgeTarget(targetEdge);
						}

						if (other != inspected) {
							lBCellobjectSelection.add(other);
							lEdgeSelection.add(targetEdge);
							stack.add(other);
						}
					}
				}
			}
		}

		// Cut "tail": remove the first an last edges in time, so that the selection
		// only has conencted
		// edges in it.
		ArrayList<DefaultWeightedEdge> edgesToRemove = new ArrayList<>();
		for (DefaultWeightedEdge edge : lEdgeSelection) {
			BCellobject source = model.getTrackModel().getEdgeSource(edge);
			BCellobject target = model.getTrackModel().getEdgeTarget(edge);
			if (!(lBCellobjectSelection.contains(source) && lBCellobjectSelection.contains(target))) {
				edgesToRemove.add(edge);
			}
		}
		lEdgeSelection.removeAll(edgesToRemove);

		// Set selection
		addBCellobjectToSelection(lBCellobjectSelection);
		addEdgeToSelection(lEdgeSelection);
	}

}
