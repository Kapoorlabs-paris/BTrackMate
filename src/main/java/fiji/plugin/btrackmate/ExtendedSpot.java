package fiji.plugin.btrackmate;

import static fiji.plugin.btrackmate.SpotCollection.VISIBILITY;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import fiji.plugin.btrackmate.util.AlphanumComparator;
import net.imglib2.AbstractEuclideanSpace;
import net.imglib2.RealLocalizable;
import net.imglib2.util.Util;

/**
 * A {@link RealLocalizable} implementation, used in bTrackMate to represent a
 * filament detection.
 * <p>
 * On top of being a {@link RealLocalizable}, it can store additional numerical
 * named features, with a {@link Map}-like syntax. Constructors enforce the
 * specification of the spot location in 3D space (if Z is unused, put 0), the
 * spot radius, and the spot quality. This somewhat cumbersome syntax is made to
 * avoid any bad surprise with missing features in a subsequent use. The spot
 * temporal features ({@link #FRAME} and {@link #POSITION_T}) are set upon
 * adding to a {@link SpotCollection}.
 * <p>
 * Each spot received at creation a unique ID (as an <code>int</code>), used
 * later for saving, retrieving and loading. Interfering with this value will
 * predictively cause undesired behavior.
 *
 * @author Varun Kapoor 2021
 *
 */
public class ExtendedSpot extends AbstractEuclideanSpace implements RealLocalizable, Comparable<ExtendedSpot> {

	/*
	 * FIELDS
	 */

	public static AtomicInteger IDcounter = new AtomicInteger(-1);

	/** Store the individual features, and their values. */
	private final ConcurrentHashMap<String, Double> features = new ConcurrentHashMap<>();

	/** A user-supplied name for this spot. */
	private String name;

	/** This spot ID. */
	private final int ID;

	/**
	 * The polygon that represents the 2D roi around the spot. Can be
	 * <code>null</code> if the detector that created this spot does not support
	 * ROIs or for 3D images.
	 */
	private ExtendedSpotRoi roi;

	/*
	 * CONSTRUCTORS
	 */

	/**
	 * Creates a new spot.
	 *
	 * @param x       the spot X coordinates, in image units.
	 * @param y       the spot Y coordinates, in image units.
	 * @param z       the spot Z coordinates, in image units.
	 * @param xx      the spot covariance XX coordinates, in image units.
	 * @param yy      the spot covariance YY coordinates, in image units.
	 * @param zz      the spot covariance ZZ coordinates, in image units.
	 * @param xy      the spot covariance XY coordinates, in image units.
	 * @param xz      the spot covariance XZ coordinates, in image units.
	 * @param yz      the spot covariance YZ coordinates, in image units.
	 * 
	 * @param radius  the spot radius, in image units.
	 * @param quality the spot quality.
	 * @param name    the spot name.
	 */
	public ExtendedSpot(final double x, final double y, final double z, final double xx, final double yy,
			final double zz, final double xy, final double xz, final double yz, final double radius,
			final double quality, final String name) {
		super(3);
		this.ID = IDcounter.incrementAndGet();
		putFeature(POSITION_X, Double.valueOf(x));
		putFeature(POSITION_Y, Double.valueOf(y));
		putFeature(POSITION_Z, Double.valueOf(z));
		putFeature(COV_XX, Double.valueOf(xx));
		putFeature(COV_YY, Double.valueOf(yy));
		putFeature(COV_ZZ, Double.valueOf(zz));
		putFeature(COV_XY, Double.valueOf(xy));
		putFeature(COV_XZ, Double.valueOf(xz));
		putFeature(COV_YZ, Double.valueOf(yz));

		putFeature(RADIUS, Double.valueOf(radius));
		putFeature(QUALITY, Double.valueOf(quality));
		if (null == name) {
			this.name = "ID" + ID;
		} else {
			this.name = name;
		}
	}

	/**
	 * Creates a new spot, and gives it a default name.
	 *
	 * @param x       the spot X coordinates, in image units.
	 * @param y       the spot Y coordinates, in image units.
	 * @param z       the spot Z coordinates, in image units.
	 * @param xx      the spot covariance XX coordinates, in image units.
	 * @param yy      the spot covariance YY coordinates, in image units.
	 * @param zz      the spot covariance ZZ coordinates, in image units.
	 * @param xy      the spot covariance XY coordinates, in image units.
	 * @param xz      the spot covariance XZ coordinates, in image units.
	 * @param yz      the spot covariance YZ coordinates, in image units.
	 * @param radius  the spot radius, in image units.
	 * @param quality the spot quality.
	 */
	public ExtendedSpot(final double x, final double y, final double z, final double xx, final double yy,
			final double zz, final double xy, final double xz, final double yz, final double radius,
			final double quality) {
		this(x, y, z, xx, yy, zz, xy, xz, yz, radius, quality, null);
	}

	/**
	 * Creates a new spot, taking its 3D coordinates from a {@link RealLocalizable}.
	 * The {@link RealLocalizable} must have at least 3 dimensions, and must return
	 * coordinates in image units.
	 *
	 * @param location   the {@link RealLocalizable} that contains the spot
	 *                   location.
	 * @param covariance the {@link RealLocalizable} that contains the spot
	 *                   covariance.
	 * @param radius     the spot radius, in image units.
	 * @param quality    the spot quality.
	 * @param name       the spot name.
	 */
	public ExtendedSpot(final RealLocalizable location, final RealLocalizable covariance, final double radius,
			final double quality, final String name) {
		this(location.getDoublePosition(0), location.getDoublePosition(1), location.getDoublePosition(2),
				covariance.getDoublePosition(0), covariance.getDoublePosition(1), covariance.getDoublePosition(2),
				covariance.getDoublePosition(3), covariance.getDoublePosition(4), covariance.getDoublePosition(5),
				radius, quality, name);
	}

	/**
	 * Creates a new spot, taking its 3D coordinates from a {@link RealLocalizable}.
	 * The {@link RealLocalizable} must have at least 3 dimensions, and must return
	 * coordinates in image units. The spot will get a default name.
	 *
	 * @param location   the {@link RealLocalizable} that contains the spot
	 *                   location.
	 * @param covariance the {@link RealLocalizable} that contains the spot
	 *                   covariance.
	 * @param radius     the spot radius, in image units.
	 * @param quality    the spot quality.
	 */
	public ExtendedSpot(final RealLocalizable location, final RealLocalizable covariance, final double radius,
			final double quality) {
		this(location, covariance, radius, quality, null);
	}

	/**
	 * Creates a new spot, taking its location, its radius, its quality value and
	 * its name from the specified spot.
	 *
	 * @param spot the spot to read from.
	 */
	public ExtendedSpot(final ExtendedSpot spot) {
		this(spot.getFeature(POSITION_X), spot.getFeature(POSITION_Y), spot.getFeature(POSITION_Z),
				spot.getFeature(COV_XX), spot.getFeature(COV_YY), spot.getFeature(COV_ZZ), spot.getFeature(COV_XY),
				spot.getFeature(COV_XZ), spot.getFeature(COV_YZ), spot.getFeature(RADIUS), spot.getFeature(QUALITY),
				spot.getName());
	}

	/**
	 * Blank constructor meant to be used when loading a spot collection from a
	 * file. <b>Will</b> mess with the {@link #IDcounter} field, so this constructor
	 * <u>should not be used for normal spot creation</u>.
	 *
	 * @param ID the spot ID to set
	 */
	public ExtendedSpot(final int ID) {
		super(3);
		this.ID = ID;
		synchronized (IDcounter) {
			if (IDcounter.get() < ID) {
				IDcounter.set(ID);
			}
		}
	}

	/*
	 * PUBLIC METHODS
	 */

	@Override
	public int hashCode() {
		return ID;
	}

	@Override
	public int compareTo(final ExtendedSpot o) {
		return ID - o.ID;
	}

	@Override
	public boolean equals(final Object other) {
		if (other == null)
			return false;
		if (other == this)
			return true;
		if (!(other instanceof ExtendedSpot))
			return false;
		final ExtendedSpot os = (ExtendedSpot) other;
		return os.ID == this.ID;
	}

	public void setRoi(final ExtendedSpotRoi roi) {
		this.roi = roi;
	}

	public ExtendedSpotRoi getRoi() {
		return roi;
	}

	/**
	 * @return the name for this Spot.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Set the name of this Spot.
	 * 
	 * @param name the name to use.
	 */
	public void setName(final String name) {
		this.name = name;
	}

	public int ID() {
		return ID;
	}

	@Override
	public String toString() {
		String str;
		if (null == name || name.equals(""))
			str = "ID" + ID;
		else
			str = name;
		return str;
	}

	/**
	 * Return a string representation of this spot, with calculated features.
	 * 
	 * @return a string representation of the spot.
	 */
	public String echo() {
		final StringBuilder s = new StringBuilder();

		// Name
		if (null == name)
			s.append("Spot: <no name>\n");
		else
			s.append("Spot: " + name + "\n");

		// Frame
		s.append("Time: " + getFeature(POSITION_T) + '\n');

		// Coordinates
		final double[] coordinates = new double[3];
		localize(coordinates);
		s.append("Position: " + Util.printCoordinates(coordinates) + "\n");

		// Covariance
		final double[] covariance = new double[6];
		localizeCov(covariance);
		s.append("Covariance: " + Util.printCoordinates(covariance) + "\n");

		// Feature list
		if (null == features || features.size() < 1)
			s.append("No features calculated\n");
		else {
			s.append("Feature list:\n");
			double val;
			for (final String key : features.keySet()) {
				s.append("\t" + key.toString() + ": ");
				val = features.get(key);
				if (val >= 1e4)
					s.append(String.format("%.1g", val));
				else
					s.append(String.format("%.1f", val));
				s.append('\n');
			}
		}
		return s.toString();
	}

	/*
	 * FEATURE RELATED METHODS
	 */

	/**
	 * Exposes the storage map of features for this spot. Altering the returned map
	 * will alter the spot.
	 *
	 * @return a map of {@link String}s to {@link Double}s.
	 */
	public Map<String, Double> getFeatures() {
		return features;
	}

	/**
	 * Returns the value corresponding to the specified spot feature.
	 *
	 * @param feature The feature string to retrieve the stored value for.
	 * @return the feature value, as a {@link Double}. Will be <code>null</code> if
	 *         it has not been set.
	 */
	public Double getFeature(final String feature) {
		return features.get(feature);
	}

	/**
	 * Stores the specified feature value for this spot.
	 *
	 * @param feature the name of the feature to store, as a {@link String}.
	 * @param value   the value to store, as a {@link Double}. Using
	 *                <code>null</code> will have unpredicted outcomes.
	 */
	public void putFeature(final String feature, final Double value) {
		features.put(feature, value);
	}

	/**
	 * Returns the difference of the feature value for this spot with the one of the
	 * specified spot. By construction, this operation is anti-symmetric (
	 * <code>A.diffTo(B) = - B.diffTo(A)</code>).
	 * <p>
	 * Will generate a {@link NullPointerException} if one of the spots does not
	 * store the named feature.
	 *
	 * @param s       the spot to compare to.
	 * @param feature the name of the feature to use for calculation.
	 * @return the difference in feature value.
	 */
	public double diffTo(final ExtendedSpot s, final String feature) {
		final double f1 = features.get(feature).doubleValue();
		final double f2 = s.getFeature(feature).doubleValue();
		return f1 - f2;
	}

	/**
	 * Returns the absolute normalized difference of the feature value of this spot
	 * with the one of the given spot.
	 * <p>
	 * If <code>a</code> and <code>b</code> are the feature values, then the
	 * absolute normalized difference is defined as
	 * <code>Math.abs( a - b) / ( (a+b)/2 )</code>.
	 * <p>
	 * By construction, this operation is symmetric ( <code>A.normalizeDiffTo(B) =
	 * B.normalizeDiffTo(A)</code>).
	 * <p>
	 * Will generate a {@link NullPointerException} if one of the spots does not
	 * store the named feature.
	 *
	 * @param s       the spot to compare to.
	 * @param feature the name of the feature to use for calculation.
	 * @return the absolute normalized difference feature value.
	 */
	public double normalizeDiffTo(final ExtendedSpot s, final String feature) {
		final double a = features.get(feature).doubleValue();
		final double b = s.getFeature(feature).doubleValue();
		if (a == -b)
			return 0d;

		return Math.abs(a - b) / ((a + b) / 2);
	}

	/**
	 * Returns the square distance from this spot to the specified spot.
	 *
	 * @param s the spot to compute the square distance to.
	 * @return the square distance as a <code>double</code>.
	 */
	public double squareDistanceTo(final RealLocalizable s) {
		double sumSquared = 0d;
		for (int d = 0; d < 3; d++) {
			final double dx = this.getDoublePosition(d) - s.getDoublePosition(d);
			sumSquared += dx * dx;
		}
		return sumSquared;
	}

	/*
	 * PUBLIC UTILITY CONSTANTS
	 */

	/*
	 * STATIC KEYS
	 */

	/** The name of the spot quality feature. */
	public static final String QUALITY = "QUALITY";

	/** The name of the radius spot feature. */
	public static final String RADIUS = "RADIUS";

	/** The name of the spot X position feature. */
	public static final String POSITION_X = "POSITION_X";

	/** The name of the spot Y position feature. */
	public static final String POSITION_Y = "POSITION_Y";

	/** The name of the spot Z position feature. */
	public static final String POSITION_Z = "POSITION_Z";

	/** The name of the spot X position feature. */
	public static final String COV_XX = "COV_XX";

	/** The name of the spot Y position feature. */
	public static final String COV_YY = "COV_YY";

	/** The name of the spot Z position feature. */
	public static final String COV_ZZ = "COV_ZZ ";

	/** The name of the spot X position feature. */
	public static final String COV_XY = "COV_XY";

	/** The name of the spot Y position feature. */
	public static final String COV_XZ = "COV_XZ";

	/** The name of the spot Z position feature. */
	public static final String COV_YZ = "COV_YZ";

	/** The name of the spot T position feature. */
	public static final String POSITION_T = "POSITION_T";

	/** The name of the frame feature. */
	public static final String FRAME = "FRAME";

	/** The position features. */
	public final static String[] POSITION_FEATURES = new String[] { POSITION_X, POSITION_Y, POSITION_Z };
	/** The covariance features. */
	public final static String[] COVARIANCE_FEATURES = new String[] { COV_XX, COV_YY, COV_ZZ, COV_XY, COV_XZ, COV_YZ };
	/**
	 * The 7 privileged spot features that must be set by a spot detector:
	 * {@link #QUALITY}, {@link #POSITION_X}, {@link #POSITION_Y},
	 * {@link #POSITION_Z}, {@link #POSITION_Z}, {@link #RADIUS}, {@link #FRAME} .
	 */
	public final static Collection<String> FEATURES = new ArrayList<>(13);

	/** The 7 privileged spot feature names. */
	public final static Map<String, String> FEATURE_NAMES = new HashMap<>(13);

	/** The 7 privileged spot feature short names. */
	public final static Map<String, String> FEATURE_SHORT_NAMES = new HashMap<>(13);

	/** The 7 privileged spot feature dimensions. */
	public final static Map<String, Dimension> FEATURE_DIMENSIONS = new HashMap<>(13);

	/** The 7 privileged spot feature isInt flags. */
	public final static Map<String, Boolean> IS_INT = new HashMap<>(13);

	static {
		FEATURES.add(QUALITY);
		FEATURES.add(POSITION_X);
		FEATURES.add(POSITION_Y);
		FEATURES.add(POSITION_Z);
		FEATURES.add(COV_XX);
		FEATURES.add(COV_YY);
		FEATURES.add(COV_ZZ);
		FEATURES.add(COV_XY);
		FEATURES.add(COV_XZ);
		FEATURES.add(COV_YZ);

		FEATURES.add(POSITION_T);
		FEATURES.add(FRAME);
		FEATURES.add(RADIUS);
		FEATURES.add(SpotCollection.VISIBILITY);

		FEATURE_NAMES.put(POSITION_X, "X");
		FEATURE_NAMES.put(POSITION_Y, "Y");
		FEATURE_NAMES.put(POSITION_Z, "Z");
		FEATURE_NAMES.put(COV_XX, "XX");
		FEATURE_NAMES.put(COV_YY, "YY");
		FEATURE_NAMES.put(COV_ZZ, "ZZ");
		FEATURE_NAMES.put(COV_XY, "XY");
		FEATURE_NAMES.put(COV_XZ, "XZ");
		FEATURE_NAMES.put(COV_YZ, "YZ");

		FEATURE_NAMES.put(POSITION_T, "T");
		FEATURE_NAMES.put(FRAME, "Frame");
		FEATURE_NAMES.put(RADIUS, "Radius");
		FEATURE_NAMES.put(QUALITY, "Quality");
		FEATURE_NAMES.put(VISIBILITY, "Visibility");

		FEATURE_SHORT_NAMES.put(POSITION_X, "X");
		FEATURE_SHORT_NAMES.put(POSITION_Y, "Y");
		FEATURE_SHORT_NAMES.put(POSITION_Z, "Z");
		FEATURE_SHORT_NAMES.put(COV_XX, "XX");
		FEATURE_SHORT_NAMES.put(COV_YY, "YY");
		FEATURE_SHORT_NAMES.put(COV_ZZ, "ZZ");
		FEATURE_SHORT_NAMES.put(COV_XY, "XY");
		FEATURE_SHORT_NAMES.put(COV_XZ, "XZ");
		FEATURE_SHORT_NAMES.put(COV_YZ, "YZ");

		FEATURE_SHORT_NAMES.put(POSITION_T, "T");
		FEATURE_SHORT_NAMES.put(FRAME, "Frame");
		FEATURE_SHORT_NAMES.put(RADIUS, "R");
		FEATURE_SHORT_NAMES.put(QUALITY, "Quality");
		FEATURE_SHORT_NAMES.put(VISIBILITY, "Visibility");

		FEATURE_DIMENSIONS.put(POSITION_X, Dimension.POSITION);
		FEATURE_DIMENSIONS.put(POSITION_Y, Dimension.POSITION);
		FEATURE_DIMENSIONS.put(POSITION_Z, Dimension.POSITION);
		FEATURE_DIMENSIONS.put(COV_XX, Dimension.POSITION);
		FEATURE_DIMENSIONS.put(COV_YY, Dimension.POSITION);
		FEATURE_DIMENSIONS.put(COV_ZZ, Dimension.POSITION);
		FEATURE_DIMENSIONS.put(COV_XY, Dimension.POSITION);
		FEATURE_DIMENSIONS.put(COV_XZ, Dimension.POSITION);
		FEATURE_DIMENSIONS.put(COV_YZ, Dimension.POSITION);

		FEATURE_DIMENSIONS.put(POSITION_T, Dimension.TIME);
		FEATURE_DIMENSIONS.put(FRAME, Dimension.NONE);
		FEATURE_DIMENSIONS.put(RADIUS, Dimension.LENGTH);
		FEATURE_DIMENSIONS.put(QUALITY, Dimension.QUALITY);
		FEATURE_DIMENSIONS.put(VISIBILITY, Dimension.NONE);

		IS_INT.put(POSITION_X, Boolean.FALSE);
		IS_INT.put(POSITION_Y, Boolean.FALSE);
		IS_INT.put(POSITION_Z, Boolean.FALSE);
		IS_INT.put(COV_XX, Boolean.FALSE);
		IS_INT.put(COV_YY, Boolean.FALSE);
		IS_INT.put(COV_ZZ, Boolean.FALSE);
		IS_INT.put(COV_XY, Boolean.FALSE);
		IS_INT.put(COV_XZ, Boolean.FALSE);
		IS_INT.put(COV_YZ, Boolean.FALSE);
		IS_INT.put(POSITION_T, Boolean.FALSE);
		IS_INT.put(FRAME, Boolean.TRUE);
		IS_INT.put(RADIUS, Boolean.FALSE);
		IS_INT.put(QUALITY, Boolean.FALSE);
		IS_INT.put(VISIBILITY, Boolean.TRUE);
	}

	@Override
	public void localize(final float[] position) {
		assert (position.length >= n);
		for (int d = 0; d < n; ++d)
			position[d] = getFloatPosition(d);

	}

	public void localizeCov(final float[] covariance) {
		assert (covariance.length >= 2 * n);

		for (int d = 0; d < 2 * n; ++d)
			covariance[d] = getFloatCovariance(d);
	}

	public void localizeCov(final double[] covariance) {
		assert (covariance.length >= 2 * n);

		for (int d = 0; d < 2 * n; ++d)
			covariance[d] = getDoubleCovariance(d);
	}

	@Override
	public void localize(final double[] position) {
		assert (position.length >= n);
		for (int d = 0; d < n; ++d)
			position[d] = getDoublePosition(d);

	}

	@Override
	public float getFloatPosition(final int d) {
		return (float) getDoublePosition(d);
	}

	@Override
	public double getDoublePosition(final int d) {
		return getFeature(POSITION_FEATURES[d]);
	}

	public double getDoubleCovariance(final int d) {
		return getFeature(COVARIANCE_FEATURES[d]);
	}

	public float getFloatCovariance(final int d) {
		return (float) getDoubleCovariance(d);
	}

	/*
	 * STATIC UTILITY
	 */

	/**
	 * A comparator used to sort spots by ascending feature values.
	 *
	 * @param feature the feature to use for comparison. It is the caller
	 *                responsibility to ensure that all spots have the target
	 *                feature.
	 * @return a new {@link Comparator}.
	 */
	public final static Comparator<ExtendedSpot> featureComparator(final String feature) {
		final Comparator<ExtendedSpot> comparator = new Comparator<ExtendedSpot>() {
			@Override
			public int compare(final ExtendedSpot o1, final ExtendedSpot o2) {
				final double diff = o2.diffTo(o1, feature);
				if (diff == 0)
					return 0;
				else if (diff < 0)
					return 1;
				else
					return -1;
			}
		};
		return comparator;
	}

	/** A comparator used to sort spots by ascending time feature. */
	public final static Comparator<ExtendedSpot> timeComparator = featureComparator(POSITION_T);

	/** A comparator used to sort spots by ascending frame. */
	public final static Comparator<ExtendedSpot> frameComparator = featureComparator(FRAME);

	/**
	 * A comparator used to sort spots by name. The comparison uses numerical
	 * natural sorting, So that "Spot_4" comes before "Spot_122".
	 */
	public final static Comparator<ExtendedSpot> nameComparator = new Comparator<ExtendedSpot>() {
		private final AlphanumComparator comparator = AlphanumComparator.instance;

		@Override
		public int compare(final ExtendedSpot o1, final ExtendedSpot o2) {
			return comparator.compare(o1.getName(), o2.getName());
		}
	};
}
