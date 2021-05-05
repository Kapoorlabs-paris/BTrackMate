package scrollbar;

import javax.swing.JProgressBar;

public class Utility {

	

	public static float computeValueFromScrollbarPosition(final int scrollbarPosition, final float min,
			final float max, final int scrollbarSize) {
		return min + (scrollbarPosition / (float) scrollbarSize) * (max - min);
	}

	public static float computeIntValueFromScrollbarPosition(final int scrollbarPosition, final float min,
			final float max, final int scrollbarSize) {
		return min + (scrollbarPosition / (max)) * (max - min);
	}

	public static int computeScrollbarPositionFromValue(final float sigma, final float min, final float max,
			final int scrollbarSize) {
		return Math.round(((sigma - min) / (max - min)) * scrollbarSize);
	}

	public static int computeIntScrollbarPositionFromValue(final float thirdDimensionslider, final float min,
			final float max, final int scrollbarSize) {
		return Math.round(((thirdDimensionslider - min) / (max - min)) * max);
	}
	
	public static float computeSigma2(final float sigma1, final int sensitivity) {
		final float k = (float) computeK(sensitivity);
		final float[] sigma = computeSigma(k, sigma1);

		return sigma[1];
	}
	public static double computeK( final float stepsPerOctave ) { return Math.pow( 2f, 1f / stepsPerOctave ); }
	public static double computeK( final int stepsPerOctave ) { return Math.pow( 2f, 1f / stepsPerOctave ); }
	public static float computeKWeight( final float k ) { return 1.0f / (k - 1.0f); }
	public static float[] computeSigma( final float k, final float initialSigma )
	{
		final float[] sigma = new float[ 2 ];

		sigma[ 0 ] = initialSigma;
		sigma[ 1 ] = sigma[ 0 ] * k;

		return sigma;
	}
	
	public static void SetProgressBar(JProgressBar jpb, double percent) {

		jpb.setValue((int) Math.round(percent));
		jpb.setOpaque(true);
		jpb.setStringPainted(true);
		jpb.setString("Finding MT ends");

	}
	
	
	public static void SetProgressBar(JProgressBar jpb, double percent, String message) {

		jpb.setValue((int) Math.round(percent));
		jpb.setOpaque(true);
		jpb.setStringPainted(true);
		jpb.setString(message);

	}

	public static void SetProgressBar(JProgressBar jpb) {
		jpb.setValue(0);
		jpb.setIndeterminate(true);
		jpb.setOpaque(true);
		jpb.setStringPainted(true);
		jpb.setString("Pre-processing Image");

	}

	public static void SetProgressBarTime(JProgressBar jpb, double percent, int framenumber, int thirdDimsize) {

		jpb.setValue((int) percent);
		jpb.setOpaque(true);
		jpb.setStringPainted(true);
		jpb.setString("Time point = " + framenumber + "/" + thirdDimsize);

	}

	public static void SetProgressBarTime(JProgressBar jpb, double percent, int framenumber, int thirdDimsize,
			String message) {

		jpb.setValue((int) percent);
		jpb.setOpaque(true);
		jpb.setStringPainted(true);
		jpb.setString(message + "= " + framenumber + "/" + thirdDimsize);

	}

}
