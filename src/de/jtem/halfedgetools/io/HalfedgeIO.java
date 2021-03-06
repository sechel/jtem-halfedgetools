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

package de.jtem.halfedgetools.io;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import com.thoughtworks.xstream.XStream;

import de.jreality.writer.WriterOBJ;
import de.jreality.writer.WriterSTL;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.jreality.ConverterHeds2JR;


public class HalfedgeIO {

	private static XStream 
		xstream = new XStream();

	public static HalfEdgeDataStructure<?,?,?> readHDS(String filename) throws IOException {
		FileReader fr = new FileReader(filename);
		xstream.setMode(XStream.ID_REFERENCES);
		return (HalfEdgeDataStructure<?,?,?>)xstream.fromXML(fr);
	}
	
	public static HalfEdgeDataStructure<?,?,?> readHDS(InputStream in) throws IOException {
		xstream.setMode(XStream.ID_REFERENCES);
		return (HalfEdgeDataStructure<?,?,?>)xstream.fromXML(in);
	}
	
	
	public static void writeHDS(HalfEdgeDataStructure<?,?,?> heds, String filename) throws IOException {
		FileWriter fw = new FileWriter(filename);
		xstream.setMode(XStream.ID_REFERENCES);
		xstream.toXML(heds, fw);
	}

	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void writeOBJ(HDS hds, AdapterSet adapters, String file) {
		try {
			FileOutputStream fos = new FileOutputStream(file);
			ConverterHeds2JR converter = new ConverterHeds2JR();
			WriterOBJ.write(converter.heds2ifs(hds, adapters,null), fos);
			fos.close();
		} catch (Exception e1) {
			System.err.println("Could not write to file " + file);
			e1.printStackTrace();
		}
		
	}

	public static void writeOBJ(List<HalfEdgeDataStructure<?, ?, ?>> hdss, List<String> names, AdapterSet adapters, String file) {
		try {
			FileOutputStream fos = new FileOutputStream(file);
			ConverterHeds2JR converter = new ConverterHeds2JR();
			Iterator<String> nameIter = names.iterator();
			HashSet<String> newNames = new HashSet<>(); 
			Iterator<HalfEdgeDataStructure<?, ?, ?>> hdsIter = hdss.iterator();
			int count = 0;
			for (; hdsIter.hasNext() && nameIter.hasNext();) {
				String name = nameIter.next();
				name = name.replace(' ','_');
				while(newNames.contains(name)) {
					name += "_";
				}
				newNames.add(name);
				HalfEdgeDataStructure<?, ?, ?> hds = hdsIter.next();
				if(hds.numVertices() == 0) {
					continue;
				}
				if(hds.numFaces() != 0) {
					count += WriterOBJ.write(converter.heds2ifs(hds, adapters,null), name, count, fos);
				} else {
					count += WriterOBJ.write(converter.heds2ifs(hds, adapters, null), name, count, true, fos);
				}
			}
			fos.close();
		} catch (Exception e1) {
			System.err.println("Could not write to file " + file);
			e1.printStackTrace();
		}
	
	}

	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void writeSTL(HDS offsetLayout, AdapterSet as, String fileName) {
		writeSTL(offsetLayout, as, fileName, false);
	}
	
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void writeSTLSolid(HDS offsetLayout, AdapterSet as, String fileName) {
		writeSTL(offsetLayout, as, fileName, true);
	}
	
	private static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void writeSTL(HDS offsetLayout, AdapterSet as, String fileName, boolean solid) {
		try {
			FileOutputStream fos = new FileOutputStream(fileName);
			ConverterHeds2JR converter = new ConverterHeds2JR();
			if(solid) {
				WriterSTL.writeSolid(converter.heds2ifs(offsetLayout, as ,null), fos);
			} else {
				WriterSTL.write(converter.heds2ifs(offsetLayout, as ,null), fos);
			}
			fos.close();
		} catch (Exception e1) {
			System.err.println("Could not write to file " + fileName);
			e1.printStackTrace();		}

	}
}

