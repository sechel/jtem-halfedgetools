package de.jtem.halfedgetools.nurbs;

import java.util.HashMap;
import java.util.Map;

import de.jreality.geometry.QuadMeshFactory;
import de.jreality.scene.IndexedFaceSet;
import de.jtem.halfedgetools.adapter.Adapter;

/**
 * A Factory for Bezier patches of arbitrary degree
 * @author Stefan Sechelmann
 *
 */
public class NURBSSurfaceFactory extends QuadMeshFactory{

	protected NURBSSurface
		surface = null;
	
	private Map<Integer, double[]> 
		indexUVMap = new HashMap<Integer,double[]>();

	private Map<Integer, double[]> 
		minCurvatureVFMap = new HashMap<Integer, double[]>(),
		maxCurvatureVFMap = new HashMap<Integer, double[]>();
	
	public NURBSSurfaceFactory() {

	}

	public void setSurface(NURBSSurface surface) {
		this.surface = surface;
	}
	
	@Override
	protected void updateImpl() {
		double[][] S = new double[getVLineCount()*getULineCount()][4];
		for(int j = 0; j < getVLineCount(); ++j) {
			for(int i = 0; i < getULineCount(); ++i) {
				double u = i / (double)(getULineCount() - 1);
				double v = j / (double)(getVLineCount() - 1);
				int index = i+j*getULineCount();
				surface.getSurfacePoint(u, v, S[index]);
				
				indexUVMap.put(index, new double[]{u,v});
				if(i == 0 || j == 0 || i == getULineCount()-1 || j == getVLineCount()-1) { // boundary of patch
					minCurvatureVFMap.put(index, new double[]{0.0,0.0,0.0,1.0});
					maxCurvatureVFMap.put(index, new double[]{0.0,0.0,0.0,1.0});
					continue;
				}
				CurvatureInfo ci = NURBSCurvatureUtility.curvatureAndDirections(surface, u, v);
				if(ci.getMinCurvature() == ci.getMaxCurvature()) { //umbillic point
					minCurvatureVFMap.put(index, new double[]{0.0,0.0,0.0,1.0});
					maxCurvatureVFMap.put(index, new double[]{0.0,0.0,0.0,1.0});
					continue;
				}
				minCurvatureVFMap.put(index, ci.getCurvatureDirectionsManifold()[0]);
				maxCurvatureVFMap.put(index, ci.getCurvatureDirectionsManifold()[1]);
				
			}
		}
		
		setVertexCoordinates(S);		
		super.updateImpl();
	}
	
	public NurbsUVAdapter getUVAdapter() {
		return new NurbsUVAdapter(indexUVMap);
	}
	
	@Override
	public IndexedFaceSet getIndexedFaceSet() {
		return (IndexedFaceSet)getGeometry();
	}

	public Adapter<double[]> getMinCurvatureVectorField() {
		return new IndexedVectorField("MinCurvature",minCurvatureVFMap);
	}
	
	public Adapter<double[]> getMaxCurvatureVectorField() {
		return new IndexedVectorField("MaxCurvature",maxCurvatureVFMap);
	}
}