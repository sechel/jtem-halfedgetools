package de.jtem.halfedgetools.bsp;

import java.util.PriorityQueue;


/**
 * A priority queue containing a distance and a point value.
 * The point with the highest distance has the maximum priority 
 */
public class KdPQueue <N> {
	private PriorityQueue<PQItem> pq;
	private int numKNearest;
	
	/**
	 * Class containing a point and a distance value
	 */
	class PQItem implements Comparable<PQItem>{
		double distance;
		N splitPos;
		
		PQItem(double dist, N sPos)
		{
			distance = dist;
			splitPos = sPos;
		}
		// Sorting the points descending 
		@Override
		public int compareTo(PQItem item) {		    
			return Double.compare(item.distance, this.distance);
		}
	}
	
	/**
	 * Creates a priority queue with "numKNearest" values.
	 * @param numberItems
	 */
	KdPQueue(int numKNearest)
	{
		pq = new PriorityQueue<PQItem>(numKNearest);
		this.numKNearest = numKNearest;
	}
	
	/**
	 * Gets the distance of the HasPosition with the highest priority
	 * @return
	 */
	public double getMaximumDistance()
	{
		if(pq.size() < numKNearest) return Double.MAX_VALUE;
		PQItem item = pq.peek();
		if(item != null) return item.distance;
		else return Double.MAX_VALUE;
	}
	
	/**
	 * Adds a value to the queue
	 * @param distance
	 * @param sPos
	 */
	public void add(double distance, N sPos)
	{
		// Remove the HasPosition with the greatest distance
		if(pq.size() == numKNearest) pq.poll();
		pq.add(new PQItem(distance, sPos));
	}

	/**
	 * Gets and removes the HasPosition with the highest priority.
	 * @return
	 */
	public N pollSplitPos(){
		PQItem item = pq.poll();
		if(item != null) return item.splitPos;
		else return null;
	}
	
	/**
	 * Gets the item count
	 * @return
	 */
	public int getNumberItems() {
		return pq.size();
	}
}
