package Buddy.plugin.trackmate.tracking.oldlap;

import Buddy.plugin.trackmate.Model;
import Buddy.plugin.trackmate.gui.ConfigurationPanel;
import Buddy.plugin.trackmate.gui.panels.tracker.SimpleLAPTrackerSettingsPanel;
import Buddy.plugin.trackmate.tracking.BCellobjectTrackerFactory;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

@Plugin( type = BCellobjectTrackerFactory.class, priority = Priority.EXTREMELY_LOW, visible = false )
public class SimpleLAPTrackerFactory extends LAPTrackerFactory
{
	public static final String THIS_TRACKER_KEY = "SIMPLE_LAP_TRACKER";

	public static final String THIS_NAME = "Simple LAP tracker";

	public static final String THIS_INFO_TEXT = "<html>" + "This tracker is identical to the LAP tracker present in this trackmate, except that it <br>" + "proposes fewer tuning options. Namely, only gap closing is allowed, based solely on <br>" + "a distance and time condition. Track splitting and merging are not allowed, resulting <br>" + "in having non-branching tracks." + " </html>";

	@Override
	public String getKey()
	{
		return THIS_TRACKER_KEY;
	}

	@Override
	public String getName()
	{
		return THIS_NAME;
	}

	@Override
	public String getInfoText()
	{
		return THIS_INFO_TEXT;
	}

	@Override
	public ConfigurationPanel getTrackerConfigurationPanel( final Model model )
	{
		final String spaceUnits = model.getSpaceUnits();
		return new SimpleLAPTrackerSettingsPanel( THIS_NAME, THIS_INFO_TEXT, spaceUnits );
	}
}
