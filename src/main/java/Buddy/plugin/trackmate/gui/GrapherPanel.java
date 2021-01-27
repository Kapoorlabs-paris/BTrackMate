package Buddy.plugin.trackmate.gui;

import Buddy.plugin.trackmate.TrackMate;
import Buddy.plugin.trackmate.features.BCellobjectFeatureGrapher;
import Buddy.plugin.trackmate.features.EdgeFeatureGrapher;
import Buddy.plugin.trackmate.features.TrackFeatureGrapher;
import Buddy.plugin.trackmate.features.edges.EdgeTimeLocationAnalyzer;
import Buddy.plugin.trackmate.features.track.TrackIndexAnalyzer;
import Buddy.plugin.trackmate.gui.panels.ActionListenablePanel;
import Buddy.plugin.trackmate.gui.panels.components.FeaturePlotSelectionPanel;
import budDetector.BCellobject;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import org.jgrapht.graph.DefaultWeightedEdge;

public class GrapherPanel extends ActionListenablePanel
{

	private static final ImageIcon BCellobject_ICON = new ImageIcon( GrapherPanel.class.getResource( "images/Icon1_print_transparency.png" ) );

	private static final ImageIcon EDGE_ICON = new ImageIcon( GrapherPanel.class.getResource( "images/Icon2_print_transparency.png" ) );

	private static final ImageIcon TRACK_ICON = new ImageIcon( GrapherPanel.class.getResource( "images/Icon3b_print_transparency.png" ) );

	public static final ImageIcon BCellobject_ICON_64x64;

	public static final ImageIcon EDGE_ICON_64x64;

	public static final ImageIcon TRACK_ICON_64x64;

	static
	{
		final Image image1 = BCellobject_ICON.getImage();
		final Image newimg1 = image1.getScaledInstance( 32, 32, java.awt.Image.SCALE_SMOOTH );
		BCellobject_ICON_64x64 = new ImageIcon( newimg1 );

		final Image image2 = EDGE_ICON.getImage();
		final Image newimg2 = image2.getScaledInstance( 32, 32, java.awt.Image.SCALE_SMOOTH );
		EDGE_ICON_64x64 = new ImageIcon( newimg2 );

		final Image image3 = TRACK_ICON.getImage();
		final Image newimg3 = image3.getScaledInstance( 32, 32, java.awt.Image.SCALE_SMOOTH );
		TRACK_ICON_64x64 = new ImageIcon( newimg3 );
	}

	private static final long serialVersionUID = 1L;

	private final TrackMate trackmate;

	private final JPanel panelBCellobject;

	private final JPanel panelEdges;

	private final JPanel panelTracks;

	private FeaturePlotSelectionPanel BCellobjectFeatureSelectionPanel;

	private FeaturePlotSelectionPanel edgeFeatureSelectionPanel;

	private FeaturePlotSelectionPanel trackFeatureSelectionPanel;

	/*
	 * CONSTRUCTOR
	 */

	public GrapherPanel( final TrackMate trackmate )
	{
		this.trackmate = trackmate;

		setLayout( new BorderLayout( 0, 0 ) );

		final JTabbedPane tabbedPane = new JTabbedPane( SwingConstants.TOP );
		add( tabbedPane, BorderLayout.CENTER );

		panelBCellobject = new JPanel();
		tabbedPane.addTab( "BCellobjects", BCellobject_ICON_64x64, panelBCellobject, null );
		panelBCellobject.setLayout( new BorderLayout( 0, 0 ) );

		panelEdges = new JPanel();
		tabbedPane.addTab( "Links", EDGE_ICON_64x64, panelEdges, null );
		panelEdges.setLayout( new BorderLayout( 0, 0 ) );

		panelTracks = new JPanel();
		tabbedPane.addTab( "Tracks", TRACK_ICON_64x64, panelTracks, null );
		panelTracks.setLayout( new BorderLayout( 0, 0 ) );

		refresh();
	}

	public void refresh()
	{
		// regen BCellobject features
		panelBCellobject.removeAll();
		final Collection< String > BCellobjectFeatures = trackmate.getModel().getFeatureModel().getBCellobjectFeatures();
		final Map< String, String > BCellobjectFeatureNames = trackmate.getModel().getFeatureModel().getBCellobjectFeatureNames();
		BCellobjectFeatureSelectionPanel = new FeaturePlotSelectionPanel( BCellobject.POSITION_T, BCellobjectFeatures, BCellobjectFeatureNames );
		panelBCellobject.add( BCellobjectFeatureSelectionPanel );
		BCellobjectFeatureSelectionPanel.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent arg0 )
			{
				BCellobjectFeatureSelectionPanel.setEnabled( false );
				new Thread( "TrackMate plot BCellobject features thread" )
				{
					@Override
					public void run()
					{
						plotBCellobjectFeatures();
						BCellobjectFeatureSelectionPanel.setEnabled( true );
					}
				}.start();
			}
		} );

		// regen edge features
		panelEdges.removeAll();
		final Collection< String > edgeFeatures = trackmate.getModel().getFeatureModel().getEdgeFeatures();
		final Map< String, String > edgeFeatureNames = trackmate.getModel().getFeatureModel().getEdgeFeatureNames();
		edgeFeatureSelectionPanel = new FeaturePlotSelectionPanel( EdgeTimeLocationAnalyzer.TIME, edgeFeatures, edgeFeatureNames );
		panelEdges.add( edgeFeatureSelectionPanel );
		edgeFeatureSelectionPanel.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent arg0 )
			{
				edgeFeatureSelectionPanel.setEnabled( false );
				new Thread( "TrackMate plot edge features thread" )
				{
					@Override
					public void run()
					{
						plotEdgeFeatures();
						edgeFeatureSelectionPanel.setEnabled( true );
					}
				}.start();
			}
		} );

		// regen trak features
		panelTracks.removeAll();
		final Collection< String > trackFeatures = trackmate.getModel().getFeatureModel().getTrackFeatures();
		final Map< String, String > trackFeatureNames = trackmate.getModel().getFeatureModel().getTrackFeatureNames();
		trackFeatureSelectionPanel = new FeaturePlotSelectionPanel( TrackIndexAnalyzer.TRACK_INDEX, trackFeatures, trackFeatureNames );
		panelTracks.add( trackFeatureSelectionPanel );
		trackFeatureSelectionPanel.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent arg0 )
			{
				trackFeatureSelectionPanel.setEnabled( false );
				new Thread( "TrackMate plot track features thread" )
				{
					@Override
					public void run()
					{
						plotTrackFeatures();
						trackFeatureSelectionPanel.setEnabled( true );
					}
				}.start();
			}
		} );
	}

	private void plotBCellobjectFeatures()
	{
		final String xFeature = BCellobjectFeatureSelectionPanel.getXKey();
		final Set< String > yFeatures = BCellobjectFeatureSelectionPanel.getYKeys();
		// Collect only the BCellobjects that are in tracks
		final List< BCellobject > BCellobjects = new ArrayList< >( trackmate.getModel().getBCellobjects().getNBCellobjects(  ) );
		for ( final Integer trackID : trackmate.getModel().getTrackModel().trackIDs( true ) )
		{
			BCellobjects.addAll( trackmate.getModel().getTrackModel().trackBCellobjects( trackID ) );
		}
		final BCellobjectFeatureGrapher grapher = new BCellobjectFeatureGrapher( xFeature, yFeatures, BCellobjects, trackmate.getModel() );
		grapher.render();
	}

	private void plotEdgeFeatures()
	{
		// Collect edges in filtered tracks
		final List< DefaultWeightedEdge > edges = new ArrayList< >();
		for ( final Integer trackID : trackmate.getModel().getTrackModel().trackIDs( true ) )
		{
			edges.addAll( trackmate.getModel().getTrackModel().trackEdges( trackID ) );
		}
		// Prepare grapher
		final String xFeature = edgeFeatureSelectionPanel.getXKey();
		final Set< String > yFeatures = edgeFeatureSelectionPanel.getYKeys();
		final EdgeFeatureGrapher grapher = new EdgeFeatureGrapher( xFeature, yFeatures, edges, trackmate.getModel() );
		grapher.render();
	}

	private void plotTrackFeatures()
	{
		// Prepare grapher
		final String xFeature = trackFeatureSelectionPanel.getXKey();
		final Set< String > yFeatures = trackFeatureSelectionPanel.getYKeys();
		final TrackFeatureGrapher grapher = new TrackFeatureGrapher( xFeature, yFeatures, trackmate.getModel() );
		grapher.render();
	}

}
