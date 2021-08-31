package listeners;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import fiji.plugin.globalbtrack.gui.components.LoadSingleImage;
import fiji.plugin.globalbtrack.gui.descriptors.BTMStartDialogDescriptor;
import fileListeners.ChooseSegMap;

public class ImageLoader implements ItemListener {

	public final BTMStartDialogDescriptor parent;

	public ImageLoader(final BTMStartDialogDescriptor parent) {

		this.parent = parent;
	}

	@Override
	public void itemStateChanged(ItemEvent e) {

		if (e.getStateChange() == ItemEvent.SELECTED) {

			parent.panelFirst.remove(parent.Panelfile);
			parent.Panelfile.removeAll();
			parent.Panelfile.validate();
			parent.Panelfile.repaint();
			parent.Paneldone.remove(parent.Checkpointbutton);
			parent.panelFirst.validate();
			parent.panelFirst.repaint();

			parent.panelFirst.add(parent.FreeMode, new GridBagConstraints(0, 3, 3, 1, 0.0, 0.0, GridBagConstraints.WEST,
					GridBagConstraints.HORIZONTAL, parent.insets, 0, 0));
			parent.panelFirst.add(parent.MaskMode, new GridBagConstraints(0, 4, 3, 1, 0.0, 0.0, GridBagConstraints.WEST,
					GridBagConstraints.HORIZONTAL, parent.insets, 0, 0));

			GridBagConstraints gbc = new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0, GridBagConstraints.NORTH,
					GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0);
			// Listeneres
			LoadSingleImage segmentation = new LoadSingleImage(parent.chooseSegstring, parent.blankimageNames, gbc);
			parent.Panelfile = segmentation.SingleChannelOption();
			segmentation.ChooseImage.addActionListener(new ChooseSegMap(parent, segmentation.ChooseImage));

			parent.panelFirst.add(parent.Panelfile, new GridBagConstraints(0, 7, 3, 1, 0.0, 0.0,
					GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, parent.insets, 0, 0));

			parent.Panelfile.validate();
			parent.Panelfile.repaint();

			parent.panelFirst.validate();
			parent.panelFirst.repaint();
			parent.Cardframe.pack();
		}

		else if (e.getStateChange() == ItemEvent.DESELECTED) {

			parent.panelFirst.remove(parent.Panelfile);
			parent.Panelfile.removeAll();
			parent.Panelfile.validate();
			parent.Panelfile.repaint();
			parent.panelFirst.validate();
			parent.panelFirst.repaint();

			parent.Cardframe.pack();
		}

	}

}
