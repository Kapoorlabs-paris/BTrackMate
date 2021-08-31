package listeners;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import fiji.plugin.globalbtrack.gui.components.LoadSingleImage;
import fiji.plugin.globalbtrack.gui.descriptors.BTStartDialogDescriptor;
import fileListeners.ChooseBudSegAMap;

public class BTrackGoFreeFlListener implements ItemListener {

	public final BTStartDialogDescriptor parent;

	public BTrackGoFreeFlListener(final BTStartDialogDescriptor parent) {

		this.parent = parent;
	}

	@Override
	public void itemStateChanged(ItemEvent e) {

		if (e.getStateChange() == ItemEvent.SELECTED) {

			parent.panelFirst.remove(parent.Panelfile);
			parent.panelFirst.validate();
			parent.panelFirst.repaint();

			GridBagConstraints gbc = new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0, GridBagConstraints.WEST,
					GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0);
			LoadSingleImage segmentation = new LoadSingleImage(parent.chooseBudSegstring, parent.blankimageNames, gbc);
			parent.Panelfile = segmentation.SingleChannelOption();
			segmentation.ChooseImage.addActionListener(new ChooseBudSegAMap(parent, segmentation.ChooseImage));

			parent.panelFirst.add(parent.Panelfile, new GridBagConstraints(0, 7, 3, 1, 0.0, 0.0,
					GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, parent.insets, 0, 0));

			parent.Panelfile.validate();
			parent.Panelfile.repaint();

			parent.panelFirst.validate();
			parent.panelFirst.repaint();
			parent.Cardframe.pack();
		}

		else if (e.getStateChange() == ItemEvent.DESELECTED) {

		}

	}

}
