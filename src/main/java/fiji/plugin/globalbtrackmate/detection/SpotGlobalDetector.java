package fiji.plugin.globalbtrackmate.detection;

import fiji.plugin.globalbtrackmate.SpotCollection;
import net.imglib2.algorithm.Benchmark;
import net.imglib2.algorithm.OutputAlgorithm;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public interface SpotGlobalDetector<T extends RealType<T> & NativeType<T>>
		extends OutputAlgorithm<SpotCollection>, Benchmark {
}
