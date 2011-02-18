package de.jtem.halfedgetools.nurbs;

import java.util.Arrays;

public class CurvatureInfo {
	
	protected double [] Su;
	protected double [] Sv;
	protected double [] Suu;
	protected double [] Suv;
	protected double [] Svv;

	protected double [][] curvatureDirectionsManifold;
	protected double [][] curvatureDirectionsDomain;
	protected double minCurvature;
	protected double maxCurvature;
	protected double [][] Weingartenoperator;
	protected double GaussCurvature;
	protected double MainCurvature;
	
	public CurvatureInfo(double[][]cM,double[][]cD, double l, double m, double [][]W){
		curvatureDirectionsManifold = cM;
		curvatureDirectionsDomain = cD;
		minCurvature = l;
		maxCurvature = m;
		Weingartenoperator = W;
		
	}
	
	public CurvatureInfo(){
		curvatureDirectionsManifold = null;
		curvatureDirectionsDomain = null;
		minCurvature = 0;
		maxCurvature = 0;
		Weingartenoperator = null;
	}
	
	public double[] getSuu() {
		return Suu;
	}
	

	public void setSuu(double[] suu) {
		Suu = suu;
	}

	public double[] getSuv() {
		return Suv;
	}

	public void setSuv(double[] suv) {
		Suv = suv;
	}

	public double[] getSvv() {
		return Svv;
	}

	public void setSvv(double[] svv) {
		Svv = svv;
	}

	public double[] getSu() {
		return Su;
	}

	public void setSu(double[] su) {
		Su = su;
	}

	public double[] getSv() {
		return Sv;
	}

	public void setSv(double[] sv) {
		Sv = sv;
	}
	
	public double[][] getCurvatureDirectionsManifold() {
		return curvatureDirectionsManifold;
	}

	public void setCurvatureDirectionsManifold(double[][] cuvatureDirectionsManifold) {
		this.curvatureDirectionsManifold = cuvatureDirectionsManifold;
	}

	public double[][] getCurvatureDirectionsDomain() {
		return curvatureDirectionsDomain;
	}

	public void setCurvatureDirectionsDomain(double[][] cuvatureDirectionsDomain) {
		this.curvatureDirectionsDomain = cuvatureDirectionsDomain;
	}

	public double getMinCurvature() {
		return minCurvature;
	}

	public void setMinCurvature(double lambda) {
		this.minCurvature = lambda;
	}

	public double getMaxCurvature() {
		return maxCurvature;
	}

	public void setMaxCurvature(double my) {
		this.maxCurvature = my;
	}

	public double[][] getWeingartenOperator() {
		return Weingartenoperator;
	}

	public void setWeingartenOperator(double[][] weingartenoperator) {
		Weingartenoperator = weingartenoperator;
	}
	
	public double getGaussCurvature() {
		return GaussCurvature;
	}

	public void setGaussCurvature(double gaussCurvature) {
		GaussCurvature = gaussCurvature;
	}

	public double getMeanCurvature() {
		return MainCurvature;
	}

	public void setMeanCurvature(double mainCurvature) {
		MainCurvature = mainCurvature;
	}
	
	@Override
	public String toString(){
		String str = new String();
		System.out.println("Su =  "+Arrays.toString(Su));
		System.out.println("Sv = "+Arrays.toString(Sv));
		System.out.println("Suu = "+Arrays.toString(Suu));
		System.out.println("Suv = "+Arrays.toString(Suv));
		System.out.println("Svv = "+Arrays.toString(Svv));
		System.out.println("curvature directions at the manifold:");
		if(Weingartenoperator[0][1] == 0 && minCurvature == maxCurvature){
			System.out.println("umbilic point");
		} else {
			System.out.println(Arrays.toString(curvatureDirectionsManifold[0]));
			System.out.println(Arrays.toString(curvatureDirectionsManifold[1]));
		}
		System.out.println("curvature directions in the domain:");
		if(Weingartenoperator[0][1] == 0 && minCurvature == maxCurvature){
			System.out.println("umbilic point");
		}else{
		System.out.println(Arrays.toString(curvatureDirectionsDomain[0]));
		System.out.println(Arrays.toString(curvatureDirectionsDomain[1]));
		}
		System.out.println("curvatures:");
		System.out.println("lambda: "+minCurvature);
		System.out.println("my: "+maxCurvature);
		System.out.println("shapeoperator:");
		System.out.println(Weingartenoperator[0][0]+"  "+Weingartenoperator[0][1]);
		System.out.println(Weingartenoperator[1][0]+"  "+Weingartenoperator[1][1]);
		System.out.println("Gauss curvature: ");
		System.out.println(GaussCurvature);
		System.out.println(" Main curvature: ");
		System.out.println(MainCurvature);
		return str;
	}
	
}


