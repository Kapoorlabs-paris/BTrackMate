package listeners;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import fiji.plugin.globalbtrack.gui.components.LoadDualImage;
import fiji.plugin.globalbtrack.gui.descriptors.BTMStartDialogDescriptor;
import globalbtrackfileListeners.ChooseMaskSegMap;
import globalbtrackfileListeners.ChooseSegMap;

public class BTrackGo3DMaskFLListener implements ItemListener {

	public final BTMStartDialogDescriptor parent;

	public BTrackGo3DMaskFLListener(final BTMStartDialogDescriptor parent) {

		this.parent = parent;
	}

	@Override
	public void itemStateChanged(ItemEvent e) {

		if (e.getStateChange() == ItemEvent.SELECTED) {

			parent.panelFirst.remove(parent.Panelfile);
			parent.Panelfile.removeAll();
			parent.Panelfile.validate();
			parent.Panelfile.repaint();

			parent.panelFirst.validate();
			parent.panelFirst.repaint();

			parent.panelFirst.add(parent.Panelfile, new GridBagConstraints(0, 7, 3, 1, 0.0, 0.0,
					GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, parent.insets, 0, 0));
			GridBagConstraints gbcSeg = new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0, GridBagConstraints.WEST,
					GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0);
			GridBagConstraints gbcMask = new GridBagConstraints(3, 0, 3, 1, 0.0, 0.0, GridBagConstraints.WEST,
					GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0);
			LoadDualImage segmentation = new LoadDualImage(parent.chooseMaskSegstring, parent.blankimageNames, gbcSeg,
					gbcMask);
			parent.Panelfile = segmentation.TwoChannelOption();

			segmentation.ChooseImage.addActionListener(new ChooseSegMap(parent, segmentation.ChooseImage));
			segmentation.ChoosesecImage.addActionListener(new ChooseMaskSegMap(parent, segmentation.ChoosesecImage));
			parent.DoMask = true;
			parent.NoMask = false;
			parent.Panelfile.validate();
			parent.Panelfile.repaint();

			parent.panelFirst.validate();
			parent.panelFirst.repaint();
			parent.Cardframe.pack();
		}

		else if (e.getStateChange() == ItemEvent.DESELECTED) {

			parent.DoMask = false;
			parent.NoMask = true;

		}

	}

}
