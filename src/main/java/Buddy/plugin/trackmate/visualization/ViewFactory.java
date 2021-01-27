package Buddy.plugin.trackmate.visualization;

import Buddy.plugin.trackmate.Model;
import Buddy.plugin.trackmate.SelectionModel;
import Buddy.plugin.trackmate.Settings;
import Buddy.plugin.trackmate.TrackMateModule;
import pluginTools.InteractiveBud;

public interface ViewFactory extends TrackMateModule
{

	/**
	 * Returns a new instance of the concrete view.
	 *
	 * @param model
	 *            the model to display in the view.
	 * @param settings
	 *            a {@link Settings} object, which specific implementation might
	 *            use to display the model.
	 * @param selectionModel
	 *            the {@link SelectionModel} model to share in the created view.
	 * @return a new view of the specified model.
	 */
	public TrackMateModelView create(final InteractiveBud parent,  final Model model, final Settings settings, final SelectionModel selectionModel );

}
