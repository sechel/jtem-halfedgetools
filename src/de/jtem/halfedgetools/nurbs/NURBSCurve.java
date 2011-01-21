package de.jtem.halfedgetools.nurbs;



public class NURBSCurve {

	private double[][] controlPoints;
	private double[] knotVector;
	private int dimAmbientSpace;
	private int deg;

	public NURBSCurve(double[] kV, int dAS,
			int d) {
		controlPoints = null;
		knotVector = kV;
		dimAmbientSpace = dAS;
		deg = d;
	}

	public NURBSCurve(double[][] cP, double[] kV, int dAS,
			int d,double[] cd) {
		controlPoints = cP;
		knotVector = kV;
		dimAmbientSpace = dAS;
		deg = d;
	}
	

	public double[][] getCtrlPoints() {
		return controlPoints;
	}

	public double[] getKnotVector() {
		return knotVector;
	}

	public int getAmbientDim() {
		return dimAmbientSpace;
	}

	public int getDeg() {
		return deg;
	}
	
	
	public void setCtrlPoints(double[][] cP) {
		controlPoints = cP;
	}

	public void setKnotVector(double[] kV) {
		knotVector = kV;
	}

	public void setDimAmbientSpace(int aS) {
		dimAmbientSpace = aS;
	}

	public void setDeg(int d) {
		deg = d;
	}
	
	
	public static String toString(double array[]) {
		String str = new String("");
		if(array == null){
			return null;
		}
		for (int i = 0; i < array.length; i++) {
			str = str + " " + array[i];
		}
		return str;
	}


	public String toString() {
		String str = new String("");
		str = str + "dimension ambient space : " + getAmbientDim() + "\n"
				+ "deg: " + getDeg() + "\n"
				+"controlPoints: " + "\n";
		for (int i = 0; i < controlPoints.length; i++) {
			str = str + NURBSCurve.toString(controlPoints[i]) + "\n";
		}
		str = str + "knotvector: ";
		for (int i = 0; i < knotVector.length; i++) {
			str = str + knotVector[i] + " ";
		}

		return str+"\n";
	}

}
