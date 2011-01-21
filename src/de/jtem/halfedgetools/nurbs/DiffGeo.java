package de.jtem.halfedgetools.nurbs;

import java.util.Arrays;

public class DiffGeo {
	
	protected double [] Su;
	protected double [] Sv;
	protected double [] Suu;
	protected double [] Suv;
	protected double [] Svv;
	
	

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

	protected double [][] curvatureDirectionsManifold;
	protected double [][] curvatureDirectionsDomain;
	protected double lambda;
	protected double my;
	protected double [][] Weingartenoperator;
	protected double GaussCurvature;
	protected double MainCurvature;
	


	public DiffGeo(double[][]cM,double[][]cD, double l, double m, double [][]W){
		curvatureDirectionsManifold = cM;
		curvatureDirectionsDomain = cD;
		lambda = l;
		my = m;
		Weingartenoperator = W;
		
	}
	
	public DiffGeo(){
		curvatureDirectionsManifold = null;
		curvatureDirectionsDomain = null;
		lambda = 0;
		my = 0;
		Weingartenoperator = null;
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

	public double getLambda() {
		return lambda;
	}

	public void setLambda(double lambda) {
		this.lambda = lambda;
	}

	public double getMy() {
		return my;
	}

	public void setMy(double my) {
		this.my = my;
	}

	public double[][] getWeingartenoperator() {
		return Weingartenoperator;
	}

	public void setWeingartenoperator(double[][] weingartenoperator) {
		Weingartenoperator = weingartenoperator;
	}
	
	public double getGaussCurvature() {
		return GaussCurvature;
	}

	public void setGaussCurvature(double gaussCurvature) {
		GaussCurvature = gaussCurvature;
	}

	public double getMainCurvature() {
		return MainCurvature;
	}

	public void setMainCurvature(double mainCurvature) {
		MainCurvature = mainCurvature;
	}
	
	public String toString(){
		String str = new String();
		System.out.println("Su =  "+Arrays.toString(Su));
		System.out.println("Sv = "+Arrays.toString(Sv));
		System.out.println("Suu = "+Arrays.toString(Suu));
		System.out.println("Suv = "+Arrays.toString(Suv));
		System.out.println("Svv = "+Arrays.toString(Svv));
		System.out.println("curvature directions at the manifold:");
		if(Weingartenoperator[0][1] == 0 && lambda == my){
			System.out.println("umbilic point");
		}else{
		System.out.println(Arrays.toString(curvatureDirectionsManifold[0]));
		System.out.println(Arrays.toString(curvatureDirectionsManifold[1]));
		}
		System.out.println("curvature directions in the domain:");
		if(Weingartenoperator[0][1] == 0 && lambda == my){
			System.out.println("umbilic point");
		}else{
		System.out.println(Arrays.toString(curvatureDirectionsDomain[0]));
		System.out.println(Arrays.toString(curvatureDirectionsDomain[1]));
		}
		System.out.println("curvatures:");
		System.out.println("lambda: "+lambda);
		System.out.println("my: "+my);
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


