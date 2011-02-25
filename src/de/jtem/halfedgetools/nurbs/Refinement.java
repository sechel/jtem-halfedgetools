package de.jtem.halfedgetools.nurbs;

public class Refinement {
	
	protected double[][][] point;
	protected double hu;
	protected double hv;
	protected int depth;
	protected double eps;
	int indexI;
	int indexJ;
	double u1;
	double v1;
	
	public Refinement(){
		
	}
	
	public Refinement(double[][][] p,double u, double v,int d,double e, int i, int j){
		point =p;
		hu = u;
		hv = v;
		depth = d;
		eps = e;
		indexI = i;
		indexJ = j;
	}
}
