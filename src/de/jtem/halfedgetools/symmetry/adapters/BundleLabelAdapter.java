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

package de.jtem.halfedgetools.symmetry.adapters;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AbstractAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.Bundle;
import de.jtem.halfedgetools.adapter.Bundle.DisplayType;
import de.jtem.halfedgetools.adapter.type.Label;

@Label
public class BundleLabelAdapter extends AbstractAdapter<String> {
	
	private static String 
		newline = System.getProperty("line.separator");
	private DecimalFormat 
		twoPlaces = new DecimalFormat("0.000");

	public BundleLabelAdapter() {
		super(String.class, true, false);
	}
	
	@Override
	public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
		return true;
	}
	
	@Override
	public double getPriority() {
		return 1;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		N extends Node<V, E, F>
	> String get(N node, AdapterSet a) {
		String s = "";
		Class c = node.getClass();
		
		for(Method m : c.getMethods()) {
			if(m.isAnnotationPresent(Bundle.class)) {
				if(m.getAnnotation(Bundle.class).display() == DisplayType.Label) {
					String toAdd = "";
					String name = m.getAnnotation(Bundle.class).name();
//					BundleType bt = m.getAnnotation(Bundle.class).type();
					
					Object ret = null;
					try {
						ret = m.invoke(node);
					} catch (IllegalArgumentException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IllegalAccessException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (InvocationTargetException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
	//				switch(bt){
	//					case Value:
	//						toAdd = name + " " + twoPlaces.format(ret) + newline;
	//					default:
	//						toAdd = name + " " + ret.toString() + newline;			
	//				}
	
					if (ret == null) {
						continue;
					}
					if(ret instanceof Double) {
						toAdd = name + " " + twoPlaces.format(ret) + newline;
					} else {
						toAdd = name + " " + ret.toString() + newline;
					}
					s += toAdd;
				}
			}
		}
		
		return s;
	}
	
}
