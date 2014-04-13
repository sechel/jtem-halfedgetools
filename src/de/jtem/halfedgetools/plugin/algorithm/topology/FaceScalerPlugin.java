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

package de.jtem.halfedgetools.plugin.algorithm.topology;

import java.awt.event.InputEvent;
import java.util.Set;

import javax.swing.KeyStroke;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.adapter.type.TexturePosition;
import de.jtem.halfedgetools.adapter.type.generic.BaryCenter3d;
import de.jtem.halfedgetools.adapter.type.generic.Position3d;
import de.jtem.halfedgetools.adapter.type.generic.TexturePosition2d;
import de.jtem.halfedgetools.algorithm.topology.TopologyAlgorithms;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmPlugin;
import de.jtem.halfedgetools.selection.Selection;
import de.jtem.jrworkspace.plugin.PluginInfo;

public class FaceScalerPlugin extends AlgorithmPlugin {

	private double 
		t = 0.5;
	private int 
		twist = 0;
	
	
	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void execute(HDS hds, AdapterSet a, HalfedgeInterface hif) {
		Set<F> faces = hif.getSelection().getFaces(hds);
		if (faces.isEmpty()) return;
		Selection s = new Selection();
		for (F oldF : faces) {
			double[] p = a.get(BaryCenter3d.class, oldF, double[].class);
			double[] tp = a.get(TexturePosition2d.class, oldF, double[].class);
			int n = HalfEdgeUtils.boundaryVertices(oldF).size();
			double[][] oldVs = new double[n][];
			double[][] oldVts = new double[n][];
			int i = 0;
			for(V bv : HalfEdgeUtils.boundaryVertices(oldF)) {
				oldVs[i] = a.get(Position3d.class, bv, double[].class);
				oldVts[i] = a.get(TexturePosition2d.class, bv, double[].class);
				i++;
			}
			F f = TopologyAlgorithms.scaleFace(oldF);
			i = 0;
			for(V v : HalfEdgeUtils.boundaryVertices(f)) {
				double[] newPos = Rn.linearCombination(null, t, p, 1.0-t, oldVs[(i+twist-1+n)%n]);
				double[] newTexPos = Rn.linearCombination(null, t, tp, 1.0-t, oldVts[(i+twist-1+n)%n]);
				a.set(Position.class, v, newPos);
				a.set(TexturePosition.class, v, newTexPos);
				i++;
			}
			s.add(f);
		}
		hif.update();
		hif.setSelection(s);		
	}

	@Override
	public String getAlgorithmName() {
		return "Scale Face";
	}
	
	@Override
	public KeyStroke getKeyboardShortcut() {
		return KeyStroke.getKeyStroke('F', InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK);
	}
	
	@Override
	public AlgorithmCategory getAlgorithmCategory() {
		return AlgorithmCategory.Editing;
	}

	@Override
	public PluginInfo getPluginInfo() {
		return new PluginInfo("Face Scaler", "Kristoffer Josefsson");
	}

}
