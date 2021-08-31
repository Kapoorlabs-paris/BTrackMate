package fiji.plugin.globalbtrackmate.gui.wizard.descriptors;

import fiji.plugin.globalbtrackmate.gui.components.LogPanel;
import fiji.plugin.globalbtrackmate.gui.wizard.WizardPanelDescriptor;

public class LogPanelDescriptor2 extends WizardPanelDescriptor {

	public static final String KEY = "LogPanel";

	public LogPanelDescriptor2(final LogPanel logPanel) {
		super(KEY);
		this.targetPanel = logPanel;
	}
}
