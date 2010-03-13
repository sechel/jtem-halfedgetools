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

package de.jtem.halfedgetools.symmetry;

import de.jreality.plugin.JRViewer;
import de.jreality.plugin.JRViewer.ContentType;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.algorithm.subdivision.TriangulatePlugin;
import de.jtem.halfedgetools.symmetry.adapters.BundleCycleColorAdapter;
import de.jtem.halfedgetools.symmetry.adapters.BundleLabelAdapter;
import de.jtem.halfedgetools.symmetry.adapters.SymmetricPositionAdapter;
import de.jtem.halfedgetools.symmetry.calculators.SymmetricSubdivisionCalculator;
import de.jtem.halfedgetools.symmetry.plugin.CompactifierPlugin;
import de.jtem.halfedgetools.symmetry.plugin.SymmetricCatmullClarkPlugin;
import de.jtem.halfedgetools.symmetry.plugin.SymmetricLoopPlugin;
import de.jtem.halfedgetools.symmetry.plugin.SymmetricSqrt3Plugin;

public class SymmetricSubdivisionTutorial {


	public static void main(String[] args) {
	
		JRViewer viewer = new JRViewer();
		viewer.addBasicUI();
		viewer.addContentUI();
		viewer.addContentSupport(ContentType.CenteredAndScaled);
		viewer.setShowPanelSlots(true, false, false, false);
		viewer.setShowToolBar(true);
		viewer.setPropertiesFile("test.jrw");
		
		HalfedgeInterface hif = new HalfedgeInterface();
		hif.addAdapter(new SymmetricPositionAdapter());
		hif.addAdapter(new BundleLabelAdapter());
		hif.addAdapter(new BundleCycleColorAdapter());
		hif.addCalculator(new SymmetricSubdivisionCalculator());
		viewer.registerPlugin(hif);
		viewer.registerPlugin(new SymmetricCatmullClarkPlugin());
		viewer.registerPlugin(new TriangulatePlugin());

		viewer.registerPlugin(new SymmetricLoopPlugin());
		
		viewer.registerPlugin(new SymmetricSqrt3Plugin());
		
		viewer.registerPlugin(new CompactifierPlugin());
		
//		viewer.registerPlugin(new EdgeFlipperPlugin<SVertex,SEdge,SFace,SHDS>());
//		viewer.registerPlugin(new TriangulatePlugin<SVertex,SEdge,SFace,SHDS>());
//		viewer.registerPlugins(TopologyOperations.topologicalEditing(
//				new SymmetricSubdivisionAdapter(),
//				new SSubdivisionAdapters.SSubdivisionEA(),
//				new SSubdivisionAdapters.SSubdivisionFA()
//		));
		
//		viewer.registerPlugin(new HalfedgeDebuggerPlugin<SVertex,SEdge,SFace,SHDS>());
		
		viewer.startup();
	}
}
