package de.jtem.halfedgetools.nurbs;

import java.util.LinkedList;

public class Refinement {
	
	protected LinkedList<double[]>umbilcs;
	protected NURBSSurface ns;
	protected double[][] point;
	protected double hu;
	protected double hv;
	protected int depth;
	int indexI;
	int indexJ;
	double u1;
	double v1;
	int counter;
	
	public Refinement(){
		
	}
	
	public Refinement(LinkedList<double[]> umb,NURBSSurface n,double u, double v,double[][] p,double hhu, double hhv,int d, int i, int j,int c){
		umbilcs = umb;
		ns = n;
		u1 =u;
		v1 = v;
		point =p;
		hu = hhu;
		hv = hhv;
		depth = d;
		indexI = i;
		indexJ = j;
		counter = c;
	}

	public LinkedList<double[]> getUmbilcs() {
		return umbilcs;
	}

	public void setUmbilcs(LinkedList<double[]> umbilcs) {
		this.umbilcs = umbilcs;
	}

}
