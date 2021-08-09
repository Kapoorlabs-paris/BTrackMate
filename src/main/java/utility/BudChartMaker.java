package utility;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import budDetector.Budpointobject;
import net.imglib2.util.Pair;

public class BudChartMaker {

	public static JFrame display(final JFreeChart chart) {
		return display(chart, new Dimension(800, 500));
	}

	public static JFrame display(final JFreeChart chart, final Dimension d) {
		final JPanel panel = new JPanel();
		final ChartPanel chartPanel = new ChartPanel(chart, d.width - 10, d.height - 35,
				ChartPanel.DEFAULT_MINIMUM_DRAW_WIDTH, ChartPanel.DEFAULT_MINIMUM_DRAW_HEIGHT,
				ChartPanel.DEFAULT_MAXIMUM_DRAW_WIDTH, ChartPanel.DEFAULT_MAXIMUM_DRAW_HEIGHT,
				ChartPanel.DEFAULT_BUFFER_USED, true, // properties
				true, // save
				true, // print
				true, // zoom
				true // tooltips
		);
		panel.add(chartPanel);

		final JFrame frame = new JFrame();
		frame.setContentPane(panel);
		frame.validate();
		frame.setSize(d);

		frame.setVisible(true);
		return frame;
	}

	public static JFreeChart makeChart(final XYSeriesCollection dataset) {
		return makeChart(dataset, "XY Chart", "x-axis", "y-axis");
	}

	public static JFreeChart makeChart(final XYSeriesCollection dataset, final String title, final String x,
			final String y) {
		final JFreeChart chart = ChartFactory.createXYLineChart(title, x, y, dataset, PlotOrientation.VERTICAL, true,
				true, false);

		return chart;
	}

	public static XYSeries drawPoints(final List<Pair<String, double[]>> mts) {
		return drawPoints(mts, "Angle evolution");
	}

	public static XYSeries drawPoints(final List<Pair<String, double[]>> mts, final String name) {
		XYSeries series = new XYSeries(name);

		if (mts != null) {
			for (final Pair<String, double[]> mt : mts)
				series.add(mt.getB()[0], mt.getB()[1]);
		}
		return series;
	}

	public static XYSeries drawPointsSecond(final List<Pair<String, double[]>> mts, final String name) {
		XYSeries series = new XYSeries(name);

		if (mts != null) {
			for (final Pair<String, double[]> mt : mts)
				series.add(mt.getB()[0], mt.getB()[2]);
		}
		return series;
	}

	public static XYSeries drawSegPoints(final ArrayList<Pair<String, Pair<Integer, Double>>> mts, final String name) {
		XYSeries series = new XYSeries(name);

		if (mts != null) {
			for (Pair<String, Pair<Integer, Double>> mt : mts)
				series.add(mt.getB().getA(), mt.getB().getB());

		}
		return series;
	}

	public static XYSeries drawPointsInt(final List<Pair<Integer, double[]>> mts) {
		return drawPointsInt(mts, "Measurement");
	}

	public static XYSeries drawPointsInt(final List<Pair<Integer, double[]>> mts, final String name) {
		XYSeries series = new XYSeries(name);

		if (mts != null) {
			for (final Pair<Integer, double[]> mt : mts)
				series.add(mt.getB()[0], mt.getB()[1]);
		}
		return series;
	}

	public static XYSeries drawPointsIntY(final List<Pair<Integer, double[]>> mts) {
		return drawPointsInt(mts, "Measurement");
	}

	public static XYSeries drawPointsIntY(final List<Pair<Integer, double[]>> mts, final String name) {
		XYSeries series = new XYSeries(name);

		if (mts != null) {
			for (final Pair<Integer, double[]> mt : mts)
				series.add(mt.getB()[0], mt.getB()[3]);
		}
		return series;
	}

	public static XYSeries drawVelocity(final ArrayList<double[]> mts, String name) {
		XYSeries series = new XYSeries(name);

		if (mts != null) {
			for (final double[] mt : mts) {
				series.add(mt[0], mt[3]);
			}
		}
		return series;
	}

	public static XYSeries drawCurvePoints(final List<Pair<String, double[]>> mts) {
		return drawPoints(mts, "Evolution");
	}

	public static XYSeries drawCurvePointsSecond(final List<Pair<String, double[]>> mts) {
		return drawPointsSecond(mts, "Evolution");
	}

	public static XYSeries drawCurveSegPoints(final ArrayList<Pair<String, Pair<Integer, Double>>> mts) {
		return drawSegPoints(mts, "Evolution");
	}

	public static XYSeries drawCurvePoints(final List<Pair<String, double[]>> mts, final String name) {
		XYSeries series = new XYSeries(name);

		if (mts != null) {
			for (final Pair<String, double[]> mt : mts)
				series.add(mt.getB()[0], mt.getB()[1]);
		}
		return series;
	}

	public static void setColor(final JFreeChart chart, final int seriesIndex, final Color col) {
		final XYPlot plot = chart.getXYPlot();
		final XYItemRenderer renderer = plot.getRenderer();
		renderer.setSeriesPaint(seriesIndex, col);
	}

	public static void setStroke(final JFreeChart chart, final int seriesIndex, final float stroke) {
		final XYPlot plot = chart.getXYPlot();
		final XYItemRenderer renderer = plot.getRenderer();
		renderer.setSeriesStroke(seriesIndex, new BasicStroke(stroke));
	}

	public static void setShape(final JFreeChart chart, final int seriesIndex, final Shape shape) {
		final XYPlot plot = chart.getXYPlot();
		final XYItemRenderer renderer = plot.getRenderer();
		renderer.setSeriesShape(seriesIndex, shape);
	}

}
