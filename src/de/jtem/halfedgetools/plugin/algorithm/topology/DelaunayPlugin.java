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

import java.util.HashMap;
import java.util.Map;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AbstractAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Length;
import de.jtem.halfedgetools.adapter.type.generic.Position3d;
import de.jtem.halfedgetools.algorithm.triangulation.Delaunay;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmPlugin;
import de.jtem.halfedgetools.util.ConsistencyCheck;
import de.jtem.halfedgetools.util.TriangulationException;
import de.jtem.jrworkspace.plugin.PluginInfo;

public class DelaunayPlugin extends AlgorithmPlugin {

	@Length
	private class DelaunayLengthAdapter extends AbstractAdapter<Double> {

		private Map<Edge<?, ?, ?>, Double>
			lMap = new HashMap<Edge<?,?,?>, Double>();
		
		public DelaunayLengthAdapter() {
			super(Double.class, true, true);
		}
		
		@Override
		public <
			V extends Vertex<V, E, F>,
			E extends Edge<V, E, F>,
			F extends Face<V, E, F>
		> Double getE(E e, AdapterSet a) {
			if (lMap.containsKey(e)) {
				return lMap.get(e);
			} else {
				double[] s = a.get(Position3d.class, e.getStartVertex(), double[].class);
				double[] t = a.get(Position3d.class, e.getTargetVertex(), double[].class);
				double l = Rn.euclideanDistance(s, t);
				lMap.put(e, l);
				lMap.put(e.getOppositeEdge(), l);
				return l;
			}
		}	
		
		@Override
		public <
			V extends Vertex<V, E, F>,
			E extends Edge<V, E, F>,
			F extends Face<V, E, F>
		> void setE(E e, Double value, AdapterSet a) {
			lMap.put(e, value);
			lMap.put(e.getOppositeEdge(), value);
		}
		
		
		@Override
		public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
			return Edge.class.isAssignableFrom(nodeClass);
		}
		
		@Override
		public double getPriority() {
			return 10;
		}
		
	}
	
	
	
	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void execute(HDS hds, AdapterSet a, HalfedgeInterface hi) {
		if (!ConsistencyCheck.isTriangulation(hds)) {
			throw new RuntimeException("Surface is no triangulation in Delaunay()");
		}
		DelaunayLengthAdapter la = new DelaunayLengthAdapter();
		a.add(la);
		try {
			Delaunay.constructDelaunay(hds, a);
		} catch (TriangulationException e) {
			throw new RuntimeException(e.getLocalizedMessage());
		} finally {
			a.remove(la);
		}
		hcp.update();
	}

	
	@Override
	public AlgorithmCategory getAlgorithmCategory() {
		return AlgorithmCategory.Geometry;
	}
	
	
	@Override
	public String getAlgorithmName() {
		return "Delaunay";
	}

	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Construct Delaunay Triangulation", "Stefan Sechelmann");
		return info;
	}
	
	
	

}
