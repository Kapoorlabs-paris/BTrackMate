package tracker;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingWorker;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import budDetector.Budobject;
import budDetector.Budpointobject;
import budDetector.Distance;
import fiji.plugin.btrack.gui.components.CovistoKalmanPanel;
import ij.ImageStack;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;
import pluginTools.BoundaryTrack;
import pluginTools.InteractiveBud;
import tracker.trackanalyzer.BudTrackVelocityAnalyzer;
import zGUI.CovistoZselectPanel;

public class BUDDYTrackResult extends SwingWorker<Void, Void> {

	final InteractiveBud parent;

	public BUDDYTrackResult(final InteractiveBud parent) {

		this.parent = parent;
	}

	@Override
	protected Void doInBackground() throws Exception {

		parent.jpb.setIndeterminate(false);
		parent.Cardframe.validate();

		parent.prestack = new ImageStack((int) parent.originalimg.dimension(0), (int) parent.originalimg.dimension(1),
				java.awt.image.ColorModel.getRGBdefault());

		parent.PanelSelectFile.add(parent.scrollPane, BorderLayout.CENTER);
		parent.Tracklist.clear();
		parent.IDlist.clear();
		parent.Finalresult.clear();

		BUDDYTrackingFunctions track = new BUDDYTrackingFunctions(parent);
		SimpleWeightedGraph<Budpointobject, DefaultWeightedEdge> simplegraph = track.Trackfunction();
		SimpleWeightedGraph<Budobject, DefaultWeightedEdge> Budsimplegraph = track.BudTrackfunction();
		// Display Graph results, make table etc
		BudDisplayGraph(Budsimplegraph);
		DisplayGraph(simplegraph);

		return null;
	}

	public static List<Budpointobject> sortTrack(List<Budpointobject> Angleset) {

		List<Budpointobject> sortedlist = new ArrayList<Budpointobject>();
		Collections.sort(Angleset, new Comparator<Budpointobject>() {

			@Override
			public int compare(Budpointobject o1, Budpointobject o2) {
				// TODO Auto-generated method stub
				return o1.t - o2.t;
			}

		});

		for (Iterator<Budpointobject> it = Angleset.iterator(); it.hasNext();) {
			Budpointobject entry = it.next();
			sortedlist.add(entry);
		}
		return sortedlist;

	}

	public static HashMap<Integer, Integer> sortByInteger(HashMap<Integer, Integer> map) {
		List<Entry<Integer, Integer>> list = new LinkedList<Entry<Integer, Integer>>(map.entrySet());
		// Defined Custom Comparator here
		Collections.sort(list, new Comparator<Entry<Integer, Integer>>() {

			@Override
			public int compare(Entry<Integer, Integer> o1, Entry<Integer, Integer> o2) {

				return -o1.getValue() + o2.getValue();
			}
		});

		// Here I am copying the sorted list in HashMap
		// using LinkedHashMap to preserve the insertion order
		HashMap<Integer, Integer> sortedHashMap = new LinkedHashMap<Integer, Integer>();
		for (Iterator<Entry<Integer, Integer>> it = list.iterator(); it.hasNext();) {
			Map.Entry<Integer, Integer> entry = (Map.Entry<Integer, Integer>) it.next();
			sortedHashMap.put(entry.getKey(), entry.getValue());
		}
		return sortedHashMap;
	}

	protected void DisplayGraph(SimpleWeightedGraph<Budpointobject, DefaultWeightedEdge> simplegraph) {

		int minid = Integer.MAX_VALUE;
		int maxid = Integer.MIN_VALUE;

		BUDDYModel model = new BUDDYModel();
		model.setTracks(simplegraph, false);
		BudTrackVelocityAnalyzer TrackVelocity = new BudTrackVelocityAnalyzer(parent);
		TrackVelocity.process(model.getTrackModel().trackIDs(false), model);
		HashMap<Integer, HashMap<Integer, Double>> VelocityMap = TrackVelocity.getVelocityMap();
		parent.BudVelocityMap = VelocityMap;

		for (final Integer id : model.getTrackModel().trackIDs(false)) {

			if (id > maxid)
				maxid = id;

			if (id < minid)
				minid = id;

			final HashSet<Budpointobject> Angleset = model.getTrackModel().trackBudpointobjects(id);

			int tracklength = Angleset.size();
			// if(tracklength >= CovistoKalmanPanel.trackduration * (parent.AutoendTime -
			// parent.AutostartTime)/100)
			parent.IDlist.put(id, tracklength);

		}
		parent.IDlist = sortByInteger(parent.IDlist);

		if (minid != Integer.MAX_VALUE) {

			for (Map.Entry<Integer, Integer> tracks : parent.IDlist.entrySet()) {

				int id = tracks.getKey();
				int tracklength = tracks.getValue();

				String ID = Integer.toString(id);

				model.getTrackModel().setName(id, "Track" + id + tracklength);
				parent.Globalmodel = model.getTrackModel();
				final HashSet<Budpointobject> Angleset = model.getTrackModel().trackBudpointobjects(id);

				List<Budpointobject> unsortedList = new ArrayList<Budpointobject>(Angleset);
				List<Budpointobject> sortedList = sortTrack(unsortedList);

				Iterator<Budpointobject> listiter = sortedList.iterator();
				Budpointobject previousbud = null;
				while (listiter.hasNext()) {

					double velocity = 0;
					Budpointobject currentbud = listiter.next();
					if (previousbud != null) {
						velocity = Math.sqrt(Distance.DistanceSq(currentbud.Location, previousbud.Location));

						velocity = velocity * (parent.calibrationX / parent.timecal);
						Budpointobject newbud = new Budpointobject(currentbud.Budcenter, currentbud.linelist,
								currentbud.dynamiclinelist, currentbud.perimeter, currentbud.label, currentbud.Location,
								currentbud.t, velocity);

						parent.Tracklist.add(new ValuePair<String, Budpointobject>(ID, newbud));
					}
					previousbud = currentbud;
				}

				double trackmeanspeed = model.getFeatureModel().getTrackFeature(id,
						BudTrackVelocityAnalyzer.TRACK_MEAN_SPEED);
				double trackmaxspeed = model.getFeatureModel().getTrackFeature(id,
						BudTrackVelocityAnalyzer.TRACK_MAX_SPEED);

				trackmeanspeed = trackmeanspeed * (parent.calibrationX / parent.timecal);
				if (trackmeanspeed == Double.NaN)
					trackmeanspeed = 0;

				trackmaxspeed = trackmaxspeed * (parent.calibrationX / parent.timecal);
				if (trackmaxspeed == Double.NaN)
					trackmaxspeed = 0;

				parent.TrackMeanVelocitylist.put(ID, trackmeanspeed * (parent.calibrationX / parent.timecal));
				parent.TrackMaxVelocitylist.put(ID, trackmaxspeed * (parent.calibrationX / parent.timecal));

			}

			for (Map.Entry<Integer, Integer> tracks : parent.IDlist.entrySet()) {
				int id = tracks.getKey();
				Budpointobject bestbud = null;

				List<Budpointobject> sortedList = new ArrayList<Budpointobject>(
						model.getTrackModel().trackBudpointobjects(id));

				Iterator<Budpointobject> iterator = sortedList.iterator();

				double tmax = Double.MIN_VALUE;
				while (iterator.hasNext()) {

					Budpointobject currentbud = iterator.next();
					int time = currentbud.t;
					if (time > tmax) {

						tmax = time;
						bestbud = currentbud;

					}

				}
				parent.Finalresult.put(Integer.toString(id), bestbud);

			}

			CreateTableView(parent);
			BUDDYDisplaySelectedTrack.Select(parent, VelocityMap);
			BUDDYDisplaySelectedTrack.Mark(parent, VelocityMap);

		}
	}

	public static HashMap<String, Budpointobject> sortByIntegerInter(HashMap<String, Budpointobject> map) {
		List<Entry<String, Budpointobject>> list = new LinkedList<Entry<String, Budpointobject>>(map.entrySet());
		// Defined Custom Comparator here
		Collections.sort(list, new Comparator<Entry<String, Budpointobject>>() {

			@Override
			public int compare(Entry<String, Budpointobject> o1, Entry<String, Budpointobject> o2) {

				return -o1.getValue().t + o2.getValue().t;
			}
		});

		// Here I am copying the sorted list in HashMap
		// using LinkedHashMap to preserve the insertion order
		HashMap<String, Budpointobject> sortedHashMap = new LinkedHashMap<String, Budpointobject>();
		for (Iterator<Entry<String, Budpointobject>> it = list.iterator(); it.hasNext();) {
			Map.Entry<String, Budpointobject> entry = (Map.Entry<String, Budpointobject>) it.next();
			sortedHashMap.put(entry.getKey(), entry.getValue());
		}
		return sortedHashMap;
	}

	protected void BudDisplayGraph(SimpleWeightedGraph<Budobject, DefaultWeightedEdge> simplegraph) {

		int minid = Integer.MAX_VALUE;
		int maxid = Integer.MIN_VALUE;
		BUDDYBudTrackModel model = new BUDDYBudTrackModel(simplegraph);

		for (final Integer id : model.trackIDs(false)) {
			if (id > maxid)
				maxid = id;

			if (id < minid)
				minid = id;
		}
		if (minid != Integer.MAX_VALUE) {

			for (final Integer id : model.trackIDs(false)) {

				Comparator<Pair<String, Budobject>> ThirdDimcomparison = new Comparator<Pair<String, Budobject>>() {

					@Override
					public int compare(final Pair<String, Budobject> A, final Pair<String, Budobject> B) {

						return A.getB().t - B.getB().t;

					}

				};

				String ID = Integer.toString(id);
				model.setName(id, "Track" + id);
				parent.BudGlobalModel = model;
				final HashSet<Budobject> Angleset = model.trackBudobjects(id);

				Iterator<Budobject> Angleiter = Angleset.iterator();
				while (Angleiter.hasNext()) {

					Budobject currentbud = Angleiter.next();

					parent.BudTracklist.add(new ValuePair<String, Budobject>(ID, currentbud));
				}
				Collections.sort(parent.BudTracklist, ThirdDimcomparison);

			}

		}
	}

	public void CreateTableView(InteractiveBud parent) {

		Object[] colnames = new Object[] { "Track Id", "Location X", "Location Y", "Location T", "Mean Growth Rate",
				"Max Growth Rate" };

		Object[][] rowvalues = new Object[0][colnames.length];

		rowvalues = new Object[parent.Finalresult.size()][colnames.length];

		parent.table = new JTable(rowvalues, colnames);
		NumberFormat f = NumberFormat.getIntegerInstance();
		f.setGroupingUsed(false);
		parent.row = 0;

		for (Map.Entry<Integer, Integer> tracks : parent.IDlist.entrySet()) {

			String ID = Integer.toString(tracks.getKey());
			double meanRate = parent.TrackMeanVelocitylist.get(ID);
			double maxRate = parent.TrackMaxVelocitylist.get(ID);
			Budpointobject currentbud = parent.Finalresult.get(ID);
			parent.table.getModel().setValueAt(Integer.toString(tracks.getKey()), parent.row, 0);
			parent.table.getModel().setValueAt(f.format(currentbud.Location[0]), parent.row, 1);
			parent.table.getModel().setValueAt(f.format(currentbud.Location[1]), parent.row, 2);
			parent.table.getModel().setValueAt(f.format(currentbud.t), parent.row, 3);
			parent.table.getModel().setValueAt(parent.nf.format(meanRate), parent.row, 4);
			parent.table.getModel().setValueAt(parent.nf.format(maxRate), parent.row, 5);
			parent.row++;

			parent.tablesize = parent.row;
		}

		makeGUI(parent);

	}

	public static void makeGUI(final InteractiveBud parent) {

		parent.PanelSelectFile.removeAll();

		parent.table.setFillsViewportHeight(true);

		parent.table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		int size = 100;
		parent.table.getColumnModel().getColumn(0).setPreferredWidth(size);
		parent.table.getColumnModel().getColumn(1).setPreferredWidth(size);
		parent.table.getColumnModel().getColumn(2).setPreferredWidth(size);
		parent.table.getColumnModel().getColumn(3).setPreferredWidth(size);
		parent.table.getColumnModel().getColumn(4).setPreferredWidth(size);
		parent.table.setPreferredScrollableViewportSize(parent.table.getPreferredSize());

		parent.table.setMinimumSize(parent.table.getPreferredSize());

		parent.scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		parent.scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		parent.scrollPane = new JScrollPane(parent.table);

		parent.scrollPane.getViewport().add(parent.table);
		parent.scrollPane.setAutoscrolls(true);

		parent.PanelSelectFile.add(parent.scrollPane, BorderLayout.CENTER);

		parent.PanelSelectFile.setBorder(parent.selectfile);

		parent.table.isOpaque();
		parent.scrollPane.setMinimumSize(new Dimension(300, 200));
		parent.scrollPane.setPreferredSize(new Dimension(300, 200));

		parent.table.repaint();
		parent.table.validate();

		parent.scrollPane.repaint();
		parent.scrollPane.validate();
		parent.PanelSelectFile.repaint();
		parent.PanelSelectFile.validate();
		parent.panelFirst.repaint();
		parent.panelFirst.validate();
		parent.Cardframe.repaint();
		parent.Cardframe.validate();

	}
}