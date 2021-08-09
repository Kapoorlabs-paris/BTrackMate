package listeners;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import pluginTools.InteractiveBud;
import pluginTools.InteractiveBud.ValueChange;

public class BudStandardMouseListener implements MouseListener {
	final InteractiveBud parent;
	final ValueChange change;

	public BudStandardMouseListener(final InteractiveBud parent, final ValueChange change) {
		this.parent = parent;
		this.change = change;
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {

		parent.updatePreview(change);

	}

	@Override
	public void mousePressed(MouseEvent arg0) {

	}

	@Override
	public void mouseExited(MouseEvent arg0) {

	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {

	}
}
