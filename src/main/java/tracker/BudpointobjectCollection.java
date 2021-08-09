package tracker;

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

import budDetector.Budpointobject;
import fiji.plugin.btrackmate.features.FeatureFilter;
import net.imglib2.algorithm.MultiThreaded;

/**
 * A utility class that wrap the {@link java.util.SortedMap} we use to store the
 * Budpointobjects contained in each frame with a few utility methods.
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
public class BudpointobjectCollection implements MultiThreaded {

	public static final Double ZERO = Double.valueOf(0d);

	public static final Double ONE = Double.valueOf(1d);

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

	/** The frame by frame list of Budpointobject this object wrap. */
	private ConcurrentSkipListMap<Integer, Set<Budpointobject>> content = new ConcurrentSkipListMap<>();

	private int numThreads;

	/*
	 * CONSTRUCTORS
	 */

	/**
	 * Construct a new empty Budpointobject collection.
	 */
	public BudpointobjectCollection() {
		setNumThreads();
	}

	/*
	 * METHODS
	 */

	/**
	 * Retrieves and returns the {@link Budpointobject} object in this collection
	 * with the specified ID. Returns <code>null</code> if the Budpointobject cannot
	 * be found. All Budpointobjects, visible or not, are searched for.
	 *
	 * @param ID the ID to look for.
	 * @return the Budpointobject with the specified ID or <code>null</code> if this
	 *         Budpointobject does not exist or does not belong to this collection.
	 */
	public Budpointobject search(final int ID) {
		Budpointobject Budpointobject = null;
		for (final Budpointobject s : iterable(false)) {
			if (s.ID() == ID) {
				Budpointobject = s;
				break;
			}
		}
		return Budpointobject;
	}

	@Override
	public String toString() {
		String str = super.toString();
		str += ": contains " + getNBudpointobjects() + " Budpointobjects total in " + keySet().size()
				+ " different frames, over which " + getNBudpointobjects() + " are visible:\n";
		for (final int key : content.keySet()) {
			str += "\tframe " + key + ": " + getNBudpointobjects(key) + " Budpointobjects total, "
					+ getNBudpointobjects(key) + " visible.\n";
		}
		return str;
	}

	/**
	 * Adds the given Budpointobject to this collection, at the specified frame, and
	 * mark it as visible.
	 * <p>
	 * If the frame does not exist yet in the collection, it is created and added.
	 * Upon adding, the added Budpointobject has its feature
	 * {@link Budpointobject#FRAME} updated with the passed frame value.
	 * 
	 * @param Budpointobject the Budpointobject to add.
	 * @param frame          the frame to add it to.
	 */
	public void add(final Budpointobject Budpointobject, final Integer frame) {
		Set<Budpointobject> Budpointobjects = content.get(frame);
		if (null == Budpointobjects) {
			Budpointobjects = new HashSet<>();
			content.put(frame, Budpointobjects);
		}
		Budpointobjects.add(Budpointobject);
		Budpointobject.t = frame;
	}

	/**
	 * Removes the given Budpointobject from this collection, at the specified
	 * frame.
	 * <p>
	 * If the Budpointobject frame collection does not exist yet, nothing is done
	 * and <code>false</code> is returned. If the Budpointobject cannot be found in
	 * the frame content, nothing is done and <code>false</code> is returned.
	 * 
	 * @param Budpointobject the Budpointobject to remove.
	 * @param frame          the frame to remove it from.
	 * @return <code>true</code> if the Budpointobject was succesfully removed.
	 */
	public boolean remove(final Budpointobject Budpointobject, final Integer frame) {
		final Set<Budpointobject> Budpointobjects = content.get(frame);
		if (null == Budpointobjects) {
			return false;
		}
		return Budpointobjects.remove(Budpointobject);
	}

	/**
	 * Filters out the content of this collection using the specified
	 * {@link FeatureFilter} collection. Budpointobjects that are filtered out are
	 * marked as invisible, and visible otherwise. To be marked as visible, a
	 * Budpointobject must pass <b>all</b> of the specified filters (AND chaining).
	 *
	 * @param filters the filter collection to use.
	 */
	public final void filter(final Collection<FeatureFilter> filters) {

		final Collection<Integer> frames = content.keySet();
		final ExecutorService executors = Executors.newFixedThreadPool(numThreads);

		for (final Integer frame : frames) {
			final Runnable command = new Runnable() {
				@Override
				public void run() {
					final Set<Budpointobject> Budpointobjects = content.get(frame);

					Double val, tval;
					boolean isAbove;
					for (final Budpointobject Budpointobject : Budpointobjects) {

						for (final FeatureFilter featureFilter : filters) {

							val = Budpointobject.getFeature(featureFilter.feature);
							tval = featureFilter.value;
							isAbove = featureFilter.isAbove;

							if (isAbove && val.compareTo(tval) < 0 || !isAbove && val.compareTo(tval) > 0) {
								break;
							}
						} // loop over filters

					} // loop over Budpointobjects

				}

			};
			executors.execute(command);
		}

		executors.shutdown();
		try {
			final boolean ok = executors.awaitTermination(TIME_OUT_DELAY, TIME_OUT_UNITS);
			if (!ok) {
				System.err.println("[BudpointobjectCollection.filter()] Timeout of " + TIME_OUT_DELAY + " "
						+ TIME_OUT_UNITS + " reached while filtering.");
			}
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns the closest {@link Budpointobject} to the given location (encoded as
	 * a Budpointobject), contained in the frame <code>frame</code>. If the frame
	 * has no Budpointobject, return <code>null</code>.
	 *
	 * @param location                   the location to search for.
	 * @param frame                      the frame to inspect.
	 * @param visibleBudpointobjectsOnly if true, will only search though visible
	 *                                   Budpointobjects. If false, will search
	 *                                   through all Budpointobjects.
	 * @return the closest Budpointobject to the specified location, member of this
	 *         collection.
	 */
	public final Budpointobject getClosestBudpointobject(final Budpointobject location, final int frame,
			final boolean visibleBudpointobjectsOnly) {
		final Set<Budpointobject> Budpointobjects = content.get(frame);
		if (null == Budpointobjects)
			return null;
		double d2;
		double minDist = Double.POSITIVE_INFINITY;
		Budpointobject target = null;
		for (final Budpointobject s : Budpointobjects) {

			d2 = s.squareDistanceTo(location);
			if (d2 < minDist) {
				minDist = d2;
				target = s;
			}

		}
		return target;
	}

	/**
	 * Returns the {@link Budpointobject} at the given location (encoded as a
	 * Budpointobject), contained in the frame <code>frame</code>. A Budpointobject
	 * is returned <b>only</b> if there exists a Budpointobject such that the given
	 * location is within the Budpointobject radius. Otherwise <code>null</code> is
	 * returned.
	 *
	 * @param location                   the location to search for.
	 * @param frame                      the frame to inspect.
	 * @param visibleBudpointobjectsOnly if true, will only search though visible
	 *                                   Budpointobjects. If false, will search
	 *                                   through all Budpointobjects.
	 * @return the closest Budpointobject such that the specified location is within
	 *         its radius, member of this collection, or <code>null</code> is such a
	 *         Budpointobjects cannot be found.
	 */
	public final Budpointobject getBudpointobjectAt(final Budpointobject location, final int frame,
			final boolean visibleBudpointobjectsOnly) {
		final Set<Budpointobject> Budpointobjects = content.get(frame);
		if (null == Budpointobjects || Budpointobjects.isEmpty()) {
			return null;
		}

		final TreeMap<Double, Budpointobject> distanceToBudpointobject = new TreeMap<>();
		double d2;
		for (final Budpointobject s : Budpointobjects) {

			d2 = s.squareDistanceTo(location);
			if (d2 < s.getFeature(Integer.toString(1)) * s.getFeature(Integer.toString(1)))
				distanceToBudpointobject.put(d2, s);
		}
		if (distanceToBudpointobject.isEmpty())
			return null;

		return distanceToBudpointobject.firstEntry().getValue();
	}

	/**
	 * Returns the <code>n</code> closest {@link Budpointobject} to the given
	 * location (encoded as a Budpointobject), contained in the frame
	 * <code>frame</code>. If the number of Budpointobjects in the frame is
	 * exhausted, a shorter list is returned.
	 * <p>
	 * The list is ordered by increasing distance to the given location.
	 *
	 * @param location                   the location to search for.
	 * @param frame                      the frame to inspect.
	 * @param n                          the number of Budpointobjects to search
	 *                                   for.
	 * @param visibleBudpointobjectsOnly if true, will only search though visible
	 *                                   Budpointobjects. If false, will search
	 *                                   through all Budpointobjects.
	 * @return a new list, with of at most <code>n</code> Budpointobjects, ordered
	 *         by increasing distance from the specified location.
	 */
	public final List<Budpointobject> getNClosestBudpointobjects(final Budpointobject location, final int frame, int n,
			final boolean visibleBudpointobjectsOnly) {
		final Set<Budpointobject> Budpointobjects = content.get(frame);
		final TreeMap<Double, Budpointobject> distanceToBudpointobject = new TreeMap<>();

		double d2;
		for (final Budpointobject s : Budpointobjects) {

			d2 = s.squareDistanceTo(location);
			distanceToBudpointobject.put(d2, s);
		}

		final List<Budpointobject> selectedBudpointobjects = new ArrayList<>(n);
		final Iterator<Double> it = distanceToBudpointobject.keySet().iterator();
		while (n > 0 && it.hasNext()) {
			selectedBudpointobjects.add(distanceToBudpointobject.get(it.next()));
			n--;
		}
		return selectedBudpointobjects;
	}

	/**
	 * Returns the total number of Budpointobjects in this collection, over all
	 * frames.
	 *
	 * @param visibleBudpointobjectsOnly if true, will only count visible
	 *                                   Budpointobjects. If false count all
	 *                                   Budpointobjects.
	 * @return the total number of Budpointobjects in this collection.
	 */
	public final int getNBudpointobjects() {
		int nBudpointobjects = 0;

		final Iterator<Budpointobject> it = iterator(true);
		while (it.hasNext()) {
			it.next();
			nBudpointobjects++;
		}

		return nBudpointobjects;
	}

	/**
	 * Returns the number of Budpointobjects at the given frame.
	 *
	 * @param frame                      the frame.
	 * @param visibleBudpointobjectsOnly if true, will only count visible
	 *                                   Budpointobjects. If false count all
	 *                                   Budpointobjects.
	 * @return the number of Budpointobjects at the given frame.
	 */
	public int getNBudpointobjects(final int frame) {

		final Iterator<Budpointobject> it = iterator(frame);
		int nBudpointobjects = 0;
		while (it.hasNext()) {
			it.next();
			nBudpointobjects++;
		}
		return nBudpointobjects;

	}

	/*
	 * FEATURES
	 */

	/**
	 * Builds and returns a new map of feature values for this Budpointobject
	 * collection. Each feature maps a double array, with 1 element per
	 * {@link Budpointobject}, all pooled together.
	 *
	 * @param features    the features to collect
	 * @param visibleOnly if <code>true</code>, only the visible Budpointobject
	 *                    values will be collected.
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
				System.err.println("[BudpointobjectCollection.collectValues()] Timeout of " + TIME_OUT_DELAY + " "
						+ TIME_OUT_UNITS + " reached while filtering.");
			}
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}

		return featureValues;
	}

	/**
	 * Returns the feature values of this Budpointobject collection as a new double
	 * array.
	 * <p>
	 * If some Budpointobjects do not have the interrogated feature set (stored
	 * value is <code>null</code>) or if the value is {@link Double#NaN}, they are
	 * skipped. The returned array might be therefore of smaller size than the
	 * number of Budpointobjects interrogated.
	 *
	 * @param feature     the feature to collect.
	 * @param visibleOnly if <code>true</code>, only the visible Budpointobject
	 *                    values will be collected.
	 * @return a new <code>double</code> array.
	 */
	public final double[] collectValues(final String feature, final boolean visibleOnly) {
		final double[] values = new double[getNBudpointobjects()];
		int index = 0;
		for (final Budpointobject Budpointobject : iterable(visibleOnly)) {
			final Double feat = Budpointobject.getFeature(feature);
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
	 * Return an iterator that iterates over all the Budpointobjects contained in
	 * this collection.
	 *
	 * @param visibleBudpointobjectsOnly if true, the returned iterator will only
	 *                                   iterate through visible Budpointobjects. If
	 *                                   false, it will iterate over all
	 *                                   Budpointobjects.
	 * @return an iterator that iterates over this collection.
	 */
	public Iterator<Budpointobject> iterator(final boolean visibleBudpointobjectsOnly) {
		if (visibleBudpointobjectsOnly)
			return new VisibleBudpointobjectsIterator();

		return new AllBudpointobjectsIterator();
	}

	/**
	 * Return an iterator that iterates over the Budpointobjects in the specified
	 * frame.
	 *
	 * @param visibleBudpointobjectsOnly if true, the returned iterator will only
	 *                                   iterate through visible Budpointobjects. If
	 *                                   false, it will iterate over all
	 *                                   Budpointobjects.
	 * @param frame                      the frame to iterate over.
	 * @return an iterator that iterates over the content of a frame of this
	 *         collection.
	 */
	public Iterator<Budpointobject> iterator(final Integer frame) {
		final Set<Budpointobject> frameContent = content.get(frame);
		if (null == frameContent) {
			return EMPTY_ITERATOR;
		}

		return frameContent.iterator();
	}

	/**
	 * A convenience methods that returns an {@link Iterable} wrapper for this
	 * collection as a whole.
	 *
	 * @param visibleBudpointobjectsOnly if true, the iterable will contains only
	 *                                   visible Budpointobjects. Otherwise, it will
	 *                                   contain all the Budpointobjects.
	 * @return an iterable view of this Budpointobject collection.
	 */
	public Iterable<Budpointobject> iterable(final boolean visibleBudpointobjectsOnly) {
		return new WholeCollectionIterable(visibleBudpointobjectsOnly);
	}

	/**
	 * A convenience methods that returns an {@link Iterable} wrapper for a specific
	 * frame of this Budpointobject collection. The iterable is backed-up by the
	 * actual collection content, so modifying it can have unexpected results.
	 *
	 * @param visibleBudpointobjectsOnly if true, the iterable will contains only
	 *                                   visible Budpointobjects of the specified
	 *                                   frame. Otherwise, it will contain all the
	 *                                   Budpointobjects of the specified frame.
	 * @param frame                      the frame of the content the returned
	 *                                   iterable will wrap.
	 * @return an iterable view of the content of a single frame of this
	 *         Budpointobject collection.
	 */
	public Iterable<Budpointobject> iterable(final int frame, final boolean visibleBudpointobjectsOnly) {
		if (visibleBudpointobjectsOnly)
			return new FrameVisibleIterable(frame);

		return content.get(frame);
	}

	/*
	 * SORTEDMAP
	 */

	/**
	 * Stores the specified Budpointobjects as the content of the specified frame.
	 * The added Budpointobjects are all marked as not visible. Their
	 * {@link Budpointobject#FRAME} is updated to be the specified frame.
	 *
	 * @param frame           the frame to store these Budpointobjects at. The
	 *                        specified Budpointobjects replace the previous content
	 *                        of this frame, if any.
	 * @param Budpointobjects the Budpointobjects to store.
	 */
	public void put(final int frame, final Collection<Budpointobject> Budpointobjects) {
		final Set<Budpointobject> value = new HashSet<>(Budpointobjects);
		for (final Budpointobject Budpointobject : value) {
			Budpointobject.putFeature(Budpointobject.POSITION_T, Double.valueOf(frame));

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

	private class AllBudpointobjectsIterator implements Iterator<Budpointobject> {

		private boolean hasNext = true;

		private final Iterator<Integer> frameIterator;

		private Iterator<Budpointobject> contentIterator;

		private Budpointobject next = null;

		public AllBudpointobjectsIterator() {
			this.frameIterator = content.keySet().iterator();
			if (!frameIterator.hasNext()) {
				hasNext = false;
				return;
			}
			final Set<Budpointobject> currentFrameContent = content.get(frameIterator.next());
			contentIterator = currentFrameContent.iterator();
			iterate();
		}

		private void iterate() {
			while (true) {

				// Is there still Budpointobjects in current content?
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
		public Budpointobject next() {
			final Budpointobject toReturn = next;
			iterate();
			return toReturn;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException(
					"Remove operation is not supported for BudpointobjectCollection iterators.");
		}

	}

	private class VisibleBudpointobjectsIterator implements Iterator<Budpointobject> {

		private boolean hasNext = true;

		private final Iterator<Integer> frameIterator;

		private Iterator<Budpointobject> contentIterator;

		private Budpointobject next = null;

		private Set<Budpointobject> currentFrameContent;

		public VisibleBudpointobjectsIterator() {
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
				// Is there still Budpointobjects in current content?
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
		public Budpointobject next() {
			final Budpointobject toReturn = next;
			iterate();
			return toReturn;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException(
					"Remove operation is not supported for BudpointobjectCollection iterators.");
		}

	}

	private class VisibleBudpointobjectsFrameIterator implements Iterator<Budpointobject> {

		private boolean hasNext = true;

		private Budpointobject next = null;

		private final Iterator<Budpointobject> contentIterator;

		public VisibleBudpointobjectsFrameIterator(final Set<Budpointobject> frameContent) {
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
		public Budpointobject next() {
			final Budpointobject toReturn = next;
			iterate();
			return toReturn;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException(
					"Remove operation is not supported for BudpointobjectCollection iterators.");
		}

	}

	/**
	 * Returns a new {@link BudpointobjectCollection}, made of only the
	 * Budpointobjects marked as visible. All the Budpointobjects will then be
	 * marked as not-visible.
	 *
	 * @return a new Budpointobject collection, made of only the Budpointobjects
	 *         marked as visible.
	 */
	public BudpointobjectCollection crop() {
		final BudpointobjectCollection ns = new BudpointobjectCollection();
		ns.setNumThreads(numThreads);

		final Collection<Integer> frames = content.keySet();
		final ExecutorService executors = Executors.newFixedThreadPool(numThreads);
		for (final Integer frame : frames) {

			final Runnable command = new Runnable() {
				@Override
				public void run() {
					final Set<Budpointobject> fc = content.get(frame);
					final Set<Budpointobject> nfc = new HashSet<>(getNBudpointobjects(frame));

					for (final Budpointobject Budpointobject : fc) {

						nfc.add(Budpointobject);
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
				System.err.println("[BudpointobjectCollection.crop()] Timeout of " + TIME_OUT_DELAY + " "
						+ TIME_OUT_UNITS + " reached while cropping.");
			}
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
		return ns;
	}

	/**
	 * A convenience wrapper that implements {@link Iterable} for this
	 * Budpointobject collection.
	 */
	private final class WholeCollectionIterable implements Iterable<Budpointobject> {

		private final boolean visibleBudpointobjectsOnly;

		public WholeCollectionIterable(final boolean visibleBudpointobjectsOnly) {
			this.visibleBudpointobjectsOnly = visibleBudpointobjectsOnly;
		}

		@Override
		public Iterator<Budpointobject> iterator() {
			if (visibleBudpointobjectsOnly)
				return new VisibleBudpointobjectsIterator();

			return new AllBudpointobjectsIterator();
		}
	}

	/**
	 * A convenience wrapper that implements {@link Iterable} for this
	 * Budpointobject collection.
	 */
	private final class FrameVisibleIterable implements Iterable<Budpointobject> {

		private final int frame;

		public FrameVisibleIterable(final int frame) {
			this.frame = frame;
		}

		@Override
		public Iterator<Budpointobject> iterator() {
			return new VisibleBudpointobjectsFrameIterator(content.get(frame));
		}
	}

	private static final Iterator<Budpointobject> EMPTY_ITERATOR = new Iterator<Budpointobject>() {

		@Override
		public boolean hasNext() {
			return false;
		}

		@Override
		public Budpointobject next() {
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
	 * Creates a new {@link BudpointobjectCollection} containing only the specified
	 * Budpointobjects. Their frame origin is retrieved from their
	 * {@link Budpointobject#FRAME} feature, so it must be set properly for all
	 * Budpointobjects. All the Budpointobjects of the new collection have the same
	 * visibility that the one they carry.
	 *
	 * @param Budpointobjects the Budpointobject collection to build from.
	 * @return a new {@link BudpointobjectCollection} instance.
	 */
	public static BudpointobjectCollection fromCollection(final Iterable<Budpointobject> Budpointobjects) {
		final BudpointobjectCollection sc = new BudpointobjectCollection();
		for (final Budpointobject Budpointobject : Budpointobjects) {
			final int frame = Budpointobject.getFeature(Budpointobject.POSITION_T).intValue();
			Set<Budpointobject> fc = sc.content.get(frame);
			if (null == fc) {
				fc = new HashSet<>();
				sc.content.put(frame, fc);
			}
			fc.add(Budpointobject);
		}
		return sc;
	}

	/**
	 * Creates a new {@link BudpointobjectCollection} from a copy of the specified
	 * map of sets. The Budpointobjects added this way are completely untouched. In
	 * particular, their {@link #VISIBLITY} feature is left untouched, which makes
	 * this method suitable to de-serialize a {@link BudpointobjectCollection}.
	 *
	 * @param source the map to buidl the Budpointobject collection from.
	 * @return a new BudpointobjectCollection.
	 */
	public static BudpointobjectCollection fromMap(final Map<Integer, Set<Budpointobject>> source) {
		final BudpointobjectCollection sc = new BudpointobjectCollection();
		sc.content = new ConcurrentSkipListMap<>(source);
		return sc;
	}
}
