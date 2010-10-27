package de.jtem.halfedgetools.bsp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.Adapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Position;


/**
 * Simple KdTree class to query neighborhood information 
 *
 */
public class KdTree <
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>,
	F extends Face<V, E, F>
> implements Serializable{

	private static final long 
		serialVersionUID = 1L;
	
	/**
	 * Simple (kd)-tree node structure
	 */
	public static class KdNode implements Serializable{
		
		private static final long 
			serialVersionUID = 1L;
		
		KdNode[] children; // The two children of the Node 

		int dim; // Dimension in which plane lies

		double[] splitPos; // Position of plane in

		KdNode() {
			children = new KdNode[2];
		}

		public KdNode[] getChildren() {
			return children;
		}

		public int getDim() {
			return dim;
		}

		public double[] getSplitPos() {
			return splitPos;
		}

		final public int getEndIndex() {
			if (this instanceof KdLeaf)
				return ((KdLeaf) this).endIndex;
			else
				return children[1].getEndIndex();
		}

		final public int getStartIndex() {
			if (this instanceof KdLeaf)
				return ((KdLeaf) this).startIndex;
			else
				return children[0].getStartIndex();
		}

	}

	/**
	 * Only leafs need to store a bucket of points
	 */
	public static class KdLeaf extends KdNode implements Serializable{
		
		private static final long 
			serialVersionUID = 1L;

		int startIndex;

		int endIndex;

		/**
		 * Create a leaf/bucket starting at startIndex and ending at endIndex
		 * @param startIndex
		 * @param endIndex
		 */
		public KdLeaf(int startIndex, int endIndex) {
			this.startIndex = startIndex;
			this.endIndex = endIndex;
		}

	}

	private int maxBucketSize; // maximum size of points per cell
	private  ArrayList<V> points; // HasPosition data
	private KdNode root; // root node
	private Adapter<double[]> posA = null;
	private AdapterSet emptySet = new AdapterSet();

	
	/**
	 * Constructor for a kd tree
	 * Builds an KdTree for the given points with maxBucketSize
	 * The order of the points in the array may change!
	 * @param points
	 * @param maxBucketSize
	 */
	public KdTree(HalfEdgeDataStructure<V, E, F> hds, AdapterSet a, int maxBucketSize, boolean useMedian) {
		this.points = new ArrayList<V>(hds.getVertices());
		this.maxBucketSize = maxBucketSize;
		this.posA = a.query(Position.class, hds.getVertexClass(), double[].class);
		if (posA == null) throw new RuntimeException("No position adapter found in KdTree()");
		if (useMedian) {
        	root = buildKdTree(0, points.size() - 1, 0);
        } else {
        	root = buildKdTree(0, points.size() - 1, getBBox());
        }
	}

	
	/**
	 * Returns the root node of the KdTree
	 * @return
	 */
	public KdNode getRoot() {
		return root;
	}

	/**
	 * Returns the points within a given radius of HasPosition p
	 * @param p
	 * @param radius
	 * @return An array containing the points or null, if there weren't any points
	 * in that radius 
	 */
	final public Collection<V> collectInRadius(double[] p, double radius) {
		// Only search in a radius greater than zero
		if(radius <= 0) {
			return Collections.emptySet();
		}
		// Start the collectInRadius search
		Collection<V> result = new LinkedList<V>();
		collectInRadius(root, p, radius, result);
		return result;
	}

	/**
	 * Implementation of the collectInRadius functonality.
	 * @param node
	 * @param target
	 * @param radius2
	 * @param vec
	 * @return A vector with the points or null, if there weren't any points
	 * in that radius 
	 */
	final protected void collectInRadius(
		KdNode node, 
		double[] target, 
		double radius2,
		Collection<V> vec
	) {
		// If we reached a leaf, perform a linear search
		if (node instanceof KdLeaf) {
			for (int i = node.getStartIndex(); i <= node.getEndIndex(); i++) {
				double[] pos = getPos(i);
				double dist2 = distance2(pos, target);
				if (dist2 != 0 && dist2 < radius2) {
					vec.add(points.get(i));
				}
			}
			return;
		}
		// Traverse into child closer to query HasPosition
		if (isSmaller(target, node.splitPos, node.dim)) {
			// Traverse left child
			collectInRadius(node.children[0], target, radius2, vec);
			// Traverse right child, if it is possible that it contains a nearer HasPosition
			if (Math.abs(node.splitPos[node.dim] - target[node.dim]) < radius2) {
				collectInRadius(node.children[1], target, radius2, vec);
			}
		} else {
			// Traverse right child
			collectInRadius(node.children[1], target, radius2, vec);
			// Traverse left child, if it is possible that it contains a nearer HasPosition
			if (Math.abs(node.splitPos[node.dim] - target[node.dim]) < radius2) {
				collectInRadius(node.children[0], target, radius2, vec);
			}
		}
	}
	
	/**
	 * Returns the k nearest points around the given HasPosition p
	 * @param p
	 * @param knearest
	 * @return An array containing the points or null
	 */
	final public Collection<V> collectKNearest(double[] p, int knearest) {
		Collection<V> result = new LinkedList<V>();
		if( knearest <= 0 ) { 
			return result;
		} 
		if( points.size() <= knearest ) {
			return points;
		}
		// Create a priority queue
		KdPQueue<V> pq = new KdPQueue<V>(knearest);
		// Start the collectKNearest search
		pq = collectKNearest(root, p, pq);
		for (int i = 0; i < pq.getNumberItems(); i++) {
            result.add(pq.pollSplitPos());
		}
	    return result;
	}

	/**
	 * Implementation of the collectKNearest functionality.
	 * @param node
	 * @param target
	 * @param pq
	 * @return
	 */
	private KdPQueue<V> collectKNearest(KdNode node, double[] target, KdPQueue<V> pq) {
		// If we reached a leaf, perform a linear search
		if (node instanceof KdLeaf) {
            int s = node.getStartIndex(); int e = node.getEndIndex();
            for (int i = s ; i <= e; i++) {
            	double[] pos = getPos(i);
				double dist2 = distance2(pos, target);
				if (dist2 != 0 && dist2 < pq.getMaximumDistance()) {
					pq.add(dist2, points.get(i));
				}
			}
			return pq;
		}
		// Traverse into child closer to query HasPosition
		if (isSmaller(target, node.splitPos, node.dim)) {
			// Traverse left child
			pq = collectKNearest(node.children[0], target, pq);
			// Traverse right child, if it is possible that it contains a nearer HasPosition
			if (Math.abs(node.splitPos[node.dim] - target[node.dim]) < pq
					.getMaximumDistance()) {
				pq = collectKNearest(node.children[1], target, pq);
			}
		} else {
			// Traverse right child
			pq = collectKNearest(node.children[1], target, pq);
			// Traverse left child, if it is possible that it contains a nearer HasPosition
			if (Math.abs(node.splitPos[node.dim] - target[node.dim]) < pq
					.getMaximumDistance()) {
				pq = collectKNearest(node.children[0], target, pq);
			}
		}
		return pq;
	}

	/**
	 * Calculate the distance between two points in three dimensional space
	 * @param p1
	 * @param p2
	 * @return
	 */
	final public double distance2(final double[] p1, final double[] p2) {
		return Rn.euclideanDistance(p1, p2);
	}

	/**
	 * Dump the kd-tree in text form
	 */
	public void dump() {
		dump(root, 0);
	}

	protected void dump(KdNode node, int depth) {
		for (int i = 0; i < depth; i++)
			System.out.print(" ");
		if (node != null) {
			System.out.println(node);
			if (node.getClass() == KdNode.class) {
				dump(node.children[0], depth + 1);
				dump(node.children[1], depth + 1);
			}
		} else
			System.out.println("Null");
	}

	/**
	 * Compare to 3 dimensional numbers in composite-number space
	 * Hence 
	 * @param a
	 * @param b
	 * @param dim
	 * @return true if a is smaller than b, false otherwise
	 */
	final boolean isSmaller(double[] a, double[] b, int dim) {
		return a[dim] < b[dim];
//		if (a[dim] < b[dim])
//			return true;
//		int codim = (dim + 1) % 3;
//		if (a[dim] == b[dim] && a[codim] < b[codim])
//			return true;
//		codim = (dim + 2) % 3;
//        return a[dim] == b[dim] && a[codim] < b[codim];
    }


	/**
	 * The points in the interval [start..end] are sorted around the median
	 * Hence all points < median are to the left of the array and all points > median to the right
	 * This is performed at the given dimension
	 * @param dim
	 * @param start
	 * @param end
	 * @return
	 */
	protected int medianSplit(int start, int end, int dim) {
		int lower, upper;
		int mid = (start + end) / 2;
		V x = null;
		V y = null;
		while (start < end) {
			x = points.get(mid);
			lower = start;
			upper = end;
			do {
				double[] xPos = posA.get(x, emptySet);
				while (isSmaller(getPos(lower), xPos, dim)) 
					lower++;
				while (isSmaller(xPos, getPos(upper), dim)) 
					upper--;
				if (lower <= upper) {
					// swap
					y = points.get(lower);
					points.set(lower, points.get(upper));
					points.set(upper, y);
					lower++;
					upper--;
				}
			} while (lower <= upper);
			if (upper < mid)
				start = lower;
			if (mid < lower)
				end = upper;
		}
		// shift median to the right to allow multiple points on one axis
		// while (mid<last  && points.get(mid)[dim] == points[mid+1][dim]) mid++;
		return mid;
	}
	
	
	
	/**
	 * Splits the points around the value pos at dimension dim returning the range
	 * where the exact pos matched. Hence:
	 * points[start..lowerMid-1] < pos = points[lowerMid...upperMid-1] < points[upperMid..end]
	 * 
	 * @param start
	 * @param end
	 * @param dim
	 * @param pos
	 * @return
	 */
	protected int[] posSplit(int start, int end, int dim, double pos) {
		V tmp;
		int lowerMid;
		int upperMid;

		int lower = start;
		int upper = end;
		do {
			while (lower < end && getPos(lower)[dim] < pos) 
				lower++;
			while (upper >= start && pos <= getPos(upper)[dim] ) 
				upper--;
			if (lower <= upper) {
				// swap
				tmp = points.get(lower);
				points.set(lower, points.get(upper));
				points.set(upper, tmp);
				lower++;
				upper--;
			}
		} while (lower <= upper);
		lowerMid = lower;
		upper = end;
		do {
			while (lower < end && getPos(lower)[dim] <= pos) 
				lower++;
			while (upper >= lowerMid && pos < getPos(upper)[dim]) 
				upper--;
			if (lower <= upper) {
				// swap
				tmp = points.get(lower);
				points.set(lower, points.get(upper));
				points.set(upper, tmp);
				lower++;
				upper--;
			}
		} while (lower <= upper);
		upperMid = lower;
		int[] result = {lowerMid - 1, upperMid - 1};
		return result;
	}


	
	/**
	 * The actual kd-Tree Algorithm using the median as a cutting plane
	 * 
	 * This leads to a O(log n) height
	 * 
	 * @param start
	 * @param end
	 * @param depth
	 * @return
	 */
	protected KdNode buildKdTree(int start, int end, int depth) {
		if (end - start < maxBucketSize) {
			// We are finished when there are less than maxBucketSize points in
			// our interval
			return new KdLeaf(start, end);
		}

		// Otherwise we split the pointset at the given dimension around the
		// median
		int dim = depth % 3;
		int median = medianSplit(start, end, dim);

		// And continue recursively with the two subsets
		KdNode result = new KdNode();
		result.dim = dim; // store dimension
		result.splitPos = getPos(median);; // store the position of the plane
		result.children[0] = buildKdTree(start, median, depth + 1); // points below or on our splitting line
		result.children[1] = buildKdTree(median + 1, end, depth + 1); // points above our splitting line
		return result;
	}

	int getLongestDim(double[][] bbox) {
		int dim;
		double[] diff = new double[]{bbox[1][0] - bbox[0][0], bbox[1][1] - bbox[0][1], bbox[1][2] - bbox[0][2]};
		if (diff[0] > diff[1]) {
			if (diff[0] > diff[2]) {
				dim = 0;
			} else {
				dim = 2;
			}
		} else {
			if (diff[1] > diff[2]) {
				dim = 1;
			} else {
				dim = 2;
			}
		}
		return dim;
	}

	/**
	 * This kd-Tree does not use the median as a split plane
	 * but rather chooses the midpoint of the bounding box on the
	 * longest axis. If the midpoint is outside the points use the min/max HasPosition
	 * accordingly.
	 * 
	 * This leads to a better aspect ratio of the cells but on the other hand
	 * can lead to O(n) tree depth
	 * 
	 * @param start
	 * @param end
	 * @param bbox
	 * @return
	 */
	protected KdNode buildKdTree(int start, int end, double[][] bbox) {
		if (end - start < maxBucketSize) {
			// We are finished when there are less than maxBucketSize points in
			// our interval
			return new KdLeaf(start, end);
		}

		// getBspPos().get the longest axis to split at
		int dim = getLongestDim(bbox);

		// Caluclate the midpoint along the axis 
		double midpoint = (bbox[0][dim] + bbox[1][dim]) / 2.0f;

		// getBspPos().get the actual minimal and maximal position of the pointset along the axis
		double min = getPos(start)[dim];
		double max = min;
		for (int i = start; i < end; i++) {
			double val = getPos(i)[dim];
			if (val < min) {
				min = val;
			} else if (val > max) { //:TODO this can be the source of the bugs in KDTree was only >!!
				max = val;
			}
		}

		// Now we generate the node
		KdNode result = new KdNode();
		
		result.splitPos = new double[3];
		// If the midpoint is outside of our pointset move it to maximum/minimum
		if (midpoint < min) {
			result.splitPos[dim] = min;
		} else if (midpoint > max) {
			result.splitPos[dim] = max;
		} else {
			result.splitPos[dim] = midpoint;
		}

		// Permute the points that we have two sets cut along our splitPos
		int[] split;
		split = posSplit(start, end, dim, result.splitPos[dim]);

		// either choose as split index
		int mid;
		if (midpoint < min)  
			mid = start;
		else if (midpoint > max)  
			mid = end;
		// if we did not cut the getBspPos().set in the mid use this as split index
		else if (split[0] > (start + end) / 2) 
			mid = split[0];
		else if (split[1] < (start + end) / 2) 
			mid = split[1];
		// otherwise take the real median if its within the range
		else 
			mid = (start + end) / 2;
		
		result.dim = dim; // store dimension

		// resize bbox and continue recursively on one side of the plane
		double oldMax = bbox[1][dim];
		bbox[1][dim] = result.splitPos[dim];
		result.children[0] = buildKdTree(start, mid, bbox);
		bbox[1][dim] = oldMax;

		// accordingly to the other side of the plane
		double oldMin = bbox[0][dim];
		bbox[0][dim] = result.splitPos[dim];
		result.children[1] = buildKdTree(mid + 1, end, bbox);
		bbox[0][dim] = oldMin;

		return result;
	}

	/**
	 * Compute the bounding box of the points
	 * @return
	 */
	public double[][] getBBox() {
		double[][] result = new double[2][];
		
		double[] pos0 = getPos(0);
		result[0] = pos0.clone();
		result[1] = pos0.clone();
		for (int i = 1; i < points.size(); i++) {
			double[] posI = getPos(i);
			for (int d = 0; d < 3; d++) {
				if (result[0][d] > posI[d]) {
					result[0][d] = posI[d];
				}
				if (result[1][d] < posI[d]) {
					result[1][d] = posI[d];
				}
			}
		}
		return result;
	}

	private Collection<KdLeaf> getLeafs(KdNode root) {
		if (root instanceof KdLeaf)
			return Collections.singleton((KdLeaf) root);
		else {
			LinkedList<KdLeaf> result = new LinkedList<KdLeaf>();
			result.addAll(getLeafs(root.getChildren()[0]));
			result.addAll(getLeafs(root.getChildren()[1]));
			return result;
		}
	}

	public Collection<KdLeaf> getLeafs() {
		return getLeafs(getRoot());
	}
	
	
	private double[] getPos(int index) {
		double[] pos = posA.get(points.get(index), emptySet);
		if (pos.length == 4) {
			double hom = pos[3];
			if (hom == 0) hom = 1.0;
			return new double[] {pos[0] / hom, pos[1] / hom, pos[2] / hom};
		} else {
			return pos;
		}
	}
	

}
