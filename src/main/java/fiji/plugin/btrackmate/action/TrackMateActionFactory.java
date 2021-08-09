package fiji.plugin.btrackmate.action;

import fiji.plugin.btrackmate.TrackMateModule;

public interface TrackMateActionFactory extends TrackMateModule {
	public TrackMateAction create();
}
