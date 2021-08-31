package fiji.plugin.globalbtrackmate;

import java.util.Arrays;

import net.imagej.ImgPlus;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.roi.IterableRegion;
import net.imglib2.roi.Masks;
import net.imglib2.roi.Regions;
import net.imglib2.roi.geom.GeomMasks;
import net.imglib2.roi.geom.real.WritableEllipsoid;
import net.imglib2.roi.geom.real.WritablePolygon2D;
import net.imglib2.type.logic.BoolType;
import net.imglib2.view.Views;

public class ExtendedSpotRoi {

	/**
	 * Polygon points X coordinates, in physical units.
	 */
	public final double[] x;

	/**
	 * Polygon points Y coordinates, in physical units.
	 */
	public final double[] y;

	/**
	 *
	 * Covariance Matrix, in physical units
	 */

	public final double[] cov;

	public ExtendedSpotRoi(final double[] x, final double[] y, final double[] cov) {
		this.x = x;
		this.y = y;
		this.cov = cov;
	}

	public ExtendedSpotRoi copy() {
		return new ExtendedSpotRoi(x.clone(), y.clone(), cov.clone());
	}

	/**
	 * Returns a new <code>int</code> array containing the X pixel coordinates to
	 * which to paint this polygon.
	 * 
	 * @param calibration    the pixel size in X, to convert physical coordinates to
	 *                       pixel coordinates.
	 * @param xcorner        the top-left X corner of the view in the image to
	 *                       paint.
	 * @param magnification  the magnification of the view.
	 * @param magnification2
	 * @return a new <code>int</code> array.
	 */
	public double[] toPolygonX(final double calibration, final double xcorner, final double spotXCenter,
			final double magnification) {
		final double[] xp = new double[x.length];
		for (int i = 0; i < xp.length; i++) {
			final double xc = (spotXCenter + x[i]) / calibration;
			xp[i] = (xc - xcorner) * magnification;
		}
		return xp;
	}

	/**
	 * Returns a new <code>int</code> array containing the Y pixel coordinates to
	 * which to paint this polygon.
	 * 
	 * @param calibration   the pixel size in Y, to convert physical coordinates to
	 *                      pixel coordinates.
	 * @param ycorner       the top-left Y corner of the view in the image to paint.
	 * @param magnification the magnification of the view.
	 * @return a new <code>int</code> array.
	 */
	public double[] toPolygonY(final double calibration, final double ycorner, final double spotYCenter,
			final double magnification) {
		final double[] yp = new double[y.length];
		for (int i = 0; i < yp.length; i++) {
			final double yc = (spotYCenter + y[i]) / calibration;
			yp[i] = (yc - ycorner) * magnification;
		}
		return yp;
	}

	public <T> IterableInterval<T> sample(final ExtendedSpot spot, final ImgPlus<T> img) {
		return sample(spot.getDoublePosition(0), spot.getDoublePosition(1), img, img.averageScale(0),
				img.averageScale(1));
	}

	public <T> IterableInterval<T> sample(final double spotXCenter, final double spotYCenter,
			final RandomAccessibleInterval<T> img, final double xScale, final double yScale) {
		final double[] center = new double[] { spotXCenter, spotYCenter };
		final WritableEllipsoid polygon = GeomMasks.closedEllipsoid(center, cov);
		final IterableRegion<BoolType> region = Masks.toIterableRegion(polygon);
		return Regions.sample(region, Views.extendMirrorDouble(Views.dropSingletonDimensions(img)));
	}

	public double radius() {
		return Math.sqrt(area() / Math.PI);
	}

	public double area() {
		return Math.abs(signedArea(x, y));
	}

	public void scale(final double alpha) {
		for (int i = 0; i < x.length; i++) {
			final double x = this.x[i];
			final double y = this.y[i];
			final double r = Math.sqrt(x * x + y * y);
			final double costheta = x / r;
			final double sintheta = y / r;
			this.x[i] = costheta * r * alpha;
			this.y[i] = sintheta * r * alpha;
		}
	}

	/**
	 * Returns the slope and the intercept of the line passing through the major
	 * axis of the ellipse
	 * 
	 * 
	 * @param mean (x,y) components of mean vector
	 * @param cov  (xx, xy, yy) components of covariance matrix
	 * @return slope and intercept of the line along the major axis
	 */
	public double[] LargestEigenvector(final double[] mean, final double[] cov) {

		// For inifinite slope lines support is provided
		final double xx = cov[0];
		final double yy = cov[1];
		final double zz = cov[2];
		final double xy = cov[3];
		final double xz = cov[4];
		final double yz = cov[5];
		final double d = Math.sqrt(xx * xx + 4 * xy * xy - 2 * xx * yy + yy * yy);
		final double[] eigenvector1 = { 2 * xy, yy - xx + d };
		double[] LargerVec = new double[eigenvector1.length + 1];

		LargerVec = eigenvector1;

		final double slope = LargerVec[1] / (LargerVec[0]);
		final double intercept = mean[1] - mean[0] * slope;

		double[] pair = { slope, intercept };
		return pair;

	}

	/**
	 * Returns the smallest eigenvalue of the ellipse
	 * 
	 * 
	 * @param mean (x,y) components of mean vector
	 * @param cov  (xx, xy, yy) components of covariance matrix
	 * @return slope and intercept of the line along the major axis
	 */
	public double SmallerEigenvalue(final double[] mean, final double[] cov) {

		// For inifinite slope lines support is provided
		final double xx = cov[0];
		final double yy = cov[1];
		final double zz = cov[2];
		final double xy = cov[3];
		final double xz = cov[4];
		final double yz = cov[5];
		final double d = Math.sqrt(xx * xx + 4 * xy * xy - 2 * xx * yy + yy * yy);

		final double smalleigenvalue = (xx + yy - d) / 2;

		return smalleigenvalue;

	}

	public static ExtendedSpot createSpot(final double[] x, final double[] y, final double[] cov,
			final double quality) {
		// Put polygon coordinates with respect to centroid.
		final double[] centroid = centroid(x, y);
		final double xc = centroid[0];
		final double yc = centroid[1];

		final double xx = cov[0];
		final double yy = cov[1];
		final double zz = cov[2];
		final double xy = cov[3];
		final double xz = cov[4];
		final double yz = cov[5];

		final double[] xr = Arrays.stream(x).map(x0 -> x0 - xc).toArray();
		final double[] yr = Arrays.stream(y).map(y0 -> y0 - yc).toArray();

		// Create roi.
		final ExtendedSpotRoi roi = new ExtendedSpotRoi(xr, yr, cov);

		// Create spot.
		final double z = 0.;
		final double r = roi.radius();
		final ExtendedSpot spot = new ExtendedSpot(xc, yc, z, xx, yy, zz, xy, xz, yz, r, quality);
		spot.setRoi(roi);
		return spot;
	}

	/*
	 * UTILS.
	 */

	private static final double[] centroid(final double[] x, final double[] y) {
		final double area = signedArea(x, y);
		double ax = 0.0;
		double ay = 0.0;
		final int n = x.length;
		for (int i = 0; i < n - 1; i++) {
			final double w = x[i] * y[i + 1] - x[i + 1] * y[i];
			ax += (x[i] + x[i + 1]) * w;
			ay += (y[i] + y[i + 1]) * w;
		}

		final double w0 = x[n - 1] * y[0] - x[0] * y[n - 1];
		ax += (x[n - 1] + x[0]) * w0;
		ay += (y[n - 1] + y[0]) * w0;
		return new double[] { ax / 6. / area, ay / 6. / area };
	}

	private static final double signedArea(final double[] x, final double[] y) {
		final int n = x.length;
		double a = 0.0;
		for (int i = 0; i < n - 1; i++)
			a += x[i] * y[i + 1] - x[i + 1] * y[i];

		return (a + x[n - 1] * y[0] - x[0] * y[n - 1]) / 2.0;
	}
}
