package fiji.plugin.globalbtrackmate.action;

import fiji.plugin.globalbtrackmate.TrackMateModule;

public interface TrackMateActionFactory extends TrackMateModule {
	public TrackMateAction create();
}
