package globalbtrackfileListeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFileChooser;

import pluginTools.InteractiveBud;

public class BTrackSaveDirectoryListener implements ActionListener {

	InteractiveBud parent;

	public BTrackSaveDirectoryListener(InteractiveBud parent) {

		this.parent = parent;

	}

	@Override
	public void actionPerformed(final ActionEvent arg0) {

		parent.chooserA = new JFileChooser();
		if (parent.saveFile == null)
			parent.chooserA.setCurrentDirectory(new java.io.File("."));
		else
			parent.chooserA.setCurrentDirectory(parent.saveFile);

		parent.chooserA.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		//

		//
		if (parent.chooserA.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
			System.out.println("getCurrentDirectory(): " + parent.chooserA.getCurrentDirectory());
			System.out.println("getSelectedFile() : " + parent.chooserA.getSelectedFile());
			parent.saveFile = parent.chooserA.getSelectedFile();
		} else {
			System.out.println("No Selection ");
		}

		parent.Batchbutton.setEnabled(true);
	}
}
