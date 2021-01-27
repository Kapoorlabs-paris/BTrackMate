package Buddy.plugin.trackmate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import budDetector.BCellobject;
import Buddy.plugin.trackmate.features.FeatureFilter;
import net.imglib2.algorithm.MultiThreaded;

/**
 * A utility class that wrap the {@link java.util.SortedMap} we use to store the
 * BCellobjects contained in each frame with a few utility methods.
 * <p>
 * Internally we rely on ConcurrentSkipListMap to allow concurrent access
 * without clashes.
 * <p>
 * This class is {@link MultiThreaded}. There are a few processes that can
 * benefit from multithreaded computation ({@link #filter(Collection)},
 * {@link #filter(FeatureFilter)}
 *
 * @author VK, Jean-Yves Tinevez &lt;jeanyves.tinevez@gmail.com&gt; - Feb 2011 -
 *         2013
 *
 */
public class BCellobjectCollection implements MultiThreaded {

	public static final Double ZERO = Double.valueOf(0d);

	public static final Double ONE = Double.valueOf(1d);
	
	public static final String VISIBLITY = "VISIBILITY";

	/**
	 * Time units for filtering and cropping operation timeouts. Filtering should
	 * not take more than 1 minute.
	 */
	private static final TimeUnit TIME_OUT_UNITS = TimeUnit.MINUTES;

	/**
	 * Time for filtering and cropping operation timeouts. Filtering should not take
	 * more than 1 minute.
	 */
	private static final long TIME_OUT_DELAY = 1;

	/** The frame by frame list of BCellobject this object wrap. */
	private ConcurrentSkipListMap<Integer, Set<BCellobject>> content = new ConcurrentSkipListMap<>();

	private int numThreads;

	/*
	 * CONSTRUCTORS
	 */

	/**
	 * Construct a new empty BCellobject collection.
	 */
	public BCellobjectCollection() {
		setNumThreads();
	}

	/*
	 * METHODS
	 */

	/**
	 * Retrieves and returns the {@link BCellobject} object in this collection with
	 * the specified ID. Returns <code>null</code> if the BCellobject cannot be
	 * found. All BCellobjects, visible or not, are searched for.
	 *
	 * @param ID
	 *            the ID to look for.
	 * @return the BCellobject with the specified ID or <code>null</code> if this
	 *         BCellobject does not exist or does not belong to this collection.
	 */
	public BCellobject search(final int ID) {
		BCellobject BCellobject = null;
		for (final BCellobject s : iterable(false)) {
			if (s.ID() == ID) {
				BCellobject = s;
				break;
			}
		}
		return BCellobject;
	}

	@Override
	public String toString() {
		String str = super.toString();
		str += ": contains " + getNBCellobjects() + " BCellobjects total in " + keySet().size()
				+ " different frames, over which " + getNBCellobjects() + " are visible:\n";
		for (final int key : content.keySet()) {
			str += "\tframe " + key + ": " + getNBCellobjects(key) + " BCellobjects total, " + getNBCellobjects(key)
					+ " visible.\n";
		}
		return str;
	}

	/**
	 * Adds the given BCellobject to this collection, at the specified frame, and
	 * mark it as visible.
	 * <p>
	 * If the frame does not exist yet in the collection, it is created and added.
	 * Upon adding, the added BCellobject has its feature {@link BCellobject#FRAME}
	 * updated with the passed frame value.
	 * 
	 * @param BCellobject
	 *            the BCellobject to add.
	 * @param frame
	 *            the frame to add it to.
	 */
	public void add(final BCellobject BCellobject, final Integer frame) {
		Set<BCellobject> BCellobjects = content.get(frame);
		if (null == BCellobjects) {
			BCellobjects = new HashSet<>();
			content.put(frame, BCellobjects);
		}
		BCellobjects.add(BCellobject);
		BCellobject.time = frame;
	}

	/**
	 * Removes the given BCellobject from this collection, at the specified frame.
	 * <p>
	 * If the BCellobject frame collection does not exist yet, nothing is done and
	 * <code>false</code> is returned. If the BCellobject cannot be found in the
	 * frame content, nothing is done and <code>false</code> is returned.
	 * 
	 * @param BCellobject
	 *            the BCellobject to remove.
	 * @param frame
	 *            the frame to remove it from.
	 * @return <code>true</code> if the BCellobject was succesfully removed.
	 */
	public boolean remove(final BCellobject BCellobject, final Integer frame) {
		final Set<BCellobject> BCellobjects = content.get(frame);
		if (null == BCellobjects) {
			return false;
		}
		return BCellobjects.remove(BCellobject);
	}
	
	public void setVisible( final boolean visible )
	{
		final Double val = visible ? ONE : ZERO;
		final Collection< Integer > frames = content.keySet();

		final ExecutorService executors = Executors.newFixedThreadPool( numThreads );
		for ( final Integer frame : frames )
		{

			final Runnable command = new Runnable()
			{
				@Override
				public void run()
				{

					final Set< BCellobject > spots = content.get( frame );
					for ( final BCellobject spot : spots )
					{
						spot.putFeature( VISIBLITY, val );
					}

				}
			};
			executors.execute( command );
		}

		executors.shutdown();
		try
		{
			final boolean ok = executors.awaitTermination( TIME_OUT_DELAY, TIME_OUT_UNITS );
			if ( !ok )
			{
				System.err.println( "[SpotCollection.setVisible()] Timeout of " + TIME_OUT_DELAY + " " + TIME_OUT_UNITS + " reached." );
			}
		}
		catch ( final InterruptedException e )
		{
			e.printStackTrace();
		}
	}

	/**
	 * Filters out the content of this collection using the specified
	 * {@link FeatureFilter} collection. BCellobjects that are filtered out are
	 * marked as invisible, and visible otherwise. To be marked as visible, a
	 * BCellobject must pass <b>all</b> of the specified filters (AND chaining).
	 *
	 * @param filters
	 *            the filter collection to use.
	 */
	public final void filter(final Collection<FeatureFilter> filters) {

		final Collection<Integer> frames = content.keySet();
		final ExecutorService executors = Executors.newFixedThreadPool(numThreads);

		for (final Integer frame : frames) {
			final Runnable command = new Runnable() {
				@Override
				public void run() {
					final Set<BCellobject> BCellobjects = content.get(frame);

					Double val, tval;
					boolean isAbove;
					for (final BCellobject BCellobject : BCellobjects) {

						for (final FeatureFilter featureFilter : filters) {

							val = BCellobject.getFeature(featureFilter.feature);
							tval = featureFilter.value;
							isAbove = featureFilter.isAbove;

							if (isAbove && val.compareTo(tval) < 0 || !isAbove && val.compareTo(tval) > 0) {
								break;
							}
						} // loop over filters

					} // loop over BCellobjects

				}

			};
			executors.execute(command);
		}

		executors.shutdown();
		try {
			final boolean ok = executors.awaitTermination(TIME_OUT_DELAY, TIME_OUT_UNITS);
			if (!ok) {
				System.err.println("[BCellobjectCollection.filter()] Timeout of " + TIME_OUT_DELAY + " "
						+ TIME_OUT_UNITS + " reached while filtering.");
			}
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns the closest {@link BCellobject} to the given location (encoded as a
	 * BCellobject), contained in the frame <code>frame</code>. If the frame has no
	 * BCellobject, return <code>null</code>.
	 *
	 * @param location
	 *            the location to search for.
	 * @param frame
	 *            the frame to inspect.
	 * @param visibleBCellobjectsOnly
	 *            if true, will only search though visible BCellobjects. If false,
	 *            will search through all BCellobjects.
	 * @return the closest BCellobject to the specified location, member of this
	 *         collection.
	 */
	public final BCellobject getClosestBCellobject(final BCellobject location, final int frame,
			final boolean visibleBCellobjectsOnly) {
		final Set<BCellobject> BCellobjects = content.get(frame);
		if (null == BCellobjects)
			return null;
		double d2;
		double minDist = Double.POSITIVE_INFINITY;
		BCellobject target = null;
		for (final BCellobject s : BCellobjects) {

			d2 = s.squareDistanceTo(location);
			if (d2 < minDist) {
				minDist = d2;
				target = s;
			}

		}
		return target;
	}

	/**
	 * Returns the {@link BCellobject} at the given location (encoded as a
	 * BCellobject), contained in the frame <code>frame</code>. A BCellobject is
	 * returned <b>only</b> if there exists a BCellobject such that the given
	 * location is within the BCellobject radius. Otherwise <code>null</code> is
	 * returned.
	 *
	 * @param location
	 *            the location to search for.
	 * @param frame
	 *            the frame to inspect.
	 * @param visibleBCellobjectsOnly
	 *            if true, will only search though visible BCellobjects. If false,
	 *            will search through all BCellobjects.
	 * @return the closest BCellobject such that the specified location is within
	 *         its radius, member of this collection, or <code>null</code> is such a
	 *         BCellobjects cannot be found.
	 */
	public final BCellobject getBCellobjectAt(final BCellobject location, final int frame,
			final boolean visibleBCellobjectsOnly) {
		final Set<BCellobject> BCellobjects = content.get(frame);
		if (null == BCellobjects || BCellobjects.isEmpty()) {
			return null;
		}

		final TreeMap<Double, BCellobject> distanceToBCellobject = new TreeMap<>();
		double d2;
		for (final BCellobject s : BCellobjects) {

			d2 = s.squareDistanceTo(location);
			double size = s.getFeature(BCellobject.RADIUS[0]) * s.getFeature(BCellobject.RADIUS[1]) * s.getFeature(BCellobject.RADIUS[2]);
			if (d2 < size)
				distanceToBCellobject.put(d2, s);
		}
		if (distanceToBCellobject.isEmpty())
			return null;

		return distanceToBCellobject.firstEntry().getValue();
	}

	/**
	 * Returns the <code>n</code> closest {@link BCellobject} to the given location
	 * (encoded as a BCellobject), contained in the frame <code>frame</code>. If the
	 * number of BCellobjects in the frame is exhausted, a shorter list is returned.
	 * <p>
	 * The list is ordered by increasing distance to the given location.
	 *
	 * @param location
	 *            the location to search for.
	 * @param frame
	 *            the frame to inspect.
	 * @param n
	 *            the number of BCellobjects to search for.
	 * @param visibleBCellobjectsOnly
	 *            if true, will only search though visible BCellobjects. If false,
	 *            will search through all BCellobjects.
	 * @return a new list, with of at most <code>n</code> BCellobjects, ordered by
	 *         increasing distance from the specified location.
	 */
	public final List<BCellobject> getNClosestBCellobjects(final BCellobject location, final int frame, int n,
			final boolean visibleBCellobjectsOnly) {
		final Set<BCellobject> BCellobjects = content.get(frame);
		final TreeMap<Double, BCellobject> distanceToBCellobject = new TreeMap<>();

		double d2;
		for (final BCellobject s : BCellobjects) {

			d2 = s.squareDistanceTo(location);
			distanceToBCellobject.put(d2, s);
		}

		final List<BCellobject> selectedBCellobjects = new ArrayList<>(n);
		final Iterator<Double> it = distanceToBCellobject.keySet().iterator();
		while (n > 0 && it.hasNext()) {
			selectedBCellobjects.add(distanceToBCellobject.get(it.next()));
			n--;
		}
		return selectedBCellobjects;
	}

	/**
	 * Returns the total number of BCellobjects in this collection, over all frames.
	 *
	 * @param visibleBCellobjectsOnly
	 *            if true, will only count visible BCellobjects. If false count all
	 *            BCellobjects.
	 * @return the total number of BCellobjects in this collection.
	 */
	public final int getNBCellobjects() {
		int nBCellobjects = 0;

		final Iterator<BCellobject> it = iterator(true);
		while (it.hasNext()) {
			it.next();
			nBCellobjects++;
		}

		return nBCellobjects;
	}

	/**
	 * Returns the number of BCellobjects at the given frame.
	 *
	 * @param frame
	 *            the frame.
	 * @param visibleBCellobjectsOnly
	 *            if true, will only count visible BCellobjects. If false count all
	 *            BCellobjects.
	 * @return the number of BCellobjects at the given frame.
	 */
	public int getNBCellobjects(final int frame) {

		final Iterator<BCellobject> it = iterator(frame);
		int nBCellobjects = 0;
		while (it.hasNext()) {
			it.next();
			nBCellobjects++;
		}
		return nBCellobjects;

	}

	/*
	 * FEATURES
	 */

	/**
	 * Builds and returns a new map of feature values for this BCellobject
	 * collection. Each feature maps a double array, with 1 element per
	 * {@link BCellobject}, all pooled together.
	 *
	 * @param features
	 *            the features to collect
	 * @param visibleOnly
	 *            if <code>true</code>, only the visible BCellobject values will be
	 *            collected.
	 * @return a new map instance.
	 */
	public Map<String, double[]> collectValues(final Collection<String> features, final boolean visibleOnly) {
		final Map<String, double[]> featureValues = new ConcurrentHashMap<>(features.size());
		final ExecutorService executors = Executors.newFixedThreadPool(numThreads);

		for (final String feature : features) {
			final Runnable command = new Runnable() {
				@Override
				public void run() {
					final double[] values = collectValues(feature, visibleOnly);
					featureValues.put(feature, values);
				}

			};
			executors.execute(command);
		}

		executors.shutdown();
		try {
			final boolean ok = executors.awaitTermination(TIME_OUT_DELAY, TIME_OUT_UNITS);
			if (!ok) {
				System.err.println("[BCellobjectCollection.collectValues()] Timeout of " + TIME_OUT_DELAY + " "
						+ TIME_OUT_UNITS + " reached while filtering.");
			}
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}

		return featureValues;
	}

	/**
	 * Returns the feature values of this BCellobject collection as a new double
	 * array.
	 * <p>
	 * If some BCellobjects do not have the interrogated feature set (stored value
	 * is <code>null</code>) or if the value is {@link Double#NaN}, they are
	 * skipped. The returned array might be therefore of smaller size than the
	 * number of BCellobjects interrogated.
	 *
	 * @param feature
	 *            the feature to collect.
	 * @param visibleOnly
	 *            if <code>true</code>, only the visible BCellobject values will be
	 *            collected.
	 * @return a new <code>double</code> array.
	 */
	public final double[] collectValues(final String feature, final boolean visibleOnly) {
		final double[] values = new double[getNBCellobjects()];
		int index = 0;
		for (final BCellobject BCellobject : iterable(visibleOnly)) {
			final Double feat = BCellobject.getFeature(feature);
			if (null == feat) {
				continue;
			}
			final double val = feat.doubleValue();
			if (Double.isNaN(val)) {
				continue;
			}
			values[index] = val;
			index++;
		}
		return values;
	}

	/*
	 * ITERABLE & co
	 */

	/**
	 * Return an iterator that iterates over all the BCellobjects contained in this
	 * collection.
	 *
	 * @param visibleBCellobjectsOnly
	 *            if true, the returned iterator will only iterate through visible
	 *            BCellobjects. If false, it will iterate over all BCellobjects.
	 * @return an iterator that iterates over this collection.
	 */
	public Iterator<BCellobject> iterator(final boolean visibleBCellobjectsOnly) {
		if (visibleBCellobjectsOnly)
			return new VisibleBCellobjectsIterator();

		return new AllBCellobjectsIterator();
	}

	/**
	 * Return an iterator that iterates over the BCellobjects in the specified
	 * frame.
	 *
	 * @param visibleBCellobjectsOnly
	 *            if true, the returned iterator will only iterate through visible
	 *            BCellobjects. If false, it will iterate over all BCellobjects.
	 * @param frame
	 *            the frame to iterate over.
	 * @return an iterator that iterates over the content of a frame of this
	 *         collection.
	 */
	public Iterator<BCellobject> iterator(final Integer frame) {
		final Set<BCellobject> frameContent = content.get(frame);
		if (null == frameContent) {
			return EMPTY_ITERATOR;
		}

		return frameContent.iterator();
	}

	/**
	 * A convenience methods that returns an {@link Iterable} wrapper for this
	 * collection as a whole.
	 *
	 * @param visibleBCellobjectsOnly
	 *            if true, the iterable will contains only visible BCellobjects.
	 *            Otherwise, it will contain all the BCellobjects.
	 * @return an iterable view of this BCellobject collection.
	 */
	public Iterable<BCellobject> iterable(final boolean visibleBCellobjectsOnly) {
		return new WholeCollectionIterable(visibleBCellobjectsOnly);
	}

	/**
	 * A convenience methods that returns an {@link Iterable} wrapper for a specific
	 * frame of this BCellobject collection. The iterable is backed-up by the actual
	 * collection content, so modifying it can have unexpected results.
	 *
	 * @param visibleBCellobjectsOnly
	 *            if true, the iterable will contains only visible BCellobjects of
	 *            the specified frame. Otherwise, it will contain all the
	 *            BCellobjects of the specified frame.
	 * @param frame
	 *            the frame of the content the returned iterable will wrap.
	 * @return an iterable view of the content of a single frame of this BCellobject
	 *         collection.
	 */
	public Iterable<BCellobject> iterable(final int frame, final boolean visibleBCellobjectsOnly) {
		if (visibleBCellobjectsOnly)
			return new FrameVisibleIterable(frame);

		return content.get(frame);
	}

	/*
	 * SORTEDMAP
	 */

	/**
	 * Stores the specified BCellobjects as the content of the specified frame. The
	 * added BCellobjects are all marked as not visible. Their
	 * {@link BCellobject#FRAME} is updated to be the specified frame.
	 *
	 * @param frame
	 *            the frame to store these BCellobjects at. The specified
	 *            BCellobjects replace the previous content of this frame, if any.
	 * @param BCellobjects
	 *            the BCellobjects to store.
	 */
	public void put(final int frame, final Collection<BCellobject> BCellobjects) {
		final Set<BCellobject> value = new HashSet<>(BCellobjects);
		for (final BCellobject BCellobject : value) {
			BCellobject.putFeature(BCellobject.POSITION_T, Double.valueOf(frame));

		}
		content.put(frame, value);
	}

	/**
	 * Returns the first (lowest) frame currently in this collection.
	 *
	 * @return the first (lowest) frame currently in this collection.
	 */
	public Integer firstKey() {
		if (content.isEmpty()) {
			return 0;
		}
		return content.firstKey();
	}

	/**
	 * Returns the last (highest) frame currently in this collection.
	 *
	 * @return the last (highest) frame currently in this collection.
	 */
	public Integer lastKey() {
		if (content.isEmpty()) {
			return 0;
		}
		return content.lastKey();
	}

	/**
	 * Returns a NavigableSet view of the frames contained in this collection. The
	 * set's iterator returns the keys in ascending order. The set is backed by the
	 * map, so changes to the map are reflected in the set, and vice-versa. The set
	 * supports element removal, which removes the corresponding mapping from the
	 * map, via the Iterator.remove, Set.remove, removeAll, retainAll, and clear
	 * operations. It does not support the add or addAll operations.
	 * <p>
	 * The view's iterator is a "weakly consistent" iterator that will never throw
	 * ConcurrentModificationException, and guarantees to traverse elements as they
	 * existed upon construction of the iterator, and may (but is not guaranteed to)
	 * reflect any modifications subsequent to construction.
	 *
	 * @return a navigable set view of the frames in this collection.
	 */
	public NavigableSet<Integer> keySet() {
		return content.keySet();
	}

	/**
	 * Removes all the content from this collection.
	 */
	public void clear() {
		content.clear();
	}

	/*
	 * MULTITHREADING
	 */

	@Override
	public void setNumThreads() {
		this.numThreads = Runtime.getRuntime().availableProcessors();
	}

	@Override
	public void setNumThreads(final int numThreads) {
		this.numThreads = numThreads;
	}

	@Override
	public int getNumThreads() {
		return numThreads;
	}

	/*
	 * PRIVATE CLASSES
	 */

	private class AllBCellobjectsIterator implements Iterator<BCellobject> {

		private boolean hasNext = true;

		private final Iterator<Integer> frameIterator;

		private Iterator<BCellobject> contentIterator;

		private BCellobject next = null;

		public AllBCellobjectsIterator() {
			this.frameIterator = content.keySet().iterator();
			if (!frameIterator.hasNext()) {
				hasNext = false;
				return;
			}
			final Set<BCellobject> currentFrameContent = content.get(frameIterator.next());
			contentIterator = currentFrameContent.iterator();
			iterate();
		}

		private void iterate() {
			while (true) {

				// Is there still BCellobjects in current content?
				if (!contentIterator.hasNext()) {
					// No. Then move to next frame.
					// Is there still frames to iterate over?
					if (!frameIterator.hasNext()) {
						// No. Then we are done
						hasNext = false;
						next = null;
						return;
					}

					contentIterator = content.get(frameIterator.next()).iterator();
					continue;
				}
				next = contentIterator.next();
				return;
			}
		}

		@Override
		public boolean hasNext() {
			return hasNext;
		}

		@Override
		public BCellobject next() {
			final BCellobject toReturn = next;
			iterate();
			return toReturn;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException(
					"Remove operation is not supported for BCellobjectCollection iterators.");
		}

	}

	private class VisibleBCellobjectsIterator implements Iterator<BCellobject> {

		private boolean hasNext = true;

		private final Iterator<Integer> frameIterator;

		private Iterator<BCellobject> contentIterator;

		private BCellobject next = null;

		private Set<BCellobject> currentFrameContent;

		public VisibleBCellobjectsIterator() {
			this.frameIterator = content.keySet().iterator();
			if (!frameIterator.hasNext()) {
				hasNext = false;
				return;
			}
			currentFrameContent = content.get(frameIterator.next());
			contentIterator = currentFrameContent.iterator();
			iterate();
		}

		private void iterate() {

			while (true) {
				// Is there still BCellobjects in current content?
				if (!contentIterator.hasNext()) {
					// No. Then move to next frame.
					// Is there still frames to iterate over?
					if (!frameIterator.hasNext()) {
						// No. Then we are done
						hasNext = false;
						next = null;
						return;
					}

					// Yes. Then start iterating over the next frame.
					currentFrameContent = content.get(frameIterator.next());
					contentIterator = currentFrameContent.iterator();
					continue;
				}
				next = contentIterator.next();
				// Is it visible?

			}
		}

		@Override
		public boolean hasNext() {
			return hasNext;
		}

		@Override
		public BCellobject next() {
			final BCellobject toReturn = next;
			iterate();
			return toReturn;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException(
					"Remove operation is not supported for BCellobjectCollection iterators.");
		}

	}

	private class VisibleBCellobjectsFrameIterator implements Iterator<BCellobject> {

		private boolean hasNext = true;

		private BCellobject next = null;

		private final Iterator<BCellobject> contentIterator;

		public VisibleBCellobjectsFrameIterator(final Set<BCellobject> frameContent) {
			if (null == frameContent) {
				this.contentIterator = EMPTY_ITERATOR;
			} else {
				this.contentIterator = frameContent.iterator();
			}
			iterate();
		}

		private void iterate() {
			while (true) {
				if (!contentIterator.hasNext()) {
					// No. Then we are done
					hasNext = false;
					next = null;
					return;
				}
				next = contentIterator.next();
				// Is it visible?

			}
		}

		@Override
		public boolean hasNext() {
			return hasNext;
		}

		@Override
		public BCellobject next() {
			final BCellobject toReturn = next;
			iterate();
			return toReturn;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException(
					"Remove operation is not supported for BCellobjectCollection iterators.");
		}

	}

	/**
	 * Returns a new {@link BCellobjectCollection}, made of only the BCellobjects
	 * marked as visible. All the BCellobjects will then be marked as not-visible.
	 *
	 * @return a new BCellobject collection, made of only the BCellobjects marked as
	 *         visible.
	 */
	public BCellobjectCollection crop() {
		final BCellobjectCollection ns = new BCellobjectCollection();
		ns.setNumThreads(numThreads);

		final Collection<Integer> frames = content.keySet();
		final ExecutorService executors = Executors.newFixedThreadPool(numThreads);
		for (final Integer frame : frames) {

			final Runnable command = new Runnable() {
				@Override
				public void run() {
					final Set<BCellobject> fc = content.get(frame);
					final Set<BCellobject> nfc = new HashSet<>(getNBCellobjects(frame));

					for (final BCellobject BCellobject : fc) {

						nfc.add(BCellobject);
					}
					ns.content.put(frame, nfc);
				}
			};
			executors.execute(command);
		}

		executors.shutdown();
		try {
			final boolean ok = executors.awaitTermination(TIME_OUT_DELAY, TIME_OUT_UNITS);
			if (!ok) {
				System.err.println("[BCellobjectCollection.crop()] Timeout of " + TIME_OUT_DELAY + " " + TIME_OUT_UNITS
						+ " reached while cropping.");
			}
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
		return ns;
	}

	/**
	 * A convenience wrapper that implements {@link Iterable} for this BCellobject
	 * collection.
	 */
	private final class WholeCollectionIterable implements Iterable<BCellobject> {

		private final boolean visibleBCellobjectsOnly;

		public WholeCollectionIterable(final boolean visibleBCellobjectsOnly) {
			this.visibleBCellobjectsOnly = visibleBCellobjectsOnly;
		}

		@Override
		public Iterator<BCellobject> iterator() {
			if (visibleBCellobjectsOnly)
				return new VisibleBCellobjectsIterator();

			return new AllBCellobjectsIterator();
		}
	}

	/**
	 * A convenience wrapper that implements {@link Iterable} for this BCellobject
	 * collection.
	 */
	private final class FrameVisibleIterable implements Iterable<BCellobject> {

		private final int frame;

		public FrameVisibleIterable(final int frame) {
			this.frame = frame;
		}

		@Override
		public Iterator<BCellobject> iterator() {
			return new VisibleBCellobjectsFrameIterator(content.get(frame));
		}
	}

	private static final Iterator<BCellobject> EMPTY_ITERATOR = new Iterator<BCellobject>() {

		@Override
		public boolean hasNext() {
			return false;
		}

		@Override
		public BCellobject next() {
			return null;
		}

		@Override
		public void remove() {
		}
	};

	/*
	 * STATIC METHODS
	 */

	/**
	 * Creates a new {@link BCellobjectCollection} containing only the specified
	 * BCellobjects. Their frame origin is retrieved from their
	 * {@link BCellobject#FRAME} feature, so it must be set properly for all
	 * BCellobjects. All the BCellobjects of the new collection have the same
	 * visibility that the one they carry.
	 *
	 * @param BCellobjects
	 *            the BCellobject collection to build from.
	 * @return a new {@link BCellobjectCollection} instance.
	 */
	public static BCellobjectCollection fromCollection(final Iterable<BCellobject> BCellobjects) {
		final BCellobjectCollection sc = new BCellobjectCollection();
		for (final BCellobject BCellobject : BCellobjects) {
			final int frame = BCellobject.getFeature(BCellobject.POSITION_T).intValue();
			Set<BCellobject> fc = sc.content.get(frame);
			if (null == fc) {
				fc = new HashSet<>();
				sc.content.put(frame, fc);
			}
			fc.add(BCellobject);
		}
		return sc;
	}

	/**
	 * Creates a new {@link BCellobjectCollection} from a copy of the specified map
	 * of sets. The BCellobjects added this way are completely untouched. In
	 * particular, their {@link #VISIBLITY} feature is left untouched, which makes
	 * this method suitable to de-serialize a {@link BCellobjectCollection}.
	 *
	 * @param source
	 *            the map to buidl the BCellobject collection from.
	 * @return a new BCellobjectCollection.
	 */
	public static BCellobjectCollection fromMap(final Map<Integer, Set<BCellobject>> source) {
		final BCellobjectCollection sc = new BCellobjectCollection();
		sc.content = new ConcurrentSkipListMap<>(source);
		return sc;
	}
}
