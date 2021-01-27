package Buddy.plugin.trackmate.features;

import static Buddy.plugin.trackmate.gui.TrackMateWizard.FONT;
import static Buddy.plugin.trackmate.gui.TrackMateWizard.SMALL_FONT;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
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
import org.jgrapht.graph.DefaultWeightedEdge;

import Buddy.plugin.trackmate.Dimension;
import Buddy.plugin.trackmate.Model;
import Buddy.plugin.trackmate.util.ExportableChartPanel;
import Buddy.plugin.trackmate.util.TMUtils;
import Buddy.plugin.trackmate.util.XYEdgeRenderer;
import Buddy.plugin.trackmate.util.XYEdgeSeries;
import Buddy.plugin.trackmate.util.XYEdgeSeriesCollection;
import budDetector.BCellobject;

public class BCellobjectFeatureGrapher extends AbstractFeatureGrapher
{

	private final Collection< BCellobject > BCellobjects;

	private final Dimension xDimension;

	private final Map< String, Dimension > yDimensions;

	private final Map< String, String > featureNames;

	/*
	 * CONSTRUCTOR
	 */

	public BCellobjectFeatureGrapher( final String xFeature, final Set< String > yFeatures, final Collection< BCellobject > BCellobjects, final Model model )
	{
		super( xFeature, yFeatures, model );
		this.BCellobjects = BCellobjects;
		this.xDimension = model.getFeatureModel().getBCellobjectFeatureDimensions().get( xFeature );
		this.yDimensions = model.getFeatureModel().getBCellobjectFeatureDimensions();
		this.featureNames = model.getFeatureModel().getBCellobjectFeatureNames();
	}

	/*
	 * PRIVATE METHODS
	 */

	@Override
	public void render()
	{

		// X label
		final String xAxisLabel = xFeature + " (" + TMUtils.getUnitsFor( xDimension, model.getSpaceUnits(), model.getTimeUnits() ) + ")";

		// Find how many different dimensions
		final Set< Dimension > dimensions = getUniqueValues( yFeatures, yDimensions );

		// Generate one panel per different dimension
		final ArrayList< ExportableChartPanel > chartPanels = new ArrayList<>( dimensions.size() );
		for ( final Dimension dimension : dimensions )
		{

			// Y label
			final String yAxisLabel = TMUtils.getUnitsFor( dimension, model.getSpaceUnits(), model.getTimeUnits() );

			// Collect suitable feature for this dimension
			final List< String > featuresThisDimension = getCommonKeys( dimension, yFeatures, yDimensions );

			// Title
			final String title = buildPlotTitle( featuresThisDimension, featureNames );

			// Data-set for points (easy)
			final XYSeriesCollection pointDataset = buildBCellobjectDataSet( featuresThisDimension, BCellobjects );

			// Point renderer
			final XYLineAndShapeRenderer pointRenderer = new XYLineAndShapeRenderer();

			// Edge renderer
			final XYEdgeRenderer edgeRenderer = new XYEdgeRenderer();

			// Data-set for edges
			final XYEdgeSeriesCollection edgeDataset = buildEdgeDataSet( featuresThisDimension, BCellobjects );

			// The chart
			final JFreeChart chart = ChartFactory.createXYLineChart( title, xAxisLabel, yAxisLabel, pointDataset, PlotOrientation.VERTICAL, true, true, false );
			chart.getTitle().setFont( FONT );
			chart.getLegend().setItemFont( SMALL_FONT );

			// The plot
			final XYPlot plot = chart.getXYPlot();
			plot.setDataset( 1, edgeDataset );
			plot.setRenderer( 1, edgeRenderer );
			plot.setRenderer( 0, pointRenderer );
			plot.getRangeAxis().setLabelFont( FONT );
			plot.getRangeAxis().setTickLabelFont( SMALL_FONT );
			plot.getDomainAxis().setLabelFont( FONT );
			plot.getDomainAxis().setTickLabelFont( SMALL_FONT );

			// Paint
			pointRenderer.setUseOutlinePaint( true );
			final int nseries = edgeDataset.getSeriesCount();
			for ( int i = 0; i < nseries; i++ )
			{
				pointRenderer.setSeriesOutlinePaint( i, Color.black );
				pointRenderer.setSeriesLinesVisible( i, false );
				pointRenderer.setSeriesShape( i, DEFAULT_SHAPE, false );
				pointRenderer.setSeriesPaint( i, paints.getPaint( ( double ) i / nseries ), false );
				edgeRenderer.setSeriesPaint( i, paints.getPaint( ( double ) i / nseries ), false );
			}

			// The panel
			final ExportableChartPanel chartPanel = new ExportableChartPanel( chart );
			chartPanel.setPreferredSize( new java.awt.Dimension( 500, 270 ) );
			chartPanels.add( chartPanel );
		}

		renderCharts( chartPanels );
	}

	/**
	 * @return a new dataset that contains the values, specified from the given
	 *         feature, and extracted from all the given BCellobjects.
	 */
	private XYSeriesCollection buildBCellobjectDataSet( final Iterable< String > targetYFeatures, final Iterable< BCellobject > lBCellobjects )
	{
		final XYSeriesCollection dataset = new XYSeriesCollection();
		for ( final String feature : targetYFeatures )
		{
			final XYSeries series = new XYSeries( featureNames.get( feature ) );
			for ( final BCellobject BCellobject : lBCellobjects )
			{
				final Double x = BCellobject.getFeature( xFeature );
				final Double y = BCellobject.getFeature( feature );
				if ( null == x || null == y )
					continue;

				series.add( x.doubleValue(), y.doubleValue() );
			}
			dataset.addSeries( series );
		}
		return dataset;
	}

	/**
	 * @return a new dataset that contains the values, specified from the given
	 *         feature, and extracted from all the given BCellobjects. The dataset
	 *         returned is a {@link XYEdgeSeriesCollection}, made to plot the
	 *         lines between 2 points representing 2 BCellobject. We therefore retrieve
	 */
	private XYEdgeSeriesCollection buildEdgeDataSet( final Iterable< String > targetYFeatures, final Collection< BCellobject > lBCellobjects )
	{
		// Collect edges
		final List< DefaultWeightedEdge > edges = getInsideEdges( lBCellobjects );

		// Build dataset
		final XYEdgeSeriesCollection edgeDataset = new XYEdgeSeriesCollection();
		Double x0, x1, y0, y1;
		XYEdgeSeries edgeSeries;
		BCellobject source, target;
		for ( final String yFeature : targetYFeatures )
		{
			edgeSeries = new XYEdgeSeries( featureNames.get( yFeature ) );
			for ( final DefaultWeightedEdge edge : edges )
			{
				source = model.getTrackModel().getEdgeSource( edge );
				target = model.getTrackModel().getEdgeTarget( edge );
				x0 = source.getFeature( xFeature );
				y0 = source.getFeature( yFeature );
				x1 = target.getFeature( xFeature );
				y1 = target.getFeature( yFeature );
				if ( null == x0 || null == y0 || null == x1 || null == y1 )
				{
					continue;
				}
				edgeSeries.addEdge( x0.doubleValue(), y0.doubleValue(), x1.doubleValue(), y1.doubleValue() );
			}
			edgeDataset.addSeries( edgeSeries );
		}
		return edgeDataset;
	}
}
