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

package de.jtem.halfedgetools.algorithm.adaptivesubdivision.interpolators;


import de.jtem.halfedgetools.algorithm.adaptivesubdivision.util.Calculator;
import de.jtem.halfedgetools.algorithm.delaunay.decorations.HasLengthSquared;
import de.jtem.halfedgetools.jreality.node.JREdge;
import de.jtem.halfedgetools.jreality.node.JRFace;
import de.jtem.halfedgetools.jreality.node.JRVertex;
/** Implementierung Modifiziertes Butterfly Verfahrens zur Bestimmung von 
 * Koordinaten von neuen Vertice auf der Kantenmitte.
 * @author Bernd Gonska
 */
public class ModButterflyAlg extends Interpolator{
	/** Liefert den Vertex, der der Halbkante gegenueber liegt.
	 * @param e Halbkante
	 * @return gegenueberliegender Vertex
	 */
	private static<
	V extends JRVertex<V,E,F>,
	E extends JREdge<V,E,F> & HasLengthSquared,
	F extends JRFace<V,E,F>
	> V getOppVertex(E e){
		return e.getNextEdge().getTargetVertex();
	}
	/** Regel fuer die interpolation auf einer Kante an einem irregulaeren Vertex. 
	 * @param e Die Kante.
	 * @param val Valenz des irregulaeren Vertex.
	 * @return Interpolierte Koordinaten.
	 */
	private static<
	V extends JRVertex<V,E,F>,
	E extends JREdge<V,E,F> & HasLengthSquared,
	F extends JRFace<V,E,F>
	> double[] special (E e, int val){
		E eo=e.getOppositeEdge();

		double[] vx=e.getTargetVertex().position;	
		double[] v0=eo.getTargetVertex().position;	
		double[] result=new double[vx.length];
		for (int i = 0; i < result.length; i++) 
			result[i]=0;
		if(val<3){
			return  Calculator.linearCombination( .5, vx, .5, v0);
		}
		
		E currEdge=eo;
		currEdge=currEdge.getOppositeEdge().getNextEdge();
		double[] v1=currEdge.getTargetVertex().position;
		currEdge=currEdge.getOppositeEdge().getNextEdge();
		double[] v2=currEdge.getTargetVertex().position;	
		if(val==3){
			result=Calculator.linearCombination(1, result, 3./4, vx);
			result=Calculator.linearCombination(1, result, 5./12, v0);
			result=Calculator.linearCombination(1, result, -1./12, v1);
			result=Calculator.linearCombination(1, result, -1./12, v2);
			return result;	
		}
		if(val==4){
			result=Calculator.linearCombination(1, result, 3./4, vx);
			result=Calculator.linearCombination(1, result, 3./8, v0);
			result=Calculator.linearCombination(1, result, -1./8, v2);
			return result;	
		}
		currEdge=eo;
		result=Calculator.linearCombination(1, result, .75, e.getTargetVertex().position);
		for(int n=0;n<val;n++){// val>=5
			double a= (1./4+Math.cos(2*Math.PI*n/val)+.5*Math.cos(4.*Math.PI*n/val))/val;
			result=Calculator.linearCombination(1, result, a, currEdge.getTargetVertex().position);
			currEdge=currEdge.getOppositeEdge().getNextEdge();
		}
		return result;
	}
	/** Interpolation an einer Kante mit zwei irregulaeren Vertice.
	 * @param e Die Halbkante.
	 * @param vale Valenz des Zielvertex der Halbkante.
	 * @param valeo Valenz des Startvertex der Halbkante.
	 * @return Interpolierte Koordinaten.
	 */
	private static <
	V extends JRVertex<V,E,F>,
	E extends JREdge<V,E,F> & HasLengthSquared,
	F extends JRFace<V,E,F>
	>double[] mix(E e,int vale,int valeo){
		double[] a=special(e,vale);
		double[] b=special(e.getOppositeEdge(),valeo);
		return Calculator.linearCombination( .5, a, .5, b);
	} 
	/** Interpolation an einer Kante mit zwei regulaeren Vertice.
	 * @param e Die Halbkante.
	 * @return Interpolierte Koordinaten.
	 */
	private static <
	V extends JRVertex<V,E,F>,
	E extends JREdge<V,E,F> & HasLengthSquared,
	F extends JRFace<V,E,F>
	>double[] regular(E e){
		E e1=e.getNextEdge();
		E e2=e1.getNextEdge();
		E eo=e.getOppositeEdge();
		E eo1=eo.getNextEdge();
		E eo2=eo1.getNextEdge();
		
		E e1o=e1.getOppositeEdge();
		E e2o=e2.getOppositeEdge();
		E eo1o=eo1.getOppositeEdge();
		E eo2o=eo2.getOppositeEdge();
		
		double[] v0=e.getTargetVertex().position;	
		double[] v1=getOppVertex(e1o).position;	
		double[] v2=e1.getTargetVertex().position;	
		double[] v3=getOppVertex(e2o).position;
		double[] v4=e2.getTargetVertex().position;
		double[] v5=getOppVertex(eo1o).position;
		double[] v6=eo1.getTargetVertex().position;
		double[] v7=getOppVertex(eo2o).position;
			
		double [] vert=new double[v0.length];
		for (int i = 0; i < vert.length; i++) {
			vert[i]= 1./2*(v0[i]+v4[i])-1./16*(v1[i]+v3[i]+v5[i]+v7[i])
			+1./8*(v2[i]+v6[i]);
		}
		return vert;
	} 
	/** Interpolation an einer Kante mit mindestens einem Randvertex.
	 * Der andere Vertex darf nicht irregulaer sein.
	 * @param e Die Halbkante.
	 * @return Interpolierte Koordinaten.
	 */
	private static<
	V extends JRVertex<V,E,F>,
	E extends JREdge<V,E,F> & HasLengthSquared,
	F extends JRFace<V,E,F>
	> double[] virtuell(E e){
		E e1=e.getNextEdge();
		E e2=e1.getNextEdge();
		E eo=e.getOppositeEdge();
		E eo1=eo.getNextEdge();
		E eo2=eo1.getNextEdge();
		
		E e1o=e1.getOppositeEdge();
		E e2o=e2.getOppositeEdge();
		E eo1o=eo1.getOppositeEdge();
		E eo2o=eo2.getOppositeEdge();
		
		double[] v0=e.getTargetVertex().position;
		double[] v2=e1.getTargetVertex().position;	
		double[] v4=e2.getTargetVertex().position;
		double[] v6=eo1.getTargetVertex().position;
		
		double[] v1,v3,v5,v7;
		if(e1o.getLeftFace()!=null)	v1=getOppVertex(e1o).position;
		else 						v1=getReflectedKoordinates(eo,e1);
		if(e2o.getLeftFace()!=null)	v3=getOppVertex(e2o).position;
		else 						v3=getReflectedKoordinates(e, e2o);
		if(eo1o.getLeftFace()!=null)v5=getOppVertex(eo1o).position;
		else 						v5=getReflectedKoordinates(e, eo1);
		if(eo2o.getLeftFace()!=null)v7=getOppVertex(eo2o).position;
		else 						v7=getReflectedKoordinates(eo, eo2o);
			
		double [] vert=new double[v0.length];
		for (int i = 0; i < vert.length; i++) {
			vert[i]= 1./2*(v0[i]+v4[i])-1./16*(v1[i]+v3[i]+v5[i]+v7[i])
			+1./8*(v2[i]+v6[i]);
		}
		return vert;
	} 
	/** Interpoliert die Koordinaten einer Kante nach dem modifizierten Butterfly-Verfahren.
	 *  Irregulaere Vertice und der Rand werden beruecksichtigt.
	 *  Der Parameter Omega ist hier fest mit 0 implementiert.(standart)
	 */
	@Override
	public <
	V extends JRVertex<V,E,F>,
	E extends JREdge<V,E,F> & HasLengthSquared,
	F extends JRFace<V,E,F>
	>void interpolate(E e,V v) {
		E eo= e.getOppositeEdge();
		if(e.getLeftFace()==null||e.getRightFace()==null){
			interpolateBoundaryEdge(e, v);
			return;
		}
		int vale=getVertexType(e);
		int valeo=getVertexType(eo);
		double[] newPos;
		if(vale>0 && valeo<=0)
			newPos=special(e,vale);
		else if(vale<=0 && valeo>0)
			newPos=special(eo,valeo);
		else if(vale>0 && valeo>0 )
			newPos=mix(e,vale,valeo);
		else if(vale==0 && valeo==0)
			newPos=regular(e);
		else {
			
			newPos=virtuell(e);
		}
		v.position=newPos;
	}
	//------------------- Boundary --------------------
	/** Interpolation auf ener Kante die Teil des Randes ist.
	 * Ihre Interpolation haengt nur vom Rand sebst ab.
	 */
	private <
	V extends JRVertex<V,E,F>,
	E extends JREdge<V,E,F> & HasLengthSquared,
	F extends JRFace<V,E,F>
	>void interpolateBoundaryEdge(E e,V v){
		if(e.getLeftFace()!=null)
			e=e.getOppositeEdge();
		E prev=e.getPreviousEdge();
		E next=e.getNextEdge();
		double[] coords=Calculator.linearCombination(
				-1./16, prev.getStartVertex().position,
				9./16, e.getStartVertex().position,
				9./16, e.getTargetVertex().position,
				-1./16, next.getTargetVertex().position 
		);
		v.position=coords;
	}
	/** Liefert den Typ des Zielvertex der Kante.
	 * @param e Kante
	 * @return
	 *  -1 = Randvertex.
	 *  0 = regulaerer Vertex. 
	 *  1,2,... = Valenz eines irregulaeren vertex. 
	 */
	private static <
	V extends JRVertex<V,E,F>,
	E extends JREdge<V,E,F> & HasLengthSquared,
	F extends JRFace<V,E,F>
	>int getVertexType(E e){
		E start= e;
		E next= e.getNextEdge().getOppositeEdge();
		int val=1;
		while(next!=start){
			val++;
			if(next.getLeftFace()==null)
				return -1;
			next= next.getNextEdge().getOppositeEdge();
		}
		if(val==6) return 0;
		return val;
	}
	/** Reflektiert die Koordinaten des Zielvertex der ersten Kante an der Geraden 
	 * die durch die zweite Kante geht. Das ist noetig um fehlende Vertice 
	 * am Rand zu simulieren. 
	 * @param orig Erste Kante (zeigt auf den zu spiegelnden Vertex).
	 * @param mirror Kante die die SpiegelAchse angibt.
	 * @return Gespiegelte Koordinaten, die einen fehlenden Vertex 
	 * ausserhalb des Randes simulieren.
	 */
	private static <
	V extends JRVertex<V,E,F>,
	E extends JREdge<V,E,F> & HasLengthSquared,
	F extends JRFace<V,E,F>
	>double[] getReflectedKoordinates(E orig,E mirror){
		double[] a=orig.getStartVertex().position;
		double[] b=mirror.getTargetVertex().position;
		double[] c=orig.getTargetVertex().position;
		double[] origVector=Calculator.sub(c,a);
		double[] mirrAx=Calculator.sub(b,a);
		// projekt [a,c] on [a,b] 
		double[] v=Calculator.times(
				Calculator.scalarProd(origVector, mirrAx)/
				Calculator.lenSq(mirrAx), mirrAx);
		return Calculator.linearCombination(
				1,c,-2,origVector,2,v);
	}
}
