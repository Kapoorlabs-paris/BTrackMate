package tracker.trackanalyzer;

import java.util.Collection;

import fiji.plugin.btrackmate.features.FeatureAnalyzer;
import net.imglib2.algorithm.Benchmark;
import net.imglib2.algorithm.MultiThreaded;
import tracker.BUDDYModel;
import tracker.BUDDYTrackModel;

public interface BudTrackAnalyzer extends Benchmark, BudFeatureAnalyzer, MultiThreaded
{

	/**
	 * Compute the features of the track whose ID is given.
	 *
	 * @param trackIDs
	 *            the IDs of the track whose features are to be calculated.
	 * @param model
	 *            the {@link Model} from which actual tracks are to be
	 *            retrieved.
	 */
	public void process( final Collection< Integer > trackIDs, final BUDDYModel model );

	/**
	 * Returns <code>true</code> if this analyzer is a local analyzer. That is:
	 * a modification that affects only one track requires the track features to
	 * be re-calculated only for this track. If <code>false</code>, any model
	 * modification involving edges will trigger a recalculation over all the
	 * visible tracks of the model.
	 * <p>
	 * Example of a local track feature: the number of spots in a track. It does
	 * not depend on the number of spots in other tracks.
	 * <p>
	 * Example of a non-local track feature: the rank of the track sorted by its
	 * number of spots, compared to other tracks.
	 */
	public boolean isLocal();

}