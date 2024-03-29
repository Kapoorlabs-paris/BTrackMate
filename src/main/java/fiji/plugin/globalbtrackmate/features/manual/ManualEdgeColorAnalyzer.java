package fiji.plugin.globalbtrackmate.features.manual;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.scijava.plugin.Plugin;

import fiji.plugin.globalbtrackmate.Dimension;
import fiji.plugin.globalbtrackmate.Model;
import fiji.plugin.globalbtrackmate.features.edges.EdgeAnalyzer;

@Plugin(type = EdgeAnalyzer.class)
public class ManualEdgeColorAnalyzer implements EdgeAnalyzer {

	public static final String FEATURE = "MANUAL_EGE_COLOR";

	public static final String KEY = "Manual edge color";

	static final List<String> FEATURES = new ArrayList<>(1);

	static final Map<String, String> FEATURE_SHORT_NAMES = new HashMap<>(1);

	static final Map<String, String> FEATURE_NAMES = new HashMap<>(1);

	static final Map<String, Dimension> FEATURE_DIMENSIONS = new HashMap<>(1);

	static final Map<String, Boolean> IS_INT = new HashMap<>(1);

	static final String INFO_TEXT = "<html>A dummy analyzer for the feature that stores the color manually assigned to each edge.</html>";

	static final String NAME = KEY;

	static {
		FEATURES.add(FEATURE);
		FEATURE_SHORT_NAMES.put(FEATURE, "Edge color");
		FEATURE_NAMES.put(FEATURE, "Manual edge color");
		FEATURE_DIMENSIONS.put(FEATURE, Dimension.NONE);
		IS_INT.put(FEATURE, Boolean.TRUE);
	}

	private long processingTime;

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
	public Map<String, Dimension> getFeatureDimensions() {
		return FEATURE_DIMENSIONS;
	}

	@Override
	public Map<String, Boolean> getIsIntFeature() {
		return IS_INT;
	}

	@Override
	public void setNumThreads() {
	}

	@Override
	public void setNumThreads(final int numThreads) {
	}

	@Override
	public int getNumThreads() {
		return 1;
	}

	@Override
	public String getInfoText() {
		return INFO_TEXT;
	}

	@Override
	public ImageIcon getIcon() {
		return null;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public void process(final Collection<DefaultWeightedEdge> edges, final Model model) {
	}

	@Override
	public boolean isLocal() {
		return true;
	}

	@Override
	public boolean isManualFeature() {
		return true;
	}
}
