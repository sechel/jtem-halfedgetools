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

package de.jtem.halfedgetools.symmetry.tutorial;

import java.util.HashSet;
import java.util.Set;

import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.plugin.JRViewer;
import de.jreality.plugin.JRViewer.ContentType;
import de.jtem.discretegroup.core.DiscreteGroup;
import de.jtem.discretegroup.core.TriangleGroup;
import de.jtem.discretegroup.plugin.TessellatedContent;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.HalfedgeListener;
import de.jtem.halfedgetools.plugin.algorithm.geometry.PerturbPlugin;
import de.jtem.halfedgetools.plugin.algorithm.subdivision.TriangulatePlugin;
import de.jtem.halfedgetools.symmetry.adapters.BundleCycleColorAdapter;
import de.jtem.halfedgetools.symmetry.adapters.SymmetricPositionAdapter;
import de.jtem.halfedgetools.symmetry.adapters.SymmetryEdgeColorAdapter;
import de.jtem.halfedgetools.symmetry.adapters.SymmetryFaceColorAdapter;
import de.jtem.halfedgetools.symmetry.calculators.SymmetricSubdivisionCalculator;
import de.jtem.halfedgetools.symmetry.node.SEdge;
import de.jtem.halfedgetools.symmetry.node.SFace;
import de.jtem.halfedgetools.symmetry.node.SHDS;
import de.jtem.halfedgetools.symmetry.node.SVertex;
import de.jtem.halfedgetools.symmetry.plugin.CompactifierPlugin;
import de.jtem.halfedgetools.symmetry.plugin.SymmetricCatmullClarkPlugin;
import de.jtem.halfedgetools.symmetry.plugin.SymmetricLoopPlugin;
import de.jtem.halfedgetools.symmetry.plugin.SymmetricSqrt3Plugin;
import de.jtem.halfedgetools.symmetry.plugin.visualizer.SymmetryVisualizer;
import de.jtem.halfedgetools.util.CuttingUtility.CuttingInfo;

public class TesselatedSymmetricSubdivisionTutorial extends TessellatedContent implements HalfedgeListener{
	
	private static TesselatedSymmetricSubdivisionTutorial tc;
	
	public static SHDS generateCube() {
		
		SHDS shds = new SHDS();
		
		TriangleGroup cubeRot = TriangleGroup.instanceOfGroup("234");
		shds.setGroup(cubeRot);
		
		SEdge e00 = shds.addNewEdge();
		SEdge e01 = shds.addNewEdge();
		SEdge e02 = shds.addNewEdge();
		SEdge e03 = shds.addNewEdge();
		SEdge e04 = shds.addNewEdge();
		SEdge e05 = shds.addNewEdge();
		SEdge e06 = shds.addNewEdge();
		SEdge e07 = shds.addNewEdge();
		SEdge e08 = shds.addNewEdge();
		SEdge e09 = shds.addNewEdge();
		SEdge e10 = shds.addNewEdge();
		SEdge e11 = shds.addNewEdge();
		SEdge e12 = shds.addNewEdge();
		SEdge e13 = shds.addNewEdge();
		SEdge e14 = shds.addNewEdge();
		SEdge e15 = shds.addNewEdge();
		SEdge e16 = shds.addNewEdge();
		SEdge e17 = shds.addNewEdge();
		
		SVertex v00 = shds.addNewVertex();
		SVertex v01 = shds.addNewVertex();
		SVertex v02 = shds.addNewVertex();
		SVertex v03 = shds.addNewVertex();
		SVertex v04 = shds.addNewVertex();
		
		e00.linkOppositeEdge(e01);
		e02.linkOppositeEdge(e12);
		e04.linkOppositeEdge(e06);
		e08.linkOppositeEdge(e10);
		e14.linkOppositeEdge(e17);	// order 4
		e16.linkOppositeEdge(e15);	// order 3
		e05.linkOppositeEdge(e07);
		e03.linkOppositeEdge(e13);
		e09.linkOppositeEdge(e11);
		
		e00.linkNextEdge(e02);
		e02.linkNextEdge(e04);
		e04.linkNextEdge(e00);
		
		e12.linkNextEdge(e14);
		e14.linkNextEdge(e10);
		e10.linkNextEdge(e12);
		
		e08.linkNextEdge(e16);
		e16.linkNextEdge(e06);
		e06.linkNextEdge(e08);

		e01.linkNextEdge(e03);
		e03.linkNextEdge(e05);
		e05.linkNextEdge(e01);
		
		e13.linkNextEdge(e15);
		e15.linkNextEdge(e11);
		e11.linkNextEdge(e13);
		
		e09.linkNextEdge(e17);
		e17.linkNextEdge(e07);
		e07.linkNextEdge(e09);
		
		e00.setTargetVertex(v02);
		e02.setTargetVertex(v03);
		e04.setTargetVertex(v01);
		e06.setTargetVertex(v03);
		e08.setTargetVertex(v00);
		e10.setTargetVertex(v03);
		e12.setTargetVertex(v02);
		e14.setTargetVertex(v00);
		e16.setTargetVertex(v01);
		
		e01.setTargetVertex(v01);
		e03.setTargetVertex(v04);
		e05.setTargetVertex(v02);
		e07.setTargetVertex(v04);
		e09.setTargetVertex(v00);
		e11.setTargetVertex(v04);
		e13.setTargetVertex(v01);
		e15.setTargetVertex(v00);
		e17.setTargetVertex(v02);
		
		HalfEdgeUtils.fillAllHoles(shds);
		
		double[][] pts = cubeRot.getTriangle();
		// project the points onto the unit cube; points by default lie on unit cube
		Pn.setToLength(pts[0], pts[0], 1.414, 0);
		Pn.setToLength(pts[1], pts[1], 1.731, 0);
		double[][] pts4 = {pts[0], pts[1], pts[2], new double[4]};
		Rn.average(pts4[3], pts);
		Pn.setToLength(pts4[3], pts4[3], 1.5, Pn.EUCLIDEAN);
		
		v00.setEmbedding(pts4[0]);
		v01.setEmbedding(pts4[1]);
		v02.setEmbedding(pts4[2]);
		v03.setEmbedding(pts4[3]);
		v04.setEmbedding(new double[] {0,0,0});
		
		CuttingInfo<SVertex, SEdge, SFace> symmetryCycles = new CuttingInfo<SVertex, SEdge, SFace>();
		
		Set<SEdge> order3 = new HashSet<SEdge>();
		Set<SEdge> order4 = new HashSet<SEdge>();
		order3.add(e15); order3.add(e16);
		order4.add(e14); order4.add(e17);
		symmetryCycles.paths.put(order3, cubeRot.getGenerators()[0]);
		symmetryCycles.paths.put(order3, cubeRot.getGenerators()[1]);
		
		shds.setSymmetryCycles(symmetryCycles);
		
		return shds;
	}


	public static void main(String[] args) {
	
		JRViewer viewer = new JRViewer();
		viewer.addBasicUI();
		viewer.addContentUI();
		viewer.addContentSupport(ContentType.CenteredAndScaled);
		viewer.setShowPanelSlots(true, false, false, false);
		viewer.setShowToolBar(true);
		
		HalfedgeInterface hif = new HalfedgeInterface();
		hif.addAdapter(new SymmetricPositionAdapter());
		hif.addAdapter(new SymmetryEdgeColorAdapter());
		hif.addAdapter(new SymmetryFaceColorAdapter());
		hif.addAdapter(new BundleCycleColorAdapter());
		hif.addCalculator(new SymmetricSubdivisionCalculator());
		
		tc = new TesselatedSymmetricSubdivisionTutorial();
		viewer.registerPlugin(tc);
		
		hif.addHalfedgeListener(tc);
//		hif.setAutomaticConversion(false);
		
		viewer.registerPlugin(hif);
		viewer.registerPlugin(new SymmetricCatmullClarkPlugin());
		viewer.registerPlugin(new TriangulatePlugin());
		viewer.registerPlugin(new PerturbPlugin());

		viewer.registerPlugin(new SymmetricLoopPlugin());
		viewer.registerPlugin(new SymmetricSqrt3Plugin());
		viewer.registerPlugin(new CompactifierPlugin());
		viewer.registerPlugin(new SymmetryVisualizer());

		viewer.startup();
		
	//	SHDS cube = generateCube();
	//	hif.set(cube);
		
	}


	@Override
	public <V extends Vertex<V, E, F>, E extends Edge<V, E, F>, F extends Face<V, E, F>, HDS extends HalfEdgeDataStructure<V, E, F>> void halfedgeChanged(
			HDS hds, AdapterSet a, HalfedgeInterface hif) {
		
			
		
			if(SHDS.class.isAssignableFrom(hds.getClass())) {
				SHDS shds = (SHDS)hds;
				DiscreteGroup group = shds.getGroup();
				if(group != null) {
					this.setGroup(shds.getGroup(), true);
				}
				System.out.println("changed heds and group");
			}
		
	}


	@Override
	public <V extends Vertex<V, E, F>, E extends Edge<V, E, F>, F extends Face<V, E, F>, HDS extends HalfEdgeDataStructure<V, E, F>> void halfedgeConverting(
			HDS hds, AdapterSet a, HalfedgeInterface hif) {
		// TODO Auto-generated method stub
		
	}
}
