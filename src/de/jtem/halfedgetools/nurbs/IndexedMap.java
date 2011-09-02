package de.jtem.halfedgetools.nurbs;

import java.util.Map;



public class IndexedMap {
	
	protected int index;
	protected Map<IntersectionPoint, Integer> curveMap;
	
	public IndexedMap(){
		
	}
	
	public IndexedMap(int i, Map<IntersectionPoint, Integer> cM){
		index = i;
		curveMap = cM;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public Map<IntersectionPoint, Integer> getCurveMap() {
		return curveMap;
	}

	public void setCurveMap(Map<IntersectionPoint, Integer> curveMap) {
		this.curveMap = curveMap;
	}
	
	
}
