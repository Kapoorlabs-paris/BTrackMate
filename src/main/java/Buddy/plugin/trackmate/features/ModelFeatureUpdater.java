package Buddy.plugin.trackmate.features;

import java.util.ArrayList;

import org.jgrapht.graph.DefaultWeightedEdge;

import Buddy.plugin.trackmate.Model;
import Buddy.plugin.trackmate.ModelChangeEvent;
import Buddy.plugin.trackmate.ModelChangeListener;
import Buddy.plugin.trackmate.Settings;
import budDetector.BCellobject;
import Buddy.plugin.trackmate.BCellobjectCollection;
import net.imglib2.algorithm.MultiThreaded;

/**
 * A utility class that listens to the change occurring in a model, and updates
 * its BCellobject, edge and track features accordingly. Useful to keep the model in 
 * sync with manual editing.
 *    
 * @author Jean-Yves Tinevez - 2013
 */
public class ModelFeatureUpdater implements ModelChangeListener, MultiThreaded
{

	private final BCellobjectFeatureCalculator BCellobjectFeatureCalculator;
	private final EdgeFeatureCalculator edgeFeatureCalculator;
	private final TrackFeatureCalculator trackFeatureCalculator;
	private final Model model;

	private int numThreads;

	/**
	 * Constructs and activate a {@link ModelFeatureUpdater}. The new instance is 
	 * registered to listen to model changes, and update its feature.
	 * @param model  the model to listen to. 
	 * @param settings the {@link Settings} the model is built against. Required 
	 * to access the raw data.
	 */
	public ModelFeatureUpdater(Model model, Settings settings) {
		this.model = model;
		this.BCellobjectFeatureCalculator = new BCellobjectFeatureCalculator(model, settings);
		this.edgeFeatureCalculator = new EdgeFeatureCalculator(model, settings);
		this.trackFeatureCalculator = new TrackFeatureCalculator(model, settings);
		model.addModelChangeListener(this);
		setNumThreads();
	}

	/**
	 * Updates the model features against the change notified here.
	 * If the event is not a {@link ModelChangeEvent#MODEL_MODIFIED},
	 * does nothing.
	 */
	@Override
	public void modelChanged(ModelChangeEvent event) {
		if (event.getEventID() != ModelChangeEvent.MODEL_MODIFIED) {
			return;
		}

		// Build BCellobject list
		ArrayList<BCellobject> BCellobjects = new ArrayList<>(event.getBCellobject().size());
		for (BCellobject BCellobject : event.getBCellobject()) {
			if (event.getBCellobjectFlag(BCellobject) != ModelChangeEvent.FLAG_BCellobject_REMOVED) {
				BCellobjects.add(BCellobject);
			}
		}
		BCellobjectCollection sc = BCellobjectCollection.fromCollection(BCellobjects);
		
		// Build edge list
		ArrayList<DefaultWeightedEdge> edges = new ArrayList<>(event.getEdges().size());
		for (DefaultWeightedEdge edge : event.getEdges()) {
			if (event.getEdgeFlag(edge) != ModelChangeEvent.FLAG_EDGE_REMOVED) {
				edges.add(edge);
			}
		}

		// Update BCellobject features
		BCellobjectFeatureCalculator.computeBCellobjectFeatures(sc, false);
		
		// Update edge features
		edgeFeatureCalculator.computeEdgesFeatures(edges, false);
		
		// Update track features
		trackFeatureCalculator.computeTrackFeatures(event.getTrackUpdated(), false);
	}
	
	/**
	 * Re-registers this instance from the listeners of the model, and stop
	 * updating its features.
	 */
	public void quit() {
		model.removeModelChangeListener(this);
	}

	@Override
	public int getNumThreads()
	{
		return numThreads;
	}

	@Override
	public void setNumThreads()
	{
		setNumThreads( Runtime.getRuntime().availableProcessors() );
	}

	@Override
	public void setNumThreads( int numThreads )
	{
		this.numThreads = numThreads;
		BCellobjectFeatureCalculator.setNumThreads( numThreads );
		edgeFeatureCalculator.setNumThreads( numThreads );
		trackFeatureCalculator.setNumThreads( numThreads );
	}

}
