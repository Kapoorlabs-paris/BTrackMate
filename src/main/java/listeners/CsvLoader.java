package listeners;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import fiji.plugin.globalbtrack.gui.components.LoadSingleImage;
import fiji.plugin.globalbtrack.gui.descriptors.BTMStartDialogDescriptor;
import fileListeners.ChooseSegMap;

public class CsvLoader implements ItemListener {

	public final BTMStartDialogDescriptor parent;

	public CsvLoader(final BTMStartDialogDescriptor parent) {

		this.parent = parent;
	}

	@Override
	public void itemStateChanged(ItemEvent e) {

		if (e.getStateChange() == ItemEvent.SELECTED) {

			parent.panelFirst.remove(parent.Panelfile);
			parent.Panelfile.removeAll();
			parent.Panelfile.validate();

			parent.panelFirst.remove(parent.FreeMode);
			parent.panelFirst.remove(parent.MaskMode);
			parent.panelFirst.validate();
			parent.panelFirst.repaint();

			parent.Paneldone.add(parent.Checkpointbutton, new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0,
					GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));

			parent.Panelfile.validate();
			parent.Panelfile.repaint();

			parent.panelFirst.validate();
			parent.panelFirst.repaint();
			parent.Cardframe.pack();
		}

		else if (e.getStateChange() == ItemEvent.DESELECTED) {

			parent.panelFirst.remove(parent.Panelfile);

			parent.Paneldone.remove(parent.Checkpointbutton);

			parent.Paneldone.validate();
			parent.Paneldone.repaint();

			parent.panelFirst.validate();
			parent.panelFirst.repaint();

			parent.Cardframe.pack();
		}

	}

}
