package Buddy.plugin.trackmate.action;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.swing.ImageIcon;

import org.scijava.plugin.Plugin;

import Buddy.plugin.trackmate.FeatureModel;
import Buddy.plugin.trackmate.Model;
import Buddy.plugin.trackmate.SelectionModel;
import Buddy.plugin.trackmate.TrackMate;
import Buddy.plugin.trackmate.gui.TrackMateGUIController;
import Buddy.plugin.trackmate.gui.TrackMateWizard;
import budDetector.BCellobject;
import ij.WindowManager;
import ij.measure.ResultsTable;
import ij.text.TextPanel;
import ij.text.TextWindow;

public class ExportAllBCellobjectsStatsAction extends AbstractTMAction
{

	public static final ImageIcon ICON = new ImageIcon( TrackMateWizard.class.getResource( "images/calculator.png" ) );

	public static final String NAME = "Export all BCellobjects statistics";

	public static final String KEY = "EXPORT_ALL_BCellobjectS_STATS";

	public static final String INFO_TEXT = "<html>"
			+ "Compute and export the statistics of all BCellobjects to ImageJ results table."
			+ "The numerical features of all visible BCellobjects are exported, "
			+ "regardless of whether they are in a track or not."
			+ "</html>";

	private static final String ID_COLUMN = "ID";

	private ResultsTable BCellobjectTable;

	private final SelectionModel selectionModel;

	private final static String BCellobject_TABLE_NAME = "All BCellobjects statistics";

	public ExportAllBCellobjectsStatsAction( final SelectionModel selectionModel )
	{
		this.selectionModel = selectionModel;
	}

	@Override
	public void execute( final TrackMate trackmate )
	{
		logger.log( "Exporting all BCellobjects statistics.\n" );

		// Model
		final Model model = trackmate.getModel();
		final FeatureModel fm = model.getFeatureModel();

		// Export BCellobjects
		final Collection< String > BCellobjectFeatures = trackmate.getModel().getFeatureModel().getBCellobjectFeatures();

		// Create table
		this.BCellobjectTable = new ResultsTable();

		final Iterable< BCellobject > iterable = model.getBCellobjects().iterable( true );
		for ( final BCellobject BCellobject : iterable )
		{
			BCellobjectTable.incrementCounter();
			BCellobjectTable.addLabel( BCellobject.getName() );
			BCellobjectTable.addValue( ID_COLUMN, "" + BCellobject.ID() );

			// Check if it is in a track.
			final Integer trackID = model.getTrackModel().trackIDOf( BCellobject );
			if ( null != trackID )
				BCellobjectTable.addValue( "TRACK_ID", "" + trackID.intValue() );
			else
				BCellobjectTable.addValue( "TRACK_ID", "None" );

			for ( final String feature : BCellobjectFeatures )
			{
				final Double val = BCellobject.getFeature( feature );
				if ( null == val )
				{
					BCellobjectTable.addValue( feature, "None" );
				}
				else
				{
					if ( fm.getBCellobjectFeatureIsInt().get( feature ).booleanValue() )
					{
						BCellobjectTable.addValue( feature, "" + val.intValue() );
					}
					else
					{
						BCellobjectTable.addValue( feature, val.doubleValue() );
					}
				}
			}
		}
		logger.log( " Done.\n" );

		// Show tables
		BCellobjectTable.show( BCellobject_TABLE_NAME  );

		// Hack to make the results tables in sync with selection model.
		if ( null != selectionModel )
		{

			/*
			 * BCellobject table listener.
			 */

			final TextWindow BCellobjectTableWindow = ( TextWindow ) WindowManager.getWindow( BCellobject_TABLE_NAME );
			final TextPanel BCellobjectTableTextPanel = BCellobjectTableWindow.getTextPanel();
			BCellobjectTableTextPanel.addMouseListener( new MouseAdapter()
			{

				@Override
				public void mouseReleased( final MouseEvent e )
				{
					final int selStart = BCellobjectTableTextPanel.getSelectionStart();
					final int selEnd = BCellobjectTableTextPanel.getSelectionEnd();
					if ( selStart < 0 || selEnd < 0 )
						return;

					final int minLine = Math.min( selStart, selEnd );
					final int maxLine = Math.max( selStart, selEnd );
					final Set< BCellobject > BCellobjects = new HashSet<>();
					for ( int row = minLine; row <= maxLine; row++ )
					{
						final int BCellobjectID = Integer.parseInt( BCellobjectTable.getStringValue( ID_COLUMN, row ) );
						final BCellobject BCellobject = model.getBCellobjects().search( BCellobjectID );
						if ( null != BCellobject )
							BCellobjects.add( BCellobject );
					}
					selectionModel.clearSelection();
					selectionModel.addBCellobjectToSelection( BCellobjects );
				}
			} );
		}
	}

	/**
	 * Returns the results table containing the BCellobject statistics, or
	 * <code>null</code> if the {@link #execute(TrackMate)} method has not been
	 * called.
	 *
	 * @return the results table containing the BCellobject statistics.
	 */
	public ResultsTable getBCellobjectTable()
	{
		return BCellobjectTable;
	}

	@Plugin( type = TrackMateActionFactory.class )
	public static class Factory implements TrackMateActionFactory
	{

		@Override
		public String getInfoText()
		{
			return INFO_TEXT;
		}

		@Override
		public String getKey()
		{
			return KEY;
		}

		@Override
		public TrackMateAction create( final TrackMateGUIController controller )
		{
			return new ExportAllBCellobjectsStatsAction( controller.getSelectionModel() );
		}

		@Override
		public ImageIcon getIcon()
		{
			return ICON;
		}

		@Override
		public String getName()
		{
			return NAME;
		}
	}

}
