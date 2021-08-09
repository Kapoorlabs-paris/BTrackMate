package tracker;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import budDetector.Budobject;
import budDetector.FlagNode;
import budDetector.NNFlagsearchKDtree;
import net.imglib2.KDTree;
import net.imglib2.RealPoint;

public class BUDDYNNsearch implements BUDDYBudTracker {

	private final ArrayList<ArrayList<Budobject>> Allblobs;
	private final long maxframe;
	private int currentframe;
	private SimpleWeightedGraph<Budobject, DefaultWeightedEdge> graph;
	protected BUDDYLogger logger = BUDDYLogger.DEFAULT_LOGGER;
	protected String errorMessage;

	public BUDDYNNsearch(final ArrayList<ArrayList<Budobject>> Allblobs, final long maxframe) {
		this.Allblobs = Allblobs;
		this.maxframe = maxframe;

	}

	@Override
	public boolean process() {

		reset();

		for (int frame = 0; frame < maxframe - 1; ++frame) {

			ArrayList<Budobject> Spotmaxbase = Allblobs.get(frame);

			ArrayList<Budobject> Spotmaxtarget = Allblobs.get(frame + 1);

			Iterator<Budobject> baseobjectiterator = Spotmaxbase.iterator();

			System.out.println(frame + "In track");

			final int Targetblobs = Spotmaxtarget.size();

			System.out.println(Targetblobs + "Targets");

			final List<RealPoint> targetCoords = new ArrayList<RealPoint>(Targetblobs);

			final List<FlagNode<Budobject>> targetNodes = new ArrayList<FlagNode<Budobject>>(Targetblobs);

			for (int index = 0; index < Spotmaxtarget.size(); ++index) {

				System.out.println(Spotmaxtarget.get(index).Budcenter.getDoublePosition(0) + " "
						+ Spotmaxtarget.get(index).Budcenter.getDoublePosition(1));
				targetCoords.add(new RealPoint(Spotmaxtarget.get(index).Budcenter));

				targetNodes.add(new FlagNode<Budobject>(Spotmaxtarget.get(index)));

			}

			if (targetNodes.size() > 0 && targetCoords.size() > 0) {

				final KDTree<FlagNode<Budobject>> Tree = new KDTree<FlagNode<Budobject>>(targetNodes, targetCoords);

				final NNFlagsearchKDtree<Budobject> Search = new NNFlagsearchKDtree<Budobject>(Tree);

				while (baseobjectiterator.hasNext()) {

					final Budobject source = baseobjectiterator.next();
					final RealPoint sourceCoords = new RealPoint(source.Budcenter);
					System.out.println("Source" + source.Budcenter.getDoublePosition(0) + " "
							+ source.Budcenter.getDoublePosition(1));
					Search.search(sourceCoords);
					final double squareDist = Search.getSquareDistance();
					final FlagNode<Budobject> targetNode = Search.getSampler().get();
					System.out.println(squareDist + " " + "dist");

					targetNode.setVisited(true);
					if (squareDist > 0) {
						synchronized (graph) {

							graph.addVertex(source);
							graph.addVertex(targetNode.getValue());
							System.out.println("Source" + source.Budcenter.getDoublePosition(0) + " "
									+ source.Budcenter.getDoublePosition(1) + " " + source.hashCode());
							System.out.println("targetNode" + targetNode.getValue().getDoublePosition(0) + " "
									+ targetNode.getValue().getDoublePosition(1) + " " + targetNode.hashCode());
							if (source.hashCode() != targetNode.hashCode()) {
								final DefaultWeightedEdge edge = graph.addEdge(source, targetNode.getValue());
								graph.setEdgeWeight(edge, squareDist);
							}

						}
					} else
						continue;

				}

				System.out.println("NN detected, moving to next frame!");
			}
		}

		return true;

	}

	@Override
	public SimpleWeightedGraph<Budobject, DefaultWeightedEdge> getResult() {
		return graph;
	}

	@Override
	public boolean checkInput() {
		final StringBuilder errrorHolder = new StringBuilder();
		;
		final boolean ok = checkInput();
		if (!ok) {
			errorMessage = errrorHolder.toString();
		}
		return ok;
	}

	public void reset() {

		graph = new SimpleWeightedGraph<Budobject, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		final Iterator<Budobject> it = Allblobs.get(0).iterator();
		while (it.hasNext()) {
			graph.addVertex(it.next());
		}
	}

	@Override
	public String getErrorMessage() {

		return errorMessage;
	}
}
