package Buddy.plugin.trackmate.visualization.trackscheme;

import Buddy.plugin.trackmate.Model;
import Buddy.plugin.trackmate.SelectionModel;
import Buddy.plugin.trackmate.Settings;
import Buddy.plugin.trackmate.visualization.TrackMateModelView;
import Buddy.plugin.trackmate.visualization.ViewFactory;
import pluginTools.InteractiveBud;

import javax.swing.ImageIcon;

import org.scijava.plugin.Plugin;

/*
 * We annotate the TrackScheme factory to be NOT visible,
 * because we do not want it to show in the GUI menu.
 */
@Plugin( type = ViewFactory.class, visible = false )
public class TrackSchemeFactory implements ViewFactory
{

	@Override
	public TrackMateModelView create( final InteractiveBud parent,  final Model model, final Settings settings, final SelectionModel selectionModel )
	{
		return new TrackScheme( parent, model, selectionModel );
	}

	@Override
	public String getName()
	{
		return "TrackScheme";
	}

	@Override
	public String getKey()
	{
		return TrackScheme.KEY;
	}

	@Override
	public ImageIcon getIcon()
	{
		return null;
	}

	@Override
	public String getInfoText()
	{
		return "<html>Not redacted!</html>";
	}

}
