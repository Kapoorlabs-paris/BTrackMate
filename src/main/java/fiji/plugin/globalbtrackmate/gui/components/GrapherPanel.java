package fiji.plugin.globalbtrackmate.gui.components;

import static fiji.plugin.globalbtrackmate.gui.Icons.EDGE_ICON_64x64;
import static fiji.plugin.globalbtrackmate.gui.Icons.SPOT_ICON_64x64;
import static fiji.plugin.globalbtrackmate.gui.Icons.TRACK_ICON_64x64;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import org.jgrapht.graph.DefaultWeightedEdge;

import fiji.plugin.globalbtrackmate.Spot;
import fiji.plugin.globalbtrackmate.TrackMate;
import fiji.plugin.globalbtrackmate.features.EdgeFeatureGrapher;
import fiji.plugin.globalbtrackmate.features.FeatureUtils;
import fiji.plugin.globalbtrackmate.features.SpotFeatureGrapher;
import fiji.plugin.globalbtrackmate.features.TrackFeatureGrapher;
import fiji.plugin.globalbtrackmate.gui.displaysettings.DisplaySettings;
import fiji.plugin.globalbtrackmate.gui.displaysettings.DisplaySettings.TrackMateObject;

public class GrapherPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private final TrackMate btrackmate;

	private final JPanel panelSpot;

	private final JPanel panelEdges;

	private final JPanel panelTracks;

	private FeaturePlotSelectionPanel spotFeatureSelectionPanel;

	private FeaturePlotSelectionPanel edgeFeatureSelectionPanel;

	private FeaturePlotSelectionPanel trackFeatureSelectionPanel;

	private final DisplaySettings displaySettings;

	/*
	 * CONSTRUCTOR
	 */

	public GrapherPanel(final TrackMate btrackmate, final DisplaySettings displaySettings) {
		this.btrackmate = btrackmate;
		this.displaySettings = displaySettings;

		setLayout(new BorderLayout(0, 0));

		final JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP);
		add(tabbedPane, BorderLayout.CENTER);

		panelSpot = new JPanel();
		tabbedPane.addTab("Spots", SPOT_ICON_64x64, panelSpot, null);
		panelSpot.setLayout(new BorderLayout(0, 0));

		panelEdges = new JPanel();
		tabbedPane.addTab("Links", EDGE_ICON_64x64, panelEdges, null);
		panelEdges.setLayout(new BorderLayout(0, 0));

		panelTracks = new JPanel();
		tabbedPane.addTab("Tracks", TRACK_ICON_64x64, panelTracks, null);
		panelTracks.setLayout(new BorderLayout(0, 0));

		refresh();
	}

	public void refresh() {
		// regen spot features
		panelSpot.removeAll();
		final Map<String, String> spotFeatureNames = FeatureUtils.collectFeatureKeys(TrackMateObject.SPOTS,
				btrackmate.getModel(), btrackmate.getSettings());
		final Set<String> spotFeatures = spotFeatureNames.keySet();
		spotFeatureSelectionPanel = new FeaturePlotSelectionPanel("T", "Mean intensity ch1", spotFeatures,
				spotFeatureNames, (xKey, yKeys) -> {
					spotFeatureSelectionPanel.setEnabled(false);
					new Thread("TrackMate plot spot features thread") {
						@Override
						public void run() {
							plotSpotFeatures(xKey, yKeys);
							spotFeatureSelectionPanel.setEnabled(true);
						}
					}.start();
				});
		panelSpot.add(spotFeatureSelectionPanel);

		// regen edge features
		panelEdges.removeAll();
		final Map<String, String> edgeFeatureNames = FeatureUtils.collectFeatureKeys(TrackMateObject.EDGES,
				btrackmate.getModel(), btrackmate.getSettings());
		final Set<String> edgeFeatures = edgeFeatureNames.keySet();
		edgeFeatureSelectionPanel = new FeaturePlotSelectionPanel("Edge time", "Speed", edgeFeatures, edgeFeatureNames,
				(xKey, yKeys) -> {
					edgeFeatureSelectionPanel.setEnabled(false);
					new Thread("TrackMate plot edge features thread") {
						@Override
						public void run() {
							plotEdgeFeatures(xKey, yKeys);
							edgeFeatureSelectionPanel.setEnabled(true);
						}
					}.start();

				});
		panelEdges.add(edgeFeatureSelectionPanel);

		// regen trak features
		panelTracks.removeAll();
		final Map<String, String> trackFeatureNames = FeatureUtils.collectFeatureKeys(TrackMateObject.TRACKS,
				btrackmate.getModel(), btrackmate.getSettings());
		final Set<String> trackFeatures = trackFeatureNames.keySet();
		trackFeatureSelectionPanel = new FeaturePlotSelectionPanel("Track index", "Number of spots in track",
				trackFeatures, trackFeatureNames, (xKey, yKeys) -> {
					trackFeatureSelectionPanel.setEnabled(false);
					new Thread("TrackMate plot track features thread") {
						@Override
						public void run() {
							plotTrackFeatures(xKey, yKeys);
							trackFeatureSelectionPanel.setEnabled(true);
						}
					}.start();
				});
		panelTracks.add(trackFeatureSelectionPanel);
	}

	private void plotSpotFeatures(final String xFeature, final Set<String> yFeatures) {
		// Collect only the spots that are in tracks
		final List<Spot> spots = new ArrayList<>(btrackmate.getModel().getSpots().getNSpots(true));
		for (final Integer trackID : btrackmate.getModel().getTrackModel().trackIDs(true))
			spots.addAll(btrackmate.getModel().getTrackModel().trackSpots(trackID));

		final SpotFeatureGrapher grapher = new SpotFeatureGrapher(xFeature, yFeatures, spots, btrackmate.getModel(),
				displaySettings);
		grapher.render();
	}

	private void plotEdgeFeatures(final String xFeature, final Set<String> yFeatures) {
		// Collect edges in filtered tracks
		final List<DefaultWeightedEdge> edges = new ArrayList<>();
		for (final Integer trackID : btrackmate.getModel().getTrackModel().trackIDs(true))
			edges.addAll(btrackmate.getModel().getTrackModel().trackEdges(trackID));

		// Prepare grapher
		final EdgeFeatureGrapher grapher = new EdgeFeatureGrapher(xFeature, yFeatures, edges, btrackmate.getModel(),
				displaySettings);
		grapher.render();
	}

	private void plotTrackFeatures(final String xFeature, final Set<String> yFeatures) {
		// Prepare grapher
		final TrackFeatureGrapher grapher = new TrackFeatureGrapher(xFeature, yFeatures, btrackmate.getModel(),
				displaySettings);
		grapher.render();
	}
}
