package Buddy.plugin.trackmate.tracking;

import Buddy.plugin.trackmate.Model;
import Buddy.plugin.trackmate.BCellobjectCollection;
import Buddy.plugin.trackmate.gui.ConfigurationPanel;

import java.util.Map;

import javax.swing.ImageIcon;

import org.jdom2.Element;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

@Plugin( type = BCellobjectTrackerFactory.class, priority = Priority.EXTREMELY_LOW ,visible = false)
public class ManualTrackerFactory implements BCellobjectTrackerFactory
{
	public static final String TRACKER_KEY = "MANUAL_TRACKER";

	public static final String NAME = "Manual tracking";

	public static final String INFO_TEXT = "<html>" + "Not a valid choice for BTrackMate.</html>";

	private String errorMessage;

	@Override
	public String getInfoText()
	{
		return INFO_TEXT;
	}

	@Override
	public ImageIcon getIcon()
	{
		return null;
	}

	@Override
	public String getKey()
	{
		return TRACKER_KEY;
	}

	@Override
	public String getName()
	{
		return NAME;
	}

	@Override
	public BCellobjectTracker create( final BCellobjectCollection BCellobjects, final Map< String, Object > settings )
	{
		return null;
	}

	@Override
	public ConfigurationPanel getTrackerConfigurationPanel( final Model model )
	{
		return null;
	}

	@Override
	public boolean marshall( final Map< String, Object > settings, final Element element )
	{
		return true;
	}

	@Override
	public boolean unmarshall( final Element element, final Map< String, Object > settings )
	{
		return true;
	}

	@Override
	public String toString( final Map< String, Object > sm )
	{
		if ( !checkSettingsValidity( sm ) ) { return errorMessage; }
		return "  Manual tracking.\n";
	}

	@Override
	public Map< String, Object > getDefaultSettings()
	{
		return null;
	}

	@Override
	public boolean checkSettingsValidity( final Map< String, Object > settings )
	{
		return true;
	}

	@Override
	public String getErrorMessage()
	{
		return errorMessage;
	}

}
