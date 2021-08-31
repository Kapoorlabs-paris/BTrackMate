package fiji.plugin.globalbtrackmate.action;

import static fiji.plugin.globalbtrackmate.gui.Icons.BIN_ICON;

import java.awt.Frame;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.ImageIcon;

import org.scijava.plugin.Plugin;

import fiji.plugin.globalbtrackmate.Model;
import fiji.plugin.globalbtrackmate.SelectionModel;
import fiji.plugin.globalbtrackmate.Spot;
import fiji.plugin.globalbtrackmate.SpotCollection;
import fiji.plugin.globalbtrackmate.TrackMate;
import fiji.plugin.globalbtrackmate.TrackModel;
import fiji.plugin.globalbtrackmate.gui.displaysettings.DisplaySettings;

public class TrimNotVisibleAction extends AbstractTMAction {

	public static final String INFO_TEXT = "<html>"
			+ "This action trims the tracking data by removing anything that is " + "not marked as visible. " + "<p>"
			+ "The spots that do not belong to a visible track will be " + "removed. The tracks that are not marked "
			+ "as visible will be removed as well. " + "<p>"
			+ "This action is irreversible. It helps limiting the memory "
			+ "and disk space of tracking data that has been properly " + "curated." + "</html>";

	public static final String KEY = "TRIM_NOT_VISIBLE";

	public static final String NAME = "Trim non-visible data";

	@Override
	public void execute(final TrackMate btrackmate, final SelectionModel selectionModel,
			final DisplaySettings displaySettings, final Frame parent) {
		final Model model = btrackmate.getModel();
		final TrackModel tm = model.getTrackModel();

		final SpotCollection spots = new SpotCollection();
		spots.setNumThreads(btrackmate.getNumThreads());
		final Collection<Spot> toRemove = new ArrayList<>();

		for (final Integer trackID : tm.unsortedTrackIDs(false)) {
			if (!tm.isVisible(trackID)) {
				for (final Spot spot : tm.trackSpots(trackID))
					toRemove.add(spot);
			} else {
				for (final Spot spot : tm.trackSpots(trackID))
					spots.add(spot, spot.getFeature(Spot.FRAME).intValue());
			}
		}

		model.beginUpdate();
		try {
			for (final Spot spot : toRemove)
				model.removeSpot(spot);

			model.setSpots(spots, false);
		} finally {
			model.endUpdate();
		}
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
			return new TrimNotVisibleAction();
		}

		@Override
		public ImageIcon getIcon() {
			return BIN_ICON;
		}

		@Override
		public String getName() {
			return NAME;
		}
	}
}
