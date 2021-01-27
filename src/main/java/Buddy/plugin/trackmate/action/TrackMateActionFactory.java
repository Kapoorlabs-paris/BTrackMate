package Buddy.plugin.trackmate.action;

import Buddy.plugin.trackmate.TrackMateModule;
import Buddy.plugin.trackmate.gui.TrackMateGUIController;

public interface TrackMateActionFactory extends TrackMateModule
{
	public TrackMateAction create( TrackMateGUIController controller );
}
