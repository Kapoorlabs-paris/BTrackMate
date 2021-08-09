package fiji.plugin.btrackmate.visualization.bigdataviewer;

import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import bdv.util.BdvOverlay;
import bdv.util.BdvSource;

import javax.swing.ImageIcon;

import org.scijava.plugin.Plugin;

import fiji.plugin.btrackmate.Model;
import fiji.plugin.btrackmate.SelectionModel;
import fiji.plugin.btrackmate.Settings;
import fiji.plugin.btrackmate.gui.displaysettings.DisplaySettings;
import fiji.plugin.btrackmate.visualization.TrackMateModelView;
import fiji.plugin.btrackmate.visualization.ViewFactory;
import ij.ImagePlus;

@Plugin(type = ViewFactory.class)
public class BigDataViewerDisplayerFactory implements ViewFactory

{

	private static final String INFO_TEXT = "<html>"
			+ "This displayer overlays the spots and tracks on the current <br>" + "BigDataViewer window. <br>" + "<p> "
			+ "This displayer allows manual editing of spots, thanks to the spot <br> "
			+ "edit tool that appear in ImageJ toolbar." + "<p>"
			+ "Double-clicking in a spot toggles the editing mode: The spot can <br> "
			+ "be moved around in a XY plane by mouse dragging. "
			+ "To move it  in time, simply change the current  time-point by <br>. To change its radius, hold the <br>"
			+ "<tt>alt</tt> key down and rotate the mouse-wheel. Holding the <br>"
			+ "<tt>shift</tt> key on top changes it faster. " + "<p>"
			+ "Alternatively, keyboard can be used to edit spots:<br/>"
			+ " - <b>A</b> creates a new spot under the mouse.<br/>"
			+ " - <b>D</b> deletes the spot under the mouse.<br/>"
			+ " - <b>Q</b> and <b>E</b> decreases and increases the radius of the spot "
			+ "under the mouse (shift to go faster).<br/>"
			+ " - <b>Space</b> + mouse drag moves the spot under the mouse.<br/>" + "<p>"
			+ "To toggle links between two spots, select two spots (Shift+Click), <br>" + "then press <b>L</b>. "
			+ "<p>" + "<b>Shift+L</b> toggle the auto-linking mode on/off. <br>"
			+ "If on, every spot created will be automatically linked with the spot <br>"
			+ "currently selected, if they are in subsequent frames." + "</html>";

	private static final String NAME = "BigDataViewer Displayer";

	@Override
	public TrackMateModelView create(final Model model, final Settings settings, final SelectionModel selectionModel,
			final DisplaySettings displaySettings) {
		final ImagePlus imp = (settings == null) ? null : settings.imp;
		return new BigDataViewerDisplayer(model, selectionModel, imp, displaySettings);
	}

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
		return BigDataViewerDisplayer.KEY;
	}

	@Override
	public ImageIcon getIcon() {
		return null;
	}
}
