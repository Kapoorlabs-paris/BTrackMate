package tracker;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import budDetector.Budobject;
import budDetector.Budpointobject;
import budDetector.Roiobject;
import fiji.plugin.globalbtrack.gui.components.CovistoKalmanPanel;
import ij.gui.OvalRoi;
import net.imglib2.KDTree;
import net.imglib2.RealPoint;
import net.imglib2.util.Pair;
import pluginTools.InteractiveBud;
import utility.FlagNode;

public class BUDDYTrackingFunctions {

	final InteractiveBud parent;

	public BUDDYTrackingFunctions(final InteractiveBud parent) {

		this.parent = parent;

	}

	public SimpleWeightedGraph<Budpointobject, DefaultWeightedEdge> Trackfunction() {

		parent.UserchosenCostFunction = new BUDDYBudPointTrackCostFunction(CovistoKalmanPanel.alpha,
				CovistoKalmanPanel.beta);

		ArrayList<ArrayList<Budpointobject>> colllist = new ArrayList<ArrayList<Budpointobject>>();
		parent.AllBudpoints = sortByIntegerInter(parent.AllBudpoints);

		for (Map.Entry<String, ArrayList<Budpointobject>> entry : parent.AllBudpoints.entrySet()) {

			ArrayList<Budpointobject> bloblist = entry.getValue();
			ArrayList<Budpointobject> copybloblist = new ArrayList<Budpointobject>(bloblist);
			String time = entry.getKey();

			ArrayList<Roiobject> currentlist = parent.BudOvalRois.get(time);

			for (Roiobject timelist : currentlist) {

				Color currentcolor = timelist.color;
				OvalRoi colorroi = timelist.roi;
				if (currentcolor == parent.RemoveBudColor) {
					double[] location = colorroi.getContourCentroid();
					Budpointobject budpoint = getNearestBPO(bloblist, location);
					copybloblist.remove(budpoint);
				}
			}

			if (copybloblist.size() > 0) {
				colllist.add(copybloblist);

			}

		}

		BUDDYKFsearch Tsearch = new BUDDYKFsearch(colllist, parent.UserchosenCostFunction,
				CovistoKalmanPanel.initialSearchradius / parent.calibrationX,
				CovistoKalmanPanel.initialSearchradius / parent.calibrationX, CovistoKalmanPanel.maxframegap,
				parent.AccountedT, parent.jpb);
		Tsearch.process();
		SimpleWeightedGraph<Budpointobject, DefaultWeightedEdge> simplegraph = Tsearch.getResult();

		return simplegraph;

	}

	public static Budpointobject getNearestBPO(ArrayList<Budpointobject> Budpointlist, double[] Clickedpoint) {

		Budpointobject KDtreeroi = null;

		final List<RealPoint> targetCoords = new ArrayList<RealPoint>(Budpointlist.size());
		final List<FlagNode<Budpointobject>> targetNodes = new ArrayList<FlagNode<Budpointobject>>(Budpointlist.size());
		for (int index = 0; index < Budpointlist.size(); ++index) {

			Budpointobject r = Budpointlist.get(index);

			targetCoords.add(new RealPoint(r.Location));

			targetNodes.add(new FlagNode<Budpointobject>(Budpointlist.get(index)));

		}

		if (targetNodes.size() > 0 && targetCoords.size() > 0) {

			final KDTree<FlagNode<Budpointobject>> Tree = new KDTree<FlagNode<Budpointobject>>(targetNodes,
					targetCoords);

			final NNFlagsearchKDtree<Budpointobject> Search = new NNFlagsearchKDtree<Budpointobject>(Tree);

			final double[] source = Clickedpoint;
			final RealPoint sourceCoords = new RealPoint(source);
			Search.search(sourceCoords);

			final FlagNode<Budpointobject> targetNode = Search.getSampler().get();

			KDtreeroi = targetNode.getValue();

		}

		return KDtreeroi;

	}

	public SimpleWeightedGraph<Budobject, DefaultWeightedEdge> BudTrackfunction() {

		parent.BudUserchosenCostFunction = new BUDDYBudTrackCostFunction(CovistoKalmanPanel.alpha,
				CovistoKalmanPanel.beta);

		ArrayList<ArrayList<Budobject>> colllist = new ArrayList<ArrayList<Budobject>>();
		parent.AllBuds = sortBudByIntegerInter(parent.AllBuds);

		for (Map.Entry<String, ArrayList<Budobject>> entry : parent.AllBuds.entrySet()) {

			ArrayList<Budobject> bloblist = entry.getValue();
			if (bloblist.size() > 0) {
				colllist.add(bloblist);

			}

		}

		BUDDYBudKFsearch Tsearch = new BUDDYBudKFsearch(colllist, parent.BudUserchosenCostFunction,
				parent.originalimg.dimension(0) * parent.originalimg.dimension(1),
				parent.originalimg.dimension(0) * parent.originalimg.dimension(1), CovistoKalmanPanel.maxframegap,
				parent.AccountedT, parent.jpb);
		Tsearch.process();
		SimpleWeightedGraph<Budobject, DefaultWeightedEdge> simplegraph = Tsearch.getResult();

		return simplegraph;

	}

	/**
	 * Sort Z or T hashmap by comparing the order in Z or T
	 * 
	 * @param map
	 * @return
	 */
	public static HashMap<String, Integer> sortByValues(HashMap<String, Integer> map) {
		List<Entry<String, Integer>> list = new LinkedList<Entry<String, Integer>>(map.entrySet());
		// Defined Custom Comparator here
		Collections.sort(list, new Comparator<Entry<String, Integer>>() {

			@Override
			public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
				return (o1.getValue()).compareTo(o2.getValue());
			}
		});

		// Here I am copying the sorted list in HashMap
		// using LinkedHashMap to preserve the insertion order
		HashMap<String, Integer> sortedHashMap = new LinkedHashMap<String, Integer>();
		for (Iterator<Entry<String, Integer>> it = list.iterator(); it.hasNext();) {
			Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>) it.next();
			sortedHashMap.put(entry.getKey(), entry.getValue());
		}
		return sortedHashMap;
	}

	public static HashMap<String, ArrayList<Budpointobject>> sortByIntegerInter(
			HashMap<String, ArrayList<Budpointobject>> map) {
		List<Entry<String, ArrayList<Budpointobject>>> list = new LinkedList<Entry<String, ArrayList<Budpointobject>>>(
				map.entrySet());
		// Defined Custom Comparator here
		Collections.sort(list, new Comparator<Entry<String, ArrayList<Budpointobject>>>() {

			@Override
			public int compare(Entry<String, ArrayList<Budpointobject>> o1,
					Entry<String, ArrayList<Budpointobject>> o2) {

				return Integer.parseInt(o1.getKey()) - Integer.parseInt(o2.getKey());
			}
		});

		// Here I am copying the sorted list in HashMap
		// using LinkedHashMap to preserve the insertion order
		HashMap<String, ArrayList<Budpointobject>> sortedHashMap = new LinkedHashMap<String, ArrayList<Budpointobject>>();
		for (Iterator<Entry<String, ArrayList<Budpointobject>>> it = list.iterator(); it.hasNext();) {
			Map.Entry<String, ArrayList<Budpointobject>> entry = (Map.Entry<String, ArrayList<Budpointobject>>) it
					.next();
			sortedHashMap.put(entry.getKey(), entry.getValue());
		}
		return sortedHashMap;
	}

	public static HashMap<String, ArrayList<Budobject>> sortBudByIntegerInter(
			HashMap<String, ArrayList<Budobject>> map) {
		List<Entry<String, ArrayList<Budobject>>> list = new LinkedList<Entry<String, ArrayList<Budobject>>>(
				map.entrySet());
		// Defined Custom Comparator here
		Collections.sort(list, new Comparator<Entry<String, ArrayList<Budobject>>>() {

			@Override
			public int compare(Entry<String, ArrayList<Budobject>> o1, Entry<String, ArrayList<Budobject>> o2) {

				return Integer.parseInt(o1.getKey()) - Integer.parseInt(o2.getKey());
			}
		});

		// Here I am copying the sorted list in HashMap
		// using LinkedHashMap to preserve the insertion order
		HashMap<String, ArrayList<Budobject>> sortedHashMap = new LinkedHashMap<String, ArrayList<Budobject>>();
		for (Iterator<Entry<String, ArrayList<Budobject>>> it = list.iterator(); it.hasNext();) {
			Map.Entry<String, ArrayList<Budobject>> entry = (Map.Entry<String, ArrayList<Budobject>>) it.next();
			sortedHashMap.put(entry.getKey(), entry.getValue());
		}
		return sortedHashMap;
	}

}
