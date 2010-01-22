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

package de.jtem.halfedgetools.symmetry.util;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import de.jreality.geometry.GeometryMergeFactory;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.GlslProgram;
import de.jreality.shader.ShaderUtility;
import de.jreality.util.Input;
import de.jreality.util.SceneGraphUtility;
import de.jtem.discretegroup.core.DiscreteGroup;
import de.jtem.discretegroup.core.DiscreteGroupSceneGraphRepresentation;
import de.jtem.discretegroup.core.DiscreteGroupSimpleConstraint;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.jreality.ConverterHeds2JR;
import de.jtem.halfedgetools.jreality.adapter.Adapter;
import de.jtem.halfedgetools.jreality.adapter.CoordinateAdapter2Ifs;
import de.jtem.halfedgetools.symmetry.adapters.CyclesAdapter;
import de.jtem.halfedgetools.symmetry.standard.SEdge;
import de.jtem.halfedgetools.symmetry.standard.SFace;
import de.jtem.halfedgetools.symmetry.standard.SHDS;
import de.jtem.halfedgetools.symmetry.standard.SVertex;
import de.jtem.halfedgetools.util.CuttingUtility.CuttingInfo;

public class SHDS2IFSConverter {
	
	private SceneGraphComponent sgcToDisp = SceneGraphUtility.createFullSceneGraphComponent("dispSGC");
	private final SceneGraphComponent mainSGC = SceneGraphUtility.createFullSceneGraphComponent("mainSGC");
	private final SceneGraphComponent normals = SceneGraphUtility.createFullSceneGraphComponent("normals");
	private final SceneGraphComponent body = SceneGraphUtility.createFullSceneGraphComponent("body");
	private final SceneGraphComponent cycles = SceneGraphUtility.createFullSceneGraphComponent("cycles");
	
	private ConverterHeds2JR<SVertex, SEdge, SFace> conv = null;
	private Adapter[] adapters = null;
	
	private SHDS shds = null;
	
	private Appearance bubbleApperance = new Appearance();
	
//	private DisplayOptionsPlugin dop = null;
	
	public SHDS2IFSConverter(SHDS heds, Adapter... adapters) {
		conv = new ConverterHeds2JR<SVertex, SEdge, SFace>();
		this.adapters = adapters;
		this.shds = heds;

		
		mainSGC.addChild(normals);
		mainSGC.addChild(body);
		mainSGC.addChild(cycles);
		
		sgcToDisp.addChild(mainSGC);
		
		// initialize bubble shader
		// TODO fix transparency x 2 and smooth shading
//		bubbleApperance.setAttribute(CommonAttributes.TUBES_DRAW, false);
//		bubbleApperance.setAttribute(CommonAttributes.EDGE_DRAW, false);
//		bubbleApperance.setAttribute(CommonAttributes.SPHERES_DRAW, false);
//		bubbleApperance.setAttribute(CommonAttributes.VERTEX_DRAW, false);
//		bubbleApperance.setAttribute(CommonAttributes.SMOOTH_SHADING, true);
//		bubbleApperance.setAttribute(CommonAttributes.ADDITIVE_BLENDING_ENABLED, true);
//		bubbleApperance.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
//		bubbleApperance.setAttribute(CommonAttributes.Z_BUFFER_ENABLED, true);
		
//		DefaultGeometryShader dgs = (DefaultGeometryShader)ShaderUtility.createDefaultGeometryShader(bubbleApperance, true);
//		dgs.createPolygonShader("glsl");
//		try {
//			System.out.println("Loading bubble shader");
//			InputStream vIn = getClass().getResourceAsStream("/bubbleV.glsl");
//			InputStream fIn = getClass().getResourceAsStream("/bubbleF.glsl");
//			Input vertexIn = Input.getInput("vertex shader", vIn);
//			Input fragmentIn = Input.getInput("fragment shader", fIn);
//			new GlslProgram(bubbleApperance, POLYGON_SHADER, vertexIn, fragmentIn);
//		} catch (Exception ex) {
//			System.out.println("Cannot load shader: " + ex.getMessage());
//		}
		
		
	}
	
	public SceneGraphComponent getSGC(boolean dispSymm) {
		
		// 0.5 BAKE

		updateSGC(shds,dispSymm);
		
		return sgcToDisp;
	}

	public void updateSGC(boolean dispSymm) {
		updateSGC(shds,dispSymm);
	}
	
	public void updateSGC(SHDS embedding, boolean dispSymm) {

		this.shds = embedding;
		
//		SHDS embedding = rs.getEmbeddedCopy(dop.getCone());
		
		// 1 BODY
		
		CuttingInfo<SVertex,SEdge,SFace> ci = new CuttingInfo<SVertex, SEdge, SFace>();
//		if(dop.getBoundaryCycles())
			ci.paths.putAll(embedding.getBoundaryCycles().paths);
//		if(dop.getSymmetryCycles())
			ci.paths.putAll(embedding.getSymmetryCycles().paths);
		
		CyclesAdapter<SVertex,SEdge,SFace,SHDS> cyclesAdapter = new CyclesAdapter<SVertex, SEdge, SFace,SHDS>();
		
		Adapter ca = null;
		for(Adapter a : adapters) {
			if(a instanceof CoordinateAdapter2Ifs<?>) {
				ca = a;
			}
		}
		
		

		// TODO this temporary hack since Sunflow can't handle colors/transparency
//		if(rs.hasSymmetry()) {
//			
//			Set<SFace> fToDel = new HashSet<SFace>();
////			Set<SEdge> eToDel = new HashSet<SEdge>();
//			for(SFace f : embedding.getFaces()) {
//				for(SEdge ee : HalfEdgeUtilsExtra.getBoundary(f)) {
//					if(ee.isRightOfSymmetryCycle() != null) {
//						fToDel.add(f);
////						eToDel.add(ee);
//					}
//				}
//			}
//			for(SFace f : fToDel) {
//				embedding.removeFace(f);
//			}
//		}
		
		// BODY
		IndexedFaceSet bodyIfs = conv.heds2ifs(embedding, adapters);
		
		IndexedFaceSetUtility.calculateAndSetNormals(bodyIfs);
		body.setGeometry(bodyIfs);
		
		Appearance appNoFaces = new Appearance();
		appNoFaces.setAttribute(CommonAttributes.FACE_DRAW, false);
		
		IndexedFaceSet boundaryIfs = conv.heds2ifs(embedding, ca, cyclesAdapter);
		cycles.setGeometry(boundaryIfs);
		cycles.setAppearance(appNoFaces);
		
		// 2 NORMALS

		IndexedLineSet[] ilss = new IndexedLineSet[shds.getInteriorVertices().size()];
		
		int i = 0;
		// create normal section
//		for(SVertex vi : shds.getInteriorVertices()) {
//			
//			SVertex v = embedding.getVertex(vi.getIndex());
//			
//			double[] pos = v.getEmbedding();
//			
//			double[] n = v.getMeanCurvatureVector();
//
//			n = Rn.times(null, 0.125, n);
//
//			List<SEdge> incoming = new LinkedList<SEdge>();
//			if(dop.getFeet()) {
//				incoming = HalfEdgeUtils.incomingEdges(v);
//			}
//			
//			int nrIncoming = incoming.size();
//			double[][] coords = new double[2 + nrIncoming][];
//			coords[0] = new double[] {pos[0],pos[1],pos[2]};
//			coords[1] = new double[] {pos[0]+n[0],pos[1]+n[1],pos[2]+n[2]};
//			
//			int index = 2;
//			for(SEdge e : incoming) {
//				double[] dir = e.getDirection();
//				double w = e.getWeight();
//				w = -0.25*w / Rn.euclideanNorm(dir);
//				dir = Rn.times(null, w, dir);
//				dir = Rn.add(null, dir, pos);
//				coords[index] = dir;
//				index++;
//			}
//			
//			int[][] indices = new int[1 + nrIncoming][];
//			for(int indInd = 0; indInd < nrIncoming+1; indInd++) {
//				indices[indInd] = new int[] {0,indInd+1};
//			}
//			
//
//			Random rnd = new Random(i);
//			double[] color = new double[] {rnd.nextDouble(), rnd.nextDouble(), rnd.nextDouble()};
//			
//			double[][] eColors = new double[1 + nrIncoming][];
//			eColors[0] = color;
//			for(int ii = 1; ii < nrIncoming + 1; ii++)
//				eColors[ii] = color;
//			
//			double[][] vColors = new double[2+nrIncoming][];
//			vColors[0] = color;
//			vColors[1] = color;
//			for(int ii = 2; ii < nrIncoming + 2; ii++)
//				vColors[ii] = color;
//			
////				Color[] eColors = new Color[1 + nrIncoming];
////				eColors[0] = Color.PINK;
////				for(int ii = 1; ii < nrIncoming + 1; ii++)
////					eColors[ii] = Color.DARK_GRAY;
////				
////				Color[] vColors = new Color[2+nrIncoming];
////				vColors[0] = Color.ORANGE;
////				vColors[1] = Color.CYAN;
////				for(int ii = 2; ii < nrIncoming + 2; ii++)
////					vColors[ii] = Color.PINK;
//			
//			IndexedLineSetFactory ilsF = new IndexedLineSetFactory();
//			
//			ilsF.setVertexCount(coords.length);
//			ilsF.setVertexCoordinates(coords);
//			ilsF.setVertexColors(vColors);
//			
//			ilsF.setEdgeCount(indices.length);
//			ilsF.setEdgeIndices(indices);
//
//			ilsF.setEdgeColors(eColors);
//			
//			ilsF.update();
//			
//			ilss[i] = ilsF.getIndexedLineSet();
//			
//			i++;
//
//		}
//		
//		GeometryMergeFactory gf = new GeometryMergeFactory();
//		if(ilss.length > 0) {
//			IndexedLineSet ils = gf.mergeIndexedLineSets(ilss);
//			normals.setGeometry(ils);
//		}
//		Appearance appNormals = new Appearance();
//		appNormals.setAttribute(CommonAttributes.VERTEX_DRAW, false);
//		appNormals.setAttribute(CommonAttributes.SPHERES_DRAW, false);
//		normals.setAppearance(appNormals);
		
//		BallAndStickFactory bsf = new BallAndStickFactory(ils);
//		bsf.setBallRadius(.04);
//		bsf.setShowBalls(false);
//        bsf.setStickRadius(.005);
//        bsf.setShowArrows(false);
//        bsf.setArrowScale(.01);
//        bsf.setArrowSlope(1.5);
//        bsf.setArrowPosition(1);
//		bsf.update();
//		GeometryMergeFactory gmf = new GeometryMergeFactory();
//		IndexedFaceSet md = gmf.mergeGeometrySets(bsf.getSceneGraphComponent());
//		normals.setGeometry(md);
			
		
		// 3 SYMMETRY
		
		// this relies on us having a triangle mesh! hence can be imporved..
//		if(shds.getGroup() != null && dop.getEmbedd()){
		if(shds.getGroup() != null){
			System.err.println("Layouting symmetry faces and edges");
			
			boolean symm = false;
			
			List<SFace> sFaces = new ArrayList<SFace>();
			for(SFace f : embedding.getFaces()) {
				for(SEdge e : HalfEdgeUtils.boundaryEdges(f)) {
					if(e.isRightOfSymmetryCycle() != null) {
						sFaces.add(f);
					}
				}
			}
			
			double[][] extraCoords = new double[3*sFaces.size()][];
			int[][] extraIndices = new int[sFaces.size()][];
			double[][] extraColors = new double[sFaces.size()][];
			
			
			int j = 0;
			for(SFace f : sFaces) {
				
				extraCoords[j] = f.getEmbeddingOnBoundary(0);
				extraCoords[j+1] = f.getEmbeddingOnBoundary(1);
				extraCoords[j+2] = f.getEmbeddingOnBoundary(2);
				
//				SEdge e = HalfEdgeUtils.boundaryEdges(f).get(0);
//				if(e.isRightIncomingOfSymmetryCycle() == null)
//					e = e.getNextEdge();
//				if(e.isRightIncomingOfSymmetryCycle() == null)
//					e = e.getNextEdge();
//				
//				extraCoords[j]   = Rn.add(null, e.getStartVertex().getEmbedding(), e.getDirection());
//				extraCoords[j+1] = Rn.add(null, extraCoords[j], e.getNextEdge().getDirection());
//				extraCoords[j+2] = Rn.add(null, extraCoords[j+1], e.getPreviousEdge().getDirection());

				extraColors[j/3] = new double[] {0.3,0.8,0.8,1};
				
				extraIndices[j/3] = new int[] {j,j+1,j+2};
				
				j += 3;
				symm = true;
			}
			
			List<SEdge> symmetryEdges = new ArrayList<SEdge>();
			for(SEdge e : embedding.getEdges()) {
				if(e.isRightOfSymmetryCycle() != null && e.isPositive()) {
					symmetryEdges.add(e);
				}
			}
		
		
			double[][] extraCoords2 = new double[2*symmetryEdges.size()][];
			int[][] extraIndices2 = new int[symmetryEdges.size()][];
			double[][] extraColors2 = new double[symmetryEdges.size()][];
			int k = 0;
			for(SEdge e : symmetryEdges) {
				
				int n = e.getNr();
				SFace f = e.getLeftFace();
				extraCoords2[k] = f.getEmbeddingOnBoundary(n + 0);
				extraCoords2[k+1] = f.getEmbeddingOnBoundary(n + 1);
				
//				extraCoords2[k] = e.getEmbeddingOnEdge(0);
//				extraCoords2[k+1] = e.getEmbeddingOnEdge(1);
				
//				extraCoords2[k] = Rn.add(null, e.getStartVertex().getEmbedding(), e.getDirection());
//				extraCoords2[k+1] = e.getTargetVertex().getEmbedding();
			 	
				extraColors2[k/2] = new double[] {1,0,1,1}; //tar2;
				
				extraIndices2[k/2] = new int[] {k,k+1};
				
				k += 2;
				symm = true;
				
			}
			
			
			if(symm) {
				
				IndexedLineSetFactory ilsextra = new IndexedLineSetFactory();
				ilsextra.setVertexCount(extraCoords2.length);
				ilsextra.setVertexCoordinates(extraCoords2);
				ilsextra.setEdgeCount(extraIndices2.length);
				ilsextra.setEdgeIndices(extraIndices2);
				ilsextra.setEdgeColors(extraColors2);
				ilsextra.update();
				IndexedLineSet eilse = ilsextra.getIndexedLineSet(); 
				
				IndexedFaceSetFactory ifsextra = new IndexedFaceSetFactory();
				ifsextra.setVertexCount(extraCoords.length);
				ifsextra.setVertexCoordinates(extraCoords);
				ifsextra.setFaceCount(extraIndices.length);
				ifsextra.setFaceIndices(extraIndices);
				ifsextra.setFaceColors(extraColors);
				ifsextra.update();
				IndexedFaceSet eifse = ifsextra.getIndexedFaceSet();

				GeometryMergeFactory gmf = new GeometryMergeFactory();
				IndexedFaceSet ifs = gmf.mergeIndexedFaceSets(new PointSet[] {eifse,bodyIfs});
//				IndexedFaceSet ifs = gmf.mergeIndexedFaceSets(new PointSet[] {eilse, bodyIfs});
				IndexedFaceSetUtility.calculateAndSetNormals(ifs);
				body.setGeometry(ifs);
				
			}
		}
	
		

		
//		if(!dop.getNormals()) {
//			normals.setVisible(false);
//		} else {
//			normals.setVisible(true);
//		}
		

		
//		if(!dop.getSoap()) {
//			Appearance app = new Appearance();
//			app.setAttribute(CommonAttributes.EDGE_DRAW, false);
//			body.setAppearance(app);
//		} else {
//			body.setAppearance(bubbleApperance);
//		}
		
//		if(shds.getGroup() != null && dop.getSymmetry()) {
		if(shds.getGroup() != null && dispSymm) {
			DiscreteGroup dg = shds.getGroup();

			DiscreteGroupSimpleConstraint dgsc = new DiscreteGroupSimpleConstraint(-1,-1,1+(int)Math.round(Math.pow(dg.getGenerators().length/2,2.0)));
			dgsc.setManhattan(true);
			dg.setConstraint(dgsc);
			DiscreteGroupSceneGraphRepresentation repn = new DiscreteGroupSceneGraphRepresentation(dg, true);
			
//			SceneGraphComponent toDist = SceneGraphUtility.createFullSceneGraphComponent("toDist");
//			toDist.addChild(body);
//			toDist.addChild(cycles);
//			
//			
//			GeometryMergeFactory gmf = new GeometryMergeFactory();
//			bodyIfs = gmf.mergeGeometrySets(toDist);
//			
//			body.setGeometry(bodyIfs);
//			
//			body.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, true);
			repn.setWorldNode(body);

			repn.update();
			
			SceneGraphComponent ss = repn.getRepresentationRoot(); 

			GeometryMergeFactory gmf2 = new GeometryMergeFactory();
			body.setGeometry(gmf2.mergeIndexedFaceSets(ss));
		} else {

		}
		
	}

}

