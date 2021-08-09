package fiji.plugin.btrackmate.action;

import static fiji.plugin.btrackmate.gui.Fonts.FONT;
import static fiji.plugin.btrackmate.gui.Fonts.SMALL_FONT;
import static fiji.plugin.btrackmate.gui.Icons.PLOT_ICON;

import java.awt.Frame;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.DefaultXYDataset;
import org.scijava.plugin.Plugin;

import fiji.plugin.btrackmate.Model;
import fiji.plugin.btrackmate.SelectionModel;
import fiji.plugin.btrackmate.Settings;
import fiji.plugin.btrackmate.Spot;
import fiji.plugin.btrackmate.SpotCollection;
import fiji.plugin.btrackmate.TrackMate;
import fiji.plugin.btrackmate.gui.displaysettings.DisplaySettings;
import fiji.plugin.btrackmate.util.ExportableChartPanel;

public class PlotNSpotsVsTimeAction extends AbstractTMAction {

	public static final String NAME = "Plot N spots vs time";

	public static final String KEY = "PLOT_NSPOTS_VS_TIME";
	public static final String INFO_TEXT = "<html>" + "Plot the number of spots in each frame as a function <br>"
			+ "of time. Only the filtered spots are taken into account. " + "</html>";

	@Override
	public void execute(final TrackMate btrackmate, final SelectionModel selectionModel,
			final DisplaySettings displaySettings, final Frame parent) {
		// Collect data
		final Model model = btrackmate.getModel();
		final Settings settings = btrackmate.getSettings();
		final SpotCollection spots = model.getSpots();
		final int nFrames = spots.keySet().size();
		final double[][] data = new double[2][nFrames];
		int index = 0;
		for (final int frame : spots.keySet()) {
			data[1][index] = spots.getNSpots(frame, true);
			if (data[1][index] > 0) {
				data[0][index] = spots.iterator(frame, false).next().getFeature(Spot.POSITION_T);
			} else {
				data[0][index] = frame * settings.dt;
			}
			index++;
		}

		// Plot data
		final String xAxisLabel = "Time (" + btrackmate.getModel().getTimeUnits() + ")";
		final String yAxisLabel = "N spots";
		final String title = "Nspots vs Time for " + btrackmate.getSettings().imp.getShortTitle();
		final DefaultXYDataset dataset = new DefaultXYDataset();
		dataset.addSeries("Nspots", data);

		final JFreeChart chart = ChartFactory.createXYLineChart(title, xAxisLabel, yAxisLabel, dataset,
				PlotOrientation.VERTICAL, true, true, false);
		chart.getTitle().setFont(FONT);
		chart.getLegend().setItemFont(SMALL_FONT);

		// The plot
		final XYPlot plot = chart.getXYPlot();
//		plot.setRenderer(0, pointRenderer);
		plot.getRangeAxis().setLabelFont(FONT);
		plot.getRangeAxis().setTickLabelFont(SMALL_FONT);
		plot.getDomainAxis().setLabelFont(FONT);
		plot.getDomainAxis().setTickLabelFont(SMALL_FONT);

		final ExportableChartPanel panel = new ExportableChartPanel(chart);

		final JFrame frame = new JFrame(title);
		frame.setSize(500, 270);
		frame.getContentPane().add(panel);
		frame.setVisible(true);
	}

	@Plugin(type = TrackMateActionFactory.class)
	public static class Factory implements TrackMateActionFactory {

		@Override
		public String getInfoText() {
			return INFO_TEXT;
		}

		@Override
		public String getName() {
			return NAME;
		}

		@Override
		public String getKey() {
			return KEY;
		}

		@Override
		public ImageIcon getIcon() {
			return PLOT_ICON;
		}

		@Override
		public TrackMateAction create() {
			return new PlotNSpotsVsTimeAction();
		}
	}
}
