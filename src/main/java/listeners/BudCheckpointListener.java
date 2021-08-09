package listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import pluginTools.InteractiveBud;
import utility.SavePink;

public class BudCheckpointListener implements ActionListener {

	final InteractiveBud parent;

	public BudCheckpointListener(final InteractiveBud parent) {

		this.parent = parent;
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		SavePink pinkies = new SavePink(parent);
		pinkies.Saver();

	}

}
