package tracker;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import budDetector.Budpointobject;

public class BUDDYModel {

	
	/*
	 * CONSTANTS
	 */

	private static final boolean DEBUG = false;

	/*
	 * FIELDS
	 */

	// FEATURES

	private final BUDDYFeatureModel featureModel;

	// TRACKS

	private final BUDDYTrackModel trackModel;

	// BudpointobjectS

	/** The Budpointobjects managed by this model. */
	protected BudpointobjectCollection Budpointobjects = new BudpointobjectCollection();

	// TRANSACTION MODEL

	/**
	 * Counter for the depth of nested transactions. Each call to beginUpdate
	 * increments this counter and each call to endUpdate decrements it. When
	 * the counter reaches 0, the transaction is closed and the respective
	 * events are fired. Initial value is 0.
	 */
	private int updateLevel = 0;

	private final HashSet< Budpointobject > BudpointobjectsAdded = new HashSet< >();

	private final HashSet< Budpointobject > BudpointobjectsRemoved = new HashSet< >();

	private final HashSet< Budpointobject > BudpointobjectsMoved = new HashSet< >();

	private final HashSet< Budpointobject > BudpointobjectsUpdated = new HashSet< >();

	/**
	 * The event cache. During a transaction, some modifications might trigger
	 * the need to fire a model change event. We want to fire these events only
	 * when the transaction closes (when the updateLevel reaches 0), so we store
	 * the event ID in this cache in the meantime. The event cache contains only
	 * the int IDs of the events listed in {@link BudModelChangeEvent}, namely
	 * <ul>
	 * <li> {@link BudModelChangeEvent#BudpointobjectS_COMPUTED}
	 * <li> {@link BudModelChangeEvent#TRACKS_COMPUTED}
	 * <li> {@link BudModelChangeEvent#TRACKS_VISIBILITY_CHANGED}
	 * </ul>
	 * The {@link BudModelChangeEvent#MODEL_MODIFIED} cannot be cached this way,
	 * for it needs to be configured with modification Budpointobject and edge targets, so
	 * it uses a different system (see {@link #flushUpdate()}).
	 */
	private final HashSet< Integer > eventCache = new HashSet< >();

	// OTHERS

	/** The logger to append processes messages. */

	private String spaceUnits = "pixels";

	private String timeUnits = "frames";

	// LISTENERS

	/**
	 * The list of listeners listening to model content change.
	 */
	Set< BudModelChangeListener > BudModelChangeListeners = new LinkedHashSet< >();

	/*
	 * CONSTRUCTOR
	 */

	public BUDDYModel()
	{
		featureModel = createFeatureModel();
		trackModel = createTrackModel();
	}

	/*
	 * HOOKS
	 */

	/**
	 * Instantiates a blank {@link TrackModel} to use whithin this model.
	 * <p>
	 * Subclassers can override this method to have the model work with their
	 * own subclass of {@link TrackModel}.
	 *
	 * @return a new instance of {@link TrackModel}.
	 */
	protected BUDDYTrackModel createTrackModel()
	{
		return new BUDDYTrackModel();
	}

	/**
	 * Instantiates a blank {@link FeatureModel} to use whithin this model.
	 * <p>
	 * Subclassers can override this method to have the model work with their
	 * own subclass of {@link FeatureModel}.
	 * 
	 * @return a new instance of {@link FeatureModel}.
	 */
	protected BUDDYFeatureModel createFeatureModel()
	{
		return new BUDDYFeatureModel( this );
	}

	/*
	 * UTILS METHODS
	 */

	@Override
	public String toString()
	{
		final StringBuilder str = new StringBuilder();

		str.append( '\n' );
		if ( null == Budpointobjects || Budpointobjects.keySet().size() == 0 )
		{
			str.append( "No Budpointobjects.\n" );
		}
		else
		{
			str.append( "Contains " + Budpointobjects.getNBudpointobjects(  ) + " Budpointobjects in total.\n" );
		}
		if ( Budpointobjects.getNBudpointobjects(  ) == 0 )
		{
			str.append( "No filtered Budpointobjects.\n" );
		}
		else
		{
			str.append( "Contains " + Budpointobjects.getNBudpointobjects(  ) + " filtered Budpointobjects.\n" );
		}

		str.append( '\n' );
		if ( trackModel.nTracks( false ) == 0 )
		{
			str.append( "No tracks.\n" );
		}
		else
		{
			str.append( "Contains " + trackModel.nTracks( false ) + " tracks in total.\n" );
		}
		if ( trackModel.nTracks( true ) == 0 )
		{
			str.append( "No filtered tracks.\n" );
		}
		else
		{
			str.append( "Contains " + trackModel.nTracks( false ) + " filtered tracks.\n" );
		}

		str.append( '\n' );
		str.append( "Physical units:\n  space units: " + spaceUnits + "\n  time units: " + timeUnits + '\n' );

		str.append( '\n' );
		str.append( featureModel.toString() );

		return str.toString();
	}

	/*
	 * DEAL WITH MODEL CHANGE LISTENER
	 */

	public void addBudModelChangeListener( final BudModelChangeListener listener )
	{
		BudModelChangeListeners.add( listener );
	}

	public boolean removeBudModelChangeListener( final BudModelChangeListener listener )
	{
		return BudModelChangeListeners.remove( listener );
	}

	public Set< BudModelChangeListener > getBudModelChangeListener()
	{
		return BudModelChangeListeners;
	}

	/*
	 * PHYSICAL UNITS
	 */

	/**
	 * Sets the physical units for the quantities stored in this model.
	 *
	 * @param spaceUnits
	 *            the spatial units (e.g. μm).
	 * @param timeUnits
	 *            the time units (e.g. min).
	 */
	public void setPhysicalUnits( final String spaceUnits, final String timeUnits )
	{
		this.spaceUnits = spaceUnits;
		this.timeUnits = timeUnits;
	}

	/**
	 * Returns the spatial units for the quantities stored in this model.
	 *
	 * @return the spatial units.
	 */
	public String getSpaceUnits()
	{
		return spaceUnits;
	}

	/**
	 * Returns the time units for the quantities stored in this model.
	 *
	 * @return the time units.
	 */
	public String getTimeUnits()
	{
		return timeUnits;
	}

	/*
	 * GRAPH MODIFICATION
	 */

	public synchronized void beginUpdate()
	{
		updateLevel++;
		if ( DEBUG )
			System.out.println( "[TrackMateModel] #beginUpdate: increasing update level to " + updateLevel + "." );
	}

	public synchronized void endUpdate()
	{
		updateLevel--;
		if ( DEBUG )
			System.out.println( "[TrackMateModel] #endUpdate: decreasing update level to " + updateLevel + "." );
		if ( updateLevel == 0 )
		{
			if ( DEBUG )
				System.out.println( "[TrackMateModel] #endUpdate: update level is 0, calling flushUpdate()." );
			flushUpdate();
		}
	}

	/*
	 * TRACK METHODS: WE DELEGATE TO THE TRACK GRAPH MODEL
	 */

	/**
	 * Removes all the tracks from this model.
	 *
	 * @param doNotify
	 *            if <code>true</code>, model listeners will be notified with a
	 *            {@link BudModelChangeEvent#TRACKS_COMPUTED} event.
	 */
	public void clearTracks( final boolean doNotify )
	{
		trackModel.clear();
		if ( doNotify )
		{
			final BudModelChangeEvent event = new BudModelChangeEvent( this, BudModelChangeEvent.TRACKS_COMPUTED );
			for ( final BudModelChangeListener listener : BudModelChangeListeners )
				listener.modelChanged( event );
		}
	}

	/**
	 * Returns the {@link TrackModel} that manages the tracks for this model.
	 * 
	 * @return the track model.
	 */
	public BUDDYTrackModel getTrackModel()
	{
		return trackModel;
	}

	/**
	 * Sets the tracks stored in this model in bulk.
	 * <p>
	 * Clears the tracks of this model and replace it by the tracks found by
	 * inspecting the specified graph. All new tracks found will be made visible
	 * and will be given a default name.
	 * <p>
	 *
	 * @param graph
	 *            the graph to parse for tracks.
	 * @param doNotify
	 *            if <code>true</code>, model listeners will be notified with a
	 *            {@link BudModelChangeEvent#TRACKS_COMPUTED} event.
	 */
	public void setTracks( final SimpleWeightedGraph< Budpointobject, DefaultWeightedEdge > graph, final boolean doNotify )
	{
		trackModel.setGraph( graph );
		if ( doNotify )
		{
			final BudModelChangeEvent event = new BudModelChangeEvent( this, BudModelChangeEvent.TRACKS_COMPUTED );
			for ( final BudModelChangeListener listener : BudModelChangeListeners )
				listener.modelChanged( event );
		}
	}

	/*
	 * GETTERS / SETTERS FOR BudpointobjectS
	 */

	/**
	 * Returns the Budpointobject collection managed by this model.
	 *
	 * @return the Budpointobject collection managed by this model.
	 */
	public BudpointobjectCollection getBudpointobjects()
	{
		return Budpointobjects;
	}

	/**
	 * Removes all the Budpointobjects from this model.
	 *
	 * @param doNotify
	 *            if <code>true</code>, model listeners will be notified with a
	 *            {@link BudModelChangeEvent#BudpointobjectS_COMPUTED} event.
	 */
	public void clearBudpointobjects( final boolean doNotify )
	{
		Budpointobjects.clear();
		if ( doNotify )
		{
			final BudModelChangeEvent event = new BudModelChangeEvent( this, BudModelChangeEvent.Budpointobject_COMPUTED );
			for ( final BudModelChangeListener listener : BudModelChangeListeners )
				listener.modelChanged( event );
		}
	}

	/**
	 * Set the {@link BudpointobjectCollection} managed by this model.
	 *
	 * @param doNotify
	 *            if true, will file a {@link BudModelChangeEvent#BudpointobjectS_COMPUTED}
	 *            event.
	 * @param Budpointobjects
	 *            the {@link BudpointobjectCollection} to set.
	 */
	public void setBudpointobjects( final BudpointobjectCollection Budpointobjects, final boolean doNotify )
	{
		this.Budpointobjects = Budpointobjects;
		if ( doNotify )
		{
			final BudModelChangeEvent event = new BudModelChangeEvent( this, BudModelChangeEvent.Budpointobject_COMPUTED );
			for ( final BudModelChangeListener listener : BudModelChangeListeners )
				listener.modelChanged( event );
		}
	}




	/*
	 * FEATURES
	 */

	public BUDDYFeatureModel getFeatureModel()
	{
		return featureModel;
	}

	/*
	 * MODEL CHANGE METHODS
	 */

	/**
	 * Moves a single Budpointobject from a frame to another, make it visible if it was
	 * not, then mark it for feature update. If the source Budpointobject could not be
	 * found in the source frame, nothing is done and <code>null</code> is
	 * returned.
	 * <p>
	 * For the model update to happen correctly and listeners to be notified
	 * properly, a call to this method must happen within a transaction, as in:
	 *
	 * <pre>
	 * model.beginUpdate();
	 * try {
	 * 	... // model modifications here
	 * } finally {
	 * 	model.endUpdate();
	 * }
	 * </pre>
	 *
	 * @param BudpointobjectToMove
	 *            the Budpointobject to move
	 * @param fromFrame
	 *            the frame the Budpointobject originated from
	 * @param toFrame
	 *            the destination frame
	 * @return the Budpointobject that was moved, or <code>null</code> if it could not be
	 *         found in the source frame
	 */
	public synchronized Budpointobject moveBudpointobjectFrom( final Budpointobject BudpointobjectToMove, final Integer fromFrame, final Integer toFrame )
	{
		final boolean ok = Budpointobjects.remove( BudpointobjectToMove, fromFrame );
		if ( !ok )
		{
			if ( DEBUG )
			{
				System.err.println( "[TrackMateModel] Could not find Budpointobject " + BudpointobjectToMove + " in frame " + fromFrame );
			}
			return null;
		}
		Budpointobjects.add( BudpointobjectToMove, toFrame );
		if ( DEBUG )
		{
			System.out.println( "[TrackMateModel] Moving " + BudpointobjectToMove + " from frame " + fromFrame + " to frame " + toFrame );
		}

		// Mark for update Budpointobject and edges
		trackModel.edgesModified.addAll( trackModel.edgesOf( BudpointobjectToMove ) );
		BudpointobjectsMoved.add( BudpointobjectToMove );
		return BudpointobjectToMove;
	}

	/**
	 * Adds a single Budpointobject to the collections managed by this model, mark it as
	 * visible, then update its features.
	 * <p>
	 * For the model update to happen correctly and listeners to be notified
	 * properly, a call to this method must happen within a transaction, as in:
	 *
	 * <pre>
	 * model.beginUpdate();
	 * try {
	 * 	... // model modifications here
	 * } finally {
	 * 	model.endUpdate();
	 * }
	 * </pre>
	 * 
	 * @param BudpointobjectToAdd
	 *            the Budpointobject to add.
	 * @param toFrame
	 *            the frame to add it to.
	 *
	 * @return the Budpointobject just added.
	 */
	public synchronized Budpointobject addBudpointobjectTo( final Budpointobject BudpointobjectToAdd, final Integer toFrame )
	{
		Budpointobjects.add( BudpointobjectToAdd, toFrame );
		BudpointobjectsAdded.add( BudpointobjectToAdd ); // TRANSACTION
		if ( DEBUG )
		{
			System.out.println( "[TrackMateModel] Adding Budpointobject " + BudpointobjectToAdd + " to frame " + toFrame );
		}
		trackModel.addBudpointobject( BudpointobjectToAdd );
		return BudpointobjectToAdd;
	}

	/**
	 * Removes a single Budpointobject from the collections managed by this model. If the
	 * Budpointobject cannot be found, nothing is done and <code>null</code> is returned.
	 * <p>
	 * For the model update to happen correctly and listeners to be notified
	 * properly, a call to this method must happen within a transaction, as in:
	 *
	 * <pre>
	 * model.beginUpdate();
	 * try {
	 * 	... // model modifications here
	 * } finally {
	 * 	model.endUpdate();
	 * }
	 * </pre>
	 *
	 * @param BudpointobjectToRemove
	 *            the Budpointobject to remove.
	 * @return the Budpointobject removed, or <code>null</code> if it could not be found.
	 */
	public synchronized Budpointobject removeBudpointobject( final Budpointobject BudpointobjectToRemove )
	{
		final int fromFrame = BudpointobjectToRemove.getFeature( Budpointobject.POSITION_T ).intValue();
		if ( Budpointobjects.remove( BudpointobjectToRemove, fromFrame ) )
		{
			BudpointobjectsRemoved.add( BudpointobjectToRemove ); // TRANSACTION
			if ( DEBUG )
				System.out.println( "[TrackMateModel] Removing Budpointobject " + BudpointobjectToRemove + " from frame " + fromFrame );

			trackModel.removeBudpointobject( BudpointobjectToRemove ); 
			// changes to edges will be caught automatically by the TrackGraphModel
			return BudpointobjectToRemove;
		}
		if ( DEBUG )
			System.err.println( "[TrackMateModel] The Budpointobject " + BudpointobjectToRemove + " cannot be found in frame " + fromFrame );

		return null;
	}

	/**
	 * Mark the specified Budpointobject for update. At the end of the model transaction,
	 * its features will be recomputed, and other edge and track features that
	 * depends on it will be as well.
	 * <p>
	 * For the model update to happen correctly and listeners to be notified
	 * properly, a call to this method must happen within a transaction, as in:
	 *
	 * <pre>
	 * model.beginUpdate();
	 * try {
	 * 	... // model modifications here
	 * } finally {
	 * 	model.endUpdate();
	 * }
	 * </pre>
	 *
	 * @param BudpointobjectToUpdate
	 *            the Budpointobject to mark for update
	 */
	public synchronized void updateFeatures( final Budpointobject BudpointobjectToUpdate )
	{
		BudpointobjectsUpdated.add( BudpointobjectToUpdate ); // Enlist for feature update when
											// transaction is marked as finished
		final Set< DefaultWeightedEdge > touchingEdges = trackModel.edgesOf( BudpointobjectToUpdate );
		if ( null != touchingEdges )
		{
			trackModel.edgesModified.addAll( touchingEdges );
		}
	}

	/**
	 * Creates a new edge between two Budpointobjects, with the specified weight.
	 * <p>
	 * For the model update to happen correctly and listeners to be notified
	 * properly, a call to this method must happen within a transaction, as in:
	 *
	 * <pre>
	 * model.beginUpdate();
	 * try {
	 * 	... // model modifications here
	 * } finally {
	 * 	model.endUpdate();
	 * }
	 * </pre>
	 *
	 * @param source
	 *            the source Budpointobject.
	 * @param target
	 *            the target Budpointobject.
	 * @param weight
	 *            the weight of the edge.
	 * @return the edge created.
	 */
	public synchronized DefaultWeightedEdge addEdge( final Budpointobject source, final Budpointobject target, final double weight )
	{
		return trackModel.addEdge( source, target, weight );
	}

	/**
	 * Removes an edge between two Budpointobjects and returns it. Returns
	 * <code>null</code> and do nothing to the tracks if the edge did not exist.
	 *
	 * @param source
	 *            the source Budpointobject.
	 * @param target
	 *            the target Budpointobject.
	 * @return the edge between the two Budpointobjects, if it existed.
	 */
	public synchronized DefaultWeightedEdge removeEdge( final Budpointobject source, final Budpointobject target )
	{
		return trackModel.removeEdge( source, target );
	}

	/**
	 * Removes an existing edge from the model.
	 * <p>
	 * For the model update to happen correctly and listeners to be notified
	 * properly, a call to this method must happen within a transaction, as in:
	 *
	 * <pre>
	 * model.beginUpdate();
	 * try {
	 * 	... // model modifications here
	 * } finally {
	 * 	model.endUpdate();
	 * }
	 * </pre>
	 *
	 * @param edge
	 *            the edge to remove.
	 * @return <code>true</code> if the edge existed in the model and was
	 *         successfully, <code>false</code> otherwise.
	 */
	public synchronized boolean removeEdge( final DefaultWeightedEdge edge )
	{
		return trackModel.removeEdge( edge );
	}

	/**
	 * Sets the weight of the specified edge.
	 * <p>
	 * For the model update to happen correctly and listeners to be notified
	 * properly, a call to this method must happen within a transaction, as in:
	 *
	 * <pre>
	 * model.beginUpdate();
	 * try {
	 * 	... // model modifications here
	 * } finally {
	 * 	model.endUpdate();
	 * }
	 * </pre>
	 *
	 * @param edge
	 *            the edge.
	 * @param weight
	 *            the weight to set.
	 */
	public synchronized void setEdgeWeight( final DefaultWeightedEdge edge, final double weight )
	{
		trackModel.setEdgeWeight( edge, weight );
	}

	/**
	 * Sets the visibility of the specified track. Throws a
	 * {@link NullPointerException} if the track ID is unknown to the model.
	 * <p>
	 * For the model update to happen correctly and listeners to be notified
	 * properly, a call to this method must happen within a transaction, as in:
	 *
	 * <pre>
	 * model.beginUpdate();
	 * try {
	 * 	... // model modifications here
	 * } finally {
	 * 	model.endUpdate();
	 * }
	 * </pre>
	 *
	 * @param trackID
	 *            the track ID.
	 * @param visible
	 *            the desired visibility.
	 * @return the specified track visibility prior to calling this method.
	 */
	public synchronized boolean setTrackVisibility( final Integer trackID, final boolean visible )
	{
		final boolean oldvis = trackModel.setVisibility( trackID, visible );
		final boolean modified = oldvis != visible;
		if ( modified )
		{
			eventCache.add( BudModelChangeEvent.TRACKS_VISIBILITY_CHANGED );
		}
		return oldvis;
	}

	/*
	 * PRIVATE METHODS
	 */

	/**
	 * Fire events. Regenerate fields derived from the filtered graph.
	 */
	private void flushUpdate()
	{

		if ( DEBUG )
		{
			System.out.println( "[TrackMateModel] #flushUpdate()." );
			System.out.println( "[TrackMateModel] #flushUpdate(): Event cache is :" + eventCache );
			System.out.println( "[TrackMateModel] #flushUpdate(): Track content is:\n" + trackModel.echo() );
		}

		/*
		 * We recompute tracks only if some edges have been added or removed,
		 * (if some Budpointobjects have been removed that causes edges to be removes, we
		 * already know about it). We do NOT recompute tracks if Budpointobjects have been
		 * added: they will not result in new tracks made of single Budpointobjects.
		 */
		final int nEdgesToSignal = trackModel.edgesAdded.size() + trackModel.edgesRemoved.size() + trackModel.edgesModified.size();

		// Do we have tracks to update?
		final HashSet< Integer > tracksToUpdate = new HashSet< >( trackModel.tracksUpdated );

		// We also want to update the tracks that have edges that were modified
		for ( final DefaultWeightedEdge modifiedEdge : trackModel.edgesModified )
		{
			tracksToUpdate.add( trackModel.trackIDOf( modifiedEdge ) );
		}

		// Deal with new or moved Budpointobjects: we need to update their features.
		final int nBudpointobjectsToUpdate = BudpointobjectsAdded.size() + BudpointobjectsMoved.size() + BudpointobjectsUpdated.size();
		if ( nBudpointobjectsToUpdate > 0 )
		{
			final HashSet< Budpointobject > BudpointobjectsToUpdate = new HashSet< >( nBudpointobjectsToUpdate );
			BudpointobjectsToUpdate.addAll( BudpointobjectsAdded );
			BudpointobjectsToUpdate.addAll( BudpointobjectsMoved );
			BudpointobjectsToUpdate.addAll( BudpointobjectsUpdated );
		}

		// Initialize event
		final BudModelChangeEvent event = new BudModelChangeEvent( this, BudModelChangeEvent.MODEL_MODIFIED );

		// Configure it with Budpointobjects to signal.
		final int nBudpointobjectsToSignal = nBudpointobjectsToUpdate + BudpointobjectsRemoved.size();
		if ( nBudpointobjectsToSignal > 0 )
		{
			event.addAllBudpointobject( BudpointobjectsAdded );
			event.addAllBudpointobject( BudpointobjectsRemoved );
			event.addAllBudpointobject( BudpointobjectsMoved );
			event.addAllBudpointobject( BudpointobjectsUpdated );

			for ( final Budpointobject Budpointobject : BudpointobjectsAdded )
			{
				event.putBudpointobjectFlag( Budpointobject, BudModelChangeEvent.FLAG_Budpointobject_ADDED );
			}
			for ( final Budpointobject Budpointobject : BudpointobjectsRemoved )
			{
				event.putBudpointobjectFlag( Budpointobject, BudModelChangeEvent.FLAG_Budpointobject_REMOVED );
			}
			for ( final Budpointobject Budpointobject : BudpointobjectsMoved )
			{
				event.putBudpointobjectFlag( Budpointobject, BudModelChangeEvent.FLAG_Budpointobject_FRAME_CHANGED );
			}
			for ( final Budpointobject Budpointobject : BudpointobjectsUpdated )
			{
				event.putBudpointobjectFlag( Budpointobject, BudModelChangeEvent.FLAG_Budpointobject_MODIFIED );
			}
		}

		// Configure it with edges to signal.
		if ( nEdgesToSignal > 0 )
		{
			event.addAllEdges( trackModel.edgesAdded );
			event.addAllEdges( trackModel.edgesRemoved );
			event.addAllEdges( trackModel.edgesModified );

			for ( final DefaultWeightedEdge edge : trackModel.edgesAdded )
			{
				event.putEdgeFlag( edge, BudModelChangeEvent.FLAG_EDGE_ADDED );
			}
			for ( final DefaultWeightedEdge edge : trackModel.edgesRemoved )
			{
				event.putEdgeFlag( edge, BudModelChangeEvent.FLAG_EDGE_REMOVED );
			}
			for ( final DefaultWeightedEdge edge : trackModel.edgesModified )
			{
				event.putEdgeFlag( edge, BudModelChangeEvent.FLAG_EDGE_MODIFIED );
			}
		}

		// Configure it with the tracks we found need updating
		event.setTracksUpdated( tracksToUpdate );

		try
		{
			if ( nEdgesToSignal + nBudpointobjectsToSignal > 0 )
			{
				if ( DEBUG )
				{
					System.out.println( "[TrackMateModel] #flushUpdate(): firing model modified event" );
					System.out.println( "[TrackMateModel] to " + BudModelChangeListeners );

				}
				for ( final BudModelChangeListener listener : BudModelChangeListeners )
				{
					listener.modelChanged( event );
				}
			}

			// Fire events stored in the event cache
			for ( final int eventID : eventCache )
			{
				if ( DEBUG )
				{
					System.out.println( "[TrackMateModel] #flushUpdate(): firing event with ID " + eventID );
				}
				final BudModelChangeEvent cachedEvent = new BudModelChangeEvent( this, eventID );
				for ( final BudModelChangeListener listener : BudModelChangeListeners )
				{
					listener.modelChanged( cachedEvent );
				}
			}

		}
		finally
		{
			BudpointobjectsAdded.clear();
			BudpointobjectsRemoved.clear();
			BudpointobjectsMoved.clear();
			BudpointobjectsUpdated.clear();
			trackModel.edgesAdded.clear();
			trackModel.edgesRemoved.clear();
			trackModel.edgesModified.clear();
			trackModel.tracksUpdated.clear();
			eventCache.clear();
		}

	}
	
	
	
}
