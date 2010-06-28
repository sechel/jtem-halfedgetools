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

package de.jtem.halfedgetools.algorithm.adaptivesubdivision.util;
public interface RnRmFunktion {
	public double[] map(double[] param);
	/** Normiert den gegebenen Vektor (0 bleibt 0).
	 * Beliebige Dimension moeglich.
	 *  @author Bernd Gonska
	 */
	public static class SphereProjector implements RnRmFunktion{
		@Override
		public double[] map(double[] param) {
			double[] ret=new double[param.length];
			double lenSq=0;
			for (int i = 0; i < ret.length; i++){ 
				ret[i]=param[i];
				lenSq+=ret[i]*ret[i];
			}
			if(lenSq!=0){
				double len=Math.sqrt(lenSq);
				for (int i = 0; i < 3; i++) 
					ret[i]=ret[i]/len;
			}
			return ret;
		}
	}
	/** Gibt eine Kopie des vektors zurueck. 
	 * Beliebige Dimension moeglich.
	 *  @author Bernd Gonska
	 */
	public static class Identity implements RnRmFunktion{
		@Override
		public double[] map(double[] param) {
			double[] ret=new double[param.length];
			for (int i = 0; i < ret.length; i++) 
				ret[i]=param[i];
			return ret;
		}
	}
}
