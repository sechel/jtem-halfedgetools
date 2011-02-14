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

package de.jtem.halfedgetools.algorithm.adaptivesubdivision;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedgetools.algorithm.adaptivesubdivision.decorations.HasType.Type;
import de.jtem.halfedgetools.algorithm.adaptivesubdivision.interpolators.Interpolator;
import de.jtem.halfedgetools.algorithm.adaptivesubdivision.interpolators.LinearEdgeSubdivAlg;
import de.jtem.halfedgetools.algorithm.adaptivesubdivision.interpolators.ModButterflyAlg;
import de.jtem.halfedgetools.algorithm.adaptivesubdivision.interpolators.SplineInterpolator;
import de.jtem.halfedgetools.algorithm.adaptivesubdivision.util.Calculator;
import de.jtem.halfedgetools.algorithm.alexandrov.delaunay.decorations.HasLengthSquared;
import de.jtem.halfedgetools.jreality.node.JREdge;
import de.jtem.halfedgetools.jreality.node.JRFace;
import de.jtem.halfedgetools.jreality.node.JRVertex;
/** Unterteilt Flaechen. 
 * Verfuegt auch ueber einen Edge-Remove-Algorithmus.
 * Benoetigt die Daten in Form einer HalfEdgeDatenstruktur.
 * @author Bernd Gonska
 *
 */
public class Subdivider<
	V extends JRVertex<V,E,F>,
	E extends JREdge<V,E,F> & HasLengthSquared,
	F extends JRFace<V,E,F>,
	HDS extends HalfEdgeDataStructure<V,E,F>
>  {
	/* edge:bad=>to long 
	 * edge:good=>not to long
	 * edge:illegal=>remove from heds
	 */
	public static enum SubdivType{ NONADAPTIVE, SIMPLE,EXTENDED}; 
	public static enum InterpolFunk{ LINEAR, BUTTERFLY,SPLINE}; 

	// node----------------------------------------
//	private static final double[] illegal=new double[]	{1,0,1};
//	//edge colors:----------------------------------------
//	private static final double[] ToBad=new double[]		{1,0,0};
//	private static final double[] ToShort=new double[]	{1,0,0};
//	private static final double[] good=new double[]		{0,1,0};
//	private static final double[] instable=new double[]	{1,.5,1};
//	private static final double[] oldEdge=new double[]	{1,1,1};
//	private static final double[] newEdge=new double[]	{0,1,0};
//	private static final double[] test=new double[]		{1,0,0};
//	// face colors----------------------------------------
//	private static final double[] fine=new double[]		{.8,1,1};
//	private static final double[] end=new double[]		{0,0,1};
//	private static final double[] way=new double[]		{0,1,0};
//	private static final double[] node=new double[]		{1,0,0};
//	private static final double[] splited=new double[]	{.5,.5,1};
//	private static final double[] waypair=new double[]	{.7,1,0};
//	private static final double[] waysingle=new double[]	{0,1,.7};
//	private static final double[] nodesplit=new double[]	{1,.5,.5};
//	// Vertex ----------------------------------------
//	private static final double[] newOne=new double[]	{1,.5,.8};
//	private static final double[] oldOne=new double[]	{.5,.7,.5};
	// Datas----------------------------------------
	private HDS heds;
	private List<E> lostSide= new LinkedList<E>();
	private List<F> badFaces= new LinkedList<F>();
	private List<E> badEdge= new LinkedList<E>();
	private List<E> shortEdge= new LinkedList<E>();
	private double toleranceShort=0.05;
	private double toleranceLong=1;
	private double toleranceCurved=1;
	private double tolerancePairAngleSq=0;
	private double toleranceRemoveAngleSq=0;
	// settings----------------------------------------
	private boolean calcNormals=true;
	private SubdivType type=SubdivType.SIMPLE;

	private boolean lengthKrit=true;
	private boolean shortKrit=false;
	private boolean flatFacesKrit=false;
	private boolean edgeCurvatureKrit=false;

	private boolean extraFlip=true;
	private Interpolator interp= new SplineInterpolator();// creates parameter-positions of new parameter-vertices
	// Auswertung -------------------
	public int longCount=0;
	public double longest=-1;
	public int shortCount=0;
	public double shortest=-1;
	public int flatCount=0;
	public double flat=-1;
	public int curvedCount=-1;
	public double curved=-1;
	
	private Map<V, double[]> newCoords = new HashMap<V, double[]>();
	
	private Map<V, Type> vTypes = new HashMap<V, Type>();
	private Map<E, Type> eTypes = new HashMap<E, Type>();
	private Map<F, Type> fTypes = new HashMap<F, Type>();
	
	private HDS newHeds = null;
	
	public Subdivider(HDS newHeds) {
		this.newHeds = newHeds;
	}
	
	
	// precalc Vertices -------------------
	private HashMap<E, V> newVert=new HashMap<E, V>();
	/** Legt einen neuen Vertice fuer die Kante an.
	 * Dieser ist dann noch nicht Teil der Flaeche. 
	 * @param e Kante
	 */
	private void setNewVertex(E e){
		if (newVert.containsKey(e.getOppositeEdge())) return;
		V v=heds.addNewVertex();
		interp.interpolate(e,v);
		vTypes.put(v, Type.illegal);
		newVert.put(e, v);
	}
	/** Ein bereits angelegter Vertex zu dieser Kante wird in die Flaeche eingefuegt.
	 * @param e Kante die durch diesen Vertex geteilt wird.
	 * @return Der eingefuegte Vertex.
	 */
	private V takeNewVertex(E e){
		if(!newVert.containsKey(e)&&!newVert.containsKey(e.getOppositeEdge())){
			System.out.println("Subdivider.getNewVertex(no vertex in data)");
			return null;
		}
		if(!newVert.containsKey(e)) return takeNewVertex(e.getOppositeEdge());
		V v= newVert.get(e);
		vTypes.put(v, Type.newOne);
		return v;
	}
	/** Ersetzt den ZielVertex einer Kante durch den hier gegebenen.
	 * Alle angrenzenden Kanten des alten Vertex richten sich 
	 * dabei auf den neuen aus.
	 * @param e Kante
	 * @param v einzusetzender Vertex
	 */
	private void assignVertex(E e,V v){
		while (e.getTargetVertex()!=v){
			e.setTargetVertex(v);
			E n=e.getNextEdge();
			E eo=n.getOppositeEdge();
			e=eo;
		}				
	}
	/** Berechnet die Vertex und Faceetten-Normalen fuer die Flaeche. 
	 */
	private void makeNormals(){
		for(V v:heds.getVertices())
			v.normal=new double[]{0,0,0};
		for(E e:heds.getEdges())
			e.normal=new double[]{0,0,0};
		for(F f:heds.getFaces())
			setNormal(f);
		for(V v:heds.getVertices())
			v.normal=Calculator.normalize(v.normal);
		for(E e:heds.getEdges())
			e.normal=Calculator.normalize(e.normal);		
	}
	/** Setzt die Anteile der Vertexnormalen die aus dieser Facette.
	 * folgen.
	 * @param f
	 */
	private void setNormal(F f){
		E e1=f.getBoundaryEdge();
		V v1= e1.getTargetVertex();
		E  e2=e1.getNextEdge();
		V v2= e2.getTargetVertex();
		E  e3=e2.getNextEdge();
		V v3= e3.getTargetVertex();
		double[] c0=v1.position;
		double[] c1=v2.position;
		double[] c2=v3.position;
		f.normal=Calculator.setAngleWeightNormal(v1.normal, c1, c2, c0);
		Calculator.setAngleWeightNormal(v2.normal, c2, c0, c1);
		Calculator.setAngleWeightNormal(v3.normal, c0, c1, c2);
		e1.normal=Calculator.add(e1.normal, f.normal);// make Edge normal (partiell,unNormalised)
		e2.normal=Calculator.add(e2.normal, f.normal);
		e3.normal=Calculator.add(e3.normal, f.normal);
		e1.getOppositeEdge().normal=Calculator.add(e1.getOppositeEdge().normal, f.normal);// make Edge normal (partiell,unNormalised)
		e2.getOppositeEdge().normal=Calculator.add(e2.getOppositeEdge().normal, f.normal);
		e3.getOppositeEdge().normal=Calculator.add(e3.getOppositeEdge().normal, f.normal);
	}

	// Splittings -----------------------
	/** Der Viertelt die Facette optimal.
	 * Nachdem die Kanten der Facette unterteilt wurden, bleibt eine Luecke 
	 * zwichen der alten Kante und der aus ihr Unterteilten. Das erhaelt die Datenstruktur.
	 * Nachdem alles unterteilt wurde, muss man diese Luecke schliessen.
	 */
	private void faceSplit(F f){
		// Plan: extrakt the old Face from the linkage except by endpoints

		// given parts
		E e0=f.getBoundaryEdge();
		E e1=e0.getNextEdge();
		E e2=e1.getNextEdge();
		V v0=e1.getTargetVertex();
		V v1=e2.getTargetVertex();
		V v2=e0.getTargetVertex();
		// new vertices

		V v01=takeNewVertex(e2);
		V v12=takeNewVertex(e0);
		V v20=takeNewVertex(e1);

		// new bounding edges
		E e0a=heds.addNewEdge();		E e0b=heds.addNewEdge();
		E e1a=heds.addNewEdge();		E e1b=heds.addNewEdge();
		E e2a=heds.addNewEdge();		E e2b=heds.addNewEdge();
		E e0ao=heds.addNewEdge();		E e0bo=heds.addNewEdge();
		E e1ao=heds.addNewEdge();		E e1bo=heds.addNewEdge();
		E e2ao=heds.addNewEdge();		E e2bo=heds.addNewEdge();
		// new inner edges
		E ev0=heds.addNewEdge();		E ev0o=heds.addNewEdge();
		E ev1=heds.addNewEdge();		E ev1o=heds.addNewEdge();
		E ev2=heds.addNewEdge();		E ev2o=heds.addNewEdge();
		// new faces(old to middleFace)
		F fv0=heds.addNewFace();		F fv1 =heds.addNewFace();
		F fv2=heds.addNewFace();
		// build the new:
		e1bo.setTargetVertex(v20);		e1ao.setTargetVertex(v2);
		e0bo.setTargetVertex(v12);		e0ao.setTargetVertex(v1);
		e2bo.setTargetVertex(v01);		e2ao.setTargetVertex(v0);

		e2a.setTargetVertex(v01);		ev0.setTargetVertex(v20);		e1b.setTargetVertex(v0);
		e2b.setTargetVertex(v1);		e0a.setTargetVertex(v12);		ev1.setTargetVertex(v01);
		e0b.setTargetVertex(v2);			e1a.setTargetVertex(v20);		ev2.setTargetVertex(v12);
		ev0o.setTargetVertex(v01);		ev1o.setTargetVertex(v12);		ev2o.setTargetVertex(v20);

		e1bo.linkNextEdge(e1ao);	e1ao.linkNextEdge(e1);	e1.linkNextEdge(e1bo);
		e0bo.linkNextEdge(e0ao);	e0ao.linkNextEdge(e0);	e0.linkNextEdge(e0bo);
		e2bo.linkNextEdge(e2ao);	e2ao.linkNextEdge(e2);	e2.linkNextEdge(e2bo);

		e2a.linkNextEdge(ev0);		ev0.linkNextEdge(e1b);		e1b.linkNextEdge(e2a);
		e2b.linkNextEdge(e0a);	e0a.linkNextEdge(ev1);		ev1.linkNextEdge(e2b);
		e0b.linkNextEdge(e1a);	e1a.linkNextEdge(ev2);		ev2.linkNextEdge(e0b);
		ev0o.linkNextEdge(ev1o);ev1o.linkNextEdge(ev2o);	ev2o.linkNextEdge(ev0o);

		e2a.setLeftFace(fv0);		ev0.setLeftFace(fv0);		e1b.setLeftFace(fv0);
		e2b.setLeftFace(fv1);		e0a.setLeftFace(fv1);		ev1.setLeftFace(fv1);
		e0b.setLeftFace(fv2);		e1a.setLeftFace(fv2);		ev2.setLeftFace(fv2);
		ev0o.setLeftFace(f);			ev1o.setLeftFace(f);			ev2o.setLeftFace(f);

		e0.setLeftFace(null);		e0ao.setLeftFace(null);		e0bo.setLeftFace(null);
		e1.setLeftFace(null);		e1ao.setLeftFace(null);		e1bo.setLeftFace(null);
		e2.setLeftFace(null);		e2ao.setLeftFace(null);		e2bo.setLeftFace(null);

		e0a.linkOppositeEdge(e0ao);		e0b.linkOppositeEdge(e0bo);
		e1a.linkOppositeEdge(e1ao);		e1b.linkOppositeEdge(e1bo);
		e2a.linkOppositeEdge(e2ao);		e2b.linkOppositeEdge(e2bo);

		ev0.linkOppositeEdge(ev0o);		ev1.linkOppositeEdge(ev1o);
		ev2.linkOppositeEdge(ev2o);

		// colcor: 
		fTypes.put(fv0, Type.nodesplit);	fTypes.put(fv1,Type.nodesplit);	
		fTypes.put(fv2, Type.nodesplit);	fTypes.put(f, Type.nodesplit);
		
		vTypes.put(v01, Type.newOne);	vTypes.put(v12, Type.newOne);	vTypes.put(v20, Type.newOne);
		
		eTypes.put(e0, Type.instable);	eTypes.put(e1, Type.instable);	eTypes.put(e2, Type.instable);
		eTypes.put(e0a, Type.oldEdge);	eTypes.put(e0b, Type.oldEdge);
		eTypes.put(e1a, Type.oldEdge);	eTypes.put(e1b, Type.oldEdge);
		eTypes.put(e2a, Type.oldEdge);	eTypes.put(e2b, Type.oldEdge);
		eTypes.put(e0ao, Type.oldEdge);	eTypes.put(e0bo, Type.oldEdge);
		eTypes.put(e1ao, Type.oldEdge);	eTypes.put(e1bo, Type.oldEdge);
		eTypes.put(e2ao, Type.oldEdge);	eTypes.put(e2bo, Type.oldEdge);
		eTypes.put(ev0, Type.newEdge);	eTypes.put(ev0o, Type.newEdge);
		eTypes.put(ev1, Type.newEdge);	eTypes.put(ev1o, Type.newEdge);
		eTypes.put(ev2, Type.newEdge);	eTypes.put(ev2o, Type.newEdge);

		lostSide.add(e0);
		lostSide.add(e1);
		lostSide.add(e2);
		if(extraFlip){ // intrinsisch !
			double r0=flipIntrinsicLenValue(ev0);
			double r1=flipIntrinsicLenValue(ev1);
			double r2=flipIntrinsicLenValue(ev2);
			double maxR=Math.max(r0,Math.max(r1, r2));
			if(maxR>1){
				if (r0==maxR) {flipEdge(ev0); eTypes.put(ev0,Type.test);}
				else if (r1==maxR){flipEdge(ev1);eTypes.put(ev1,Type.test);}
				else if (r2==maxR){flipEdge(ev2);eTypes.put(ev2,Type.test);}
			}
		}
	}
	/** Flipt eine Kante.
	 * @param e Kante
	 */
	private void flipEdge(E e){		
		E e1=e.getNextEdge();		
		E e2 =e1.getNextEdge();
		E eo=e.getOppositeEdge();
		E eo1=eo.getNextEdge();
		E eo2=eo1.getNextEdge();
		F f=e.getLeftFace();		F fo=eo.getLeftFace();
		e.setTargetVertex(e1.getTargetVertex());
		eo.setTargetVertex(eo1.getTargetVertex());
		e.linkNextEdge(e2);		e2.linkNextEdge(eo1);		eo1.linkNextEdge(e);
		eo.linkNextEdge(eo2);		eo2.linkNextEdge(e1);		e1.linkNextEdge(eo);
		e2.setLeftFace(f);	eo1.setLeftFace(f);
		eo2.setLeftFace(fo);	e1.setLeftFace(fo);
		eTypes.put(e,Type.newEdge);
		eTypes.put(eo,Type.newEdge);
	}
	/** Unterteilt den Ganzen Subdivgraph-Weg der zu dieser Facette gehoert.  
	 * Nachdem die Kanten der Facetten unterteilt wurden, bleibt eine Luecke 
	 * zwichen den alten Kanten und den aus ihr Unterteilten. Das erhaelt die Datenstruktur.
	 * Nachdem alles unterteilt wurde muss man diese Luecke schliessen.
	 * @param f
	 */
	private void way(F f){//checked
		// find one bad edge of  f:
		E firstBad=otherBadEdge(f.getBoundaryEdge());//es gibt ja 2
		// find beginning of Way
		firstBad=findFirstWayEdge(firstBad, firstBad);
		// start removing pairs
		while (haveAPair(firstBad)){
			E oneSide=firstBad;
			E flipEdge= otherBadEdge(firstBad);
			E otherSide=otherBadEdge(flipEdge.getOppositeEdge());
			if( type==SubdivType.EXTENDED &&
					!toFar(firstBad, otherSide)&&
					!Helper.doNotPair(flipEdge,tolerancePairAngleSq)){
				firstBad =otherSide.getOppositeEdge();// next first bad
				E a=split(oneSide,Type.waypair,takeNewVertex(oneSide));
				E b=split(otherSide,Type.waypair,takeNewVertex(otherSide));
				flipEdge(flipEdge);
				// intrinsic flip ! 
				if(extraFlip&&flipIntrinsicLenValue(a)>1&&!flipCreatesOverlap(a)){flipEdge(a); eTypes.put(a,Type.test);}
				if(extraFlip&&flipIntrinsicLenValue(b)>1&&!flipCreatesOverlap(b)){flipEdge(b); eTypes.put(b,Type.test);}
			}
			else{// Querkante waere zu lang(oder diese Regel wird nicht unterstuetzt)=> kein Paar
				E other=otherBadEdge(firstBad);
				E a=split( firstBad,Type.waysingle,takeNewVertex(firstBad));
				firstBad=other.getOppositeEdge();
				E splitEdge=split( other,Type.waysingle,takeNewVertex(other));
				shortEdge.add(splitEdge);
				// not intrinsic!
				if(flipTrueLengthTest(a)){flipEdge(a); eTypes.put(a,Type.test);}
			}
		}	
		if (firstBad.getLeftFace()!=null){// one face left
			E other=otherBadEdge(firstBad);
			E a=split( firstBad,Type.waysingle,takeNewVertex(firstBad));
			E splitEdge=split( other,Type.waysingle,takeNewVertex(other));
			shortEdge.add(splitEdge);
			// not intrinsic!
			if(flipTrueLengthTest(a)){flipEdge(a); eTypes.put(a,Type.test);}
		}
	}
	/** Einseitiger Edge-Split. Spaltet eine Facette an ihrer zu langen Seite.
	 * Nachdem die Kante der Facette unterteilt wurde, bleibt eine Luecke 
	 * zwichen der alten Kante und der aus ihr Unterteilten. Das erhaelt die Datenstruktur.
	 * Nachdem alles unterteilt wurde muss man diese Luecke schliessen.
	 * @param f Facette
	 * @param splitcolor Farbe der beiden neuen Facetten.
	 * @return Kante die in der Mitte der Facette entstand.
	 */
	private E split(F f,Type splitcolor){
		// find a bad edge:
		E e0=f.getBoundaryEdge();//edge to split
		if (eTypes.get(e0)==Type.good) e0=e0.getNextEdge();
		if (eTypes.get(e0)==Type.good) e0=e0.getNextEdge();
		return split(e0,splitcolor,takeNewVertex(e0));
	}
	/** Einseitiger Edge-Split. Spaltet eine Facette(leftFace) an der angegebenen Kante.  
	 * Nachdem die Kante der Facette unterteilt wurde, bleibt eine Luecke 
	 * zwichen der alten Kante und der aus ihr Unterteilten. Das erhaelt die Datenstruktur.
	 * Nachdem alles unterteilt wurde muss man diese Luecke schliessen.
	 * @param e0 Kante
	 * @param splitcolor Farbe der beiden neuen Facetten.
	 * @param v auf der Kante einzufuegender Vertex.
	 * @return Kante die in der Mitte der Facette entstand.
	 */
	private E split(E e0,Type splitcolor,V v){
		//given Infos
		F f=e0.getLeftFace();
		E e1=e0.getNextEdge();
		E e2=e1.getNextEdge();
		V v0=e1.getTargetVertex();
		V v1=e2.getTargetVertex();
		V v2=e0.getTargetVertex();
		// new Parts
		F f2= heds.addNewFace();
		E ea=heds.addNewEdge();
		E eao=heds.addNewEdge();
		E eb=heds.addNewEdge();
		E ebo=heds.addNewEdge();
		E em=heds.addNewEdge();
		E emo=heds.addNewEdge();
		//
		ea.setTargetVertex(v);		eao.setTargetVertex(v1);		
		eb.setTargetVertex(v2);		ebo.setTargetVertex(v);
		em.setTargetVertex(v);		emo.setTargetVertex(v0);

		e2.linkNextEdge(ea);	ea.linkNextEdge(emo);	emo.linkNextEdge(e2);
		eb.linkNextEdge(e1);	e1.linkNextEdge(em);	em.linkNextEdge(eb);
		eao.linkNextEdge(e0);	e0.linkNextEdge(ebo);	ebo.linkNextEdge(eao);

		eao.setLeftFace(null);e0.setLeftFace(null);	ebo.setLeftFace(null);
		e2.setLeftFace(f2);	ea.setLeftFace(f2);	emo.setLeftFace(f2);
		e1.setLeftFace(f);	em.setLeftFace(f);	eb.setLeftFace(f);

		em.linkOppositeEdge(emo);
		ea.linkOppositeEdge(eao);
		eb.linkOppositeEdge(ebo);
		//	colors
//		f.setType(splitcolor);
//		f2.setType(splitcolor);
		fTypes.put(f, splitcolor);
		fTypes.put(f2, splitcolor);
		
		vTypes.put(v, Type.newOne);
		eTypes.put(e0,Type.instable);
		eTypes.put(em,Type.newEdge);
		eTypes.put(emo,Type.newEdge);

		// remember lost side 
		lostSide.add(e0);
		return em;// the edge which splits
	}
	/** Schliest die bei der Unterteilung entstande Luecke. 
	 *  Diese Luecke muss am Rand liegen.
	 * @param e urspruengliche Kante.
	 */
	private void closeBoundingGap(E e){
		E eo=e.getOppositeEdge();

		E e1=e.getNextEdge();
		E e2=e1.getNextEdge();

		E eX1=eo.getPreviousEdge();
		E eX2=eo.getNextEdge();

		eX1.linkNextEdge(e1);
		e2.linkNextEdge(eX2);

		e1.setLeftFace(null);
		e2.setLeftFace(null);

		eTypes.put(e,Type.illegal); eTypes.put(eo,Type.illegal);// means they are handled
	}
	/** Schliest die bei der Unterteilung entstande Luecke. 
	 *  Diese Luecke darf am Rand liegen.
	 * @param e Urspruengliche Kante
	 */
	private void closeGap(E e){ 
		if (eTypes.get(e)==Type.illegal)
			return;// allready handled
		if (eTypes.get(e.getOppositeEdge())!=Type.instable){
			closeBoundingGap(e);
			return;
		}
		E eo=e.getOppositeEdge();
		E e1=e.getNextEdge();
		E e2=e1.getNextEdge();
		E eo1=eo.getNextEdge();
		E eo2=eo1.getNextEdge();

		E e1o=e1.getOppositeEdge();
		E e2o=e2.getOppositeEdge();
		E eo1o=eo1.getOppositeEdge();
		E eo2o=eo2.getOppositeEdge();

		e1o.linkOppositeEdge(eo2o);
		e2o.linkOppositeEdge(eo1o);

		eTypes.put(e1,Type.illegal);		eTypes.put(e2,Type.illegal);
		eTypes.put(eo,Type.illegal);		eTypes.put(eo2,Type.illegal);
		eTypes.put(e,Type.illegal);			eTypes.put(eo,Type.illegal);// means they are handled

	}
	/** Edge-Remove. Entfernt die Kante. Dabei werden die beiden angrenzenden Vertice
	 *  verschmolzen. Die neuen Koordinaten werden mittels Spline interpoliert.  
	 * @param e zu entfernende Kante
	 */
	private void removeShortEdgeWithSpline(E e){
		E eo=e.getOppositeEdge();
		F f=e.getLeftFace();	F fo=eo.getLeftFace();
		E e1=e.getNextEdge();	
		E e2=e1.getNextEdge();
		E eo1=eo.getNextEdge();
		E eo2=eo1.getNextEdge();
		
		E e1o=e1.getOppositeEdge();
		E e2o=e2.getOppositeEdge();
		E eo1o=eo1.getOppositeEdge();
		E eo2o=eo2.getOppositeEdge();
		V v=eo.getTargetVertex();
		V vx=e.getTargetVertex();
						
		Interpolator spliner= new SplineInterpolator();
		spliner.interpolate(e, v);
		
		e1o.linkOppositeEdge(e2o);		
		eo1o.linkOppositeEdge(eo2o);

		// get luggage into a stable state
		V vx1=heds.addNewVertex();
		V vx2=heds.addNewVertex();
		e1.linkOppositeEdge(eo2);
		e2.linkOppositeEdge(eo1);
		e.setTargetVertex(vx);
		eo.setTargetVertex(vx2);
		e1.setTargetVertex(vx1);
		e2.setTargetVertex(vx2);
		eo1.setTargetVertex(vx1);
		eo2.setTargetVertex(vx);

////	 targetVertices
		assignVertex(e1o, v);
		// remove luggage &colors:
		vTypes.put(v, Type.newOne);
		eTypes.put(e2o,Type.newEdge);		eTypes.put(e1o,Type.newEdge);
		eTypes.put(eo2o,Type.newEdge);		eTypes.put(eo1o,Type.newEdge);
		
		eTypes.put(e,Type.illegal);		eTypes.put(e1,Type.illegal);	eTypes.put(e2,Type.illegal);//(dont try to remove again)
		eTypes.put(eo,Type.illegal);	eTypes.put(eo1,Type.illegal);	eTypes.put(eo2,Type.illegal);//(dont try to remove again)
		fTypes.put(f, Type.illegal);	fTypes.put(fo, Type.illegal);
		vTypes.put(vx, Type.illegal); vTypes.put(vx1, Type.illegal); vTypes.put(vx2, Type.illegal); 
	}
	/** Markiert die Kanten die zu lang sind. */
	private void markLongEdges(){
		double longestSq=-1;
		longCount=0;
		for (E e:heds.getEdges()){
			if(e.getLengthSquared()>longestSq)longestSq=e.getLengthSquared();
			if(e.getLengthSquared()>toleranceLong*toleranceLong) {
				if(eTypes.get(e)!=Type.ToBad){
					eTypes.put(e,Type.ToBad);
					badEdge.add(e);
				}
				longCount++;
			}
		}
		longest= Math.sqrt(longestSq);
	}
	/** Markiert Kanten die zu kurz sind.  */
	private void markShortEdges(){
		double shortestSq=-1;
		shortCount=0;
		shortestSq = heds.getEdge(0).getLengthSquared();
		for (E e:heds.getEdges()){
			if(e.getLengthSquared()<shortestSq) 
				shortestSq=e.getLengthSquared();
			if(e.getLengthSquared()<toleranceShort*toleranceShort) {
				shortCount++;
				if(eTypes.get(e)!=Type.ToShort){
					eTypes.put(e,Type.ToShort);
					badEdge.add(e);
				}
			}
		}
		shortest=Math.sqrt(shortestSq);
	}
	/** Markiert Kanten deren Facetten auf dieser Kante eine zu kleine Hoehe haben. */
	private void markFlatEdges(){
		flatCount=0;
		flat=Helper.altitudeSq(heds.getEdge(0));
		for (E e:heds.getEdges()){
			F f=e.getLeftFace();
			if(f!=null){
				double val=Helper.altitudeSq(e);
				if(val<flat)flat=val;
				if(val<toleranceShort*toleranceShort){
					flatCount++;
					eTypes.put(e,Type.ToBad);
					if(eTypes.get(e.getOppositeEdge())!=Type.ToBad){
						eTypes.put(e.getOppositeEdge(),Type.ToBad);
						badEdge.add(e.getOppositeEdge());
					}
				}
			}
		}
	}
	/** Markiert Kanten deren Kruemmung zu stark ist.
	 * Gemessen wird dies an der Stellung der angrenzenden.
	 * VertexNormalen.  
	 */
	private void markHighCurvedEdges(){
		curvedCount=0;
		curved=0;
		for (E e:heds.getEdges()){
			V p=e.getStartVertex();
			V q=e.getTargetVertex();
			double [] u=p.normal;
			double [] v=q.normal;
			double[] vec=Calculator.sub(p.position,q.position);
			double len=Calculator.len(vec);
			double a=Math.acos(Calculator.scalarProd(u, vec)/len)-Math.PI/2;
			double b=Math.acos(Calculator.scalarProd(v, vec)/len)-Math.PI/2;
			double curv=(Math.abs(a-b)+Math.abs(a+b))/len;
			curv=curv*len*len;
			if(curv>curved)curved=curv;
			if(curv>toleranceCurved) {
				curvedCount++;
				if(eTypes.get(e)!=Type.ToBad){
					eTypes.put(e,Type.ToBad);
					badEdge.add(e);
				}
				if(eTypes.get(e.getOppositeEdge())!=Type.ToBad){
					eTypes.put(e.getOppositeEdge(),Type.ToBad);
					badEdge.add(e.getOppositeEdge());
				}
			}
		}
	}
	/** Testet ob der Flip eine Tasche erzeugen wuerde.
	 * @param ed Die zu testende Kante.
	 * @return true, falls eine Tache erzeugt wird. 
	 */
	private boolean flipCreatesOverlap(E ed){
		double[] d=ed.getTargetVertex().position;
		double[] b=ed.getNextEdge().getTargetVertex().position;
		double[] c=ed.getStartVertex().position;
		double[] a=ed.getOppositeEdge().getPreviousEdge().getStartVertex().position;
		return Calculator.flipCreatesOverlap(a, b, c, d);
	}  
	/** Gibt an ob ein Flip der Kante diese verkuerzt.
	 * @param ed Die zu testende Kante.
	 * @return true, falls der Flip sich lohnt.
	 */
	private boolean flipTrueLengthTest(E ed){ 
		V v1=ed.getTargetVertex();
		V v2=ed.getNextEdge().getTargetVertex();
		V v3=ed.getStartVertex();
		V v4=ed.getOppositeEdge().getPreviousEdge().getStartVertex();
		double now=Calculator.distSq(v1.position,v3.position);
		double alt=Calculator.distSq(v2.position,v4.position);		
		// flippen wenn >0:
		if(alt==0) return false;
		return now/alt>1;
	}
	/** Gibt an um wieviel ein Flip der Kante innerhalb der Flaeche diese verkuerzt.
	 * @param ed zu testende Kante.
	 * @return das Quadrat des Verhaeltnisses in dem die alte Kantenlaenge zur alten steht.
	 * falls die neue Kantenlaenge 0 ist wird 1 zurueckgegeben was bedeutet das der Flip sich nicht lohnt.
	 */
	private double flipIntrinsicLenValue(E ed){
		V v1=ed.getTargetVertex();
		V v2=ed.getNextEdge().getTargetVertex();
		V v3=ed.getStartVertex();
		V v4=ed.getOppositeEdge().getPreviousEdge().getStartVertex();

		double aa=Calculator.distSq(v1.position, v2.position);
		double bb=Calculator.distSq(v2.position,v3.position);
		double cc=Calculator.distSq(v1.position,v3.position);
		double ee=Calculator.distSq(v1.position, v4.position);
		double ff=Calculator.distSq(v4.position, v3.position);

		// intrinsische Distanz(Quadriert)

		double p1p1=(aa+cc-bb)*(aa+cc-bb)/(4*cc);
		double p2p2=(ee+cc-ff)*(ee+cc-ff)/(4*cc);
		double h1h1=aa-p1p1;
		double h2h2=ee-p2p2;
		double dd=h1h1+h2h2+2*Math.sqrt(h1h1*h2h2)+(aa-bb-ee+ff)*(aa-bb-ee+ff)/(4*cc);
		// flippen wenn >1:
		if(dd==0)
			return 1;// keine Verbesserung! die Kante wuerde sonst degeneriert.
		return cc/dd;
	}
	/**Kopiert die Datenstruktur ohne die zur entfernung markierten Komponenten.
	 * Das schneller als das loeschen der einzelnen Komponente in einer Schleife.
	 * Benoetigt linearen Aufwand bzgl der Kantenmenge. 
	 * 
	 * Brauche eine RefferenzListe fuer alte zu neue Referenzen
	 * Also:
	 * 1) Erzeuge neue HEDS und leere ForwardListe(Node->Node).
	 * 2) Fuer jeden alten guten Node baue neuen in HEDS, 
	 * 	verweise in ForwardList auf ihn
	 * 	und kopiere seine Daten(ohne Refferenzen).
	 * 3) Nehme alten guten Node:
	 * 	Nehme dazugehoerigen neuen Node(ForwardList):
	 *      Biege alle Zeiger des neuen auf 
	 *      die weiterverzeigerten Ziele des alten.
	 * 4) Loesche die alte Liste.
	 */
		@SuppressWarnings("unchecked")
		private void removeMarkedNodes(){
			
		
		Object[]  forwardVert= new Object[heds.numVertices()];
		Object[]  forwardEdg= new Object[heds.numEdges()];
		Object[]  forwardFac= new Object[heds.numFaces()];
		for(V v:heds.getVertices()){
			if(vTypes.get(v)!=Type.illegal){
				V vNew=newHeds.addNewVertex();
				copy(v, vNew);
				forwardVert[v.getIndex()]=vNew;
			}
		}
		for(E e:heds.getEdges()){
			if(eTypes.get(e)!=Type.illegal){
				E eNew=newHeds.addNewEdge();
				copy(e, eNew);
				forwardEdg[e.getIndex()]=eNew;
			}
		}
		for(F f:heds.getFaces()){
			if(fTypes.get(f)!=Type.illegal){
				F fNew=newHeds.addNewFace();
				copy(f, fNew);
				forwardFac[f.getIndex()]=fNew;
			}
		}
		for(E e:heds.getEdges()){
			if(eTypes.get(e)!=Type.illegal){
				E eNew=(E)forwardEdg[e.getIndex()];
				if(e.getLeftFace()!=null)
					eNew.setLeftFace((F)forwardFac[e.getLeftFace().getIndex()]);
				if(e.getTargetVertex()!=null)
					eNew.setTargetVertex((V)forwardVert[e.getTargetVertex().getIndex()]);
				//else System.out.println("Subdivider.removeMarkedNodes(NoVertex)");
				eNew.linkNextEdge((E)forwardEdg[e.getNextEdge().getIndex()]);
				if(e.getOppositeEdge()!=null)
					eNew.linkOppositeEdge((E)forwardEdg[e.getOppositeEdge().getIndex()]);
			}
		}
		heds=newHeds;
	}
	
//	private void removeMarkedNodes() {
//		
//		List<V> vToRem = new ArrayList<V>();
//		List<E> eToRem = new ArrayList<E>();
//		List<F> fToRem = new ArrayList<F>();
//		
//		for(V v : heds.getVertices()) {
//			if(vTypes.get(v) == Type.illegal)
//				vToRem.add(v);
//		}
//		
//		for(E e: heds.getEdges()) {
//			if(eTypes.get(e) == Type.illegal)
//				eToRem.add(e);
//		}
//		
//		for(F f : heds.getFaces()) {
//			if(fTypes.get(f) == Type.illegal)
//				fToRem.add(f);
//		}
//		
//		for(V v : vToRem)
//			heds.removeVertex(v);
//		
//		for(E e : eToRem)
//			heds.removeEdge(e);
//		
//		for(F f : fToRem)
//			heds.removeFace(f);
//	}
	//----------------- publics ----------------------
	/** Entsorgt ueberfluesige Daten.
	 * Setzt alle Vertex-, Facetten- und Kanten-Typen zurueck auf "optimal". */ 
	private void thinkPositive(){
		cleanUp();
		for(V v:heds.getVertices())
			vTypes.put(v, Type.oldOne);
		for(E e:heds.getEdges())
			eTypes.put(e,Type.good);
		for(F f:heds.getFaces()){
			fTypes.put(f, Type.fine);
		}
	}
	/** Markiert alle Facetten und Kanten als "schlecht".
	 * Vermerkt sie in den entsprechenden Listen.
	 * Wird fuer nicht adaptive Verfahren benutzt. 
	 */
	private void thinkAndDoNegative(){
		cleanUp();
		for(E e:heds.getEdges()){		
			eTypes.put(e,Type.ToBad);
			badEdge.add(e);
		}
		for(F f:heds.getFaces()){
			fTypes.put(f, Type.node);
			badFaces.add(f);
		}
	}
/** Typisiert die Komponenten der Flaeche, 
 * anhand der gesetzten Kriterien fuer die Unterteilung/Entfeinerung.   
 * @return Anzahl der fuer die Kriterien ungenuegenden Kanten.
 */
	public int MarkAndAddEdgesAndFaces(){
		// Normalen berechnen
		if(calcNormals){// fuer spline und curvature
			makeNormals(); // gewichtet:true/false
		}
		if(lengthKrit || shortKrit)
			for (E e:heds.getEdges()){
				double [] start=e.getStartVertex().position;
				double [] targ= e.getTargetVertex().position;
				e.setLengthSquared(Calculator.distSq(start, targ));
			}				
		return _MarkAndAddEdgesAndFaces();
	}
	/** Markiert die Kanten fuer die Typisierung der Facetten nach den 
	 * gesetzten Kriterien. 
	 * @return true, wenn mindestens eine Kante den Anforderungen wiederspricht.
	 */
	private int _MarkAndAddEdgesAndFaces(){
		int found=0;
		// clear
		thinkPositive();
		// mark and add long
		if(lengthKrit){
			markLongEdges();
		}
		// mark and add long
		if(shortKrit){
			markShortEdges();
		}
		// mark and add curved (not twice)
		if(edgeCurvatureKrit){
			markHighCurvedEdges();
		}
		if(flatFacesKrit){
			markFlatEdges();
		}
		// mark and add Faces | mark add all (case adaptive)
		found=gatherInfos();
		return found;
	}
	/** Liefert eine Randkante die auf diesen Vertex zeigt.
	 * Falls es keine solche Kante gibt wird <code>null</code>zurueckgegeben. 
	 * @param v
	 * @return Randkante
	 */
	private E getBoundaryEdge(V v){
		E e=v.getIncomingEdge();
		E start=e;
		if(e.getLeftFace()==null)
			return e;
		e=e.getNextEdge().getOppositeEdge();
		while(e!=start){
			if(e.getLeftFace()==null)
				return e;
			e=e.getNextEdge().getOppositeEdge();
		}
		return null;
	}
	/** Berechnet die neuen Koordinaten der alten Vertice fuer die 
	 * sqrt(3) Subdivision. Setzt diese Koordinaten aber noch nicht als position.
	 */
	private void calcNewCoordsForOldVerts(){
		// vor der kombinatorischen aenderung anwenden
		// erst mal die valenz und die summe der Nachbarn speichern
		// gespeichert wird unter dem aenderungsanfaelligen Vertexindex   
		List<V> verts=heds.getVertices();
		int vc=verts.size();
		int[] valence=new int[vc];
		boolean[] boundary=new boolean[vc];
		for (int i = 0; i < vc; i++) {
			valence[i]=0;
			boundary[i]=false;
		}
		for(V v: heds.getVertices()){
//			v.newCoords=new double[v.position.length];
			newCoords.put(v, new double[v.position.length]);
		}
		for(E e:heds.getEdges()){
			V t=e.getTargetVertex();
			if(e.getLeftFace()==null)
				boundary[t.getIndex()]=true;
			else{
				V s=e.getStartVertex();
//				t.newCoords=Calculator.add(t.newCoords, s.position);
				newCoords.put(t, Calculator.add(newCoords.get(t), s.position));
				valence[t.getIndex()]++;
			}
		}
		for(V v: heds.getVertices()){
			if(!boundary[v.getIndex()]){
				double val=valence[v.getIndex()];
				double alpha=(4.0-2.0*Math.cos(2.0*Math.PI/val))/9;
//				v.newCoords=Calculator.linearCombination(
//						(1.0-alpha), v.position, 
//						alpha/val,v.newCoords );
				
				newCoords.put(v, 
						Calculator.linearCombination(
								(1.0-alpha), v.position, 
								alpha/val, newCoords.get(v))
						);
			}
			else {
				E e= getBoundaryEdge(v);
				double[] v0=e.getStartVertex().position;
				double[] v1=e.getTargetVertex().position;
				double[] v2=getNextBorder(e).getTargetVertex().position;
				double[] result= new double[v0.length];
				result=Calculator.linearCombination( 1, result, 4./27, v0);
				result=Calculator.linearCombination( 1, result, 19./27, v1);
				result=Calculator.linearCombination( 1, result, 4./27, v2);
//				v.newCoords=result;
				newCoords.put(v, result);
			}
		}
		valence=null;
	}
	/** Setzt die vorberechneten neuen Koordinaten der alten Vertice.
	 * Wird fuer die sqrt(3) Subdivision gebraucht.  
	 */
	private void setNewCoordsForOldVerts(){
		//nach der kombinatorischen aenderung anwednen
		for(V v: heds.getVertices()){
			if(vTypes.get(v)==Type.oldOne)
//				v.position=v.newCoords;
//				v.newCoords=null;
				
				v.position = newCoords.get(v);
		}
	}
	/** Liefert zu einer Randkante die nachfolgende Randkante.
	 * @param e Die Randkante.
	 * @return Die Nachfolge-Randkante.
	 */
	private E getNextBorder(E e){
		if (e.getLeftFace()==null)// Boundary is left face
			return e.getNextEdge();
		else// boundary is right face
			return e.getOppositeEdge().getPreviousEdge().getOppositeEdge();
	} 
	/** Ist nur fuer die sqrt(3)-subdivision wichtig. 
	 * Testet ob die Facette, die an dieser Seite liegt, eine einzige Randkante hat.
	 * In diesem Fall mueste man dann die Randregel beachten.
	 * Fall es so eine Kante gibt, wird sie zurueckgegeben. 
	 * Sonst wird <code>null</code> zurueckgegeben.
	 * @param e Kante an der zu pruefenden Facette.
	 * @return Die einzige Randkante der Facette (falls existent).
	 */
	private E sqrtSingleBorder(E e){
		E boundEdge=null;
		int boundCount=0;
		for(int i=0;i<3;i++){
			if(e.getOppositeEdge().getLeftFace()==null){
				boundCount++;
				boundEdge=e;
			}
			e=e.getNextEdge();
		}
		if(boundCount!=1)
			return null;
		else return boundEdge; 
	}
	/** Versucht Facette eine Randkante fuer den Sqrt(3)-Algorithmus zweifach zu unterteilen.
	 * Tut dies nur wenn das Dreieck flach genug ist.
	 * @param e Die Randkante.
	 * @return true, wenn die Unterteilung erfolgt ist.
	 */
	private boolean sqrtFlatBorder(E e){
		double[] v0=e.getStartVertex().position;
		double[] v1=e.getTargetVertex().position;
		double[] v2=e.getNextEdge().getTargetVertex().position;
		double aa=Calculator.distSq(v0, v1);
		double bb=Calculator.distSq(v1, v2);
		double cc=Calculator.distSq(v2, v0);
		double cosCos=(bb+cc-aa)*(bb+cc-aa)/(4*bb*cc);
		if(bb+cc-aa>0 && cosCos>1/4)//kein flaches Dreieck (nicht flach ueber der Boundary edge)
			return false;
		return true;
	}
	/** Unterteilt eine Facette wenn sie am Rand liegt,
	 *  und spezielle Unterteilung benoetigt.   
	 *  Ist das nicht der Fall, wird nichts getan.
	 * @param f vermeintliche Rand-Facette 
	 * @return true, falls eine gesonderte Unterteilung erfolgt ist.
	 */
	private boolean sqrtBorderSplit(F f){
		E bound=sqrtSingleBorder(f.getBoundaryEdge());
		if(bound== null)
			return false;
		if(!sqrtFlatBorder(bound))
			return false;
		// unterteile die Boundary Edge. kein Trisplit!!
		E b=bound.getOppositeEdge();
		E e0=bound;
		E e1=e0.getNextEdge();
		E e2=e1.getNextEdge();
		E bp=b.getPreviousEdge();
		E bn=b.getNextEdge();
		V v0=e2.getTargetVertex();
		V v1=e0.getTargetVertex();
		V v2=e1.getTargetVertex();
		
		E ex0=heds.addNewEdge();
		E ex2=heds.addNewEdge();
		E ex2o=heds.addNewEdge();
		E ex0o=heds.addNewEdge();
		E ey0=heds.addNewEdge();
		E ey1=heds.addNewEdge();
		E ey1o=heds.addNewEdge();
		E ey0o=heds.addNewEdge();
		F fx=heds.addNewFace();
		F fy=heds.addNewFace();
		V vx=heds.addNewVertex();
		V vy=heds.addNewVertex();

		// new positions:
		V vp=null;
		if(eTypes.get(b.getPreviousEdge())==Type.ToBad)
			vp=b.getPreviousEdge().getStartVertex();
		else//bereits unterteilt:
			vp=b.getPreviousEdge().getPreviousEdge().getPreviousEdge().getStartVertex();
		V vn=null;
		if(eTypes.get(b.getNextEdge())==Type.ToBad)
			vn=b.getNextEdge().getTargetVertex();
		else// bereits unterteilt:
			vn=b.getNextEdge().getNextEdge().getNextEdge().getTargetVertex();
		double[] posx= new double[v1.position.length];
		double[] posy= new double[v1.position.length];
		posx=Calculator.linearCombination( 1./27,vp.position ,16./27, v1.position);
		posx=Calculator.linearCombination( 1., posx ,10./27, v0.position);
		posy=Calculator.linearCombination( 1./27,vn.position ,16./27, v0.position);
		posy=Calculator.linearCombination( 1., posy ,10./27, v1.position);
		vx.position=posx;
		vy.position=posy;
				
		e1.setLeftFace(fx);		ex0.setLeftFace(fx);		ex2.setLeftFace(fx);
		e0.setLeftFace(f);		ex2o.setLeftFace(f);		ey1o.setLeftFace(f);
		ey0.setLeftFace(fy);		ey1.setLeftFace(fy);		e2.setLeftFace(fy);

		ex0.linkOppositeEdge(ex0o);		ey0.linkOppositeEdge(ey0o);
		ex2.linkOppositeEdge(ex2o);		ey1.linkOppositeEdge(ey1o);
		
		bp.linkNextEdge(ex0o);		ex0o.linkNextEdge(b);		
		b.linkNextEdge(ey0o);		ey0o.linkNextEdge(bn);
		ex0.linkNextEdge(e1);		e1.linkNextEdge(ex2);		ex2.linkNextEdge(ex0);
		e0.linkNextEdge(ex2o);		ex2o.linkNextEdge(ey1o);		ey1o.linkNextEdge(e0);
		ey0.linkNextEdge(ey1);		ey1.linkNextEdge(e2);		e2.linkNextEdge(ey0);
		
		ex0o.setTargetVertex(vx);		b.setTargetVertex(vy);		ey0o.setTargetVertex(v0);
		ex2.setTargetVertex(vx);		ex0.setTargetVertex(v1);
		ex2o.setTargetVertex(v2);		ey1o.setTargetVertex(vy);		e0.setTargetVertex(vx);
		ey0.setTargetVertex(vy);		ey1.setTargetVertex(v2);
		
		return true;
	}
	/** Unterteilt eine Facette mit einem gleichmaesigen 1:3 Split. 
	 * @param f0 Facette
	 */
	private void triSplit(F f0){
		E e0=f0.getBoundaryEdge();
		E e1=e0.getNextEdge();
		E e2=e1.getNextEdge();
		
		V v0=e1.getTargetVertex();
		V v1=e2.getTargetVertex();
		V v2=e0.getTargetVertex();
		
		E e01=heds.addNewEdge();
		E e02=heds.addNewEdge();
		E e10=heds.addNewEdge();
		E e12=heds.addNewEdge();
		E e20=heds.addNewEdge();
		E e21=heds.addNewEdge();
		
		V v=heds.addNewVertex();
		
		F f1=heds.addNewFace();
		F f2=heds.addNewFace();
		
		
		e0.linkNextEdge(e20);		e20.linkNextEdge(e10);		e10.linkNextEdge(e0);
		e1.linkNextEdge(e01);		e01.linkNextEdge(e21);		e21.linkNextEdge(e1);
		e2.linkNextEdge(e12);		e12.linkNextEdge(e02);		e02.linkNextEdge(e2);
		
		e0.setLeftFace(f0);		e20.setLeftFace(f0);		e10.setLeftFace(f0);
		e1.setLeftFace(f1);		e01.setLeftFace(f1);		e21.setLeftFace(f1);
		e2.setLeftFace(f2);		e12.setLeftFace(f2);		e02.setLeftFace(f2);
		
		e20.linkOppositeEdge(e21);		e21.linkOppositeEdge(e20);
		e10.linkOppositeEdge(e12);		e12.linkOppositeEdge(e10);
		e01.linkOppositeEdge(e02);		e02.linkOppositeEdge(e01);
		 
		e20.setTargetVertex(v);		e10.setTargetVertex(v1);
		e01.setTargetVertex(v);		e21.setTargetVertex(v2);
		e12.setTargetVertex(v);		e02.setTargetVertex(v0);
		
		eTypes.put(e20,Type.newEdge);	eTypes.put(e10,Type.newEdge);
		eTypes.put(e01,Type.newEdge);	eTypes.put(e21,Type.newEdge);
		eTypes.put(e12,Type.newEdge);	eTypes.put(e02,Type.newEdge);	
		
		v.position=Calculator.linearCombination( 1./3, v1.position, 1./3, v2.position);
		v.position=Calculator.linearCombination( 1./3, v0.position, 1., v.position);
	}
	/** Flipt alle alten Kanten fuer den sqrt(3)-Algorithmus. 
	 */
	private void flipAllOldForSqrt(){
		for (E e:badEdge) 
			if(e.isPositive()){
				if (e.getLeftFace()!=null && e.getOppositeEdge().getLeftFace()!=null)	flipEdge(e);
			}
	} 
//	/** Objekt zum Vergleichen fuer das sortieren der Kanten nach Laenge.  
//	 */
//	private Compare compLength= new Compare(){
//		@SuppressWarnings("unchecked")
//		public int doCompare(Object e1, Object e2) {
//			E ed1=(E)e1;
//			E ed2=(E)e2;
//			if(ed1.getLengthSquared()<ed2.getLengthSquared())return-1;
//			if(ed1.getLengthSquared()>ed2.getLengthSquared())return 1;
//			if(ed1.getLengthSquared()==ed2.getLengthSquared()){
//				if(ed1.hashCode()<ed2.hashCode())	return -1;
//				if(ed1.hashCode()>ed2.hashCode())	return 1;
//			}
//			return 0;
//		}
//	};
	/** Sortiert die Kanten nach ihrer Laenge. 
	 * @param up Gibt an, ob in aufsteigender(true) 
	 * oder absteigender(false) Reihenfolge sortiert werden soll. 
	 */
	
	private class EdgeLengthComparator implements Comparator<E> {

		@Override
		public int compare(E e1, E e2) {
			if(e1.getLengthSquared() < e2.getLengthSquared())
				return -1;
			else if (e1.getLengthSquared() == e2.getLengthSquared())
				return 0;
			else return 1;
		}

	}
	
	private void sortEdgeLength(boolean up){
		
		Collections.sort(badEdge, new EdgeLengthComparator());
		// FIXME bad code...
//		E[] eds= new E[badEdge.size()];
//		int i=0;
//		for(E e:badEdge){	eds[i]=e;			i++;	}
//		Sort.quicksort(eds, compLength);
//		badEdge=new LinkedList<E>();
//		for(E e:eds)		
//			if(up)badEdge.add(e);
//			else badEdge.add(0,e);
//		eds=null;
	}
	/** Entfernt zu kurze Kanten.
	 * Benutzt zur Einschaetzung die gesetzte Toleranz.
	 * Ist nicht unbedingt in der Lage alle zu kurzen Kanten zu entfernen.
	 * Manche Kanten koennen ueberhaubt nicht, andere nur durch wiederholte 
	 * Anwednung der Methode entfernt werden.
	 * @return Zahl der als zu kurz eingeschaetzten Kanten vor der Entfernung.
	 */
	public int removeShortEdges(){
		int badEdgeCount=0;
		// mark Verts as (not moved)
		boolean len=lengthKrit;			boolean sh=shortKrit; boolean fl=flatFacesKrit;	
		boolean ed=edgeCurvatureKrit;		SubdivType t=type;
		lengthKrit=false;			edgeCurvatureKrit=false;
		flatFacesKrit=false;
		shortKrit=true;			type=SubdivType.SIMPLE;
		MarkAndAddEdgesAndFaces();
		lengthKrit=len;		shortKrit=sh;		edgeCurvatureKrit=ed; type=t; flatFacesKrit=fl;
		// sort by length
		badEdgeCount=badEdge.size();
		sortEdgeLength(true);
		// merge if none of the verts was moved(or removed)
		// and interpol the new vert(spline,instantly possible)
		for(E e: badEdge){
			if(    eTypes.get(e)==Type.ToShort &&// insbesondere nicht illegal
					
					eTypes.get(e.getOppositeEdge())==Type.ToShort &&// nur zur sicherheit!
					vTypes.get(e.getTargetVertex())==Type.oldOne &&
					vTypes.get(e.getStartVertex())==Type.oldOne &&
					!Helper.doNotRemove(e,toleranceRemoveAngleSq)
				) removeShortEdgeWithSpline(e);
		}
		removeMarkedNodes();
		return badEdgeCount;
	}	
	/** Subdividiert einen Schritt mit dem SplitAlgorithmus von Stock.
	 * Ist in der Lage berandete Flaechen zu bearbeiten.
	 * Arbeitet adaptiv.
	 * Interolationsfunktion, und Kriterien zur Unterteilung 
	 * werden genutzt wie sie eingestellt sind.
	 * @return Die Anzahl der schlechten Kanten vor der Subdivision.
	 */
	public int subdivideSplit(){
		int badEdgeCount=0;
		boolean len=lengthKrit;			boolean sh=shortKrit;  	
		boolean ed=edgeCurvatureKrit;		SubdivType t=type;
		lengthKrit=true;			edgeCurvatureKrit=false;
		shortKrit=false;			type=SubdivType.SIMPLE;
		badEdgeCount=MarkAndAddEdgesAndFaces();
		lengthKrit=len;		shortKrit=sh;		edgeCurvatureKrit=ed; type=t;
		// sort by length
		sortEdgeLength(false);
		// precalc new Vertices
		for (E e:badEdge ) {
			setNewVertex(e);
		}
		
		// merge if none of the verts was moved(or removed)
		// and interpol the new vert(spline,instantly possible)
		for(E e: badEdge){
			if(eTypes.get(e)==Type.ToBad){
				E eo=e.getOppositeEdge();				
				if(e.getLeftFace()!=null)
					split(e, Type.newOne, takeNewVertex(e));
				if(eo.getLeftFace()!=null)
					split(eo, Type.newOne, takeNewVertex(eo));
			}
		}
		for(E e :lostSide)//Ok
			closeGap(e);
		removeMarkedNodes();
		return badEdgeCount;
	}
	/** Subdividiert einen Schritt mit dem sqrt(3)-Algorithmus.
	 * Ist in der Lage berandete Flaechen zu bearbeiten.
	 * Der Randkanten werden allerdings nicht jeden zweiten 
	 * Durchlauf unterteilt, sondern danach ob sie zu flach sind.
	 * Ist nicht adaptiv. 
	 * Unterteilt die Flaeche falls eine Kante den
	 * Eingestellten Kriterien wiederspricht. 
	 * @return Gibt zurueck, wie viele Kanten den Kriterien wiedersprachen.
	 */
	public int subdivideSqrt(){
		int badEdgeCount=0;
		// Subdivgraph einfaerben
		badEdgeCount=MarkAndAddEdgesAndFaces();
		// now subdiv on sqrt()
		if(badEdgeCount>0){
			calcNewCoordsForOldVerts();
			for (F f: badFaces) {
				if(!sqrtBorderSplit(f))
					triSplit(f);
			}
			flipAllOldForSqrt();
			setNewCoordsForOldVerts();
		}
		return badEdgeCount;
	}
	/** Subdividiert einen Schritt mit dem von mir entwickelten Algorithmus.
	 * Ist in der Lage berandete Flaechen zu bearbeiten.
	 * Arbeitet adaptiv.
	 * Interpoliert je nach eingestelltem Interpolationsverfahren. 
	 * Subdividiert je nach eingesteltem Kriteriun. 
	 * @return Gibt zurueck, wie viele Kanten den Kriterien wiedersprachen.
	 */
	public int subdivide(){
		int badEdgeCount=0;
		shortKrit=false;
		// Werte der gegebenen Punkte berechnen(inklusive vorberechnung) 
		badEdgeCount=MarkAndAddEdgesAndFaces();
		// potentiell neue Vertices erstellen und ggf. deren images berechnen :
		for (E e:badEdge ) {
			setNewVertex(e);
		}
		// start refining :
		for(F f:badFaces){// Ok
			if (fTypes.get(f)==Type.node)	faceSplit(f);
			if (fTypes.get(f)==Type.end)	split(f,Type.splited);
		}
		for(F f:badFaces)//Ok
			if (fTypes.get(f)==Type.way)	way(f);
		// close gaps :
		for(E e :lostSide)//Ok
			closeGap(e);
		removeMarkedNodes();
		return badEdgeCount;
	}
	/** wiederholt den von mir entwickelten Algorithmus 
	 * die angegebene Anzahl von Schritten.
	 * Bricht vorzeitig ab, falls keine Kanten den Kriterien wiedersprechen.
	 * @param max maximale Anzahl von auszufuehrenden Schritten
	 * @return Anzahl der Kanten die den Kriterien wiedersprachen 
	 * vor der Ausfuehrung des letzten Schrittes. Bei vorzeitigem Abbruch 0.
	 */
	public int subdivideRepeat(int max){
		if(max<=0) return -1;// Abbruchbedingung
		int badEdgeCount=subdivide();
		if(badEdgeCount>0){
			int n=subdivideRepeat( max-1);
			if(n==-1)	return badEdgeCount;
			else  badEdgeCount=n;
		}
		return badEdgeCount;
	}
	/** Wiederholt das Entfernen von kurzen Kanten 
	 * die angegebene Anzahl von Schritten.
	 * Bricht vorzeitig ab, falls die Anzahl der  
	 * Kanten die den Kriterien wiedersprechen, sich nicht mehr aendert.
	 * @param max Maximale Anzahl von auszufuehrenden Schritten.
	 * @return Anzahl der Kanten die den Kriterien wiedersprachen 
	 * vor der Ausfuehrung des letzten Schrittes. 
	 */
	public int removeShortEdgesRepeat(int max){
		int n=0;
		int badEdgeCount=removeShortEdges();
		int finish=badEdgeCount+1;
		while(n<max && badEdgeCount<finish){
			finish=badEdgeCount;
			badEdgeCount=removeShortEdges();
			n++;
		} 
		return badEdgeCount;
	}
	/** Wiederholt den Edge-Split-Algorithmus von Stock 
	 * die angegebene Anzahl von Schritten.
	 * Bricht vorzeitig ab, falls keine Kanten den Kriterien wiedersprechen.
	 * @param max Maximale Anzahl von auszufuehrenden Schritten.
	 * @return Anzahl der Kanten die den Kriterien wiedersprachen 
	 * vor der Ausfuehrung des letzten Schrittes. Bei vorzeitigem Abbruch 0.
	 */
	public int subdivideSplitRepeat(int max){
		if(max<=0) return -1;// Abbruchbedingung
		int badEdgeCount=subdivideSplit();
		if(badEdgeCount>0){
			int n=subdivideSplitRepeat( max-1);
			if(n==-1)	return badEdgeCount;
			else  badEdgeCount=n;
		}
		return badEdgeCount;
	}
	/** Wiederholt den Sqrt(3)-Algorithmus 
	 * die angegebene Anzahl von Schritten.
	 * Bricht vorzeitig ab, falls keine Kanten den Kriterien wiedersprechen.
	 * @param max Maximale Anzahl von auszufuehrenden Schritten
	 * @return Anzahl der Kanten die den Kriterien wiedersprachen 
	 * vor der Ausfuehrung des letzten Schrittes. Bei vorzeitigem Abbruch 0.
	 */
	public int subdivideSqrtRepeat(int max){
		if(max<=0) return -1;// Abbruchbedingung
		int badEdgeCount=subdivideSqrt();
		if(badEdgeCount>0){
			int n=subdivideSqrtRepeat( max-1);
			if(n==-1)	return badEdgeCount;
			else  badEdgeCount=n;
		}
		return badEdgeCount;
	}
	/** Loescht die Listen die nur fuer die bearbeitung eines Schrittes
	 * benoetigt wurden. */
	public void cleanUp(){
		lostSide= new LinkedList<E>();
		badFaces= new LinkedList<F>();
		shortEdge= new LinkedList<E>();
		badEdge=new LinkedList<E>();
		System.gc();
	}

//	--------- helper methods ------------------
	/** Findet den Anfang eines Subdivisionsgraph-Weges. das ist die 
	 * erste schlechte Kante des Weges. Falls der Weg ein Ring ist, ist 
	 * es eine beliebige Kante aus dem Weg.
	 * @param startEdge Wird Benoetigt um einen Ring zu erkennen.
	 * @param currentEdge Aktuelle Kante. 
	 * @return Anfang des Weges.
	 */
	private E findFirstWayEdge(E startEdge,E currentEdge){//checked
		E opp=currentEdge.getOppositeEdge();
		if(opp.getLeftFace()==null)
			return currentEdge;// hafe a beginning
		E next=otherBadEdge(opp);
		if (next==startEdge)
			return startEdge;// have a ringway(cycle)
		return findFirstWayEdge(startEdge, next); // recursiv search 
	}
	/** Testet ob auf die Kante noch mindestens 2 Facetten im Weg folgen.
	 * Man beachte: die Wege enden durch die vorangegangenen Unterteilungen
	 * stets mit einer Luecke.
	 * @param e  Anfang des Restweges
	 * @return true, falls es noch mindestens 2 Facetten gibt. 
	 */
	private boolean haveAPair(E e){
		if (e.getLeftFace()==null)
			return false;// have none
		E other=otherBadEdge(e);
		E othOpp=other.getOppositeEdge();
		return (othOpp.getLeftFace()!=null);// have Two ore one?
	}
	/** Sucht die anderen beiden Kanten ab und liefert eine, die  
	 * <code>bad</code> ist.
	 * Eine der beiden anderen Kanten muss <code>bad</code> sein.
	 * @param e Kante   
	 * @return Andere schlechte Kante.
	 */
	private E otherBadEdge(E e){
		e= e.getNextEdge();
		if (eTypes.get(e)==Type.ToBad)
			return e;
		return e.getNextEdge();
	}
	/** Sammelt die Informationen der verschiedenen Kriterien.
	 * Weist den Facetten ihren Typ zu.
	 */
	private int gatherInfos(){
		int found=badEdge.size();
		if (found==0) return 0;
		if(type==SubdivType.NONADAPTIVE){
			thinkAndDoNegative();
			return found;
		}
		for(F f:heds.getFaces()){
			int weight=0;
			E e=f.getBoundaryEdge();
			if (eTypes.get(e)==Type.ToBad|eTypes.get(e)==Type.ToShort) weight++;
			e=e.getNextEdge();
			if (eTypes.get(e)==Type.ToBad|eTypes.get(e)==Type.ToShort) weight++;
			e=e.getNextEdge();
			if (eTypes.get(e)==Type.ToBad|eTypes.get(e)==Type.ToShort) weight++;
			if(weight==0) fTypes.put(f, Type.fine);;
			if(weight==1) fTypes.put(f, Type.end);
			if(weight==2) fTypes.put(f, Type.way);
			if(weight==3) fTypes.put(f, Type.node);
			if(weight>0){
				badFaces.add(f);
			}
		}
		return found;
	}
	/** Kopiert die Daten eines Vertex in einen anderen. */
	private void copy(V from,V to){
		to.color=from.color;
		vTypes.put(to,vTypes.get(from));
		to.normal=from.normal;	
		to.position=from.position;
		to.textCoord=from.textCoord;
	}
	/** Kopiert die Daten einer Halbkante in eine andere. */
	private void copy(E from,E to){
		to.color=from.color;
		eTypes.put(to, eTypes.get(from));
	}
	/** Kopiert die Daten einer Facette in eine andere. */
	private  void copy(F from,F to){
		to.color=from.color;
		fTypes.put(to, fTypes.get(from));
		to.normal=from.normal;
	}
	/** Bewertet, ob die Paerchenbildung eine Kante in der mitte 
	 * schaffen wuerde, welche nicht kurz genug ist.
	 * Ermittelt deshalb den Abstand der neuen Vertice der gegebenen Kanten.
	 * Richtet sich nach der aktuellen Toleranz. 
	 * @param e1 Eine Kante
	 * @param e2 Andere Kante
	 * @return true, falls der Abstand zu gross ist. 
	 * Dann wird vom Paerchen wird abgeraten.   
	 */ 
	private boolean toFar(E e1,E e2){
		double [] p1=checkNewVertexCoords(e1);
		double [] p2=checkNewVertexCoords(e2);
		return (Calculator.distSq(p1, p2)>toleranceLong*toleranceLong);		
	}
	/** Liefert die Daten eines vorberechneten noch nicht eingefuegten Vertex. 
	 */
	private double[] checkNewVertexCoords(E e){ 
		if(!newVert.containsKey(e)&&!newVert.containsKey(e.getOppositeEdge())){
			System.out.println("Subdivider.getNewVertex(no vertex in data)");
			return null;
		}
		if(!newVert.containsKey(e)) return checkNewVertexCoords(e.getOppositeEdge());
		return newVert.get(e).position; 
	}
//	--------- getter setter -----------------------
	/** Liefert die HalfEdgeDaten-Struktur der Flaeche. */
	public HDS getHeds() {
		return newHeds;
	}
	/** Setzt eine neue Flaeche.
	 * @param heds
	 */
	public void setHeds(
			HDS heds) {
		this.heds = heds;
	}
	/** Kann auf false gesetzt werden, wenn die Datenstruktur bereits Normalen hat,
	 *  die nicht neu berechnet werden sollen.
	 *  Faehrt zu Fehlern, falls die Datenstruktur keine Normalen hat.
	 * @param calcNormals
	 */
	public void setCalcNormals(boolean calcNormals) {
		this.calcNormals = calcNormals;
	}
	/** Gibt an, ob Normalen nachberechnet werden.
	 * @return true, falls Nachberechnet wird.
	 */
	public boolean isCalcNormals() {
		return calcNormals;
	}
	/** Setzt die Toleranz, die entscheidet ob eine Kante zu lang ist. 
	 * @param toleranceLong
	 */
	public void setToleranceLong(double toleranceLong) {
		if(toleranceLong>0)this.toleranceLong = toleranceLong;
	}
	/** @return Die Toleranz, die entscheidet ob eine Kante zu lang ist. */
	public double getToleranceLong() {
		return toleranceLong;
	}
	/** Setzt die Toleranz, die entscheidet ob eine Kante 
	 * zu unterschiedliche Normalen an ihren Vertice hat. 
	 * @param toleranceCurved
	 */
	public void setToleranceCurved(double toleranceCurved) {
		if(toleranceCurved>0)this.toleranceCurved = toleranceCurved;
	}
	/** @return Die Toleranz, die entscheidet ob eine Kante 
	 * zu unterschiedliche Normalen an ihren Vertice hat.
	 */
	public double getToleranceCurved() {
		return toleranceCurved;
	}
	/** Setzt die Toleranz, die entscheidet ob eine Kante 
	 * zu kurz ist. 
	 * @param toleranceShort
	 */
	public void setToleranceShort(double toleranceShort) {
		this.toleranceShort = toleranceShort;
	}
	/** @return Die Toleranz, die entscheidet ob eine Kante 
	 * zu kurz ist.
	 */
	public double getToleranceShort() {
		return toleranceShort;
	}
	/** Setzt den Winkel, in Grad, ab dem Entschieden wird,
	 * ob die Umgebung einer Kante nicht eben genug ist
	 *  um die Kante zu entfernen.
	 * @param toleranceRemoveAngle
	 */
	public void setToleranceRemoveAngle(double toleranceRemoveAngle) {
		double cos=Math.cos(toleranceRemoveAngle*Math.PI/180);
		this.toleranceRemoveAngleSq = cos*cos;
	}
	/** @return Der Winkel, in Grad, ab dem Entschieden wird,
	 * ob die Umgebung einer Kante nicht eben genug ist
	 *  um die Kante zu entfernen.
	 */
	public double getToleranceRemoveAngle() {
		return Math.sqrt(toleranceRemoveAngleSq)*180/Math.PI; 
	}
	/** Setzt den Winkel, ab dem zwei Facetten nicht mehr als
	 *  Paerchen zusammengefast werden duerfen.
	 * @param tolerancePairAngle
	 */
	public void setTolerancePairAngle(double tolerancePairAngle) {
		double cos=Math.cos(tolerancePairAngle*Math.PI/180);
		this.tolerancePairAngleSq = cos*cos;
	}
	/**@return Der Winkel, ab dem zwei Facetten nicht mehr als
	 *  Paerchen zusammengefast werden duerfen.
	 */
	public double getTolerancePairAngle() {
		return Math.sqrt(tolerancePairAngleSq)*180/Math.PI;
	}
	/** Setzt, ob Regel 3+, Regel4+, Regel5+ und Regel5++
	 * bei meinem Algorithmus angewendet werden sollen.   
	 * @param extraFlip
	 */
	public void setExtraFlip(boolean extraFlip) {
		this.extraFlip = extraFlip;
	}
	/** @return Gibt an ob Regel 3+, Regel4+, Regel5+ und Regel5++   
	 * bei meinem Algorithmus angewendet werden sollen.
	 */
	public boolean isExtraFlip() {
		return extraFlip;
	}
	/** Gibt an, ob das Kriterium Kantenkruemmung angewendet werden soll.
	 * Dabei wird eine Kante als schlecht angesehen, wenn die Normalen 
	 * ihrer Vertice zu schlecht zueinander passen. Dabei wird sowohl ihre Abweichung
	 * von einander sowie die Abweichung von der orthogonalebene der Kante beruecksichtigt. 
	 * @param edgeCurvatureKrit
	 */
	public void setEdgeCurvatureKrit(boolean edgeCurvatureKrit) {
		this.edgeCurvatureKrit = edgeCurvatureKrit;
	}
	/**Gibt an, ob das Kriterium Kantenkruemmung angewendet werden soll.
	 * Dabei wird eine Kante als schlecht angesehen, wenn die Normalen 
	 * ihrer Vertice zu schlecht zueinander passen. Dabei wird sowohl ihre Abweichung
	 * von einander sowie die Abweichung von der orthogonalebene der Kante beruecksichtigt. 
	 */
	public boolean isEdgeCurvatureKrit() {
		return edgeCurvatureKrit;
	}
	/** Gibt an, ob das Kriterium Kantenlaenge angewendet werden soll.
	 * Dabei wird eine Kante als schlecht angesehen, wenn 
	 * ihre Laenge die zugehoerige Toleranz uebersteigt oder gleicht. 
	 */
	public void setLengthKrit(boolean lengthKrit) {
		this.lengthKrit = lengthKrit;
	}
	/** Gibt an, ob das Kriterium Kantenlaenge angewendet werden soll.
	 * Dabei wird eine Kante als schlecht angesehen, wenn 
	 * ihre Laenge die zugehoerige Toleranz uebersteigt oder gleicht. 
	 */
	public boolean isLengthKrit() {
		return lengthKrit;
	}
	/** Gibt an, ob das Kriterium Kantenkuerze angewendet werden soll.
	 * Dabei wird eine Kante als schlecht angesehen, wenn 
	 * ihre Laenge die zugehoerige Toleranz unterschreitet bzw gleicht.
	 * Wird automatisch bei remove genutzt. 
	 * Kann zum markieren gewaehlt werden. 
	 */
	public void setShortKrit(boolean shortKrit) {
		this.shortKrit = shortKrit;
	}
	/** Gibt an, ob das Kriterium Kantenkuerze angewendet werden soll.
	 * Dabei wird eine Kante als schlecht angesehen, wenn 
	 * ihre Laenge die zugehoerige Toleranz unterschreitet bzw gleicht.
	 * Wird automatisch bei remove genutzt. 
	 * Kann zum markieren gewaehlt werden. 
	 */
	public boolean isShortKrit() {
		return shortKrit;
	}
	/** Gibt an, ob das Kriterium Facetten-Flachheit angewendet werden soll.
	 * Dabei wird eine Kante als schlecht angesehen, wenn die Hoehe 
	 * einer Facette ueber dieser Kante unterhalb oder gleich der 
	 * <code>toleranceShort</code> liegt. Wird zum unterteilen 
	 * schlecht geformter Facetten benutzt.
	 */
	public void setFlatFacesKrit(boolean flatFacesKrit) {
		this.flatFacesKrit = flatFacesKrit;
	}
	/** Gibt an, ob das Kriterium Facetten-Flachheit angewendet werden soll.
	 * Dabei wird eine Kante als schlecht angesehen, wenn die Hoehe 
	 * einer Facette ueber dieser Kante unterhalb oder gleich der 
	 * <code>toleranceShort</code> liegt. Wird zum unterteilen 
	 * schlecht geformter Facetten benutzt.
	 */
	public boolean isFlatFacesKrit() {
		return flatFacesKrit;
	}
	/** Gibt den Typ der Subdivision mit meinem Algorithmus zurueck.
	 *  NONADAPTIVE, SIMPLE oder EXTENDED
	 */
	public SubdivType getType() {
		return type;
	}
	/** Waehlt Typ den Subdivision mit meinem Algorithmus.
	 *  NONADAPTIVE, SIMPLE oder EXTENDED
	 */
	public void setType(SubdivType type) {
		this.type = type;
	}
	/** Setzt einen der vorgefertigten Interpolationsalgorithmen.
	 * Mein Algorithmus sowie der Edge-Split-Algorithmus von Stock,
	 * werden diesen Algorithmus zur Bestimmung der neuen Koordinaten benutzen.
	 * @param funk
	 */
	public void usePredefinedInterpolFunk(InterpolFunk funk){
		if (funk==InterpolFunk.LINEAR) interp= new LinearEdgeSubdivAlg();
		if (funk==InterpolFunk.SPLINE) interp= new SplineInterpolator();
		if (funk==InterpolFunk.BUTTERFLY) interp= new ModButterflyAlg();
	}
	/** Setzt einen Interpolationsalgorithmus.
	 * Mein Algorithmus sowie der Edge-Split-Algorithmus von Stock,
	 * werden diesen Algorithmus zur Bestimmung der neuen Koordinaten benutzen.
	 * Der Algorithmus muss das Interface Interpolator erfuellen. 
	 * Fuer vorgefertigte Algorithmen siehe 
	 * <code>usePredefinedInterpolFunk</code>
	 * @param interp
	 */
	public void setInterpolationFunktion(Interpolator interp) {
		this.interp = interp;
	}
	/**	Liefert den Interpolationsalgorithmus.
	 * Mein Algorithmus sowie der Edge-Split-Algorithmus von Stock,
	 * benutzen diesen Algorithmus zur Bestimmung der neuen Koordinaten.
	 * @return Interpolations-Algorithmus
	 */
	public Interpolator getInterpolationFunktion() {
		return interp;
	}
}



