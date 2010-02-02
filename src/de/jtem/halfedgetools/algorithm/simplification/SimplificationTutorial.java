package de.jtem.halfedgetools.algorithm.simplification;

import de.jreality.math.Rn;
import de.jreality.plugin.JRViewer;
import de.jreality.plugin.basic.Inspector;
import de.jreality.plugin.basic.ViewMenuBar;
import de.jreality.plugin.basic.ViewToolBar;
import de.jreality.plugin.menu.BackgroundColor;
import de.jreality.plugin.menu.CameraMenu;
import de.jreality.plugin.menu.DisplayOptions;
import de.jreality.plugin.menu.ExportMenu;
import de.jtem.halfedgetools.algorithm.Coord3DAdapter;
import de.jtem.halfedgetools.algorithm.simplification.adapters.SimplificationAdapters.AreaAdapter;
import de.jtem.halfedgetools.algorithm.simplification.adapters.SimplificationAdapters.NormalAdapter;
import de.jtem.halfedgetools.jreality.adapter.Adapter.AdapterType;
import de.jtem.halfedgetools.jreality.adapter.standard.StandardCoordinateAdapter;
import de.jtem.halfedgetools.jreality.node.standard.StandardEdge;
import de.jtem.halfedgetools.jreality.node.standard.StandardFace;
import de.jtem.halfedgetools.jreality.node.standard.StandardHDS;
import de.jtem.halfedgetools.jreality.node.standard.StandardVertex;
import de.jtem.halfedgetools.plugin.HalfedgeInterfacePlugin;
import de.jtem.halfedgetools.plugin.buildin.SimplificationPlugin;

public class SimplificationTutorial {
	
	
	public static class PosA implements Coord3DAdapter<StandardVertex>{

		@Override
		public double[] getCoord(StandardVertex v) {
			return v.position;
		}

		@Override
		public void setCoord(StandardVertex v, double[] p) {
			v.position = p;
			
		}


		
	}
	
	public static class NormA implements NormalAdapter<StandardFace>{


		@Override
		public double[] getNormal(StandardFace f) {
			StandardEdge e1 = f.getBoundaryEdge();
			StandardEdge e2 = f.getBoundaryEdge().getNextEdge();
			double[] v1 = e1.getTargetVertex().position.clone();
			double[] v2 = e2.getTargetVertex().position.clone();
			double[] v3 = e1.getStartVertex().position.clone();
			Rn.subtract(v1,v3,v1);
			Rn.subtract(v2,v3,v2);
			double[] normal = Rn.crossProduct(v3,v1,v2);
			Rn.normalize(normal,normal);
			return normal;
		}
		
		
	}
	
	public static class AreaA implements AreaAdapter<StandardFace>{


		@Override
		public double getArea(StandardFace f) {
			StandardEdge e1 = f.getBoundaryEdge();
			StandardEdge e2 = f.getBoundaryEdge().getNextEdge();
			double[] v1 = e1.getStartVertex().position;
			double[] v2 = e2.getStartVertex().position;
			double[] v3 = e2.getTargetVertex().position;
			double a = 0,b = 0,c = 0;
			for (int k=0; k<3; k++) {
	           a += (v1[k]-v2[k])*(v1[k]-v2[k]);
	           b += (v1[k]-v2[k])*(v3[k]-v2[k]);
	           c += (v3[k]-v2[k])*(v3[k]-v2[k]);
			}
			double area = a*c-b*b;
			if (area <= 0.)
	           return 0.;
	       	else
	           return Math.sqrt(area)/2.;
		}
		
	}

	public static void main(String[] args) {
		JRViewer viewer = new JRViewer();
		viewer.registerPlugin(new Inspector());
		
		viewer.registerPlugin(new BackgroundColor());
		viewer.registerPlugin(new DisplayOptions());
		viewer.registerPlugin(new ViewMenuBar());
		viewer.registerPlugin(new ViewToolBar());
		
		viewer.registerPlugin(new ExportMenu());
		viewer.registerPlugin(new CameraMenu());
		
		viewer.registerPlugin(
				new SimplificationPlugin<StandardVertex,StandardEdge,StandardFace,StandardHDS>(
						new SimplificationTutorial.PosA(),
						new SimplificationTutorial.NormA(),
						new SimplificationTutorial.AreaA()
					)
				);
		
		viewer.registerPlugin(
				new HalfedgeInterfacePlugin<StandardVertex,StandardEdge,StandardFace,StandardHDS>(
						StandardHDS.class, 
						new StandardCoordinateAdapter(AdapterType.VERTEX_ADAPTER)
						)
				);
		
		viewer.addContentUI();
		viewer.setShowPanelSlots(true, false, false, false);
		viewer.setShowToolBar(true);
		
		viewer.startup();
		
	}
	
}
