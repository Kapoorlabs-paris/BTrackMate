package tracker.trackanalyzer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;

import javax.swing.ImageIcon;

import fiji.plugin.btrackmate.Dimension;
import budDetector.Budpointobject;
import net.imglib2.multithreading.SimpleMultiThreading;
import tracker.BUDDYDimension;
import tracker.BUDDYFeatureModel;
import tracker.BUDDYModel;
import tracker.BUDDYTrackModel;

public class BudTrackLocationAnalyzer implements BudTrackAnalyzer {

	/*
	 * FEATURE NAMES
	 */
	public static final String KEY = "Track location";

	public static final String X_LOCATION = "TRACK_X_LOCATION";

	public static final String Y_LOCATION = "TRACK_Y_LOCATION";

	public static int featurenumber = 2;
	public static final List<String> FEATURES = new ArrayList<>(featurenumber);

	public static final Map<String, String> FEATURE_NAMES = new HashMap<>(featurenumber);

	public static final Map<String, String> FEATURE_SHORT_NAMES = new HashMap<>(featurenumber);

	public static final Map<String, BUDDYDimension> FEATURE_DIMENSIONS = new HashMap<>(featurenumber);

	public static final Map<String, Boolean> IS_INT = new HashMap<>(featurenumber);

	static {
		FEATURES.add(X_LOCATION);
		FEATURES.add(Y_LOCATION);

		FEATURE_NAMES.put(X_LOCATION, "X Location (mean)");
		FEATURE_NAMES.put(Y_LOCATION, "Y Location (mean)");

		FEATURE_SHORT_NAMES.put(X_LOCATION, "X");
		FEATURE_SHORT_NAMES.put(Y_LOCATION, "Y");

		FEATURE_DIMENSIONS.put(X_LOCATION, BUDDYDimension.POSITION);
		FEATURE_DIMENSIONS.put(Y_LOCATION, BUDDYDimension.POSITION);

		IS_INT.put(X_LOCATION, Boolean.FALSE);
		IS_INT.put(Y_LOCATION, Boolean.FALSE);
	}

	private int numThreads;

	private long processingTime;

	/*
	 * CONSTRUCTOR
	 */

	public BudTrackLocationAnalyzer() {
		setNumThreads();
	}

	@Override
	public boolean isLocal() {
		return true;
	}

	@Override
	public void process(final Collection<Integer> trackIDs, final BUDDYModel model) {

		if (trackIDs.isEmpty()) {
			return;
		}

		final ArrayBlockingQueue<Integer> queue = new ArrayBlockingQueue<>(trackIDs.size(), false, trackIDs);
		final BUDDYFeatureModel fm = model.getFeatureModel();

		final Thread[] threads = SimpleMultiThreading.newThreads(numThreads);
		for (int i = 0; i < threads.length; i++) {
			threads[i] = new Thread("TrackLocationAnalyzer thread " + i) {
				@Override
				public void run() {
					Integer trackID;
					while ((trackID = queue.poll()) != null) {

						final Set<Budpointobject> track = model.getTrackModel().trackBudpointobjects(trackID);

						double x = 0;
						double y = 0;

						for (final Budpointobject spot : track) {
							x += spot.getFeature(Budpointobject.POSITION_X);
							y += spot.getFeature(Budpointobject.POSITION_Y);
						}
						final int nspots = track.size();
						x /= nspots;
						y /= nspots;

						fm.putTrackFeature(trackID, X_LOCATION, x);
						fm.putTrackFeature(trackID, Y_LOCATION, y);

					}

				}
			};
		}

		final long start = System.currentTimeMillis();
		SimpleMultiThreading.startAndJoin(threads);
		final long end = System.currentTimeMillis();
		processingTime = end - start;
	}

	@Override
	public int getNumThreads() {
		return numThreads;
	}

	@Override
	public void setNumThreads() {
		this.numThreads = Runtime.getRuntime().availableProcessors();
	}

	@Override
	public void setNumThreads(final int numThreads) {
		this.numThreads = numThreads;

	}

	@Override
	public long getProcessingTime() {
		return processingTime;
	}

	@Override
	public String getKey() {
		return KEY;
	}

	@Override
	public List<String> getFeatures() {
		return FEATURES;
	}

	@Override
	public Map<String, String> getFeatureShortNames() {
		return FEATURE_SHORT_NAMES;
	}

	@Override
	public Map<String, String> getFeatureNames() {
		return FEATURE_NAMES;
	}

	@Override
	public Map<String, BUDDYDimension> getFeatureDimensions() {
		return FEATURE_DIMENSIONS;
	}

	@Override
	public String getInfoText() {
		return null;
	}

	@Override
	public ImageIcon getIcon() {
		return null;
	}

	@Override
	public String getName() {
		return KEY;
	}

	@Override
	public Map<String, Boolean> getIsIntFeature() {
		return IS_INT;
	}

	@Override
	public boolean isManualFeature() {
		return false;
	}

}
