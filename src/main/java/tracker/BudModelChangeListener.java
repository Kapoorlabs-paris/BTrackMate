package tracker;

import java.util.EventListener;
public interface BudModelChangeListener extends EventListener {

	



		/**
		 * This notification is fired when a {@link Model} has been changed.
		 * 
		 * @param event
		 *            the {@link ModelChangeEvent}.
		 */
		public void modelChanged(final BudModelChangeEvent event);

	}

	

