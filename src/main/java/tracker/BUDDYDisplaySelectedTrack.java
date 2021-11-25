package tracker;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;

import budDetector.Budpointobject;
import budDetector.Distance;
import budDetector.Roiobject;
import ij.gui.ImageCanvas;
import ij.gui.OvalRoi;
import ij.gui.Overlay;
import ij.gui.Roi;
import listeners.AddBudKeyListener;
import net.imglib2.Interval;
import net.imglib2.KDTree;
import net.imglib2.Localizable;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.RealPoint;
import net.imglib2.algorithm.region.hypersphere.HyperSphere;
import net.imglib2.algorithm.region.hypersphere.HyperSphereCursor;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;
import net.imglib2.view.Views;
import pluginTools.ComputeBorder;
import pluginTools.InteractiveBud;
import utility.BudChartMaker;
import utility.FlagNode;
import utility.SavePink;
import zGUI.CovistoZselectPanel;

public class BUDDYDisplaySelectedTrack {

	public static void Select(final InteractiveBud parent, HashMap<Integer, HashMap<Integer, Double>> VelocityMap) {

		if (parent.mvl != null)
			parent.imp.getCanvas().removeMouseListener(parent.mvl);
		if (parent.kvl != null)
			parent.imp.getCanvas().removeKeyListener(parent.kvl);
		parent.imp.getCanvas().addKeyListener(new AddBudKeyListener(parent));
		parent.imp.getCanvas().addMouseListener(parent.mvl = new MouseListener() {

			final ImageCanvas canvas = parent.imp.getWindow().getCanvas();

			@Override
			public void mouseClicked(MouseEvent e) {

			}

			@Override
			public void mousePressed(MouseEvent e) {
				int x = canvas.offScreenX(e.getX());
				int y = canvas.offScreenY(e.getY());
				parent.Clickedpoints[0] = x;
				parent.Clickedpoints[1] = y;
				ArrayList<Roiobject> Allrois = new ArrayList<Roiobject>();
				int time = parent.thirdDimension;
				if (SwingUtilities.isLeftMouseButton(e) && e.isShiftDown()) {

					// Still to write this function
					displayclicked(parent, parent.rowchoice);

				}

				// To select or deselect a point
				if (SwingUtilities.isLeftMouseButton(e) && !e.isShiftDown() && parent.AddDot != "a") {

					ArrayList<Budpointobject> Budpointlist = parent.AllBudpoints.get(Integer.toString(time));

					Budpointobject nearest = getNearestBPO(Budpointlist, new double[] { x, y });

					int X = (int) Math.round(nearest.Location[0]);
					int Y = (int) Math.round(nearest.Location[1]);

					OvalRoi points = new OvalRoi((int) X - parent.BudDotsize / 2, (int) Y - parent.BudDotsize / 2,
							parent.BudDotsize, parent.BudDotsize);

					Roiobject nearestroi = getNearestRois(parent.BudOvalRois.get(Integer.toString(time)),
							new double[] { X, Y });

					if (nearestroi != null) {
						if (nearestroi.color != parent.RemoveBudColor)
							points.setStrokeColor(parent.RemoveBudColor);
						if (nearestroi.color == parent.RemoveBudColor)
							points.setStrokeColor(parent.BudColor);
						points.setStrokeWidth(parent.BudDotsize);
						parent.overlay.remove(nearestroi.roi);
						parent.overlay.add(points);

						for (int i = 0; i < parent.overlay.size(); ++i) {

							OvalRoi roi = (OvalRoi) parent.overlay.get(i);

							double[] point = new double[] { roi.getContourCentroid()[0], roi.getContourCentroid()[1] };

							Budpointobject nearestBPO = getNearestBPO(Budpointlist, point);

							Localizable labelpoint = new Point(
									new long[] { (long) nearestBPO.Budcenter.getDoublePosition(0),
											(long) nearestBPO.Budcenter.getDoublePosition(1) });
							
							RandomAccess<IntType> ranac =  parent.CurrentViewInt.randomAccess();
							ranac.setPosition(labelpoint);
							int label =  ranac.get().get();
							
							if (roi.getStrokeColor() == parent.RemoveBudColor
									|| roi.getStrokeColor() == parent.BudColor)
								Allrois.add(new Roiobject(roi.getStrokeColor(), roi, new RealPoint(point), label));

						}

						parent.BudOvalRois.put(Integer.toString(parent.thirdDimension), Allrois);

						parent.imp.updateAndDraw();
					}

				}

				if (SwingUtilities.isLeftMouseButton(e) && !e.isShiftDown() && parent.AddDot == "a") {
					ArrayList<Budpointobject> Budpointlist = parent.AllBudpoints.get(Integer.toString(time));
					RandomAccess<IntType> checkintranac = parent.CurrentViewInt.randomAccess();

					checkintranac.setPosition(new int[] { x, y });

					int checklabel = checkintranac.get().get();
					if (checklabel > 0) {
						OvalRoi points = new OvalRoi((int) x - parent.BudDotsize / 2, (int) y - parent.BudDotsize / 2,
								parent.BudDotsize, parent.BudDotsize);
						points.setStrokeColor(parent.BudColor);
						points.setStrokeWidth(parent.BudDotsize);
						parent.overlay.add(points);

						for (int i = 0; i < parent.overlay.size(); ++i) {

							OvalRoi roi = (OvalRoi) parent.overlay.get(i);

							double[] point = new double[] { roi.getContourCentroid()[0], roi.getContourCentroid()[1] };

							Budpointobject nearestBPO = getNearestBPO(Budpointlist, point);
							Localizable labelpoint = new Point(
									new long[] { (long) nearestBPO.Budcenter.getDoublePosition(0),
											(long) nearestBPO.Budcenter.getDoublePosition(1) });
							
							RandomAccess<IntType> ranac = parent.CurrentViewInt.randomAccess();
							ranac.setPosition(labelpoint);
							
							
							int label = ranac.get().get();
							

							if (roi.getStrokeColor() == parent.RemoveBudColor
									|| roi.getStrokeColor() == parent.BudColor)
								Allrois.add(new Roiobject(roi.getStrokeColor(), roi, new RealPoint(point), label));

						}

						parent.BudOvalRois.put(Integer.toString(parent.thirdDimension), Allrois);

						parent.imp.updateAndDraw();

						for (Budpointobject currentbud : Budpointlist) {

							int budlabel = currentbud.label;

							RandomAccess<IntType> intranac = parent.CurrentViewInt.randomAccess();

							intranac.setPosition(new int[] { x, y });

							int mylabel = intranac.get().get();

							if (mylabel == budlabel) {

								Budpointobject newbud = new Budpointobject(currentbud.Budcenter, currentbud.linelist,
										currentbud.dynamiclinelist, currentbud.perimeter, mylabel,
										new double[] { x, y }, time, currentbud.velocity);

								Budpointlist.add(newbud);
								break;
							}

						}
					}
					parent.AllBudpoints.put(Integer.toString(time), Budpointlist);

					// Change the keyboard click to a
					parent.AddDot = "b";

				}

			}

			@Override
			public void mouseReleased(MouseEvent e) {

			}

			@Override
			public void mouseEntered(MouseEvent e) {

			}

			@Override
			public void mouseExited(MouseEvent e) {

			}

		});

	}

	public static Roiobject getNearestRois(ArrayList<Roiobject> arrayList, double[] Clickedpoint) {

		Roiobject KDtreeroi = null;

		final List<RealPoint> targetCoords = new ArrayList<RealPoint>(arrayList.size());
		final List<FlagNode<Roiobject>> targetNodes = new ArrayList<FlagNode<Roiobject>>(arrayList.size());
		for (int index = 0; index < arrayList.size(); ++index) {

			Roi r = arrayList.get(index).roi;

			targetCoords.add(new RealPoint(r.getContourCentroid()));

			targetNodes.add(new FlagNode<Roiobject>(arrayList.get(index)));

		}

		if (targetNodes.size() > 0 && targetCoords.size() > 0) {

			final KDTree<FlagNode<Roiobject>> Tree = new KDTree<FlagNode<Roiobject>>(targetNodes, targetCoords);

			final NNFlagsearchKDtree<Roiobject> Search = new NNFlagsearchKDtree<Roiobject>(Tree);

			final double[] source = Clickedpoint;
			final RealPoint sourceCoords = new RealPoint(source);
			Search.search(sourceCoords);
			final FlagNode<Roiobject> targetNode = Search.getSampler().get();

			KDtreeroi = targetNode.getValue();

		}

		return KDtreeroi;
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

	public static void Mark(final InteractiveBud parent, HashMap<Integer, HashMap<Integer, Double>> VelocityMap) {

		parent.Drawcolor = Color.ORANGE;

		if (parent.table != null)
			parent.table.addMouseListener(parent.tvl = new MouseListener() {

				@Override
				public void mouseClicked(MouseEvent event) {

					Roi[] Roipoints = parent.imp.getOverlay().toArray();

					for (Roi point : Roipoints) {
						if (Roi.getColor() == parent.Drawcolor) {

							parent.imp.getOverlay().remove(point);

						}

					}
					parent.imp.updateAndDraw();

					java.awt.Point point = event.getPoint();
					int row = parent.table.rowAtPoint(point);

					parent.rowchoice = row;
					String ID = (String) parent.table.getValueAt(row, 0);
					String CordX = (String) parent.table.getValueAt(row, 1);
					String CordY = (String) parent.table.getValueAt(row, 2);

					for (ValuePair<String, Budpointobject> Track : parent.Tracklist) {

						String ImageID = Track.getA();

						if (ID.equals(ImageID)) {

							OvalRoi points = new OvalRoi((int) Integer.parseInt(CordX), (int) Integer.parseInt(CordY),
									25, 25);
							parent.imp.getOverlay().add(points);
							points.setStrokeColor(parent.Drawcolor);
							points.setStrokeWidth(40);
							parent.imp.updateAndDraw();
						}

					}
					parent.imp.updateAndDraw();
					displayclicked(parent, row);
					parent.table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
						@Override
						public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
								boolean hasFocus, int row, int col) {

							super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
							if (row == parent.rowchoice) {
								setBackground(Color.green);

							} else {
								setBackground(Color.white);
							}
							return this;
						}
					});

					parent.table.validate();
					parent.scrollPane.validate();
					parent.panelFirst.repaint();
					parent.panelFirst.validate();

				}

				@Override
				public void mousePressed(MouseEvent e) {
					// TODO Auto-generated method stub

				}

				@Override
				public void mouseReleased(MouseEvent e) {
					// TODO Auto-generated method stub

				}

				@Override
				public void mouseEntered(MouseEvent e) {
					// TODO Auto-generated method stub

				}

				@Override
				public void mouseExited(MouseEvent e) {
					// TODO Auto-generated method stub

				}

			});

		if (parent.ml != null)
			parent.imp.getCanvas().removeMouseMotionListener(parent.ml);
		parent.imp.getCanvas().addMouseMotionListener(parent.ml = new MouseMotionListener() {

			final ImageCanvas canvas = parent.imp.getWindow().getCanvas();

			@Override
			public void mouseMoved(MouseEvent e) {

				int x = canvas.offScreenX(e.getX());
				int y = canvas.offScreenY(e.getY());

				final HashMap<Integer, double[]> loc = new HashMap<Integer, double[]>();

				loc.put(0, new double[] { x, y });

				double distmin = Double.MAX_VALUE;
				if (parent.tablesize > 0 && parent.table.getRowCount() > 0) {
					NumberFormat f = NumberFormat.getInstance();
					for (int row = 0; row < parent.tablesize; ++row) {
						String CordX = (String) parent.table.getValueAt(row, 1);
						String CordY = (String) parent.table.getValueAt(row, 2);

						double dCordX = 0, dCordZ = 0, dCordY = 0;
						try {
							dCordX = f.parse(CordX).doubleValue();

							dCordY = f.parse(CordY).doubleValue();
						} catch (ParseException e1) {

						}
						double dist = Distance.DistanceSq(new double[] { dCordX, dCordY }, new double[] { x, y });
						if (Distance.DistanceSq(new double[] { dCordX, dCordY }, new double[] { x, y }) < distmin
								&& parent.ndims > 3) {

							parent.rowchoice = row;
							distmin = dist;

						}
						if (Distance.DistanceSq(new double[] { dCordX, dCordY }, new double[] { x, y }) < distmin
								&& parent.ndims <= 3) {

							parent.rowchoice = row;
							distmin = dist;

						}

					}

					parent.table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
						@Override
						public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
								boolean hasFocus, int row, int col) {

							super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
							if (row == parent.rowchoice) {
								setBackground(Color.green);

							} else {
								setBackground(Color.white);
							}
							return this;
						}
					});

					parent.table.validate();
					parent.scrollPane.validate();
					parent.panelFirst.repaint();
					parent.panelFirst.validate();

				}

			}

			@Override
			public void mouseDragged(MouseEvent e) {

			}

		});

	}

	public static void displayclicked(InteractiveBud parent, int trackindex) {

		// Make something happen
		if (parent.Tracklist.size() > 0) {
			parent.row = trackindex;
			String ID = (String) parent.table.getValueAt(trackindex, 0);
			if (parent.resultimp != null)
				parent.resultimp.close();
			BUDDYDisplayTrack display = new BUDDYDisplayTrack(parent, parent.Globalmodel);
			display.getImp();

			HashMap<Integer, Double> VelocityID = parent.BudVelocityMap.get(Integer.parseInt(ID));
			ArrayList<double[]> Trackinfo = new ArrayList<double[]>();

			for (Pair<String, Budpointobject> Track : parent.Tracklist) {

				if (Track.getA().equals(ID)) {

					double time = Track.getB().t * parent.timecal;
					double LocationX = Track.getB().Location[0] * parent.calibrationX;
					double LocationY = Track.getB().Location[1] * parent.calibrationY;
					double Velocity = 0;
					if (VelocityID.get(Track.getB().t) != null)
						Velocity = VelocityID.get(Track.getB().t);
					Trackinfo.add(new double[] { time, LocationX, LocationY, Velocity });

				}

			}

			if (parent.imp != null) {
				parent.imp.setOverlay(parent.overlay);
				parent.imp.updateAndDraw();
			}

		}

	}

}