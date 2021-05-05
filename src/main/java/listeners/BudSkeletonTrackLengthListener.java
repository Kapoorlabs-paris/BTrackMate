package listeners;

import java.awt.TextComponent;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;

import fiji.plugin.btrack.gui.components.CovistoKalmanPanel;
import pluginTools.InteractiveBud;

public class BudSkeletonTrackLengthListener implements TextListener {

	public InteractiveBud parent;
	
	public BudSkeletonTrackLengthListener(final InteractiveBud parent) {
		
		this.parent = parent;
	}

	@Override
	public void textValueChanged(TextEvent e) {
		final TextComponent tc = (TextComponent)e.getSource();
	    String s = tc.getText();
	   
	    if (s.length() > 0)
	    	CovistoKalmanPanel.trackduration = Integer.parseInt(s);
		
		
	}
	
}