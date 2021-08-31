package fiji.plugin.globalbtrackmate.visualization.trackscheme;

import javax.swing.ImageIcon;

import org.scijava.plugin.Plugin;

import fiji.plugin.globalbtrackmate.Model;
import fiji.plugin.globalbtrackmate.SelectionModel;
import fiji.plugin.globalbtrackmate.Settings;
import fiji.plugin.globalbtrackmate.gui.displaysettings.DisplaySettings;
import fiji.plugin.globalbtrackmate.visualization.TrackMateModelView;
import fiji.plugin.globalbtrackmate.visualization.ViewFactory;

/*
 * We annotate the TrackScheme factory to be NOT visible,
 * because we do not want it to show in the GUI menu.
 */
@Plugin(type = ViewFactory.class, visible = false)
public class TrackSchemeFactory implements ViewFactory {

	@Override
	public TrackMateModelView create(final Model model, final Settings settings, final SelectionModel selectionModel,
			final DisplaySettings displaySettings) {
		return new TrackScheme(model, selectionModel, displaySettings);
	}

	@Override
	public String getName() {
		return "TrackScheme";
	}

	@Override
	public String getKey() {
		return TrackScheme.KEY;
	}

	@Override
	public ImageIcon getIcon() {
		return null;
	}

	@Override
	public String getInfoText() {
		return "<html>Not redacted!</html>";
	}
}
