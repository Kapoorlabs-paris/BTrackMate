package Buddy.plugin.trackmate.graph;


import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import net.imglib2.algorithm.Benchmark;
import net.imglib2.algorithm.OutputAlgorithm;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import budDetector.BCellobject;

public class FromContinuousBranches implements OutputAlgorithm< SimpleWeightedGraph< BCellobject, DefaultWeightedEdge > >, Benchmark
{

	private static final String BASE_ERROR_MSG = "[FromContinuousBranches] ";

	private long processingTime;

	private final Collection< List< BCellobject >> branches;

	private final Collection< List< BCellobject >> links;

	private String errorMessage;

	private SimpleWeightedGraph< BCellobject, DefaultWeightedEdge > graph;

	public FromContinuousBranches( final Collection< List< BCellobject >> branches, final Collection< List< BCellobject >> links )
	{
		this.branches = branches;
		this.links = links;
	}

	@Override
	public long getProcessingTime()
	{
		return processingTime;
	}

	@Override
	public boolean checkInput()
	{
		final long start = System.currentTimeMillis();
		if ( null == branches )
		{
			errorMessage = BASE_ERROR_MSG + "branches are null.";
			return false;
		}
		if ( null == links )
		{
			errorMessage = BASE_ERROR_MSG + "links are null.";
			return false;
		}
		for ( final List< BCellobject > link : links )
		{
			if ( link.size() != 2 )
			{
				errorMessage = BASE_ERROR_MSG + "A link is not made of two BCellobjects.";
				return false;
			}
			if ( !checkIfInBranches( link.get( 0 ) ) )
			{
				errorMessage = BASE_ERROR_MSG + "A BCellobject in a link is not present in the branch collection: " + link.get( 0 ) + " in the link " + link.get( 0 ) + "-" + link.get( 1 ) + ".";
				return false;
			}
			if ( !checkIfInBranches( link.get( 1 ) ) )
			{
				errorMessage = BASE_ERROR_MSG + "A BCellobject in a link is not present in the branch collection: " + link.get( 1 ) + " in the link " + link.get( 0 ) + "-" + link.get( 1 ) + ".";
				return false;
			}
		}
		final long end = System.currentTimeMillis();
		processingTime = end - start;
		return true;
	}

	@Override
	public boolean process()
	{
		final long start = System.currentTimeMillis();

		graph = new SimpleWeightedGraph<>( DefaultWeightedEdge.class );
		for ( final List< BCellobject > branch : branches )
		{
			for ( final BCellobject BCellobject : branch )
			{
				graph.addVertex( BCellobject );
			}
		}

		for ( final List< BCellobject > branch : branches )
		{
			final Iterator< BCellobject > it = branch.iterator();
			BCellobject previous = it.next();
			while ( it.hasNext() )
			{
				final BCellobject BCellobject = it.next();
				graph.addEdge( previous, BCellobject );
				previous = BCellobject;
			}
		}

		for ( final List< BCellobject > link : links )
		{
			graph.addEdge( link.get( 0 ), link.get( 1 ) );
		}

		final long end = System.currentTimeMillis();
		processingTime = end - start;
		return true;
	}

	@Override
	public String getErrorMessage()
	{
		return errorMessage;
	}

	@Override
	public SimpleWeightedGraph< BCellobject, DefaultWeightedEdge > getResult()
	{
		return graph;
	}

	private final boolean checkIfInBranches( final BCellobject BCellobject )
	{
		for ( final List< BCellobject > branch : branches )
		{
			if ( branch.contains( BCellobject ) ) { return true; }
		}
		return false;
	}

}
