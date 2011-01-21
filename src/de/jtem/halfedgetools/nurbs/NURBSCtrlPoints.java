package de.jtem.halfedgetools.nurbs;


public class NURBSCtrlPoints {
	
	private double[] point;
	private int index;
	
	public NURBSCtrlPoints(){
		point = null;
		index = 0;
	}
	
	public NURBSCtrlPoints(double[] p, int i) {
		point = p;
		index = i;
	}
	
	public double[] getPoint(){
		return  point;
	}
	
	public int getIndex(){
		return index;
	}
	
	public void setPoint(double [] p){
		point = p;
	}
	
	public void setIndex(int i){
		index = i;
	}
	
	public static String toString(double array[]) {
		String str = new String("");
		for (int i = 0; i < array.length; i++) {
			str = str + " " + array[i];
		}
		return str;
	}
	
	public String toString(){
		
		return "ctrl point: "+ NURBSCtrlPoints.toString(point) + " index: " + index ;
	}

}
