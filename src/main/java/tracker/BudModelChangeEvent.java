package tracker;





import java.util.Collection;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import budDetector.Budpointobject;
import org.jgrapht.graph.DefaultWeightedEdge;

	public class BudModelChangeEvent extends EventObject {

		private static final long serialVersionUID = -1L;
		/** Indicate that a Budpointobject was added to the model. */
		public static final int FLAG_Budpointobject_ADDED = 0;
		/** Indicate that a Budpointobject was removed from the model. */
		public static final int FLAG_Budpointobject_REMOVED = 1;
		/**
		 * Indicate a modification of the features of a Budpointobject. It may have changed
		 * of position and feature, but not of frame.
		 */
		public static final int FLAG_Budpointobject_MODIFIED = 2;
		/**
		 * Indicate that a Budpointobject has changed of frame, and possible of position,
		 * features, etc.. .
		 */
		public static final int FLAG_Budpointobject_FRAME_CHANGED = 3;
		/** Indicate that an edge was added to the model. */
		public static final int FLAG_EDGE_ADDED = 4;
		/** Indicate that an edge was removed from the model. */
		public static final int FLAG_EDGE_REMOVED = 5;
		/**
		 * Indicate that an edge has been modified. Edge modifications occur when the
		 * target or source Budpointobject are modified, or when the weight of the edge has
		 * been modified.
		 */
		public static final int FLAG_EDGE_MODIFIED = 6;

		public static final Map<Integer, String> flagsToString = new HashMap<>(7);
		static {
			flagsToString.put(FLAG_Budpointobject_ADDED, "Budpointobject added");
			flagsToString.put(FLAG_Budpointobject_FRAME_CHANGED, "Budpointobject frame changed");
			flagsToString.put(FLAG_Budpointobject_MODIFIED, "Budpointobject modified");
			flagsToString.put(FLAG_Budpointobject_REMOVED, "Budpointobject removed");
			flagsToString.put(FLAG_EDGE_ADDED, "Edge added");
			flagsToString.put(FLAG_EDGE_MODIFIED, "Edge modified");
			flagsToString.put(FLAG_EDGE_REMOVED, "Edge removed");
		}

		/**
		 * Event type indicating that the Budpointobject of the model were computed, and
		 * are now accessible through {@link Model#getBudpointobject()}.
		 */
		public static final int Budpointobject_COMPUTED = 4;
		/**
		 * Event type indicating that the Budpointobject of the model were filtered.
		 */
		public static final int Budpointobject_FILTERED = 5;
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
		 * changing the feature of some Budpointobject, and/or adding or removing edges in
		 * the tracks. Content of the modification can be accessed by
		 * {@link #getBudpointobject()}, {@link #getBudpointobjectFlag(Budpointobject)},
		 * {@link #getFromFrame(Budpointobject)} and {@link #getToFrame(Budpointobject)}, and
		 * for the tracks: {@link #getEdges()} and
		 * {@link #getEdgeFlag(DefaultWeightedEdge)} .
		 */
		public static final int MODEL_MODIFIED = 8;

		/** Budpointobject affected by this event. */
		private final HashSet<Budpointobject> Budpointobject = new HashSet<>();
		/** Edges affected by this event. */
		private final HashSet<DefaultWeightedEdge> edges = new HashSet<>();
		/**
		 * For Budpointobject removed or moved: frame from which they were removed or
		 * moved.
		 */
		private final HashMap<Budpointobject, Integer> fromFrame = new HashMap<>();
		/**
		 * For Budpointobject removed or added: frame to which they were added or moved.
		 */
		private final HashMap<Budpointobject, Integer> toFrame = new HashMap<>();
		/** Modification flag for Budpointobject affected by this event. */
		private final HashMap<Budpointobject, Integer> BudpointobjectFlags = new HashMap<>();
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
		public BudModelChangeEvent(final Object source, final int eventID) {
			super(source);
			this.eventID = eventID;
		}

		public int getEventID() {
			return this.eventID;
		}

		public boolean addAllBudpointobject(final Collection<Budpointobject> lBudpointobject) {
			return this.Budpointobject.addAll(lBudpointobject);
		}

		public boolean addBudpointobject(final Budpointobject Budpointobject) {
			return this.Budpointobject.add(Budpointobject);
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

		public Integer putBudpointobjectFlag(final Budpointobject Budpointobject, final Integer flag) {
			return BudpointobjectFlags.put(Budpointobject, flag);
		}

		public Integer putFromFrame(final Budpointobject Budpointobject, final Integer lFromFrame) {
			return this.fromFrame.put(Budpointobject, lFromFrame);
		}

		public Integer putToFrame(final Budpointobject Budpointobject, final Integer lToFrame) {
			return this.toFrame.put(Budpointobject, lToFrame);
		}

		/**
		 * @return the set of Budpointobject that are affected by this event. Is empty if
		 *         no Budpointobject is affected by this event.
		 */
		public Set<Budpointobject> getBudpointobject() {
			return Budpointobject;
		}

		/**
		 * @return the set of edges that are affected by this event. Is empty if no edge
		 *         is affected by this event.
		 */
		public Set<DefaultWeightedEdge> getEdges() {
			return edges;
		}

		/**
		 * Returns the modification flag for the given Budpointobject affected by this
		 * event.
		 * 
		 * @param Budpointobject
		 *            the Budpointobject to query.
		 * @return the modification flag.
		 * @see #FLAG_Budpointobject_ADDED
		 * @see #FLAG_Budpointobject_MODIFIED
		 * @see #FLAG_Budpointobject_REMOVED
		 */
		public Integer getBudpointobjectFlag(final Budpointobject Budpointobject) {
			return BudpointobjectFlags.get(Budpointobject);
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

		public Integer getToFrame(final Budpointobject Budpointobject) {
			return toFrame.get(Budpointobject);
		}

		public Integer getFromFrame(final Budpointobject Budpointobject) {
			return fromFrame.get(Budpointobject);
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
			case Budpointobject_COMPUTED:
				str.append("Budpointobject computed\n");
				break;
			case Budpointobject_FILTERED:
				str.append("Budpointobject filtered\n");
				break;
			case TRACKS_COMPUTED:
				str.append("Tracks computed\n");
				break;
			case TRACKS_VISIBILITY_CHANGED:
				str.append("Track visibility changed\n");
				break;
			case MODEL_MODIFIED:
				str.append("Model modified, with:\n");
				str.append("\t- Budpointobject modified: " + (Budpointobject != null ? Budpointobject.size() : 0) + "\n");
				for (final Budpointobject Budpointobject : Budpointobject) {
					str.append("\t\t" + Budpointobject + ": " + flagsToString.get(BudpointobjectFlags.get(Budpointobject)) + "\n");
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

	

