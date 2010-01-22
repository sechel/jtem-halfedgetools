/**
This file is part of a jTEM project.
All jTEM projects are licensed under the FreeBSD license 
or 2-clause BSD license (see http://www.opensource.org/licenses/bsd-license.php). 

Copyright (c) 2002-2009, Technische Universität Berlin, jTEM
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

package de.jtem.halfedgetools.util;

import static de.jtem.halfedgetools.jreality.adapter.Adapter.AdapterType.EDGE_ADAPTER;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.jreality.adapter.ColorAdapter2Ifs;
import de.jtem.halfedgetools.jreality.adapter.RelRadiusAdapter2Ifs;
import de.jtem.halfedgetools.util.CuttingUtility.CuttingInfo;

public class MarkedEdgesAdapter
<V extends Vertex<V,E,F>,
E extends Edge<V,E,F>,
F extends Face<V,E,F>>  implements ColorAdapter2Ifs<E>
, RelRadiusAdapter2Ifs<E> 
{

	protected CuttingInfo<V, E, F>
		context = new CuttingInfo<V, E, F>();
	protected Random
		rnd = new Random();
	protected double[]
	    normalColor = {1, 1, 1};
	protected Map<Set<E>, double[]>
		pathColors = new HashMap<Set<E>, double[]>();
	
	
	public MarkedEdgesAdapter() {
	}
	
	public MarkedEdgesAdapter(CuttingInfo<V, E, F> context) {
		this.context = context;
		updatePathColors();
	}
	
	public void setContext(CuttingInfo<V, E, F> context) {
		this.context = context;
		updatePathColors();
	}	
	
	
	private void updatePathColors() {
		pathColors.clear();
		if(context == null){
			System.err.println("context not set in MarkedEdgesAdapter");
		} else if (context.paths == null) {
			System.err.println("invalid context in MarkedEdgesAdapter");
		}
		
		int i = 0;
		for (Set<E> path : context.paths.keySet()) {
			rnd.setSeed(i);
			double[] color = new double[] {rnd.nextDouble(), rnd.nextDouble(), rnd.nextDouble()};
			pathColors.put(path, color);
			i++;
		}
	}
	
	
	@Override
	public double[] getColor(E e) {
		for (Set<E> path : context.paths.keySet()) {
			Set<E> coPath = context.pathCutMap.get(path);
			if (path.contains(e) || path.contains(e.getOppositeEdge())) {
				return pathColors.get(path);
			}
			if(coPath != null) {
				if (coPath.contains(e) || coPath.contains(e.getOppositeEdge())) {
					return pathColors.get(path);
				}
			}
		}

		return normalColor;
	}
	
	@Override
	public double getReelRadius(E e) {
		for (Set<E> path : context.paths.keySet()) {
			Set<E> coPath = context.pathCutMap.get(path);
			if (path.contains(e) || path.contains(e.getOppositeEdge())) {
				return 2.0;
			}
			if(coPath != null) {
				if (coPath.contains(e) || coPath.contains(e.getOppositeEdge())) {
					return 2.0;
				}
			}
		}
		return 0.5;
	}
	
	
	@Override
	public AdapterType getAdapterType() {
		return EDGE_ADAPTER;
	}

}
