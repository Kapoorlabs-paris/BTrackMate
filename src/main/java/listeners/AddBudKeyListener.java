package listeners;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import pluginTools.InteractiveBud;

public class AddBudKeyListener implements KeyListener {

	InteractiveBud parent;

	public AddBudKeyListener(InteractiveBud parent) {

		this.parent = parent;

	}

	@Override
	public void keyTyped(KeyEvent e) {

		if (e.getKeyChar() == 'a')

			parent.AddDot = "a";

	}

	@Override
	public void keyPressed(KeyEvent e) {

		if (e.getKeyChar() == 'a')

			parent.AddDot = "a";

	}

	@Override
	public void keyReleased(KeyEvent e) {

		if (e.getKeyChar() == 'a')

			parent.AddDot = "a";

	}

}
