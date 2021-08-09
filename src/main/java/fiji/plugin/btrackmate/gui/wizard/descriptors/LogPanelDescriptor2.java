package fiji.plugin.btrackmate.gui.wizard.descriptors;

import fiji.plugin.btrackmate.gui.components.LogPanel;
import fiji.plugin.btrackmate.gui.wizard.WizardPanelDescriptor;

public class LogPanelDescriptor2 extends WizardPanelDescriptor {

	public static final String KEY = "LogPanel";

	public LogPanelDescriptor2(final LogPanel logPanel) {
		super(KEY);
		this.targetPanel = logPanel;
	}
}
