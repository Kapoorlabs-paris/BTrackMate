package listeners;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import pluginTools.InteractiveBud;
import pluginTools.InteractiveBud.ValueChange;

public class BudMouseListener implements MouseMotionListener {
	final InteractiveBud parent;
	final ValueChange change;

	public BudMouseListener(final InteractiveBud parent, final ValueChange change) {
		this.parent = parent;
		this.change = change;
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {

		parent.updatePreview(change);

	}

	@Override
	public void mouseMoved(MouseEvent arg0) {

	}

}