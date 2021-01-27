package Buddy.plugin.trackmate.features.BCellobject;

import net.imglib2.algorithm.Algorithm;
import net.imglib2.algorithm.Benchmark;

/**
 * Interface for a class that can compute feature on a collection of spots.
 * <p>
 * The spot collection to operate on is given at construction by the
 * {@link SpotAnalyzerFactory} that instantiated and configured this instance.
 * Calling the {@link #process()} method result in updating the feature map of
 * each spot directly, calling
 * {@link Buddy.plugin.trackmate.Spot#putFeature(String, Double)}.
 */
public interface BCellobjectAnalyzer< T > extends Algorithm, Benchmark
{}
