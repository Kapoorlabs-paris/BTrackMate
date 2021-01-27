package Buddy.plugin.trackmate;

import java.io.File;

import Buddy.plugin.trackmate.io.TmXmlReader;
import Buddy.plugin.trackmate.tracking.kalman.KalmanTracker;

public class Debug {



	/**
	 * @param args
	 */
	public static void main2(final String[] args) {
		final File file = new File("/Users/tinevez/Desktop/CRTD62.xml");
		final TmXmlReader reader = new TmXmlReader(file);
		if (!reader.isReadingOk()) {
			System.err.println(reader.getErrorMessage());
			return;
		}

		final Model model = reader.getModel();
		final BCellobjectCollection BCellobjects = model.getBCellobjects();

		final double maxSearchRadius = 2;
		final int maxFrameGap = 2;
		final double initialSearchRadius = 1.5;
		final KalmanTracker tracker = new KalmanTracker(BCellobjects, maxSearchRadius, maxFrameGap,
				initialSearchRadius);
		if (!tracker.checkInput() || !tracker.process()) {
			System.err.println(tracker.getErrorMessage());
			return;
		}

		System.out.println(tracker.getResult());// DEBUG
	}
}
