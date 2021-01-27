package Buddy.plugin.trackmate.visualization.trackscheme;

import static Buddy.plugin.trackmate.gui.TrackMateWizard.FONT;
import static Buddy.plugin.trackmate.gui.TrackMateWizard.SMALL_FONT;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import Buddy.plugin.trackmate.FeatureModel;
import Buddy.plugin.trackmate.Model;
import Buddy.plugin.trackmate.SelectionChangeEvent;
import Buddy.plugin.trackmate.SelectionChangeListener;
import Buddy.plugin.trackmate.SelectionModel;
import Buddy.plugin.trackmate.features.BCellobjectFeatureGrapher;
import Buddy.plugin.trackmate.util.OnRequestUpdater;
import Buddy.plugin.trackmate.util.OnRequestUpdater.Refreshable;
import Buddy.plugin.trackmate.util.TMUtils;
import budDetector.BCellobject;
import ij.measure.ResultsTable;

public class InfoPane extends JPanel implements SelectionChangeListener
{

	private static final long serialVersionUID = -1L;

	private FeaturePlotSelectionPanel featureSelectionPanel;

	private JTable table;

	private JScrollPane scrollTable;

	private final boolean doHighlightSelection = true;

	private final Model model;

	private final SelectionModel selectionModel;

	/**
	 * A copy of the last BCellobject collection highlighted in this infopane, sorted
	 * by frame order.
	 */
	private Collection< BCellobject > BCellobjectSelection;

	private final OnRequestUpdater updater;

	/** The table headers, taken from BCellobject feature names. */
	private final String[] headers;

	/*
	 * CONSTRUCTOR
	 */

	/**
	 * Creates a new Info pane that displays information on the current BCellobject
	 * selection in a table.
	 *
	 * @param model
	 *            the {@link Model} from which the BCellobject collection is taken.
	 * @param selectionModel
	 *            the {@link SelectionModel} from which we read what to show in
	 *            the table.
	 */
	public InfoPane( final Model model, final SelectionModel selectionModel )
	{
		this.model = model;
		this.selectionModel = selectionModel;
		final List< String > features = new ArrayList< >( model.getFeatureModel().getBCellobjectFeatures() );
		final Map< String, String > featureNames = model.getFeatureModel().getBCellobjectFeatureShortNames();
		final List< String > headerList = TMUtils.getArrayFromMaping( features, featureNames );
		headerList.add( 0, "Track ID" );
		headers = headerList.toArray( new String[] {} );

		this.updater = new OnRequestUpdater( new Refreshable()
		{
			@Override
			public void refresh()
			{
				SwingUtilities.invokeLater( new Runnable()
				{
					@Override
					public void run()
					{
						update();
					}
				} );
			}
		} );
		// Add a listener to ensure we remove this panel from the listener list
		// of the model
		addAncestorListener( new AncestorListener()
		{
			@Override
			public void ancestorRemoved( final AncestorEvent event )
			{
				InfoPane.this.selectionModel.removeSelectionChangeListener( InfoPane.this );
			}

			@Override
			public void ancestorMoved( final AncestorEvent event )
			{}

			@Override
			public void ancestorAdded( final AncestorEvent event )
			{}
		} );
		selectionModel.addSelectionChangeListener( this );
		init();
	}

	/*
	 * PUBLIC METHODS
	 */

	@Override
	public void selectionChanged( final SelectionChangeEvent event )
	{
		// Echo changed in a different thread for performance
		SwingUtilities.invokeLater( new Runnable()
		{
			@Override
			public void run()
			{
				highlightBCellobjects( selectionModel.getBCellobjectSelection() );
			}
		} );
	}

	/**
	 * Show the given BCellobject selection as a table displaying their individual
	 * features.
	 */
	private void highlightBCellobjects( final Collection< BCellobject > BCellobjects )
	{
		if ( !doHighlightSelection )
			return;
		BCellobjectSelection = BCellobjects;
		if ( BCellobjects.size() == 0 )
		{
			// Clear display of the table, but not the table.
			final DefaultTableModel tableModel = ( DefaultTableModel ) table.getModel();
			tableModel.setRowCount( 0 );
			tableModel.setColumnIdentifiers( new String[] { "ø" } );
			tableModel.setColumnCount( 1 );
			table.getColumnModel().getColumn( 0 ).setPreferredWidth( 10 );
			return;
		}

		// Copy and sort selection by frame
		updater.doUpdate();
	}

	private void update()
	{
		/*
		 * Sort using a list; TreeSet does not allow several identical frames,
		 * which is likely to happen.
		 */
		final List< BCellobject > sortedBCellobjects = new ArrayList< >( BCellobjectSelection );
		Collections.sort( sortedBCellobjects, BCellobject.frameComparator );

		@SuppressWarnings( "serial" )
		final DefaultTableModel dm = new DefaultTableModel()
		{ // Un-editable model
			@Override
			public boolean isCellEditable( final int row, final int column )
			{
				return false;
			}
		};

		final List< String > features = new ArrayList< >( model.getFeatureModel().getBCellobjectFeatures() );
		for ( final BCellobject BCellobject : sortedBCellobjects )
		{
			if ( null == BCellobject )
			{
				continue;
			}
			final Object[] columnData = new Object[ features.size() + 1 ];
			columnData[ 0 ] = String.format( "%d", model.getTrackModel().trackIDOf( BCellobject ) );
			for ( int i = 1; i < columnData.length; i++ )
			{
				final String feature = features.get( i - 1 );
				final Double feat = BCellobject.getFeature( feature );
				if ( null == feat )
				{
					columnData[ i ] = "";
				}
				else if ( model.getFeatureModel().getBCellobjectFeatureIsInt().get( feature ).booleanValue() )
				{
					columnData[ i ] = "" + feat.intValue();
				}
				else
				{
					columnData[ i ] = String.format( "%.4g", feat.doubleValue() );
				}
			}
			dm.addColumn( BCellobject.toString(), columnData );
		}
		table.setModel( dm );

		// Tune look
		@SuppressWarnings( "serial" )
		final DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer()
		{
			@Override
			public boolean isOpaque()
			{
				return false;
			}

			@Override
			public Color getBackground()
			{
				return Color.BLUE;
			}
		};
		headerRenderer.setBackground( Color.RED );
		headerRenderer.setFont( FONT );

		final DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setOpaque( false );
		renderer.setHorizontalAlignment( SwingConstants.RIGHT );
		renderer.setFont( SMALL_FONT );

		final FontMetrics fm = table.getGraphics().getFontMetrics( FONT );
		for ( int i = 0; i < table.getColumnCount(); i++ )
		{
			table.setDefaultRenderer( table.getColumnClass( i ), renderer );
			// Set width auto
			table.getColumnModel().getColumn( i ).setMinWidth( ( int ) ( 1.4d * fm.stringWidth( dm.getColumnName( i ) ) ) );
		}
		for ( final Component c : scrollTable.getColumnHeader().getComponents() )
		{
			c.setBackground( getBackground() );
		}
		scrollTable.getColumnHeader().setOpaque( false );
		scrollTable.setVisible( true );
		validate();
	}

	/*
	 * PRIVATE METHODS
	 */

	private void displayPopupMenu( final Point point )
	{
		// Prepare menu
		final JPopupMenu menu = new JPopupMenu( "Selection table" );
		final JMenuItem exportItem = menu.add( "Export to ImageJ table" );
		exportItem.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent arg0 )
			{
				exportTableToImageJ();
			}
		} );
		// Display it
		menu.show( table, ( int ) point.getX(), ( int ) point.getY() );
	}

	private void exportTableToImageJ()
	{
		final ResultsTable lTable = new ResultsTable();
		final FeatureModel fm = model.getFeatureModel();
		final List< String > features = new ArrayList< >( fm.getBCellobjectFeatures() );

		final int ncols = BCellobjectSelection.size();
		final int nrows = headers.length;
		final BCellobject[] BCellobjectArray = BCellobjectSelection.toArray( new BCellobject[] {} );

		/*
		 * Track ID
		 */

		lTable.incrementCounter();
		lTable.setLabel( "TRACK_ID", 0 );
		for ( int i = 0; i < ncols; i++ )
		{
			final BCellobject BCellobject = BCellobjectArray[ i ];
			final Integer trackID = model.getTrackModel().trackIDOf( BCellobject );
			if ( null == trackID )
			{
				lTable.addValue( BCellobject.getName(), "None" );
			}
			else
			{
				lTable.addValue( BCellobject.getName(), "" + trackID.intValue() );
			}
		}

		/*
		 * Other features
		 */

		for ( int j = 0; j < nrows - 1; j++ )
		{
			lTable.incrementCounter();
			final String feature = features.get( j );
			lTable.setLabel( feature, j + 1 );
			for ( int i = 0; i < ncols; i++ )
			{
				final BCellobject BCellobject = BCellobjectArray[ i ];
				final Double val = BCellobject.getFeature( feature );
				if ( val == null )
				{
					lTable.addValue( BCellobject.getName(), "None" );
				}
				else
				{
					if ( fm.getBCellobjectFeatureIsInt().get( feature ) )
					{
						lTable.addValue( BCellobject.getName(), "" + val.intValue() );
					}
					else
					{
						lTable.addValue( BCellobject.getName(), val.doubleValue() );
					}
				}
			}
		}

		lTable.show( "TrackMate Selection" );
	}

	private void init()
	{

		@SuppressWarnings( "serial" )
		final AbstractListModel< String > lm = new AbstractListModel< String >()
		{
			@Override
			public int getSize()
			{
				return headers.length;
			}

			@Override
			public String getElementAt( final int index )
			{
				return headers[ index ];
			}
		};

		table = new JTable();
		table.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
		table.setOpaque( false );
		table.setFont( SMALL_FONT );
		table.setPreferredScrollableViewportSize( new Dimension( 120, 400 ) );
		table.getTableHeader().setOpaque( false );
		table.setSelectionForeground( Color.YELLOW.darker().darker() );
		table.setGridColor( TrackScheme.GRID_COLOR );
		// Init with default content
		final DefaultTableModel tableModel = ( DefaultTableModel ) table.getModel();
		tableModel.setColumnIdentifiers( new String[] { "ø" } );
		tableModel.setColumnCount( 1 );
		table.getColumnModel().getColumn( 0 ).setPreferredWidth( 10 );
		// Listener for popup menu
		table.addMouseListener( new MouseAdapter()
		{
			@Override
			public void mousePressed( final MouseEvent e )
			{
				if ( e.isPopupTrigger() )
					displayPopupMenu( e.getPoint() );
			}

			@Override
			public void mouseReleased( final MouseEvent e )
			{
				if ( e.isPopupTrigger() )
					displayPopupMenu( e.getPoint() );
			}
		} );

		final JList< String > rowHeader = new JList< >( lm );
		rowHeader.setFixedCellHeight( table.getRowHeight() );
		rowHeader.setCellRenderer( new RowHeaderRenderer( table ) );
		rowHeader.setBackground( getBackground() );

		scrollTable = new JScrollPane( table );
		scrollTable.setRowHeaderView( rowHeader );
		scrollTable.getRowHeader().setOpaque( false );
		scrollTable.setOpaque( false );
		scrollTable.getViewport().setOpaque( false );

		final List< String > features = new ArrayList< >( model.getFeatureModel().getBCellobjectFeatures() );
		final Map< String, String > featureNames = model.getFeatureModel().getBCellobjectFeatureShortNames();
		featureSelectionPanel = new FeaturePlotSelectionPanel( BCellobject.POSITION_T, features, featureNames );

		final JSplitPane inner = new JSplitPane( JSplitPane.VERTICAL_SPLIT, scrollTable, featureSelectionPanel );
		inner.setDividerLocation( 200 );
		inner.setResizeWeight( 1.0d );
		inner.setBorder( null );
		setLayout( new BorderLayout() );
		add( inner, BorderLayout.CENTER );

		// Add listener for plot events
		featureSelectionPanel.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				final String xFeature = featureSelectionPanel.getXKey();
				final Set< String > yFeatures = featureSelectionPanel.getYKeys();
				plotSelectionData( xFeature, yFeatures );
			}
		} );

	}

	/**
	 * Reads the content of the current BCellobject selection and plot the selected
	 * features in this {@link InfoPane} for the target BCellobjects.
	 *
	 * @param xFeature
	 *            the feature to use as X axis.
	 * @param yFeatures
	 *            the features to plot as Y axis.
	 */
	private void plotSelectionData( final String xFeature, final Set< String > yFeatures )
	{
		final Set< BCellobject > BCellobjects = selectionModel.getBCellobjectSelection();
		if ( yFeatures.isEmpty() || BCellobjects.isEmpty() ) { return; }

		final BCellobjectFeatureGrapher grapher = new BCellobjectFeatureGrapher( xFeature, yFeatures, BCellobjects, model );
		grapher.render();
	}

	/*
	 * INNER CLASS
	 */

	private class RowHeaderRenderer extends JLabel implements ListCellRenderer< String >
	{

		private static final long serialVersionUID = -1L;

		RowHeaderRenderer( final JTable table )
		{
			final JTableHeader header = table.getTableHeader();
			setOpaque( false );
			setBorder( UIManager.getBorder( "TableHeader.cellBorder" ) );
			setForeground( header.getForeground() );
			setBackground( header.getBackground() );
			setFont( SMALL_FONT.deriveFont( 9.0f ) );
			setHorizontalAlignment( SwingConstants.LEFT );
		}

		@Override
		public Component getListCellRendererComponent( final JList< ? extends String > list, final String value,
				final int index, final boolean isSelected, final boolean cellHasFocus )
		{
			setText( ( value == null ) ? "" : value );
			return this;
		}
	}
}
