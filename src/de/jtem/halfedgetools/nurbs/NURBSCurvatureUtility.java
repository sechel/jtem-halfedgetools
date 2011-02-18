package de.jtem.halfedgetools.nurbs;

import de.jreality.math.Rn;

public class NURBSCurvatureUtility {
		
	
	/**
	 * 
	 * @param ns
	 * @param u
	 * @param v
	 * @return lambda my K H
	 */
	public static CurvatureInfo curvatureAndDirections(NURBSSurface ns, double u, double v){
		CurvatureInfo dG = new CurvatureInfo();
		
		double[] FFs = new double[6];
		double[] U = ns.U;
		double[] V = ns.V;
		int p = ns.p;
		int q = ns.q;
		
		double[][][]SKL1 = new double[p+1][q+1][4];
		double[][][]SKL = new double[p+1][q+1][3];

		
		int nl = ns.controlMesh.length-1;
		int ml = ns.controlMesh[0].length-1;
		NURBSAlgorithm.SurfaceDerivatives(ml, p, U, nl, q, V, ns.controlMesh, u, v, 4, SKL1);		
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
		
		
		dG.setSuv(SKL[1][1]);
		if(p <= 1) {
			dG.setSuu(new double[]{0,0,0});
		} else {
			dG.setSuu(SKL[2][0]);
		}
		if(q <= 1) {
			dG.setSvv(new double[]{0,0,0});
		} else {
			dG.setSvv(SKL[0][2]);
		}
		double E = Rn.innerProduct(SKL[1][0], SKL[1][0]);
		double F = Rn.innerProduct(SKL[1][0], SKL[0][1]);
		double G = Rn.innerProduct(SKL[0][1], SKL[0][1]);
		
		double[] normal = new double[3];
	
		Rn.crossProduct(normal, SKL[1][0], SKL[0][1]);
		Rn.normalize(normal, normal);
		
		double l = Rn.innerProduct(normal,dG.getSuu());
		double m = Rn.innerProduct(normal, SKL[1][1]);
		double n = Rn.innerProduct(normal,dG.getSvv());

		FFs[0] = E;
		FFs[1] = F;
		FFs[2] = G;
		FFs[3] = l;
		FFs[4] = m;
		double[][] W = new double[2][2];
		double a11 = (G*l-F*m)/(E*G-F*F);
		double a12 = (G*m-F*n)/(E*G-F*F);
		double a21 = (E*m-F*l)/(E*G-F*F);
		double a22 = (E*n-F*m)/(E*G-F*F);
		W[0][0] = a11;
		W[0][1] = a12;
		W[1][0] = a21;
		W[1][1] = a22;
		dG.setWeingartenOperator(W);
		
		//lambda
		double lambda = (a11 + a22)/2 + Math.sqrt((a11-a22) * (a11-a22) + 4 * a12 * a21) / 2;
		
		//my
		double my = (a11 + a22)/2 - Math.sqrt((a11-a22) * (a11-a22) + 4 * a12 * a21) / 2;
		if(lambda > my) {
			double tmp = my;
			my = lambda;
			lambda = tmp;
		}
		dG.setMinCurvature(lambda);
		dG.setMaxCurvature(my);
		//K	
		dG.setGaussCurvature(a11*a22-a12*a21);
		//H
		dG.setMeanCurvature((a11 + a22) / 2);
		
		double[][] curvatureVectorDomain = new double[2][2];
		if(a12 != 0){
			curvatureVectorDomain[0][0] = 1; 
			curvatureVectorDomain[0][1] = (lambda - a11) / a12; 
	
		}
		else if(a21 != 0){
			curvatureVectorDomain[0][0] = (lambda - a22)/a21; 
			curvatureVectorDomain[0][1] = 1; 
	
		}
		else if(Math.abs(a11 - lambda) < Math.abs(a22 - lambda) ){
			curvatureVectorDomain[0][0] = 1; 
			curvatureVectorDomain[0][1] = 0; 
		}
		else if(Math.abs(a22 - lambda) < Math.abs(a11 - lambda)){
			curvatureVectorDomain[0][0] = 0; 
			curvatureVectorDomain[0][1] = 1;
		}
		if(a12 != 0){
			curvatureVectorDomain[1][0] = 1; 
			curvatureVectorDomain[1][1] = (my - a11) / a12; 
		}
		else if(a21 != 0){
			curvatureVectorDomain[1][0] = (my - a22)/a21; 
			curvatureVectorDomain[1][1] = 1; 
	
		}
		else if(Math.abs(a11 - my) < Math.abs(a22 - my)){
			curvatureVectorDomain[1][0] = 1; 
			curvatureVectorDomain[1][1] = 0; 
		}
		else if(Math.abs(a22 - my) < Math.abs(a11 - my)){
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
	
}
