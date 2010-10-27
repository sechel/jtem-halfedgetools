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

package de.jtem.halfedgetools.plugin;

import java.util.HashSet;
import java.util.Set;

import de.jtem.halfedgetools.plugin.algorithm.generator.ConvexHullGenerator;
import de.jtem.halfedgetools.plugin.algorithm.generator.RandomSphereGenerator;
import de.jtem.halfedgetools.plugin.algorithm.geometry.PerturbPlugin;
import de.jtem.halfedgetools.plugin.algorithm.geometry.ProjectPlugin;
import de.jtem.halfedgetools.plugin.algorithm.selection.BoundaryEdgeSelection;
import de.jtem.halfedgetools.plugin.algorithm.selection.BoundaryFaceSelection;
import de.jtem.halfedgetools.plugin.algorithm.selection.BoundaryVertexSelection;
import de.jtem.halfedgetools.plugin.algorithm.selection.ClearEdgeSelection;
import de.jtem.halfedgetools.plugin.algorithm.selection.ClearFaceSelection;
import de.jtem.halfedgetools.plugin.algorithm.selection.ClearSelection;
import de.jtem.halfedgetools.plugin.algorithm.selection.ClearVertexSelection;
import de.jtem.halfedgetools.plugin.algorithm.selection.ExportSelection;
import de.jtem.halfedgetools.plugin.algorithm.selection.ImportSelection;
import de.jtem.halfedgetools.plugin.algorithm.selection.InsideEdgesSelection;
import de.jtem.halfedgetools.plugin.algorithm.selection.InsideFacesSelection;
import de.jtem.halfedgetools.plugin.algorithm.selection.InvertEdgeSelection;
import de.jtem.halfedgetools.plugin.algorithm.selection.InvertFaceSelection;
import de.jtem.halfedgetools.plugin.algorithm.selection.InvertVertexSelection;
import de.jtem.halfedgetools.plugin.algorithm.selection.TouchingEdgesSelection;
import de.jtem.halfedgetools.plugin.algorithm.selection.TouchingFacesSelection;
import de.jtem.halfedgetools.plugin.algorithm.simplification.GarlandHeckbertPlugin;
import de.jtem.halfedgetools.plugin.algorithm.subdivision.CatmullClarkLinearPlugin;
import de.jtem.halfedgetools.plugin.algorithm.subdivision.CatmullClarkPlugin;
import de.jtem.halfedgetools.plugin.algorithm.subdivision.LoopLinearPlugin;
import de.jtem.halfedgetools.plugin.algorithm.subdivision.LoopPlugin;
import de.jtem.halfedgetools.plugin.algorithm.subdivision.MedialGraphLinearPlugin;
import de.jtem.halfedgetools.plugin.algorithm.subdivision.QuadGraphLinearPlugin;
import de.jtem.halfedgetools.plugin.algorithm.subdivision.Sqrt3LinearPlugin;
import de.jtem.halfedgetools.plugin.algorithm.subdivision.Sqrt3Plugin;
import de.jtem.halfedgetools.plugin.algorithm.subdivision.StellarLinearPlugin;
import de.jtem.halfedgetools.plugin.algorithm.subdivision.TriangulatePlugin;
import de.jtem.halfedgetools.plugin.algorithm.topology.DelaunayPlugin;
import de.jtem.halfedgetools.plugin.algorithm.topology.EdgeCollapsePlugin;
import de.jtem.halfedgetools.plugin.algorithm.topology.EdgeFlipperPlugin;
import de.jtem.halfedgetools.plugin.algorithm.topology.EdgeRemoverFillPlugin;
import de.jtem.halfedgetools.plugin.algorithm.topology.EdgeRemoverPlugin;
import de.jtem.halfedgetools.plugin.algorithm.topology.EdgeSplitterPlugin;
import de.jtem.halfedgetools.plugin.algorithm.topology.FaceCollapserPlugin;
import de.jtem.halfedgetools.plugin.algorithm.topology.FaceRemoverPlugin;
import de.jtem.halfedgetools.plugin.algorithm.topology.FaceScalerPlugin;
import de.jtem.halfedgetools.plugin.algorithm.topology.FaceSplitterPlugin;
import de.jtem.halfedgetools.plugin.algorithm.topology.FillHolesPlugin;
import de.jtem.halfedgetools.plugin.algorithm.topology.RemoveVertexFillPlugin;
import de.jtem.halfedgetools.plugin.algorithm.topology.VertexRemoverPlugin;
import de.jtem.halfedgetools.plugin.algorithm.vectorfield.CurvatureVectorFields;
import de.jtem.halfedgetools.plugin.modes.EditMode;
import de.jtem.halfedgetools.plugin.modes.SelectionMode;
import de.jtem.halfedgetools.plugin.visualizers.DirichletEnergyVisualizer;
import de.jtem.halfedgetools.plugin.visualizers.EdgeLengthVisualizer;
import de.jtem.halfedgetools.plugin.visualizers.FacePlanarityVisualizer;
import de.jtem.halfedgetools.plugin.visualizers.NodeIndexVisualizer;
import de.jtem.halfedgetools.plugin.visualizers.NormalVisualizer;
import de.jtem.halfedgetools.plugin.widget.ContextMenuWidget;
import de.jtem.halfedgetools.plugin.widget.MarqueeWidget;
import de.jtem.halfedgetools.plugin.widget.ViewSwitchWidget;
import de.jtem.jrworkspace.plugin.Plugin;

public class HalfedgePluginFactory {


	public static Set<Plugin> createEditingPlugins() {
		Set<Plugin> hs = new HashSet<Plugin>();
		hs.add(new VertexRemoverPlugin());
		hs.add(new RemoveVertexFillPlugin());
		hs.add(new FaceRemoverPlugin());
		hs.add(new FaceCollapserPlugin());
		hs.add(new FaceScalerPlugin());
		hs.add(new FaceSplitterPlugin());
		hs.add(new EdgeCollapsePlugin());
		hs.add(new EdgeRemoverFillPlugin());
		hs.add(new EdgeRemoverPlugin());
		hs.add(new EdgeSplitterPlugin());
		hs.add(new FillHolesPlugin());
		hs.add(new EdgeFlipperPlugin());
		hs.add(new DelaunayPlugin());
		return hs;
	}
	
	public static Set<Plugin> createGeometryPlugins() {
		Set<Plugin> hs = new HashSet<Plugin>();
		hs.add(new PerturbPlugin());
		hs.add(new ProjectPlugin());
		return hs;
	}
	
	
	public static Set<Plugin> createSubdivisionPlugins() {
		Set<Plugin> s = new HashSet<Plugin>();
		s.add(new CatmullClarkLinearPlugin());
		s.add(new CatmullClarkPlugin());
		s.add(new MedialGraphLinearPlugin());
		s.add(new LoopPlugin());
		s.add(new LoopLinearPlugin());
		s.add(new QuadGraphLinearPlugin());
		s.add(new GarlandHeckbertPlugin());
		s.add(new Sqrt3LinearPlugin());
		s.add(new Sqrt3Plugin());
		s.add(new StellarLinearPlugin());
		s.add(new TriangulatePlugin());
		return s;
	}
	
	
	public static Set<Plugin> createGeneratorPlugins() {
		Set<Plugin> s = new HashSet<Plugin>();
		s.add(new RandomSphereGenerator());
		s.add(new ConvexHullGenerator());
		return s;
	}
	
	public static Set<Plugin> createSelectionPlugins() {
		Set<Plugin> s = new HashSet<Plugin>();
		s.add(new BoundaryVertexSelection());
		s.add(new BoundaryEdgeSelection());
		s.add(new BoundaryFaceSelection());
		s.add(new InvertVertexSelection());
		s.add(new InsideEdgesSelection());
		s.add(new InsideFacesSelection());
		s.add(new TouchingEdgesSelection());
		s.add(new TouchingFacesSelection());
		s.add(new InvertEdgeSelection());
		s.add(new InvertFaceSelection());
		s.add(new ClearSelection());
		s.add(new ClearVertexSelection());
		s.add(new ClearEdgeSelection());
		s.add(new ClearFaceSelection());
		s.add(new ExportSelection());
		s.add(new ImportSelection());
		return s;
	}
	
	public static Set<Plugin> createVisualizerPlugins() {
		Set<Plugin> s = new HashSet<Plugin>();
		s.add(new NodeIndexVisualizer());
		s.add(new EdgeLengthVisualizer());
		s.add(new NormalVisualizer());
		s.add(new FacePlanarityVisualizer());
		s.add(new DirichletEnergyVisualizer());
		return s;
	}
	
	
	public static Set<Plugin> createWidgetPlugins() {
		Set<Plugin> s = new HashSet<Plugin>();
		s.add(new MarqueeWidget());
		s.add(new ViewSwitchWidget());
		s.add(new ContextMenuWidget());
		return s;
	}
	
	
	public static Set<Plugin> createEditorModePlugins() {
		Set<Plugin> s = new HashSet<Plugin>();
		s.add(new EditorManager());
		s.add(new EditMode());
		s.add(new SelectionMode());
		return s;
	}
	
	
	public static Set<Plugin> createPlugins() {
		Set<Plugin> s = new HashSet<Plugin>();
		s.addAll(createGeneratorPlugins());
		s.addAll(createGeometryPlugins());
		s.addAll(createSelectionPlugins());
		s.addAll(createSubdivisionPlugins());
		s.addAll(createEditingPlugins());
		s.addAll(createVisualizerPlugins());
		s.addAll(createWidgetPlugins());
		s.addAll(createEditorModePlugins());
		s.add(new VectorFieldManager());
		s.add(new CurvatureVectorFields());
		return s;
	}
	
}
