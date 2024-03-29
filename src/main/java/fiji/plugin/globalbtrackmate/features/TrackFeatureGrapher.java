package fiji.plugin.globalbtrackmate.features;

import static fiji.plugin.globalbtrackmate.gui.Fonts.FONT;
import static fiji.plugin.globalbtrackmate.gui.Fonts.SMALL_FONT;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import fiji.plugin.globalbtrackmate.Dimension;
import fiji.plugin.globalbtrackmate.FeatureModel;
import fiji.plugin.globalbtrackmate.Model;
import fiji.plugin.globalbtrackmate.gui.displaysettings.Colormap;
import fiji.plugin.globalbtrackmate.gui.displaysettings.DisplaySettings;
import fiji.plugin.globalbtrackmate.util.ExportableChartPanel;
import fiji.plugin.globalbtrackmate.util.TMUtils;

public class TrackFeatureGrapher extends AbstractFeatureGrapher {

	private final Dimension xDimension;

	private final Map<String, Dimension> yDimensions;

	private final Map<String, String> featureNames;

	public TrackFeatureGrapher(final String xFeature, final Set<String> yFeatures, final Model model,
			final DisplaySettings displaySettings) {
		super(xFeature, yFeatures, model, displaySettings);
		this.xDimension = model.getFeatureModel().getTrackFeatureDimensions().get(xFeature);
		this.yDimensions = model.getFeatureModel().getTrackFeatureDimensions();
		this.featureNames = model.getFeatureModel().getTrackFeatureNames();
	}

	@Override
	public void render() {
		final Colormap colormap = displaySettings.getColormap();

		// Check x units
		final String xdim = TMUtils.getUnitsFor(xDimension, model.getSpaceUnits(), model.getTimeUnits());
		if (null == xdim) { // not a number feature
			return;
		}

		// X label
		final String xAxisLabel = xFeature + " (" + xdim + ")";

		// Find how many different dimensions
		final Set<Dimension> dimensions = getUniqueValues(yFeatures, yDimensions);

		// Generate one panel per different dimension
		final ArrayList<ExportableChartPanel> chartPanels = new ArrayList<>(dimensions.size());
		for (final Dimension dimension : dimensions) {

			// Y label
			final String yAxisLabel = TMUtils.getUnitsFor(dimension, model.getSpaceUnits(), model.getTimeUnits());

			// Check y units
			if (null == yAxisLabel) { // not a number feature
				continue;
			}

			// Collect suitable feature for this dimension
			final List<String> featuresThisDimension = getCommonKeys(dimension, yFeatures, yDimensions);

			// Title
			final String title = buildPlotTitle(featuresThisDimension, featureNames);

			// Data-set for points (easy)
			final XYSeriesCollection pointDataset = buildTrackDataSet(featuresThisDimension);

			// Point renderer
			final XYLineAndShapeRenderer pointRenderer = new XYLineAndShapeRenderer();

			// The chart
			final JFreeChart chart = ChartFactory.createXYLineChart(title, xAxisLabel, yAxisLabel, pointDataset,
					PlotOrientation.VERTICAL, true, true, false);
			chart.getTitle().setFont(FONT);
			chart.getLegend().setItemFont(SMALL_FONT);

			// The plot
			final XYPlot plot = chart.getXYPlot();
			plot.setRenderer(0, pointRenderer);
			plot.getRangeAxis().setLabelFont(FONT);
			plot.getRangeAxis().setTickLabelFont(SMALL_FONT);
			plot.getDomainAxis().setLabelFont(FONT);
			plot.getDomainAxis().setTickLabelFont(SMALL_FONT);

			// Paint
			pointRenderer.setUseOutlinePaint(true);
			final int nseries = pointDataset.getSeriesCount();
			for (int i = 0; i < nseries; i++) {
				pointRenderer.setSeriesOutlinePaint(i, Color.black);
				pointRenderer.setSeriesLinesVisible(i, false);
				pointRenderer.setSeriesShape(i, DEFAULT_SHAPE, false);
				pointRenderer.setSeriesPaint(i, colormap.getPaint((double) i / nseries), false);
			}

			// The panel
			final ExportableChartPanel chartPanel = new ExportableChartPanel(chart);
			chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
			chartPanels.add(chartPanel);
		}

		renderCharts(chartPanels);
	}

	/**
	 * @return a new dataset that contains the values, specified from the given
	 *         feature, and extracted from all the visible tracks in the model.
	 */
	private XYSeriesCollection buildTrackDataSet(final Iterable<String> targetYFeatures) {
		final XYSeriesCollection dataset = new XYSeriesCollection();
		final FeatureModel fm = model.getFeatureModel();
		for (final String feature : targetYFeatures) {
			final XYSeries series = new XYSeries(featureNames.get(feature));
			for (final Integer trackID : model.getTrackModel().trackIDs(true)) {
				final Double x = fm.getTrackFeature(trackID, xFeature);
				final Double y = fm.getTrackFeature(trackID, feature);
				if (null == x || null == y)
					continue;

				series.add(x.doubleValue(), y.doubleValue());
			}
			dataset.addSeries(series);
		}
		return dataset;
	}
}
