package listeners;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import fiji.plugin.globalbtrack.gui.components.LoadSingleImage;
import fiji.plugin.globalbtrack.gui.descriptors.BTStartDialogDescriptor;
import globalbtrackfileListeners.ChooseBudOrigMap;

public class BTrackGoBudListener implements ItemListener {

	public final BTStartDialogDescriptor parent;

	public BTrackGoBudListener(final BTStartDialogDescriptor parent) {

		this.parent = parent;
	}

	@Override
	public void itemStateChanged(ItemEvent e) {

		if (e.getStateChange() == ItemEvent.SELECTED) {

			parent.panelFirst.remove(parent.Panelfileoriginal);
			parent.panelFirst.validate();
			parent.panelFirst.repaint();

			GridBagConstraints gbc = new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0, GridBagConstraints.NORTH,
					GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0);

			LoadSingleImage original = new LoadSingleImage(parent.chooseoriginalbudfilestring, parent.blankimageNames,
					gbc);

			parent.Panelfileoriginal = original.SingleChannelOption();

			parent.panelFirst.add(parent.Panelfileoriginal, new GridBagConstraints(0, 2, 3, 1, 0.0, 0.0,
					GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, parent.insets, 0, 0));

			original.ChooseImage.addActionListener(new ChooseBudOrigMap(parent, original.ChooseImage));
			parent.Panelfileoriginal.validate();
			parent.Panelfileoriginal.repaint();
			parent.panelFirst.validate();
			parent.panelFirst.repaint();

		}

		else if (e.getStateChange() == ItemEvent.DESELECTED) {

			parent.Panelfileoriginal.validate();
			parent.Panelfileoriginal.repaint();
			parent.panelFirst.validate();
			parent.panelFirst.repaint();

		}

	}

}
