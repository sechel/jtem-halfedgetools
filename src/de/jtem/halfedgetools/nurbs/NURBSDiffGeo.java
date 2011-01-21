package de.jtem.halfedgetools.nurbs;

import java.util.Arrays;

import de.jreality.math.Rn;

public class NURBSDiffGeo {
		
	
	/**
	 * 
	 * @param ns
	 * @param u
	 * @param v
	 * @return lambda my K H
	 */
	
	public static diffGeo curvatureAndDirections(NURBSSurface ns, double u, double v){
		diffGeo dG = new diffGeo();
		double [] FFs = new double[6];
		double[] U = ns.U;
		double[] V = ns.V;
		int p = ns.p;
		int q = ns.q;
		System.out.println("p: "+p+" q: "+q);
		double[][][]SKL = new double[p+1][q+1][3];
		double [][][]cm = NURBSAlgorithm.affineCoords(ns.controlMesh);
		NURBSAlgorithm.SurfaceDerivatives(1, p, U, 1, q, V, cm, u, v, 4, SKL);
		dG.setSu(SKL[1][0]);
		dG.setSv(SKL[0][1]);
		System.out.println("S "+ Arrays.toString(SKL[0][0]));
		System.out.println("Su "+ Arrays.toString(SKL[1][0]));
		System.out.println("Sv "+ Arrays.toString(SKL[0][1]));
		System.out.println("Suv "+ Arrays.toString(SKL[1][1]));
		double E = Rn.innerProduct(SKL[1][0], SKL[1][0]);
		System.out.println("E "+ E);
	
		double F = Rn.innerProduct(SKL[1][0], SKL[0][1]);
		System.out.println("F "+ F);
		double G = Rn.innerProduct(SKL[0][1], SKL[0][1]);
		System.out.println("G "+ G);
		double[] N = new double[3];
		N =	Rn.crossProduct(N, SKL[1][0], SKL[0][1]);
		N= Rn.normalize(null, N);
		System.out.println("N "+ Arrays.toString(N));
		double l;
		if(SKL.length < 3){
			l = 0;
		}else{
			l = Rn.innerProduct(N, SKL[2][0]);
		}
		System.out.println("l "+ l);
		double m = Rn.innerProduct(N, SKL[1][1]);
		System.out.println("m "+ m);
		double n;
		if(SKL[0].length < 3){
			n = 0;
		}else{
			n = Rn.innerProduct(N, SKL[0][2]);
		}
		System.out.println("n "+ n);
		FFs[0] = E;
		FFs[1] = F;
		FFs[2] = G;
		FFs[3] = l;
		FFs[4] = m;
		double[][] W = new double[2][2];
		System.out.println("factor: " +1/(E*G-F*F));
		double a11 = (G*l-F*m)/(E*G-F*F);
		double a12 = (G*m-F*n)/(E*G-F*F);
		double a21 = (E*m-F*l)/(E*G-F*F);
		double a22 = (E*n-F*m)/(E*G-F*F);
		W[0][0] = a11;
		W[0][1] = a12;
		W[1][0] = a21;
		W[1][1] = a22;
		dG.setWeingartenoperator(W);
		
		//lambda
		double lambda = (a11 + a22)/2 + Math.sqrt((a11-a22) * (a11-a22) + 4 * a12 * a21) / 2;
		dG.setLambda(lambda);
		//my
		double my = (a11 + a22)/2 - Math.sqrt((a11-a22) * (a11-a22) + 4 * a12 * a21) / 2;
		dG.setMy(my);
		//K	
		dG.setGaussCurvature(a11*a22-a12*a21);
		//H
		dG.setMainCurvature((a11 + a22) / 2);
		
		double[][] curvatureVectorDomain = new double[2][2];
		if(a12 != 0){
			curvatureVectorDomain[0][0] = 1; 
			curvatureVectorDomain[0][1] = (lambda - a11) / a12; 
	
		}
		else if(a22 != lambda ){
			curvatureVectorDomain[0][0] = 1; 
			curvatureVectorDomain[0][1] = 0; 
		}
		else if(a11 != lambda){
			curvatureVectorDomain[0][0] = 0; 
			curvatureVectorDomain[0][1] = 1;
		}
		if(a12 != 0){
			curvatureVectorDomain[1][0] = 1; 
			curvatureVectorDomain[1][1] = (my - a11) / a12; 
		}
		else if(a22 != my){
			curvatureVectorDomain[1][0] = 1; 
			curvatureVectorDomain[1][1] = 0; 
		}
		else if(a11 != my){
			curvatureVectorDomain[1][0] = 0; 
			curvatureVectorDomain[1][1] = 1; 
		}
		dG.setCurvatureDirectionsDomain(curvatureVectorDomain);
		double[][] curvatureVectorManifold = new double[2][3];
		curvatureVectorManifold[0] = Rn.add(null, Rn.times(null, curvatureVectorDomain[0][0], SKL[1][0]), Rn.times(null, curvatureVectorDomain[0][1], SKL[0][1]));
		curvatureVectorManifold[1] = Rn.add(null, Rn.times(null, curvatureVectorDomain[1][0], SKL[1][0]), Rn.times(null, curvatureVectorDomain[1][1], SKL[0][1]));
		dG.setCurvatureDirectionsManifold(curvatureVectorManifold);
		
		return dG;
	}
	
	
	public static diffGeo curvatureAndDirections1(NURBSSurface ns, double u, double v){
		diffGeo dG = new diffGeo();
		double [] FFs = new double[6];
		double[] U = ns.U;
		double[] V = ns.V;
		int p = ns.p;
		int q = ns.q;
		double[][][]SKL1 = new double[p+1][q+1][4];
		double[][][]SKL = new double[p+1][q+1][3];
		

		NURBSAlgorithm.SurfaceDerivatives(1, p, U, 1, q, V, ns.controlMesh, u, v, 4, SKL1);
		double [][][] Aders = new double[SKL1.length][SKL1[0].length][3];
		double [][] wders = new double[SKL1.length][SKL1[0].length];
		for (int i = 0; i < SKL1.length; i++) {
			for (int j = 0; j < SKL1[0].length; j++) {
				wders[i][j]=SKL1[i][j][3];
				Aders[i][j][0] = SKL1[i][j][0];
				Aders[i][j][1] = SKL1[i][j][1];
				Aders[i][j][2] = SKL1[i][j][2];
			}
		}
		NURBSAlgorithm.RatSurfaceDerivs(Aders, wders, p+q, SKL);
		dG.setSu(SKL[1][0]);
		dG.setSv(SKL[0][1]);
		dG.setSuu(SKL[2][0]);
		dG.setSuv(SKL[1][1]);
		dG.setSvv(SKL[0][2]);
		System.out.println("S "+ Arrays.toString(SKL[0][0]));
		System.out.println("norm: "+ Math.sqrt(Rn.innerProduct(SKL[0][0], SKL[0][0])));
		System.out.println("Su "+ Arrays.toString(SKL[1][0]));
		System.out.println("Sv "+ Arrays.toString(SKL[0][1]));
		System.out.println("Suv "+ Arrays.toString(SKL[1][1]));
		double E = Rn.innerProduct(SKL[1][0], SKL[1][0]);
		System.out.println("E "+ E);
	
		double F = Rn.innerProduct(SKL[1][0], SKL[0][1]);
		System.out.println("F "+ F);
		double G = Rn.innerProduct(SKL[0][1], SKL[0][1]);
		System.out.println("G "+ G);
		double[] N = new double[3];
	
		N =	Rn.crossProduct(N, SKL[1][0], SKL[0][1]);
		N= Rn.normalize(null, N);
		System.out.println("N "+ Arrays.toString(N));
		double l;
		if(SKL.length < 3){
			l = 0;
		}else{
			l = Rn.innerProduct(N, SKL[2][0]);
		}
		System.out.println("l "+ l);
		double m = Rn.innerProduct(N, SKL[1][1]);
		System.out.println("m "+ m);
		double n;
		if(SKL[0].length < 3){
			n = 0;
		}else{
			n = Rn.innerProduct(N, SKL[0][2]);
		}
		
		System.out.println("n "+ n);
		FFs[0] = E;
		FFs[1] = F;
		FFs[2] = G;
		FFs[3] = l;
		FFs[4] = m;
		double[][] W = new double[2][2];
		System.out.println("factor: " +1/(E*G-F*F));
		double a11 = (G*l-F*m)/(E*G-F*F);
		double a12 = (G*m-F*n)/(E*G-F*F);
		double a21 = (E*m-F*l)/(E*G-F*F);
		double a22 = (E*n-F*m)/(E*G-F*F);
		W[0][0] = a11;
		W[0][1] = a12;
		W[1][0] = a21;
		W[1][1] = a22;
		dG.setWeingartenoperator(W);
		
		//lambda
		double lambda = (a11 + a22)/2 + Math.sqrt((a11-a22) * (a11-a22) + 4 * a12 * a21) / 2;
		dG.setLambda(lambda);
		//my
		double my = (a11 + a22)/2 - Math.sqrt((a11-a22) * (a11-a22) + 4 * a12 * a21) / 2;
		dG.setMy(my);
		//K	
		dG.setGaussCurvature(a11*a22-a12*a21);
		//H
		dG.setMainCurvature((a11 + a22) / 2);
		
		double[][] curvatureVectorDomain = new double[2][2];
		if(a12 != 0){
			curvatureVectorDomain[0][0] = 1; 
			curvatureVectorDomain[0][1] = (lambda - a11) / a12; 
	
		}
		else if(a22 != lambda ){
			curvatureVectorDomain[0][0] = 1; 
			curvatureVectorDomain[0][1] = 0; 
		}
		else if(a11 != lambda){
			curvatureVectorDomain[0][0] = 0; 
			curvatureVectorDomain[0][1] = 1;
		}
		if(a12 != 0){
			curvatureVectorDomain[1][0] = 1; 
			curvatureVectorDomain[1][1] = (my - a11) / a12; 
		}
		else if(a11 != my){
			curvatureVectorDomain[1][0] = 0; 
			curvatureVectorDomain[1][1] = 1; 
		}
		else if(a22 != my){
			curvatureVectorDomain[1][0] = 1; 
			curvatureVectorDomain[1][1] = 0; 
		}
		dG.setCurvatureDirectionsDomain(curvatureVectorDomain);
		double[][] curvatureVectorManifold = new double[2][3];
		curvatureVectorManifold[0] = Rn.add(null, Rn.times(null, curvatureVectorDomain[0][0], SKL[1][0]), Rn.times(null, curvatureVectorDomain[0][1], SKL[0][1]));
		curvatureVectorManifold[1] = Rn.add(null, Rn.times(null, curvatureVectorDomain[1][0], SKL[1][0]), Rn.times(null, curvatureVectorDomain[1][1], SKL[0][1]));
		dG.setCurvatureDirectionsManifold(curvatureVectorManifold);
		
		return dG;
	}
	
	
	public static void main(String[] args){
		NURBSSurface ns = new NURBSSurface();
		
		double u = 0.125;
		double v = 0.25;
		double [] U = ns.getUKnotVector();
		double [] V = ns.getVKnotVector();
		double[][][]P = ns.getControlMesh();
		double [] S = new double[4];
		int p = ns.p;
		int q = ns.q;
		System.out.println("point in the domain");
		System.out.println("u = "+ u+" v = "+v);
		System.out.println("point at the manifold");
		NURBSAlgorithm.SurfacePoint(p, U, q, V, P, u, v, S);
		
		System.out.println(	curvatureAndDirections(ns, u, v).toString());
		System.out.println("neu");
		double []S1 = {S[0]/S[3],S[1]/S[3],S[2]/S[3]};
		System.out.println("point: "+Arrays.toString(S1));
		System.out.println(curvatureAndDirections1(ns, u, v).toString());
	}
	
	
	
	
}
