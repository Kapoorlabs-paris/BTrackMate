package tracker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.util.NeighborCache;
import org.jgrapht.event.GraphEdgeChangeEvent;
import org.jgrapht.event.GraphVertexChangeEvent;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.util.ModifiableInteger;

import budDetector.Budobject;







public class BUDDYBudTimeDirectedNeighborIndex extends NeighborCache< Budobject, DefaultWeightedEdge >
{

	// ~ Instance fields
	// --------------------------------------------------------

	Map< Budobject, Neighbors< Budobject, DefaultWeightedEdge > > predecessorMap = new HashMap< Budobject, Neighbors< Budobject, DefaultWeightedEdge > >();

	Map< Budobject, Neighbors< Budobject, DefaultWeightedEdge > > successorMap = new HashMap< Budobject, Neighbors< Budobject, DefaultWeightedEdge > >();

	private final Graph< Budobject, DefaultWeightedEdge > graph;

	// ~ Constructors
	// -----------------------------------------------------------

	public BUDDYBudTimeDirectedNeighborIndex( final Graph< Budobject, DefaultWeightedEdge > g )
	{
		super( g );
		this.graph = g;
	}

	// ~ Methods
	// ----------------------------------------------------------------

	/**
	 * Returns the set of vertices which are the predecessors of a specified
	 * vertex. The returned set is backed by the index, and will be updated when
	 * the graph changes as long as the index has been added as a listener to
	 * the graph.
	 *
	 * @param v
	 *            the vertex whose predecessors are desired
	 *
	 * @return all unique predecessors of the specified vertex
	 */
	public Set< Budobject > predecessorsOf( final Budobject v )
	{
		return getPredecessors( v ).getNeighbors();
	}

	/**
	 * Returns the set of vertices which are the predecessors of a specified
	 * vertex. If the graph is a multigraph, vertices may appear more than once
	 * in the returned list. Because a list of predecessors can not be
	 * efficiently maintained, it is reconstructed on every invocation by
	 * duplicating entries in the neighbor set. It is thus more efficient to use
	 * {@link #predecessorsOf(Budobject)} unless duplicate neighbors are required.
	 *
	 * @param v
	 *            the vertex whose predecessors are desired
	 *
	 * @return all predecessors of the specified vertex
	 */
	public List< Budobject > predecessorListOf( final Budobject v )
	{
		return getPredecessors( v ).getNeighborList();
	}

	/**
	 * Returns the set of vertices which are the successors of a specified
	 * vertex. The returned set is backed by the index, and will be updated when
	 * the graph changes as long as the index has been added as a listener to
	 * the graph.
	 *
	 * @param v
	 *            the vertex whose successors are desired
	 *
	 * @return all unique successors of the specified vertex
	 */
	public Set< Budobject > successorsOf( final Budobject v )
	{
		return getSuccessors( v ).getNeighbors();
	}

	/**
	 * Returns the set of vertices which are the successors of a specified
	 * vertex. If the graph is a multigraph, vertices may appear more than once
	 * in the returned list. Because a list of successors can not be efficiently
	 * maintained, it is reconstructed on every invocation by duplicating
	 * entries in the neighbor set. It is thus more efficient to use
	 * {@link #successorsOf(Budobject)} unless duplicate neighbors are required.
	 *
	 * @param v
	 *            the vertex whose successors are desired
	 *
	 * @return all successors of the specified vertex
	 */
	public List< Budobject > successorListOf( final Budobject v )
	{
		return getSuccessors( v ).getNeighborList();
	}

	/**
	 * @see org.jgrapht.event.GraphListener#edgeAdded(GraphEdgeChangeEvent)
	 */
	@Override
	public void edgeAdded( final GraphEdgeChangeEvent< Budobject, DefaultWeightedEdge > e )
	{
		final DefaultWeightedEdge edge = e.getEdge();
		final Budobject source = graph.getEdgeSource( edge );
		final Budobject target = graph.getEdgeTarget( edge );

		// if a map does not already contain an entry,
		// then skip addNeighbor, since instantiating the map
		// will take care of processing the edge (which has already
		// been added)

		if ( successorMap.containsKey( source ) )
		{
			getSuccessors( source ).addNeighbor( target );
		}
		else
		{
			getSuccessors( source );
		}
		if ( predecessorMap.containsKey( target ) )
		{
			getPredecessors( target ).addNeighbor( source );
		}
		else
		{
			getPredecessors( target );
		}
	}

	/**
	 * @see org.jgrapht.event.GraphListener#edgeRemoved(GraphEdgeChangeEvent)
	 */
	@Override
	public void edgeRemoved( final GraphEdgeChangeEvent< Budobject, DefaultWeightedEdge > e )
	{
		final DefaultWeightedEdge edge = e.getEdge();
		final Budobject source = graph.getEdgeSource( edge );
		final Budobject target = graph.getEdgeTarget( edge );
		if ( successorMap.containsKey( source ) )
		{
			successorMap.get( source ).removeNeighbor( target );
		}
		if ( predecessorMap.containsKey( target ) )
		{
			predecessorMap.get( target ).removeNeighbor( source );
		}
	}

	/**
	 * @see org.jgrapht.event.VertexSetListener#vertexAdded(GraphVertexChangeEvent)
	 */
	@Override
	public void vertexAdded( final GraphVertexChangeEvent< Budobject > e )
	{
		// nothing to cache until there are edges
	}

	/**
	 * @see org.jgrapht.event.VertexSetListener#vertexRemoved(GraphVertexChangeEvent)
	 */
	@Override
	public void vertexRemoved( final GraphVertexChangeEvent< Budobject > e )
	{
		predecessorMap.remove( e.getVertex() );
		successorMap.remove( e.getVertex() );
	}

	private Neighbors< Budobject, DefaultWeightedEdge > getPredecessors( final Budobject v )
	{
		Neighbors< Budobject, DefaultWeightedEdge > neighbors = predecessorMap.get( v );
		if ( neighbors == null )
		{
			final List< Budobject > nl = Graphs.neighborListOf( graph, v );
			final List< Budobject > bnl = new ArrayList< Budobject >();
			final int ts = v.getFeature( Budobject.TIME ).intValue();
			for ( final Budobject Budobject : nl )
			{
				final int tt = Budobject.getFeature( Budobject.TIME ).intValue();
				if ( tt < ts )
				{
					bnl.add( Budobject );
				}
			}
			neighbors = new Neighbors< Budobject, DefaultWeightedEdge >( v, bnl );
			predecessorMap.put( v, neighbors );
		}
		return neighbors;
	}

	private Neighbors< Budobject, DefaultWeightedEdge > getSuccessors( final Budobject v )
	{
		Neighbors< Budobject, DefaultWeightedEdge > neighbors = successorMap.get( v );
		if ( neighbors == null )
		{
			final List< Budobject > nl = Graphs.neighborListOf( graph, v );
			final List< Budobject > bnl = new ArrayList< Budobject >();
			final int ts = v.getFeature( Budobject.TIME ).intValue();
			for ( final Budobject Budobject : nl )
			{
				final int tt = Budobject.getFeature( Budobject.TIME ).intValue();
				if ( tt > ts )
				{
					bnl.add( Budobject );
				}
			}
			neighbors = new Neighbors< Budobject, DefaultWeightedEdge >( v, bnl );
			successorMap.put( v, neighbors );
		}
		return neighbors;
	}

	// ~ Inner Classes
	// ----------------------------------------------------------

	/**
	 * Stores cached neighbors for a single vertex. Includes support for live
	 * neighbor sets and duplicate neighbors.
	 */
	static class Neighbors< V, E >
	{
		private final Map< V, ModifiableInteger > neighborCounts =
				new LinkedHashMap< V, ModifiableInteger >();

		// TODO could eventually make neighborSet modifiable, resulting
		// in edge removals from the graph
		private final Set< V > neighborSet =
				Collections.unmodifiableSet(
						neighborCounts.keySet() );

		public Neighbors( final V v, final Collection< V > neighbors )
		{
			// add all current neighbors
			for ( final V neighbor : neighbors )
			{
				addNeighbor( neighbor );
			}
		}

		public void addNeighbor( final V v )
		{
			ModifiableInteger count = neighborCounts.get( v );
			if ( count == null )
			{
				count = new ModifiableInteger( 1 );
				neighborCounts.put( v, count );
			}
			else
			{
				count.increment();
			}
		}

		public void removeNeighbor( final V v )
		{
			final ModifiableInteger count = neighborCounts.get( v );
			if ( count == null ) { throw new IllegalArgumentException(
					"Attempting to remove a neighbor that wasn't present" ); }

			count.decrement();
			if ( count.getValue() == 0 )
			{
				neighborCounts.remove( v );
			}
		}

		public Set< V > getNeighbors()
		{
			return neighborSet;
		}

		public List< V > getNeighborList()
		{
			final List< V > neighbors = new ArrayList< V >();
			for ( final Map.Entry< V, ModifiableInteger > entry : neighborCounts.entrySet() )
			{
				final V v = entry.getKey();
				final int count = entry.getValue().intValue();
				for ( int i = 0; i < count; i++ )
				{
					neighbors.add( v );
				}
			}
			return neighbors;
		}
	}

}
