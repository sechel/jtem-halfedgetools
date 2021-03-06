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
import java.util.List;

import javax.swing.KeyStroke;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.algorithm.topology.TopologyAlgorithms;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.SelectionInterface;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmPlugin;
import de.jtem.halfedgetools.plugin.image.ImageHook;
import de.jtem.halfedgetools.selection.Selection;
import de.jtem.halfedgetools.selection.TypedSelection;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.PluginInfo;

public class EdgeFlipperPlugin extends AlgorithmPlugin {

	private final Integer
		CHANNEL_FLIPPED_EDGES = 23423433;

	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void execute(HDS hds, AdapterSet a, HalfedgeInterface hcp) {
		Selection flippedEdges = new Selection();
		TypedSelection<E> edges = hcp.getSelection().getEdges(hds);
		if (edges.isEmpty()) return;
		for (E e : edges) {
			if (e.isPositive()) continue;
			boolean canFlip = true;
			if (e.getLeftFace() != null) {
				List<E> b = HalfEdgeUtils.boundaryEdges(e.getLeftFace());
				if (b.size() != 3) {
					canFlip = false;
				}
			}
			if (e.getRightFace() != null) {
				List<E> b = HalfEdgeUtils.boundaryEdges(e.getRightFace());
				if (b.size() != 3) {
					canFlip = false;
				}
			}
			if (!canFlip) {
				throw new RuntimeException("Can only flip edges between triangles");
			}
			TopologyAlgorithms.flipEdge(e);
			flippedEdges.add(e, CHANNEL_FLIPPED_EDGES);
			flippedEdges.add(e.getOppositeEdge(), CHANNEL_FLIPPED_EDGES);
		}
		hcp.update();
		hcp.addSelection(flippedEdges);
	}

	
	@Override
	public AlgorithmCategory getAlgorithmCategory() {
		return AlgorithmCategory.Editing;
	}
	
	
	@Override
	public String getAlgorithmName() {
		return "Flip Edge";
	}

	@Override
	public KeyStroke getKeyboardShortcut() {
		return KeyStroke.getKeyStroke('F', InputEvent.SHIFT_DOWN_MASK);
	}
	
	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Edge Flipper", "Kristoffer Josefsson");
		info.icon = ImageHook.getIcon("edgeflip.png", 16, 16);
		return info;
	}
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		c.getPlugin(SelectionInterface.class).registerChannelName(CHANNEL_FLIPPED_EDGES, "Flipped Edges");
	}
	

}
