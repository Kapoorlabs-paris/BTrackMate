package listeners;

import java.awt.TextComponent;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;

import pluginTools.InteractiveBud;

public class BTrackAutoEndListener implements TextListener {

	final InteractiveBud parent;

	public BTrackAutoEndListener(final InteractiveBud parent) {

		this.parent = parent;
	}

	@Override
	public void textValueChanged(TextEvent e) {

		final TextComponent tc = (TextComponent) e.getSource();
		String s = tc.getText();

		if (s.length() > 0)
			parent.AutoendTime = Integer.parseInt(s);

	}

}
