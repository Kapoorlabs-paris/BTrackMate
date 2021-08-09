package listeners;

import java.awt.TextComponent;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;

import pluginTools.InteractiveBud;

public class BudTrackidListener implements TextListener {

	final InteractiveBud parent;

	public BudTrackidListener(final InteractiveBud parent) {

		this.parent = parent;

	}

	@Override
	public void textValueChanged(TextEvent e) {
		final TextComponent tc = (TextComponent) e.getSource();
		String s = tc.getText();

		parent.selectedID = s;

	}

}
