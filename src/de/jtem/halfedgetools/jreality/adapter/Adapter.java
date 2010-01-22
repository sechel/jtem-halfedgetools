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

package de.jtem.halfedgetools.jreality.adapter;


public interface Adapter {
	/** 
	 * Adapters are nescecary to access the Data of the H.E.D.S.
	 *  you can use adapters as subtypes of the following types:
	 *  ColorAdapter2Ifs			ColorAdapter2Heds
	 *  CoordinateAdapter2Ifs 		CoordinateAdapter2Heds 
	 *  LabelAdapter2Ifs			LabelAdapter2Heds
	 *  NormalAdapter2Ifs			NormalAdapter2Heds
	 *  PointSizeAdapter2Ifs		PointSizeAdapter2Heds
	 *  RelRadiusAdapter2Ifs		RelRadiusAdapter2Heds
	 *  TextCoordsAdapter2Ifs		TextCoordsAdapter2Heds
	 *  
	 *  ..2Ifs are used for reading Data 
	 *  ..2Heds are used for writing Data 
	 *  every adapter supports one of the following:
	 *   vertices, edges or faces
	 *  
	 * @author gonska
	 *
	 */
	public static enum AdapterType{
		VERTEX_ADAPTER,
		EDGE_ADAPTER,
		FACE_ADAPTER
	}
	public AdapterType getAdapterType();
	
}
