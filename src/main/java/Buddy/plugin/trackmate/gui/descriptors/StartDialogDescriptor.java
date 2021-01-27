package Buddy.plugin.trackmate.gui.descriptors;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import Buddy.plugin.trackmate.gui.panels.StartDialogPanel;
import Buddy.plugin.trackmate.Model;
import Buddy.plugin.trackmate.SelectionModel;
import Buddy.plugin.trackmate.Settings;
import Buddy.plugin.trackmate.TrackMate;
import Buddy.plugin.trackmate.features.edges.EdgeAnalyzer;
import Buddy.plugin.trackmate.features.BCellobject.BCellobjectAnalyzerFactory;
import Buddy.plugin.trackmate.features.track.TrackAnalyzer;
import Buddy.plugin.trackmate.gui.TrackMateGUIController;
import Buddy.plugin.trackmate.gui.descriptors.WizardPanelDescriptor;
import Buddy.plugin.trackmate.providers.EdgeAnalyzerProvider;
import Buddy.plugin.trackmate.providers.BCellobjectAnalyzerProvider;
import Buddy.plugin.trackmate.providers.TrackAnalyzerProvider;
import Buddy.plugin.trackmate.visualization.hyperstack.HyperStackDisplayer;
import ij.ImagePlus;
import ij.WindowManager;
import pluginTools.InteractiveBud;

public class StartDialogDescriptor implements WizardPanelDescriptor
{

	private static final String KEY = "Start";

	private final StartDialogPanel panel;

	private final ArrayList< ActionListener > actionListeners = new ArrayList<>();

	private final TrackMateGUIController controller;

	/**
	 * The view that is launched immediately when this descriptor leaves. It
	 * will be used as a central view.
	 */
	private HyperStackDisplayer mainView;

	public StartDialogDescriptor( final TrackMateGUIController controller )
	{
		this.controller = controller;
		this.panel = new StartDialogPanel();
		panel.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent event )
			{
				fireAction( event );
			}
		} );
	}

	/*
	 * METHODS
	 */

	/**
	 * Returns <code>true</code> if the {@link ImagePlus} selected is valid and
	 * can be processed.
	 *
	 * @return a boolean flag.
	 */
	public boolean isImpValid()
	{
		return panel.isImpValid();
	}

	/*
	 * WIZARDPANELDESCRIPTOR METHODS
	 */

	@Override
	public StartDialogPanel getComponent()
	{
		return panel;
	}

	@Override
	public void aboutToDisplayPanel()
	{
		controller.getGUI().setDisplayConfigButtonEnabled( false );
	}

	@Override
	public void displayingPanel()
	{
		ImagePlus imp;
		final TrackMate trackmate = controller.getPlugin();
		if ( null == trackmate.getSettings().imp )
		{
			imp = WindowManager.getCurrentImage();
		}
		else
		{
			panel.echoSettings( trackmate.getModel(), trackmate.getSettings() );
			imp = trackmate.getSettings().imp;
		}
		panel.getFrom( imp );
	}

	@Override
	public void aboutToHidePanel()
	{
		final TrackMate trackmate = controller.getPlugin();
		final Settings settings = trackmate.getSettings();
		final Model model = trackmate.getModel();

		final InteractiveBud parent = trackmate.getParent();
		/*
		 * Get settings and pass them to the trackmate managed by the wizard
		 */

		panel.updateTo( model, settings );
		trackmate.getModel().getLogger().log( settings.toStringImageInfo() );

		/*
		 * Configure settings object with BCellobject, edge and track analyzers as
		 * specified in the providers.
		 */

		settings.clearBCellobjectAnalyzerFactories();
		final BCellobjectAnalyzerProvider BCellobjectAnalyzerProvider = controller.getBCellobjectAnalyzerProvider();
		final List< String > BCellobjectAnalyzerKeys = BCellobjectAnalyzerProvider.getKeys();
		for ( final String key : BCellobjectAnalyzerKeys )
		{
			final BCellobjectAnalyzerFactory< ? > BCellobjectFeatureAnalyzer = BCellobjectAnalyzerProvider.getFactory( key );
			settings.addBCellobjectAnalyzerFactory( BCellobjectFeatureAnalyzer );
		}

		settings.clearEdgeAnalyzers();
		final EdgeAnalyzerProvider edgeAnalyzerProvider = controller.getEdgeAnalyzerProvider();
		final List< String > edgeAnalyzerKeys = edgeAnalyzerProvider.getKeys();
		for ( final String key : edgeAnalyzerKeys )
		{
			final EdgeAnalyzer edgeAnalyzer = edgeAnalyzerProvider.getFactory( key );
			settings.addEdgeAnalyzer( edgeAnalyzer );
		}

		settings.clearTrackAnalyzers();
		final TrackAnalyzerProvider trackAnalyzerProvider = controller.getTrackAnalyzerProvider();
		final List< String > trackAnalyzerKeys = trackAnalyzerProvider.getKeys();
		for ( final String key : trackAnalyzerKeys )
		{
			final TrackAnalyzer trackAnalyzer = trackAnalyzerProvider.getFactory( key );
			settings.addTrackAnalyzer( trackAnalyzer );
		}

		trackmate.getModel().getLogger().log( settings.toStringFeatureAnalyzersInfo() );
		trackmate.computeBCellobjectFeatures( true );
		trackmate.computeEdgeFeatures( true );
		trackmate.computeTrackFeatures( true );

		/*
		 * Launch the ImagePlus view now.
		 */

		// De-register old one, if any.
		if ( mainView != null )
		{
			mainView.clear();
			model.removeModelChangeListener( mainView );
		}

		final SelectionModel selectionModel = controller.getSelectionModel();
		mainView = new HyperStackDisplayer(parent,  model, selectionModel, settings.imp );
		controller.getGuimodel().addView( mainView );
		final Map< String, Object > displaySettings = controller.getGuimodel().getDisplaySettings();
		for ( final String key : displaySettings.keySet() )
		{
			mainView.setDisplaySettings( key, displaySettings.get( key ) );
		}
		mainView.render();
		controller.getGUI().setDisplayConfigButtonEnabled( true );
	}

	@Override
	public void comingBackToPanel()
	{}

	@Override
	public String getKey()
	{
		return KEY;
	}

	/*
	 * LISTERNER METHODS
	 */

	/**
	 * Adds an {@link ActionListener} to this panel. These listeners will be
	 * notified when a button is pushed or when the feature to color is changed.
	 */
	public void addActionListener( final ActionListener listener )
	{
		actionListeners.add( listener );
	}

	/**
	 * Removes an ActionListener from this panel.
	 *
	 * @return true if the listener was in the ActionListener collection of this
	 *         instance.
	 */
	public boolean removeActionListener( final ActionListener listener )
	{
		return actionListeners.remove( listener );
	}

	public Collection< ActionListener > getActionListeners()
	{
		return actionListeners;
	}

	/**
	 * Forward the given {@link ActionEvent} to all the {@link ActionListener}
	 * of this panel.
	 */
	private void fireAction( final ActionEvent e )
	{
		for ( final ActionListener l : actionListeners )
		{
			l.actionPerformed( e );
		}
	}

}
