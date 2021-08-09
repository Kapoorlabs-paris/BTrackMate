package fiji.plugin.btrackmate.action;

import static fiji.plugin.btrackmate.gui.Icons.CALCULATOR_ICON;

import java.awt.Frame;

import javax.swing.ImageIcon;

import org.scijava.plugin.Plugin;

import fiji.plugin.btrackmate.Model;
import fiji.plugin.btrackmate.SelectionModel;
import fiji.plugin.btrackmate.TrackMate;
import fiji.plugin.btrackmate.gui.displaysettings.DisplaySettings;
import fiji.plugin.btrackmate.visualization.table.AllSpotsTableView;

public class ExportAllSpotsStatsAction extends AbstractTMAction {

	public static final String NAME = "Export all spots statistics";

	public static final String KEY = "EXPORT_ALL_SPOTS_STATS";

	public static final String INFO_TEXT = "<html>" + "Export the statistics of all spots to a table. "
			+ "The numerical features of all visible spots are exported, "
			+ "regardless of whether they are in a track or not." + "</html>";

	@Override
	public void execute(final TrackMate btrackmate, final SelectionModel selectionModel,
			final DisplaySettings displaySettings, final Frame parent) {
		createSpotsTable(btrackmate.getModel(), selectionModel, displaySettings).render();
	}

	public static final AllSpotsTableView createSpotsTable(final Model model, final SelectionModel selectionModel,
			final DisplaySettings displaySettings) {
		return new AllSpotsTableView(model, selectionModel, displaySettings);
	}

	@Plugin(type = TrackMateActionFactory.class)
	public static class Factory implements TrackMateActionFactory {

		@Override
		public String getInfoText() {
			return INFO_TEXT;
		}

		@Override
		public String getKey() {
			return KEY;
		}

		@Override
		public TrackMateAction create() {
			return new ExportAllSpotsStatsAction();
		}

		@Override
		public ImageIcon getIcon() {
			return CALCULATOR_ICON;
		}

		@Override
		public String getName() {
			return NAME;
		}
	}
}
