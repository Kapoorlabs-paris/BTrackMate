package fiji.plugin.globalbtrackmate.gui.wizard.descriptors;

import java.awt.Frame;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.SwingUtilities;

import fiji.plugin.globalbtrackmate.Logger;
import fiji.plugin.globalbtrackmate.TrackMate;
import fiji.plugin.globalbtrackmate.Logger.StringBuilderLogger;
import fiji.plugin.globalbtrackmate.gui.components.LogPanel;
import fiji.plugin.globalbtrackmate.gui.displaysettings.DisplaySettings;
import fiji.plugin.globalbtrackmate.gui.wizard.WizardSequence;
import fiji.plugin.globalbtrackmate.io.IOUtils;
import fiji.plugin.globalbtrackmate.io.TmXmlWriter;
import fiji.plugin.globalbtrackmate.util.TMUtils;

public class SaveDescriptor extends SomeDialogDescriptor {

	private static final String KEY = "Saving";

	private final TrackMate btrackmate;

	private final DisplaySettings displaySettings;

	private final WizardSequence sequence;

	public SaveDescriptor(final TrackMate btrackmate, final DisplaySettings displaySettings,
			final WizardSequence sequence) {
		super(KEY, (LogPanel) sequence.logDescriptor().getPanelComponent());
		this.btrackmate = btrackmate;
		this.displaySettings = displaySettings;
		this.sequence = sequence;
	}

	@Override
	public void displayingPanel() {
		 LogPanel logPanel = (LogPanel) targetPanel;
		final Logger logger = logPanel.getLogger();
		logger.log("Saving data...\n", Logger.BLUE_COLOR);
		if (null == file)
			file = TMUtils.proposeTrackMateSaveFile(btrackmate.getSettings(), logger);

		/*
		 * If we are to save tracks, we better ensures that track and edge features are
		 * there, even if we have to enforce it.
		 */
		if (btrackmate.getModel().getTrackModel().nTracks(false) > 0) {
			btrackmate.computeEdgeFeatures(true);
			btrackmate.computeTrackFeatures(true);
		}

		final File tmpFile = IOUtils.askForFileForSaving(file, (Frame) SwingUtilities.getWindowAncestor(logPanel),
				logger);
		if (null == tmpFile)
			return;
		file = tmpFile;

		/*
		 * Write model, settings and GUI state
		 */

		final TmXmlWriter writer = new TmXmlWriter(file, logger);

		writer.appendLog(logPanel.getTextContent());
		writer.appendModel(btrackmate.getModel());
		writer.appendSettings(btrackmate.getSettings());
		writer.appendGUIState(sequence.current().getPanelDescriptorIdentifier());
		writer.appendDisplaySettings(displaySettings);
		
		try {
			writer.writeToFile();
			logger.log("Data saved to: " + file.toString() + '\n');
		} catch (final FileNotFoundException e) {
			logger.error("File not found:\n" + e.getMessage() + '\n');
			return;
		} catch (final IOException e) {
			logger.error("Input/Output error:\n" + e.getMessage() + '\n');
			return;
		}
		
		logPanel.setTextContent("Welcome to tracking world");
		logPanel.repaint();
		logPanel.validate();
	}
}
