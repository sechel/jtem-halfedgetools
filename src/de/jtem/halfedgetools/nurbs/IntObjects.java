package de.jtem.halfedgetools.nurbs;

import java.util.LinkedList;


public class IntObjects {
	
	protected LinkedList<double[]> points;
	protected double[] orientation;
	protected boolean nearby;
	protected boolean maxMin;
	protected boolean cyclic = false;
	protected int umbilicIndex;
	
	public int getUmbilicIndex() {
		return umbilicIndex;
	}

	public void setUmbilicIndex(int umbilicIndex) {
		this.umbilicIndex = umbilicIndex;
	}

	public IntObjects(){
		points = null;
		orientation = null;
		nearby = false;
	}
	
	public IntObjects(LinkedList<double[]> p,double[] o,boolean n,boolean m){
		points = p;
		orientation = o;
		nearby = n;
		maxMin = m;
	}
	public IntObjects(double[] o,boolean n){
		orientation = o;
		nearby = n;
	}
	

	public LinkedList<double[]> getPoints() {
		return points;
	}

	public void setPoints(LinkedList<double[]> points) {
		this.points = points;
	}

	public double[] getOrientation() {
		return orientation;
	}

	public void setOrientation(double[] orientation) {
		this.orientation = orientation;
	}

	public boolean isNearby() {
		return nearby;
	}

	public void setNearby(boolean nearby) {
		this.nearby = nearby;
	}

	public void setCyclic(boolean b) {
		cyclic = b;
	}

}
