package listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import pluginTools.InteractiveBud;


public class BudSaveBatchListener implements ActionListener {

	
	
	final InteractiveBud parent;
	
	public BudSaveBatchListener(final InteractiveBud parent) {
		
		this.parent = parent;
		
	}
	
	@Override
	public void actionPerformed(final ActionEvent arg0) {
		
		
		CreateINIfile recordparam = new CreateINIfile(parent);
		recordparam.RecordParent();
		
		
	}
	
	
}