package de.jtem.halfedgetools.nurbs;

import java.util.LinkedList;

public class IndexedCurveList {
	
	protected int index;
	protected LinkedList<IntersectionPoint> curveList;
	
	
	public IndexedCurveList(){
		
	}
	
	public IndexedCurveList(int i, LinkedList<IntersectionPoint> cL){
		index = i;
		curveList = cL;
	}


	public int getIndex() {
		return index;
	}


	public void setIndex(int index) {
		this.index = index;
	}


	public LinkedList<IntersectionPoint> getCurveList() {
		return curveList;
	}


	public void setCurveList(LinkedList<IntersectionPoint> curveList) {
		this.curveList = curveList;
	}
	
	
}
