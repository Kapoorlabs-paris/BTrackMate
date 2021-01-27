package Buddy.plugin.trackmate.graph;

import Buddy.plugin.trackmate.TrackModel;
import budDetector.BCellobject;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Supplier;
import org.jgrapht.alg.util.NeighborCache;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

public class GraphUtils {


	/**
	 * @return a pretty-print string representation of a {@link TrackModel}, as long it is 
	 * a tree (each BCellobject must not have more than one predecessor).
	 * @throws IllegalArgumentException if the given graph is not a tree.
	 */
	public static final String toString(final TrackModel model) {
		/*
		 * Get directed cache
		 */
		TimeDirectedNeighborIndex cache = model.getDirectedNeighborIndex();
		
		/*
		 * Check input
		 */
		if (!isTree(model, cache)) {
			throw new IllegalArgumentException("toString cannot be applied to graphs that are not trees (each vertex must have at most one predecessor).");
		}
		
		/*
		 * Get column widths
		 */
		Map<BCellobject, Integer> widths = cumulativeBranchWidth(model);
		
		/*
		 * By the way we compute the largest BCellobject name
		 */
		int largestName = 0;
		for (BCellobject BCellobject : model.vertexSet()) {
			if (BCellobject.getName().length() > largestName) {
				largestName = BCellobject.getName().length();
			}
		}
		largestName += 2;

		/*
		 * Find how many different frames we have
		 */
		TreeSet<Integer> frames = new TreeSet<>();
		for (BCellobject BCellobject : model.vertexSet()) {
			frames.add(BCellobject.getFeature(BCellobject.POSITION_T).intValue());
		}
		int nframes = frames.size();


		/*
		 * Build string, one StringBuilder per frame
		 */
		HashMap<Integer, StringBuilder> strings = new HashMap<>(nframes);
		for (Integer frame : frames) {
			strings.put(frame, new StringBuilder());
		}

		HashMap<Integer, StringBuilder> below = new HashMap<>(nframes);
		for (Integer frame : frames) {
			below.put(frame, new StringBuilder());
		}

		/*
		 * Keep track of where the carret is for each BCellobject
		 */
		Map<BCellobject, Integer> carretPos = new HashMap<>(model.vertexSet().size()); 

		/*
		 * Comparator to have BCellobjects order by name
		 */
		Comparator<BCellobject> comparator = BCellobject.nameComparator;
		
		/*
		 * Let's go!
		 */

		for (Integer trackID : model.trackIDs(true)) {
			
			/*
			 *  Get the 'first' BCellobject for an iterator that starts there
			 */
			Set<BCellobject> track = model.trackBCellobjects(trackID);
			Iterator<BCellobject> it = track.iterator();
			BCellobject first = it.next();
			for (BCellobject BCellobject : track) {
				if (first.diffTo(BCellobject, BCellobject.POSITION_T) > 0) {
					first = BCellobject;
				}
			}

			/*
			 * First, fill the linesBelow with spaces
			 */
			for (Integer frame : frames) {
				int columnWidth = widths.get(first);
				below.get(frame).append(makeSpaces(columnWidth*largestName));
			}
			
			/*
			 * Iterate down the tree
			 */
			SortedDepthFirstIterator<BCellobject,DefaultWeightedEdge> iterator = model.getSortedDepthFirstIterator(first, comparator, true);
			while (iterator.hasNext()) {

				BCellobject BCellobject = iterator.next();
				int frame = BCellobject.getFeature(BCellobject.POSITION_T).intValue();
				boolean isLeaf = cache.successorsOf(BCellobject).size() == 0;

				int columnWidth = widths.get(BCellobject);
				String str = BCellobject.getName();
				int nprespaces = largestName/2 - str.length()/2;
				strings.get(frame).append(makeSpaces(columnWidth / 2 * largestName));
				strings.get(frame).append(makeSpaces(nprespaces));
				strings.get(frame).append(str);
				// Store bar position - deal with bars below
				int currentBranchingPosition = strings.get(frame).length() - str.length()/2;
				carretPos.put(BCellobject, currentBranchingPosition);
				// Resume filling the branch
				strings.get(frame).append(makeSpaces(largestName - nprespaces - str.length()));
				strings.get(frame).append(makeSpaces( (columnWidth*largestName) - (columnWidth/2*largestName) - largestName));

				// is leaf? then we fill all the columns below
				if (isLeaf) {
					SortedSet<Integer> framesToFill = frames.tailSet(frame, false);
					for (Integer subsequentFrame : framesToFill) {
						strings.get(subsequentFrame).append(makeSpaces(columnWidth * largestName));
					}
				} else {
					// Is there an empty slot below? Like when a link jumps above several frames?
					Set<BCellobject> successors = cache.successorsOf(BCellobject);
					for (BCellobject successor : successors) {
						if (successor.diffTo(BCellobject, BCellobject.POSITION_T) > 1) {
							for (int subFrame = successor.getFeature(BCellobject.POSITION_T).intValue(); subFrame <= successor.getFeature(BCellobject.POSITION_T).intValue(); subFrame++) {
								strings.get(subFrame-1).append(makeSpaces(columnWidth * largestName));
							}
						}
					}
				}
				
				

			} // Finished iterating over BCellobject of the track
			
			// Fill remainder with spaces
			
			for (Integer frame : frames) {
				int columnWidth = widths.get(first);
				StringBuilder sb = strings.get(frame);
				int pos = sb.length();
				int nspaces = columnWidth * largestName - pos;
				if (nspaces > 0) {
					sb.append(makeSpaces(nspaces));
				}
			}

		} // Finished iterating over the track
		
		
		/*
		 * Second iteration over edges
		 */
		
		Set<DefaultWeightedEdge> edges = model.edgeSet();
		for (DefaultWeightedEdge edge : edges) {
			
			BCellobject source = model.getEdgeSource(edge);
			BCellobject target = model.getEdgeTarget(edge);
			
			int sourceCarret = carretPos.get(source) - 1;
			int targetCarret = carretPos.get(target) - 1;
			
			int sourceFrame = source.getFeature(BCellobject.POSITION_T).intValue();
			int targetFrame = target.getFeature(BCellobject.POSITION_T).intValue();
			
			for (int frame = sourceFrame; frame < targetFrame; frame++) {
				below.get(frame).setCharAt(sourceCarret, '|');
			}
			for (int frame = sourceFrame+1; frame < targetFrame; frame++) {
				strings.get(frame).setCharAt(sourceCarret, '|');
			}
			
			if (cache.successorsOf(source).size() > 1) {
				// We have branching
				int minC = Math.min(sourceCarret, targetCarret);
				int maxC = Math.max(sourceCarret, targetCarret);
				StringBuilder sb = below.get(sourceFrame);
				for (int i = minC+1; i < maxC; i++) {
					if (sb.charAt(i) == ' ') {
						sb.setCharAt(i, '-');
					}
				}
				sb.setCharAt(minC, '+');
				sb.setCharAt(maxC, '+');
			}
		}
		

		/*
		 * Concatenate strings
		 */

		StringBuilder finalString = new StringBuilder();
		for (Integer frame : frames) {

			finalString.append(strings.get(frame).toString());
			finalString.append('\n');
			finalString.append(below.get(frame).toString());
			finalString.append('\n');
		}


		return finalString.toString();

	}
	
	
	
	
	public static final boolean isTree(TrackModel model, TimeDirectedNeighborIndex cache) {
		return isTree(model.vertexSet(), cache);
	}
	

	
	public static final boolean isTree(Iterable<BCellobject> BCellobjects, TimeDirectedNeighborIndex cache) {
		for (BCellobject BCellobject : BCellobjects) {
			if (cache.predecessorsOf(BCellobject).size() > 1) {
				return false;
			}
		}
		return true;
	}
	
	
	
	
	public static final Map<BCellobject, Integer> cumulativeBranchWidth(final TrackModel model) {

		/*
		 * Elements stored:
		 * 	0. cumsum of leaf
		 */
		Supplier<int[]> factory = new Supplier<int[]>() {
			@Override
			public int[] get() {
				return new int[1];
			}
		};

		/*
		 * Build isleaf tree
		 */

		final TimeDirectedNeighborIndex cache = model.getDirectedNeighborIndex();

		Function1<BCellobject, int[]> isLeafFun = new Function1<BCellobject, int[]>() {
			@Override
			public void compute(BCellobject input, int[] output) {
				if (cache.successorsOf(input).size() == 0) {
					output[0] = 1;
				} else {
					output[0] = 0;
				}
			}
		};


		Map<BCellobject, int[]> mappings = new HashMap<>();
		SimpleDirectedWeightedGraph<int[], DefaultWeightedEdge> leafTree = model.copy(factory, isLeafFun, mappings);

		/*
		 * Find root BCellobjects & first BCellobjects
		 * Roots are BCellobjects without any ancestors. There might be more than one per track.
		 * First BCellobjects are the first root found in a track. There is only one per track.
		 * 
		 * By the way we compute the largest BCellobject name
		 */

		Set<BCellobject> roots = new HashSet<>(model.nTracks(false)); // approx
		Set<BCellobject> firsts = new HashSet<>(model.nTracks(false)); // exact
		Set<Integer> ids = model.trackIDs(false);
		for (Integer id : ids) {
			Set<BCellobject> track = model.trackBCellobjects(id);
			boolean firstFound = false;
			for (BCellobject BCellobject : track) {

				if (cache.predecessorsOf(BCellobject).size() == 0) {
					if (!firstFound) {
						firsts.add(BCellobject);
					}
					roots.add(BCellobject);
					firstFound = true;
				}
			}
		}

		/*
		 * Build cumsum value
		 */

		Function2<int[], int[]> cumsumFun = new Function2<int[], int[]>() {
			@Override
			public void compute(int[] input1, int[] input2, int[] output) {
				output[0] = input1[0] + input2[0];
			}
		};

		RecursiveCumSum<int[], DefaultWeightedEdge> cumsum = new RecursiveCumSum<>(leafTree, cumsumFun);
		for(BCellobject root : firsts) {
			int[] current = mappings.get(root);
			cumsum.apply(current);
		}
		
		/*
		 * Convert to map of BCellobject vs integer 
		 */
		Map<BCellobject, Integer> widths = new HashMap<>();
		for (BCellobject BCellobject : model.vertexSet()) {
			widths.put(BCellobject, mappings.get(BCellobject)[0]);
		}
		
		return widths;
	}
	
	
	

	private static char[] makeSpaces(int width) {
		return makeChars(width, ' ');
	}


	private static char[] makeChars(int width, char c) {
		char[] chars = new char[width];
		Arrays.fill(chars, c);
		return chars;
	}


	/**
	 * @return true only if the given model is a tree; that is: every BCellobject has one or less
	 * predecessors.
	 */
	public static final Set<BCellobject> getSibblings(final NeighborCache<BCellobject, DefaultWeightedEdge> cache, final BCellobject BCellobject) {
		HashSet<BCellobject> sibblings = new HashSet<>();
		Set<BCellobject> predecessors = cache.predecessorsOf(BCellobject);
		for (BCellobject predecessor : predecessors) {
			sibblings.addAll(cache.successorsOf(predecessor));
		}
		return sibblings;
	}


}
