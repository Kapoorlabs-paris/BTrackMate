package fiji.plugin.btrackmate.action;

import static fiji.plugin.btrackmate.gui.Icons.ICY_ICON;

import java.awt.Frame;
import java.io.File;

import javax.swing.ImageIcon;

import org.scijava.plugin.Plugin;

import fiji.plugin.btrackmate.Model;
import fiji.plugin.btrackmate.SelectionModel;
import fiji.plugin.btrackmate.TrackMate;
import fiji.plugin.btrackmate.gui.displaysettings.DisplaySettings;
import fiji.plugin.btrackmate.io.IOUtils;
import fiji.plugin.btrackmate.io.IcyTrackFormatWriter;

public class IcyTrackExporter extends AbstractTMAction {

	private static final String INFO_TEXT = "<html>" + "Export the visible tracks in the current model to a "
			+ "XML file that can be read by the TrackManager plugin of the "
			+ "<a href='http://icy.bioimageanalysis.org/'>Icy software</a>." + "</html>";

	private static final String NAME = "Export tracks to Icy";

	private static final String KEY = "ICY_EXPORTER";

	@Override
	public void execute(final TrackMate btrackmate, final SelectionModel selectionModel,
			final DisplaySettings displaySettings, final Frame parent) {

		logger.log("Exporting tracks to Icy format.\n");
		final Model model = btrackmate.getModel();
		final int ntracks = model.getTrackModel().nTracks(true);
		if (ntracks == 0) {
			logger.log("No visible track found. Aborting.\n");
			return;
		}

		File folder;
		try {
			folder = new File(btrackmate.getSettings().imp.getOriginalFileInfo().directory);
		} catch (final NullPointerException npe) {
			folder = new File(System.getProperty("user.dir")).getParentFile().getParentFile();
		}

		File file;
		try {
			String filename = btrackmate.getSettings().imageFileName;
			filename = filename.substring(0, filename.indexOf("."));
			file = new File(folder.getPath() + File.separator + filename + "_Icy.xml");
		} catch (final NullPointerException npe) {
			file = new File(folder.getPath() + File.separator + "IcyTracks.xml");
		}
		file = IOUtils.askForFileForSaving(file, parent, logger);
		if (null == file) {
			return;
		}

		logger.log("  Writing to file.\n");

		final double[] calibration = new double[3];
		calibration[0] = btrackmate.getSettings().dx;
		calibration[1] = btrackmate.getSettings().dy;
		calibration[2] = btrackmate.getSettings().dz;
		final IcyTrackFormatWriter writer = new IcyTrackFormatWriter(file, model, calibration);

		if (!writer.checkInput() || !writer.process()) {
			logger.error(writer.getErrorMessage());
		} else {
			logger.log("Done.\n");
		}
	}

	@Plugin(type = TrackMateActionFactory.class, enabled = true)
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
		public TrackMateAction create() {
			return new IcyTrackExporter();
		}

		@Override
		public ImageIcon getIcon() {
			return ICY_ICON;
		}
	}
}
