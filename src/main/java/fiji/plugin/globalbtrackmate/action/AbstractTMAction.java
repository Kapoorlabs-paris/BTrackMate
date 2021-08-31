package fiji.plugin.globalbtrackmate.action;

import fiji.plugin.globalbtrackmate.Logger;

public abstract class AbstractTMAction implements TrackMateAction {

	protected Logger logger = Logger.VOID_LOGGER;

	@Override
	public void setLogger(final Logger logger) {
		this.logger = logger;
	}
}
