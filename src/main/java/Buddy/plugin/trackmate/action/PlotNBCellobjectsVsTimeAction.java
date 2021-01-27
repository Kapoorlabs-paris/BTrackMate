package Buddy.plugin.trackmate.action;

import static Buddy.plugin.trackmate.gui.TrackMateWizard.FONT;
import static Buddy.plugin.trackmate.gui.TrackMateWizard.SMALL_FONT;

import Buddy.plugin.trackmate.BCellobjectCollection;
import Buddy.plugin.trackmate.Model;
import Buddy.plugin.trackmate.Settings;
import Buddy.plugin.trackmate.TrackMate;
import Buddy.plugin.trackmate.gui.TrackMateGUIController;
import Buddy.plugin.trackmate.util.ExportableChartPanel;
import Buddy.plugin.trackmate.visualization.trackscheme.TrackSchemeFrame;
import budDetector.BCellobject;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.DefaultXYDataset;
import org.scijava.plugin.Plugin;

public class PlotNBCellobjectsVsTimeAction extends AbstractTMAction {


	public static final ImageIcon ICON = new ImageIcon(TrackSchemeFrame.class.getResource("resources/plots.png"));
	public static final String NAME = "Plot N BCellobjects vs time";

	public static final String KEY = "PLOT_NBCellobjectS_VS_TIME";
	public static final String INFO_TEXT =  "<html>" +
			"Plot the number of BCellobjects in each frame as a function <br>" +
			"of time. Only the filtered BCellobjects are taken into account. " +
			"</html>";

	@Override
	public void execute(final TrackMate trackmate) {
		// Collect data
		final Model model = trackmate.getModel();
		final Settings settings = trackmate.getSettings();
		final BCellobjectCollection BCellobjects = model.getBCellobjects();
		final int nFrames = BCellobjects.keySet().size();
		final double[][] data = new double[2][nFrames];
		int index = 0;
		for (final int frame : BCellobjects.keySet()) {
			data[1][index] = BCellobjects.getNBCellobjects(frame);
			if (data[1][index] > 0) {
				data[0][index] = BCellobjects.iterator(frame).next().getFeature(BCellobject.POSITION_T);
			} else {
				data[0][index] = frame * settings.dt;
			}
			index++;
		}

		// Plot data
		final String xAxisLabel = "Time ("+trackmate.getModel().getTimeUnits()+")";
		final String yAxisLabel = "N BCellobjects";
		final String title = "NBCellobjects vs Time for "+trackmate.getSettings().imp.getShortTitle();
		final DefaultXYDataset dataset = new DefaultXYDataset();
		dataset.addSeries("NBCellobjects", data);

		final JFreeChart chart = ChartFactory.createXYLineChart(title, xAxisLabel, yAxisLabel, dataset, PlotOrientation.VERTICAL, true, true, false);
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


	@Plugin( type = TrackMateActionFactory.class )
	public static class Factory implements TrackMateActionFactory
	{

		@Override
		public String getInfoText()
		{
			return INFO_TEXT;
		}

		@Override
		public String getName()
		{
			return NAME;
		}

		@Override
		public String getKey()
		{
			return KEY;
		}

		@Override
		public ImageIcon getIcon()
		{
			return ICON;
		}

		@Override
		public TrackMateAction create( final TrackMateGUIController controller )
		{
			return new PlotNBCellobjectsVsTimeAction();
		}
	}
}
