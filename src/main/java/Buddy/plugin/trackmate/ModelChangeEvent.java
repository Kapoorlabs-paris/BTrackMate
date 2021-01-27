package Buddy.plugin.trackmate;

import java.util.Collection;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import budDetector.BCellobject;

import org.jgrapht.graph.DefaultWeightedEdge;

public class ModelChangeEvent extends EventObject {

	private static final long serialVersionUID = -1L;
	/** Indicate that a BCellobject was added to the model. */
	public static final int FLAG_BCellobject_ADDED = 0;
	
	/** Indicate that a BCellobject was removed from the model. */
	public static final int FLAG_BCellobject_REMOVED = 1;
	/**
	 * Indicate a modification of the features of a BCellobject. It may have changed
	 * of position and feature, but not of frame.
	 */
	public static final int FLAG_BCellobject_MODIFIED = 2;
	/**
	 * Indicate that a BCellobject has changed of frame, and possible of position,
	 * features, etc.. .
	 */
	public static final int FLAG_BCellobject_FRAME_CHANGED = 3;
	/** Indicate that an edge was added to the model. */
	public static final int FLAG_EDGE_ADDED = 4;
	/** Indicate that an edge was removed from the model. */
	public static final int FLAG_EDGE_REMOVED = 5;
	/**
	 * Indicate that an edge has been modified. Edge modifications occur when the
	 * target or source BCellobject are modified, or when the weight of the edge has
	 * been modified.
	 */
	public static final int FLAG_EDGE_MODIFIED = 6;

	public static final Map<Integer, String> flagsToString = new HashMap<>(7);
	static {
		flagsToString.put(FLAG_BCellobject_ADDED, "BCellobject added");
		flagsToString.put(FLAG_BCellobject_FRAME_CHANGED, "BCellobject frame changed");
		flagsToString.put(FLAG_BCellobject_MODIFIED, "BCellobject modified");
		flagsToString.put(FLAG_BCellobject_REMOVED, "BCellobject removed");
		flagsToString.put(FLAG_EDGE_ADDED, "Edge added");
		flagsToString.put(FLAG_EDGE_MODIFIED, "Edge modified");
		flagsToString.put(FLAG_EDGE_REMOVED, "Edge removed");
	}

	/**
	 * Event type indicating that the BCellobject of the model were computed, and
	 * are now accessible through {@link Model#getBCellobject()}.
	 */
	public static final int BCellobject_COMPUTED = 4;
	/**
	 * Event type indicating that the BCellobject of the model were filtered.
	 */
	public static final int BCellobject_FILTERED = 5;
	/**
	 * Event type indicating that the tracks of the model were computed.
	 */
	public static final int TRACKS_COMPUTED = 6;
	/**
	 * Event type indicating that the tracks of the model had their visibility
	 * changed.
	 */
	public static final int TRACKS_VISIBILITY_CHANGED = 7;

	/**
	 * Event type indicating that model was modified, by adding, removing or
	 * changing the feature of some BCellobject, and/or adding or removing edges in
	 * the tracks. Content of the modification can be accessed by
	 * {@link #getBCellobject()}, {@link #getBCellobjectFlag(BCellobject)},
	 * {@link #getFromFrame(BCellobject)} and {@link #getToFrame(BCellobject)}, and
	 * for the tracks: {@link #getEdges()} and
	 * {@link #getEdgeFlag(DefaultWeightedEdge)} .
	 */
	public static final int MODEL_MODIFIED = 8;

	/** BCellobject affected by this event. */
	private final HashSet<BCellobject> BCellobject = new HashSet<>();
	/** Edges affected by this event. */
	private final HashSet<DefaultWeightedEdge> edges = new HashSet<>();
	/**
	 * For BCellobject removed or moved: frame from which they were removed or
	 * moved.
	 */
	private final HashMap<BCellobject, Integer> fromFrame = new HashMap<>();
	/**
	 * For BCellobject removed or added: frame to which they were added or moved.
	 */
	private final HashMap<BCellobject, Integer> toFrame = new HashMap<>();
	/** Modification flag for BCellobject affected by this event. */
	private final HashMap<BCellobject, Integer> BCellobjectFlags = new HashMap<>();
	/** Modification flag for edges affected by this event. */
	private final HashMap<DefaultWeightedEdge, Integer> edgeFlags = new HashMap<>();
	/** The event type for this instance. */
	private final int eventID;
	private Set<Integer> trackUpdated;

	/**
	 * Create a new event, reflecting a change in a {@link Model}.
	 *
	 * @param source
	 *            the object source of this event.
	 * @param eventID
	 *            the evend ID to use for this event.
	 */
	public ModelChangeEvent(final Object source, final int eventID) {
		super(source);
		this.eventID = eventID;
	}

	public int getEventID() {
		return this.eventID;
	}

	public boolean addAllBCellobject(final Collection<BCellobject> lBCellobject) {
		return this.BCellobject.addAll(lBCellobject);
	}

	public boolean addBCellobject(final BCellobject BCellobject) {
		return this.BCellobject.add(BCellobject);
	}

	public boolean addAllEdges(final Collection<DefaultWeightedEdge> lEdges) {
		return this.edges.addAll(lEdges);
	}

	public boolean addEdge(final DefaultWeightedEdge edge) {
		return edges.add(edge);
	}

	public Integer putEdgeFlag(final DefaultWeightedEdge edge, final Integer flag) {
		return edgeFlags.put(edge, flag);
	}

	public Integer putBCellobjectFlag(final BCellobject BCellobject, final Integer flag) {
		return BCellobjectFlags.put(BCellobject, flag);
	}

	public Integer putFromFrame(final BCellobject BCellobject, final Integer lFromFrame) {
		return this.fromFrame.put(BCellobject, lFromFrame);
	}

	public Integer putToFrame(final BCellobject BCellobject, final Integer lToFrame) {
		return this.toFrame.put(BCellobject, lToFrame);
	}

	/**
	 * @return the set of BCellobject that are affected by this event. Is empty if
	 *         no BCellobject is affected by this event.
	 */
	public Set<BCellobject> getBCellobject() {
		return BCellobject;
	}

	/**
	 * @return the set of edges that are affected by this event. Is empty if no edge
	 *         is affected by this event.
	 */
	public Set<DefaultWeightedEdge> getEdges() {
		return edges;
	}

	/**
	 * Returns the modification flag for the given BCellobject affected by this
	 * event.
	 * 
	 * @param BCellobject
	 *            the BCellobject to query.
	 * @return the modification flag.
	 * @see #FLAG_BCellobject_ADDED
	 * @see #FLAG_BCellobject_MODIFIED
	 * @see #FLAG_BCellobject_REMOVED
	 */
	public Integer getBCellobjectFlag(final BCellobject BCellobject) {
		return BCellobjectFlags.get(BCellobject);
	}

	/**
	 * Returns the modification flag for the given edge affected by this event.
	 * 
	 * @param edge
	 *            the edge to query.
	 * @return the modification flag.
	 * @see #FLAG_EDGE_ADDED
	 * @see #FLAG_EDGE_REMOVED
	 */
	public Integer getEdgeFlag(final DefaultWeightedEdge edge) {
		return edgeFlags.get(edge);
	}

	public Integer getToFrame(final BCellobject BCellobject) {
		return toFrame.get(BCellobject);
	}

	public Integer getFromFrame(final BCellobject BCellobject) {
		return fromFrame.get(BCellobject);
	}

	public void setSource(final Object source) {
		this.source = source;
	}

	@Override
	public String toString() {
		final StringBuilder str = new StringBuilder("[ModelChangeEvent]:\n");
		str.append(" - source: " + source.getClass() + "_" + source.hashCode() + "\n");
		str.append(" - event type: ");
		switch (eventID) {
		case BCellobject_COMPUTED:
			str.append("BCellobject computed\n");
			break;
		case BCellobject_FILTERED:
			str.append("BCellobject filtered\n");
			break;
		case TRACKS_COMPUTED:
			str.append("Tracks computed\n");
			break;
		case TRACKS_VISIBILITY_CHANGED:
			str.append("Track visibility changed\n");
			break;
		case MODEL_MODIFIED:
			str.append("Model modified, with:\n");
			str.append("\t- BCellobject modified: " + (BCellobject != null ? BCellobject.size() : 0) + "\n");
			for (final BCellobject BCellobject : BCellobject) {
				str.append("\t\t" + BCellobject + ": " + flagsToString.get(BCellobjectFlags.get(BCellobject)) + "\n");
			}
			str.append("\t- edges modified: " + (edges != null ? edges.size() : 0) + "\n");
			for (final DefaultWeightedEdge edge : edges) {
				str.append("\t\t" + edge + ": " + flagsToString.get(edgeFlags.get(edge)) + "\n");
			}
			str.append("\t- tracks to update: " + trackUpdated + "\n");
		}
		return str.toString();
	}

	public void setTracksUpdated(final Set<Integer> tracksToUpdate) {
		this.trackUpdated = tracksToUpdate;
	}

	/**
	 * @return the IDs of track that were modified or created by this event.
	 */
	public Set<Integer> getTrackUpdated() {
		return trackUpdated;
	}


}
