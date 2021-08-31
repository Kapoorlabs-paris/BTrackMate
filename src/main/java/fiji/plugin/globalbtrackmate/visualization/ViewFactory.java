package fiji.plugin.globalbtrackmate.visualization;

import fiji.plugin.globalbtrackmate.Model;
import fiji.plugin.globalbtrackmate.SelectionModel;
import fiji.plugin.globalbtrackmate.Settings;
import fiji.plugin.globalbtrackmate.TrackMateModule;
import fiji.plugin.globalbtrackmate.gui.displaysettings.DisplaySettings;

public interface ViewFactory extends TrackMateModule {

	/**
	 * Returns a new instance of the concrete view.
	 *
	 * @param model           the model to display in the view.
	 * @param settings        a {@link Settings} object, which specific
	 *                        implementation might use to display the model.
	 * @param selectionModel  the {@link SelectionModel} model to share in the
	 *                        created view.
	 * @param displaySettings the display settings to use to paint the view.
	 * @return a new view of the specified model.
	 */
	public TrackMateModelView create(final Model model, final Settings settings, final SelectionModel selectionModel,
			final DisplaySettings displaySettings);

}
