package de.jtem.halfedgetools.nurbs;

import java.util.Arrays;

import de.jreality.math.Rn;

public class NURBSChristoffelUtility {
	
	public static ChristoffelInfo christoffel(NURBSSurface ns, double u, double v){
		
		ChristoffelInfo cI = new ChristoffelInfo();
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
		cI.setSu(SKL[1][0]);
		cI.setSv(SKL[0][1]);
		cI.setSuv(SKL[1][1]);
		if(p <= 1) {
			cI.setSuu(new double[]{0,0,0});
		} else {
			cI.setSuu(SKL[2][0]);
		}
		if(q <= 1) {
			cI.setSvv(new double[]{0,0,0});
		} else {
			cI.setSvv(SKL[0][2]);
		}
//		System.out.println("Suu: "+ Arrays.toString(cI.Suu));
//		System.out.println("Svv: "+ Arrays.toString(cI.Svv));
//		System.out.println("Suv: "+ Arrays.toString(cI.Suv));
		
		// partial derivatives of the metric tensor
		
		double Eu = 2 * Rn.innerProduct(cI.Suu, cI.Su);
		double Ev = 2 * Rn.innerProduct(cI.Suv, cI.Su);
		double Fu = Rn.innerProduct(cI.Suu, cI.Sv) + Rn.innerProduct(cI.Su, cI.Suv);
		double Fv = Rn.innerProduct(cI.Svv, cI.Su) + Rn.innerProduct(cI.Sv, cI.Suv);
		double Gu = 2 * Rn.innerProduct(cI.Suv, cI.Sv);
		double Gv = 2 * Rn.innerProduct(cI.Svv, cI.Sv);
		
		// inverse of the metric tensor
		
		double detMetric = Rn.innerProduct(cI.Su, cI.Su) * Rn.innerProduct(cI.Sv, cI.Sv) - 2 * Rn.innerProduct(cI.Su, cI.Sv);
		double g11 = 1/detMetric * Rn.innerProduct(cI.Sv, cI.Sv);
		double g12 = -1/detMetric * Rn.innerProduct(cI.Su, cI.Sv);
		double g21 = -1/detMetric * Rn.innerProduct(cI.Su, cI.Sv);
		double g22 = 1/detMetric * Rn.innerProduct(cI.Su, cI.Su);
		
		cI.setG111(0.5 * (g11 * Eu + g12 * (2 *Fu - Ev)));
		cI.setG121(0.5 * (g11 * Ev + g12 * Gu));
		cI.setG112(0.5 * (g21 * Eu + g22 * (2 *Fu - Ev)));
		cI.setG122(0.5 * (g21 * Ev + g22 * Gu));
		cI.setG211(cI.G121);
		cI.setG212(cI.G122);
		cI.setG221(0.5 * (g11 * (2 * Fv - Gu) + g12 * Gv));
		cI.setG222(0.5 * (g21 * (2 * Fv - Gu) + g22 * Gv));
//		System.out.println("G111: "+ cI.G111);
//		System.out.println("G112: "+ cI.G112);
//		System.out.println("G121: "+ cI.G121);
//		System.out.println("G122: "+ cI.G122);
//		System.out.println("G211: "+ cI.G211);
//		System.out.println("G222: "+ cI.G222);
		
		return cI;
	}

}
