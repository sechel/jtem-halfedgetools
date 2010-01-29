/**
This file is part of a jTEM project.
All jTEM projects are licensed under the FreeBSD license 
or 2-clause BSD license (see http://www.opensource.org/licenses/bsd-license.php). 

Copyright (c) 2002-2010, Technische Universität Berlin, jTEM
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, 
are permitted provided that the following conditions are met:

-	Redistributions of source code must retain the above copyright notice, 
	this list of conditions and the following disclaimer.

-	Redistributions in binary form must reproduce the above copyright notice, 
	this list of conditions and the following disclaimer in the documentation 
	and/or other materials provided with the distribution.
 
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, 
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS 
BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, 
OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT 
OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING 
IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY 
OF SUCH DAMAGE.
**/

package de.jtem.halfedgetools.symmetry.plugin;

import java.util.List;

import de.jreality.math.Rn;
import de.jreality.plugin.JRViewer;
import de.jreality.plugin.basic.Inspector;
import de.jreality.plugin.basic.ViewMenuBar;
import de.jreality.plugin.basic.ViewToolBar;
import de.jreality.plugin.menu.BackgroundColor;
import de.jreality.plugin.menu.CameraMenu;
import de.jreality.plugin.menu.DisplayOptions;
import de.jreality.plugin.menu.ExportMenu;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.algorithm.Coord3DAdapter;
import de.jtem.halfedgetools.algorithm.subdivision.adapters.SubdivisionCoord3DAdapter;
import de.jtem.halfedgetools.algorithm.subdivision.adapters.SubdivisionEdge3DAdapter;
import de.jtem.halfedgetools.jreality.adapter.Adapter.AdapterType;
import de.jtem.halfedgetools.plugin.HalfedgeDebuggerPlugin;
import de.jtem.halfedgetools.plugin.HalfedgeInterfacePlugin;
import de.jtem.halfedgetools.plugin.buildin.TriangulatePlugin;
import de.jtem.halfedgetools.plugin.buildin.topology.EdgeFlipperPlugin;
import de.jtem.halfedgetools.symmetry.adapters.CyclesAdapter;
import de.jtem.halfedgetools.symmetry.adapters.DebugBundleAdapter;
import de.jtem.halfedgetools.symmetry.adapters.SymmetricCoordinateAdapter;
import de.jtem.halfedgetools.symmetry.adapters.SymmetricSymmetryFaceColorAdapter;
import de.jtem.halfedgetools.symmetry.standard.SEdge;
import de.jtem.halfedgetools.symmetry.standard.SFace;
import de.jtem.halfedgetools.symmetry.standard.SHDS;
import de.jtem.halfedgetools.symmetry.standard.SVertex;

public class SymmetricSubdivisionTutorial {

	private static class MyVAdapter implements SubdivisionCoord3DAdapter<SVertex> {

		public double[] getCoord(SVertex v) {
			return v.getEmbedding();
		}

		public void setCoord(SVertex v, double[] c) {
			v.setEmbedding(c);
		}
		
	}
	
	private static class MyEAdapter implements SubdivisionEdge3DAdapter<SEdge> {

		public double[] getCoord(SEdge e, double a, boolean ignore) {

			return e.getEmbeddingOnEdge(a, ignore);
			
		}
	}
		
	private static class MyFAdapter implements Coord3DAdapter<SFace> {

		public double[] getCoord(SFace f) {
			double[] sum = {0, 0, 0};
			List<SEdge> b = HalfEdgeUtils.boundaryEdges(f);
			int size = 0;
			for (SEdge e : b) {
				Rn.add(sum, sum, e.getEmbeddingOnEdge(1,true));
				size++;
			}
			Rn.times(sum, 1.0 / size, sum);
			return sum;
		}

		public void setCoord(SFace f, double[] c) {
			// doesn't make sense
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
		viewer.addContentUI();
		viewer.setShowPanelSlots(true, false, false, false);
		viewer.setShowToolBar(true);
		viewer.setPropertiesFile("test.jrw");

//		viewer.registerPlugin(new SymmetricCatmullClarkPlugin<SVertex,SEdge,SFace,SHDS>(
//				new MyVAdapter(),
//				new MyEAdapter(),
//				new MyFAdapter()
//				));
//		
		viewer.registerPlugin(new SymmetricLoopPlugin<SVertex,SEdge,SFace,SHDS>(
				new MyVAdapter(),
				new MyEAdapter(),
				new MyFAdapter()
				));
		
		viewer.registerPlugin(new SymmetricSqrt3Plugin<SVertex,SEdge,SFace,SHDS>(
		new MyVAdapter(),
		new MyEAdapter(),
		new MyFAdapter()
		));

		
		viewer.registerPlugin(new CompactifierPlugin());
		
		viewer.registerPlugin(new EdgeFlipperPlugin<SVertex,SEdge,SFace,SHDS>());
		viewer.registerPlugin(new TriangulatePlugin<SVertex,SEdge,SFace,SHDS>());
//		viewer.registerPlugins(TopologyOperations.topologicalEditing(new MyVAdapter(), new MyEAdapter(), new MyFAdapter()));
		
		viewer.registerPlugin(new HalfedgeDebuggerPlugin<SVertex,SEdge,SFace,SHDS>());
		
		viewer.registerPlugin(new HalfedgeInterfacePlugin
				<SVertex,SEdge,SFace,SHDS>(SHDS.class,
				new SymmetricCoordinateAdapter(AdapterType.VERTEX_ADAPTER),
				new SymmetricSymmetryFaceColorAdapter<SVertex,SEdge,SFace>(),
				new CyclesAdapter<SVertex,SEdge,SFace,SHDS>(),
				new DebugBundleAdapter(AdapterType.EDGE_ADAPTER)));
		
		viewer.startup();
	}
}
