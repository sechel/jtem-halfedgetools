package de.jtem.halfedgetools.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.jtem.halfedgetools.nurbs.NURBSCurve;
import de.jtem.halfedgetools.nurbs.NURBSSurface;
import de.jtem.halfedgetools.nurbs.NURBSTrimLoop;

public class NurbsIO {

	private static class NURBSCtrlPoints {
		
		private double[] point;
		private int index;
		
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
		
		public static String toString(double array[]) {
			String str = new String("");
			for (int i = 0; i < array.length; i++) {
				str = str + " " + array[i];
			}
			return str;
		}
		
		@Override
		public String toString(){
			return "ctrl point: "+ NURBSCtrlPoints.toString(point) + " index: " + index ;
		}

	}
	
	
	public static NURBSSurface readNURBS(Reader reader) {
		LineNumberReader lnr = new LineNumberReader(reader);
		NURBSSurface ns = new NURBSSurface();
		try {

			LinkedList<NURBSTrimLoop> trimC = new LinkedList<NURBSTrimLoop>();
			LinkedList<NURBSTrimLoop> holeC = new LinkedList<NURBSTrimLoop>();
			double[] surfDomain = new double[4];

			Pattern vertex = Pattern
					.compile("v(\\s[-+]?[0-9]*\\.?[0-9]*(e-[0-9]+)?)*");
			Pattern paramU = Pattern
					.compile("parm\\su(\\s[-+]?[0-9]*\\.?[0-9]*)+");
			Pattern paramV = Pattern
					.compile("parm\\sv(\\s[-+]?[0-9]*\\.?[0-9]*)+");
			Pattern degSurf = Pattern
					.compile("deg\\s([+-]?[0-9])\\s([+-]?[0-9])");
			Pattern surf = Pattern
					.compile("surf\\s([-+]?[0-9]*\\.?[0-9]*)\\s([-+]?[0-9]*\\.?[0-9]*)\\s([-+]?[0-9]*\\.?[0-9]*)\\s([-+]?[0-9]*\\.?[0-9]*)(\\s[-+]?[0-9]*)*");
			Pattern trim = Pattern
					.compile("trim((\\s[-+]?[0-9]*\\.?[0-9]*)\\s([-+]?[0-9]*\\.?[0-9]*)(\\s[-+]?[0-9]*))*");
			Pattern hole = Pattern
					.compile("hole\\s([-+]?[0-9]*\\.?[0-9]*)\\s([-+]?[0-9]*\\.?[0-9]*)(\\s[-+]?[0-9]*)*");
			// trim loop
			Pattern curvedim = Pattern
					.compile("curv([0-9])(\\s[-+]?[0-9]*\\.?[0-9]*)*");
			Pattern vp = Pattern.compile("vp(\\s[-+]?[0-9]*\\.?[0-9]*)*");
			Pattern degCurve = Pattern.compile("deg\\s([+-]?[0-9])*");
			Pattern paramCurve = Pattern
					.compile("parm\\su(\\s[-+]?[0-9]*\\.?[0-9]*)+");
			String line = null;
			boolean surfKnot = false;
			boolean backSlash = false;
			int[] d = new int[2];
			LinkedList<NURBSCurve> curves = new LinkedList<NURBSCurve>();
			LinkedList<LinkedList<NURBSCtrlPoints>> trimCurvesList = new LinkedList<LinkedList<NURBSCtrlPoints>>();
			LinkedList<LinkedList<NURBSCtrlPoints>> holeCurvesList = new LinkedList<LinkedList<NURBSCtrlPoints>>();
			LinkedList<double[]> vList = new LinkedList<double[]>();
			LinkedList<double[]> ctrlPoints = new LinkedList<double[]>();
			LinkedList<double[]> ctrlPointsCurve = new LinkedList<double[]>();
			LinkedList<Double> surfKnotU = new LinkedList<Double>();
			LinkedList<Double> surfKnotV = new LinkedList<Double>();
			LinkedList<Integer> curveIndex = new LinkedList<Integer>();
			LinkedList<Integer> surfIndex = new LinkedList<Integer>();
			double[] curveDomain = new double[2];
			LinkedList<LinkedList<Integer>> index = new LinkedList<LinkedList<Integer>>();
			int curveDeg = 0;
			int curveDim = 0;
			int trimCounter = 0;
			int holeCounter = 0;
			int trimIndex;
			String str = new String();
			String help = new String();
			while ((line = lnr.readLine()) != null) {

				if (line.endsWith("\\")) {
					if (line.startsWith(" ")) {
						help = line.replaceFirst("\\s", "");
					} else {
						help = line;
					}
					str = str + help.replaceAll("\\\\", "");
					backSlash = true;
				} else {

					if (backSlash) {
						help = line.replaceFirst("\\s", "");
						str = str + help;
						backSlash = false;
					} else
						str = line;

					// surf
					Matcher mVertex = vertex.matcher(str); // surface vertex
					Matcher mParamU = paramU.matcher(str); // 1. knotvector
															// surface
					Matcher mParamV = paramV.matcher(str); // 2. knotvector
															// surface
					Matcher mDegSurf = degSurf.matcher(str); // degree surface
					Matcher mSurf = surf.matcher(str); // surface domain and
														// index control mesh
					Matcher mTrim = trim.matcher(str); // domain and index curve
					Matcher mHole = hole.matcher(str); // domain and index curve
					// trim loop
					Matcher mVp = vp.matcher(str); // curve vertex
					Matcher mDegCurve = degCurve.matcher(str); // degree curve
					Matcher mCurveDim = curvedim.matcher(str); // dimension
																// ambient space
																// curve
					Matcher mParamCurve = paramCurve.matcher(str); // knotvector
																	// curve

					String[] splitLineFirst = str.split("\\s");
					int space = 0;
					for (int i = 0; i < splitLineFirst.length; i++) {
						if (splitLineFirst[i].isEmpty()) {
							space = space + 1;
						}
					}

					String[] splitLine = new String[splitLineFirst.length
							- space];
					int counter = 0;
					for (int i = 0; i < splitLineFirst.length; i++) {
						if (splitLineFirst[i].isEmpty()) {
							counter = counter + 1;
						} else {
							splitLine[i - counter] = splitLineFirst[i];
						}
					}

					if (mVertex.matches()) {
						double[] v = { 0, 0, 0, 1 };
						for (int i = 1; i < splitLine.length; i++) {
							v[i - 1] = Double.parseDouble(splitLine[i]);

						}
						// System.out.println(Arrays.toString(v));
						vList.add(v);

					} else if (mParamU.matches() && surfKnot == true) {
						for (int i = 2; i < splitLine.length; i++) {
							surfKnotU.add(Double.parseDouble(splitLine[i]));
						}
					} else if (mParamV.matches() && surfKnot == true) {
						for (int i = 2; i < splitLine.length; i++) {
							surfKnotV.add(Double.parseDouble(splitLine[i]));

						}

					} else if (mDegSurf.matches()) {
						surfKnot = true;
						for (int i = 1; i < splitLine.length; i++) {
							d[i - 1] = Integer.parseInt(splitLine[i]);
						}
					}

					else if (mSurf.matches()) {
						for (int i = 1; i < 5; i++) {
							surfDomain[i - 1] = Double
									.parseDouble(splitLine[i]);
						}
						for (int i = 5; i < splitLine.length; i++) {
							surfIndex.add(Integer.parseInt(splitLine[i]));
						}
					} else if (mTrim.matches()) {
						trimCounter = trimCounter + 1;
						trimCurvesList.add(new LinkedList<NURBSCtrlPoints>());
						for (int i = 1; i < splitLine.length; i++) {
							if (i % 3 == 1) {
								curveDomain[0] = Double
										.parseDouble(splitLine[i]);
							} else if (i % 3 == 2) {
								curveDomain[1] = Double
										.parseDouble(splitLine[i]);

							} else if (i % 3 == 0) {
								trimIndex = Integer.parseInt(splitLine[i]);
								NURBSCtrlPoints p = new NURBSCtrlPoints(
										curveDomain, trimIndex);
								trimCurvesList.get(trimCounter - 1).add(p);
								curveDomain = new double[2];
							}
						}
					}

					else if (mHole.matches()) {
						holeCounter = holeCounter + 1;
						holeCurvesList.add(new LinkedList<NURBSCtrlPoints>());
						for (int i = 1; i < splitLine.length; i++) {
							if (i % 3 == 1) {
								curveDomain[0] = Double
										.parseDouble(splitLine[i]);
							} else if (i % 3 == 2) {
								curveDomain[1] = Double
										.parseDouble(splitLine[i]);

							} else if (i % 3 == 0) {
								trimIndex = Integer.parseInt(splitLine[i]);
								NURBSCtrlPoints p = new NURBSCtrlPoints(
										curveDomain, trimIndex);
								holeCurvesList.get(trimCounter - 1).add(p);
								curveDomain = new double[2];
							}
						}
					}

					else if (mVp.matches()) {
						double[] v = new double[splitLine.length];
						for (int i = 1; i < splitLine.length; i++) {
							v[i - 1] = Double.parseDouble(splitLine[i]);
							if (splitLine.length == 3) {
								v[2] = 1; // setze hier vorraus, dass die curve
											// im
											// 2-dim parameterbereich liegt
							}
						}
						ctrlPointsCurve.add(v);
					} else if (mDegCurve.matches()) {
						curveDeg = Integer.parseInt(splitLine[1]);

					} else if (mCurveDim.matches()) {
						curveDim = Integer.parseInt(mCurveDim.group(1));
						for (int i = 1; i < splitLine.length; i++) {
							curveIndex.add(Integer.parseInt(splitLine[i]));
						}
					}

					else if (mParamCurve.matches() && surfKnot == false) {
						double[] curveKnotU = new double[splitLine.length - 2];
						for (int i = 2; i < splitLine.length; i++) {
							curveKnotU[i - 2] = Double
									.parseDouble(splitLine[i]);
						}

						index.add(curveIndex);
						curves.add(new NURBSCurve(curveKnotU, curveDim,
								curveDeg));
						curveIndex = new LinkedList<Integer>();

					}
					str = "";
				}
			}

			// hier werden die kurven mit index definiert
			for (int i = 0; i < curves.size(); i++) {
				double[][] pts = new double[index.get(i).size()][];
				for (int j = 0; j < pts.length; j++) {
					pts[j] = ctrlPointsCurve.get(index.get(i).get(j) - 1);
				}
				curves.get(i).setCtrlPoints(pts);
			}
			// hier wird das surf kontrollgitter mit index definiert
//			for (int i = 0; i < vList.size(); i++) {
//				System.out.println("vList: " + Arrays.toString(vList.get(i)));
//			}
//			System.out.println(surfIndex);
			for (int i = 0; i < surfIndex.size(); i++) {
//				System.out.println(i);
				ctrlPoints.add(vList.get(surfIndex.get(i) - 1));
			}
			for (int i = 0; i < ctrlPoints.size(); i++) {
//				System.out.println("ctrl " + Arrays.toString(ctrlPoints.get(i)));
			}
			// hier werden trimkurven definiert
			for (int i = 0; i < trimCurvesList.size(); i++) {
				NURBSTrimLoop loop = new NURBSTrimLoop();
				for (int j = 0; j < trimCurvesList.get(i).size(); j++) {
					int ci = trimCurvesList.get(i).get(j).getIndex() - 1;
					double[] cd = trimCurvesList.get(i).get(j).getPoint();
					NURBSCurve c = curves.get(ci);
					loop.addCurve(c, cd);
				}
				// ns.getTrimCurves().add(loop);
				trimC.add(loop);
			}
			// hier werden die holekurven definiert
			for (int i = 0; i < holeCurvesList.size(); i++) {
				NURBSTrimLoop loop = new NURBSTrimLoop();
				for (int j = 0; j < holeCurvesList.get(i).size(); j++) {
					int ci = holeCurvesList.get(i).get(j).getIndex() - 1;
					double[] cd = holeCurvesList.get(i).get(j).getPoint();
					NURBSCurve c = curves.get(ci);
					loop.addCurve(c, cd);
				}
				ns.getHoleCurves().add(loop);
				// holeC.add(loop);
			}

			double[] U = new double[surfKnotU.size()];
			double[] V = new double[surfKnotV.size()];
			for (int i = 0; i < U.length; i++) {
				U[i] = surfKnotU.get(i);
			}
//			System.out.println("Knotvector U: " + Arrays.toString(U));
			for (int i = 0; i < V.length; i++) {
				V[i] = surfKnotV.get(i);
			}
//			System.out.println("Knotvector V: " + Arrays.toString(V));
			int p = d[0];
//			System.out.println("P: " + p);
			int q = d[1];
//			System.out.println("Q: " + q);
			int m = U.length - p - 1;
			int n = V.length - q - 1;
//			System.out.println("MESH U " + m);
//			System.out.println("MESH V " + n);
			double[][][] controlMesh = new double[m][n][4];
			for (int i = 0; i < m; i++) {
				for (int j = 0; j < n; j++) {
					controlMesh[i][j] = ctrlPoints.get(i * n + j);
//					System.out.println("i: " + j);
//					System.out.println("j: " + i);
//
//					System.out.println(Arrays.toString(controlMesh[i][j]));
				}
			}
			ns.setControlMesh(controlMesh);
			ns.setHoleCurves(holeC);
			ns.setTrimCurves(trimC);
			ns.setUKnotVector(U);
			ns.setVKnotVector(V);
			ns.setUDegree(p);
			ns.setVDegree(q);
			ns.setSurfDomain(surfDomain);


		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			// Close the BufferedWriter
			try {
				if (lnr != null) {
					lnr.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return ns;
	}
}
