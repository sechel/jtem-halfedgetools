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

import java.util.ArrayList;
import java.util.LinkedList;

/** Diese Klasse entfernt zu kurze Kanten aus einer Triangulierten Flaeche die sich
 * in einer Instanz der Klasse FaceCycleSubdivider befindet.
 * Diese Flaeche darf berandet, nicht orientierbar und keine Mannigfaltigkeit sein.
 * 
 * @author Bernd Gonska
 */

public class Remover {
	private FaceCycleSubdivider s;
	private double tolSq=0.01;
	private double tolNormalAngleSq=0;// = 90 Grad
	private LinkedList<Integer> shortFaces;
	private short[] worstSide;
	private ArrayList<Integer>[] faceOfVert;
	private boolean[] changed;
	private boolean[] deadFace;
	private boolean[] deadVertex;
	private int deadFaceCount;
	private int deadVertCount;
	private boolean usePseudoNormals; 

	/** Zyklischer Nachfolger einer Zahl von 0 bis 2. */
	private short next(int i){return (short)((i+1)%3); }
	/** Zyklischer Vorgaenger einer Zahl von 0 bis 2. */
	private short prev(int i){return (short)((i+2)%3); }

	/** Klasse zum Entfernen kurzer Kanten einer Flaeche. 
	 * @param fcs Man setzt den FaceCycleSubdivider der die zu bearbeitende Flaeche enthaelt.
	 */
	public Remover(FaceCycleSubdivider fcs) {
		s=fcs;
	}
	/** Entfernt zu kurze Kanten aus dem gesetzten FaceCycleSubdivider. */
	public void removeShortEdges(){
		s.calcNormals(usePseudoNormals);
		calcLen();
		initFaceOfVert();
		markAndPointShortest();
		removeList();
		removeData();
	}
	/** Loescht die zum Entfernen vermerkten Facetten und Vertice
	 *  endgueltig aus dem FaceCycleSubdivider.
	 *  Setzt die Refferenzen um.
	 */
	private void removeData(){
		int[] reffTableVerts= new int[s.coords.size()];
		int[] reffTableFaces= new int[s.faceIndis.size()];
		int seqNum=0;
		ArrayList<double[]> coordsNew=new ArrayList<double[]>(s.coords.size()-deadVertCount);
		if(s.useImages()){
			ArrayList<double[]> coordsImageNew=new ArrayList<double[]>(s.coords.size()-deadVertCount);
			for (int i = 0; i < s.coords.size(); i++) {
				if(deadVertex[i]){
					reffTableVerts[i]=-1;// deleted
				}
				else{
					coordsNew.add(s.coords.get(i));					
					coordsImageNew.add(s.coordsImage.get(i));					
					reffTableVerts[i]=seqNum;
					seqNum++;
				}	
			}			
			s.coordsImage=coordsImageNew;
		}
		else			
			for (int i = 0; i < s.coords.size(); i++) {
				if(deadVertex[i]){
					reffTableVerts[i]=-1;// deleted
				}
				else{
					coordsNew.add(s.coords.get(i));					
					reffTableVerts[i]=seqNum;
					seqNum++;
				}	
			}
		s.coords=coordsNew;
		ArrayList<int[]> faceIndisNew= new ArrayList<int[]>(s.faceIndis.size()-deadFaceCount);
		ArrayList<int[]> faceNextNew= new ArrayList<int[]>(s.faceIndis.size()-deadFaceCount);
		ArrayList<short[]> faceNextSideNew= new ArrayList<short[]>(s.faceIndis.size()-deadFaceCount);
		seqNum=0;
		for (int f = 0; f < reffTableFaces.length; f++) {
			if(deadFace[f]){
				reffTableFaces[f]=-1;// deleted
			}
			else{
				faceIndisNew.add(s.faceIndis.get(f));
				faceNextNew.add(s.faceNext.get(f));
				faceNextSideNew.add(s.faceNextSide.get(f));
				reffTableFaces[f]=seqNum;
				seqNum++;
			}
		}
		s.faceIndis=faceIndisNew;
		s.faceNext=faceNextNew;
		s.faceNextSide=faceNextSideNew;
		for (int i = 0; i < s.faceIndis.size(); i++) {
			int[] indis= s.faceIndis.get(i);
			int[] next= s.faceNext.get(i);
			for (int j = 0; j < 3; j++) {
				indis[j]=reffTableVerts[indis[j]];
				next[j]=reffTableFaces[next[j]];
			}			
		}
		deadFace=null;
		deadVertex=null;
		worstSide=null;
		changed=null;
		faceOfVert=null;
		s.cleanDataStructure();
	}
	/** Vermerkt die Facette als geloescht.
	 * @param face
	 */
	private void setDeadFace(int face){
		deadFace[face]=true;
		deadFaceCount++;
	}
	/** Vermerkt den Vertex als geloescht.
	 * @param vert
	 */
	private void setDeadVert(int vert){
		deadVertex[vert]=true;
		deadVertCount++;
	}
	/** Berechnet die Quadrate der Laengen der Kanten. */
	private void calcLen(){
		s.faceSideLenSq=new ArrayList<double[]>(s.faceIndis.size());
		for (int i = 0; i < s.faceIndis.size(); i++) {
			s.faceSideLenSq.add(new double[]{0,0,0});
		}
		for (int face = 0; face < s.faceIndis.size(); face++) {
			int[] faceVerts=s.faceIndis.get(face);
			for (short side = 0; side < 3; side++) {
				double[] u=s.getPosition(faceVerts[side]);
				double[] v=s.getPosition(faceVerts[next(side)]);
				s.faceSideLenSq.get(face)[side]=Calculator.distSq(u, v);
			}
		}
	}
	/** Setzt jedem Vertex eine Liste seiner angrenzenden Facetten. */
	@SuppressWarnings("unchecked")
	private void initFaceOfVert(){
		faceOfVert=new ArrayList[s.coords.size()];
		for(int i = 0; i < faceOfVert.length; i++) 
			faceOfVert[i]=new ArrayList<Integer>(6);
		for (int f = 0; f < s.faceIndis.size(); f++) {
			int[] verts= s.faceIndis.get(f);
			for (int i = 0; i < 3; i++) {
				faceOfVert[verts[i]].add(f);
			}
		}
	}
	/** Setzt jeder Facette einen Zeiger auf ihre kuerzeste Seite, die entfernt werden darf.
	 * Zeigt andernfalls auf <code>null</code>. 
	 * Faengt die Facetten, die eine solche Seite haben, einer Liste hinzu. 
	 */ 
	private void markAndPointShortest(){
		shortFaces= new LinkedList<Integer>();
		int numFaces=s.faceIndis.size();
		worstSide=new short[numFaces];
		for (int f = 0; f < numFaces; f++) {// every Face
			short side=-1;
			boolean used=false;
			double tol=tolSq;
			for(short j=0;j<3;j++){
				if(s.faceSideLenSq.get(f)[j]<tol){
					if(removeable(f,j)){ // check ob diese Seite entfernt werden darf
						tol=s.faceSideLenSq.get(f)[j];
						side=j;
						used=true;
					}
				}
			}
			worstSide[f]=side;
			if(used)
				shortFaces.add(f);
		}
	}
	/** Sucht die Nachbarschaft eines Vertex nach einer Kante ab, die noch kuerzer
	 * ist als die vorgegebene Laenge. 
	 * @param face Die start-Facette. 
	 * @param vert Der Vertex.
	 * @param len Die zu vergleichende Laenge.
	 * @return Facette mit kuerzeste, entfernbaren Seite in der Umgebung des Vertex.
	 */
	private int getFaceWithShortestSideOfVertex(int face, int vert, double len){
		ArrayList<Integer> faces=faceOfVert[vert];
		for(int f: faces){
			if(worstSide[f]<0)continue;
			if(s.faceSideLenSq.get(f)[worstSide[f]]<len){
				len=s.faceSideLenSq.get(f)[worstSide[f]];
				face= f;
			}
		}
		return face;
	}
	/** Sucht rekursiv die Nachbarschaft 
	 * nach einer Facette mit einer kurzen entfernbaren Kante ab,
	 * welche keine kuerzeren, noch entfernbaren Kanten in ihrer unmittelbaren Umgebung hat.
	 * Das first im Namen bezieht sich auf den ersten der beiden Vertice der Kante 
	 * @param face Start-Facette.
	 * @return Beste kurze Kante der Umgebung.
	 */
	private int searchForBestFirst(int face){
		int side= worstSide[face];
		double len=s.faceSideLenSq.get(face)[side];
		int vert=s.faceIndis.get(face)[side];
		int first= getFaceWithShortestSideOfVertex(face, vert, len);
		return searchForBestSeccond(first,vert);	
	}
	/** Handelt genauso wie searchForBestFirst, muss aber nur bei dem zweiten Vertex testen,
	 * da es fuer den ersten(first) bereits als die optimale Kante bestimmt wurde.
	 * @param first Die start-Facette.
	 * @param oldVert
	 * @return Beste kurze Kante der Umgebung.
	 */
	private int searchForBestSeccond(int first, int oldVert){
		int side= worstSide[first];
		int vert;
		if(s.faceIndis.get(first)[side]==oldVert)
			vert=s.faceIndis.get(first)[next(side)];
		else vert=s.faceIndis.get(first)[side];
		double len=s.faceSideLenSq.get(first)[side];
		int best= getFaceWithShortestSideOfVertex(first, vert, len);
		if(first==best){
			return best;
		}
		else{
			return searchForBestSeccond(best, vert);
		}
	}
	/** Entfernt die Facetten und Vertice im Ramen des EdgeRemove. 
	 * Die geloeschten Elemente werden markiert. Die Datenstruktur wird neu vezeigert.
	 * Es werden noch keine Elemente aus der ArrayList im FaceCycleSubdivider entfernt.
	 */
	private void removeList(){
		changed= new boolean[s.faceIndis.size()];
		deadVertex= new boolean[s.coords.size()];
		deadFace= new boolean[s.faceIndis.size()];
		deadFaceCount=0;
		deadVertCount=0;
		for (int i = 0; i < s.coords.size(); i++) 
			deadVertex[i]=false;
		for (int i = 0; i < s.faceIndis.size(); i++){ 
			changed[i]=false;
			deadFace[i]=false;
		}
		while(shortFaces.size()>0){
			int face=shortFaces.getFirst();
			short side=worstSide[face];
			if (side>=0){ // try to remove, or remove better one first
				int best= searchForBestFirst(face);
				remove(best);
			}
			if (worstSide[face]<0)// if it is used or vorbidden: remove it from List!
				shortFaces.removeFirst();
		}
	}
	/** Entfernt eine Kante. Die zu loeschenden Elemente werden markiert.
	 * Die Datenstruktur wird neu verzeigert.
	 * Die Elemente werden noch nicht aus der ArrayListe des FaceCycleSubdividers entfernt. 
	 */
	private void remove(int face){
		short side=worstSide[face];
		// mark as removed/vorbidden
		int v1=s.faceIndis.get(face)[side];
		int v2=s.faceIndis.get(face)[next(side)];
		// remove
		LinkedList<int[]> toRemove=facesToRemove(v1,v2);
		for (int[] fs: toRemove){ 
			mergeCyclesAndRemove(fs);
			worstSide[fs[0]]=-1;// removed
			setDeadFace(fs[0]);
			int v3=s.faceIndis.get(fs[0])[prev(fs[1])];
			//TODO!!!
//			if((v1==v3)||(v2==v3)||(v1==v2))
//				System.out.println("Remover.remove()"+v1+"|"+v2+"|"+v3+"---------------------------------------------------------------------------");
			faceOfVert[v1].remove(new Integer(fs[0]));
			faceOfVert[v2].remove(new Integer(fs[0]));
			faceOfVert[v3].remove(new Integer(fs[0]));
		}
		mergeVerts(v1,v2);
		repointFacesOfMovedVert(v2);
	}
	/** Das entfernen einer Kante schliesst das Vereinen zweier Vertice mit ein.
	 * ihre neuen Koordinaten werden mittels einer Spline-Interpolation bestimmt.
	 * @param v1
	 * @param v2
	 */
	private void mergeVerts(int v1,int v2){		
		ArrayList<Integer> faces1=faceOfVert[v1];
		for (int f: faces1) {
			int[] indis=s.faceIndis.get(f);
			for (int i = 0; i < 3; i++) 
				if(indis[i]==v1)
					indis[i]=v2;
			faceOfVert[v2].add(f);
		}
		double[] val=s.spline(v1,v2,usePseudoNormals);
		s.coords.set(v2,val);
		s.setImage(v2);
		setDeadVert(v1);
		faceOfVert[v1]=null;
	}
	/** Entfernen einer Kante aus der Nachbarschaft von moeglicherweise
	 * mehreren Facetten(Nicht-Mannigfaltigkeit). Die Nachbarschaftsverhaeltnisse werden neu organisiert.
	 * Diese Operation arbeitet direkt auf der FaceCycle-Datenstruktur des FaceCycleSubdividers.
	 * @param faceSide
	 */
	private void mergeCyclesAndRemove(int[] faceSide){
		int face=faceSide[0];
		short s1=next(faceSide[1]);
		short s2=prev(faceSide[1]);
		int[] prev1= s.getPrevFaceSides(face, s1);
		int[] prev2= s.getPrevFaceSides(face, s2);
		int[] next1= s.nextFaceSide(new int[]{face, s1});
		int[] next2= s.nextFaceSide(new int[]{face, s2});
		if(prev1[0]==face){// boundary
			s.removeFaceSideFromZykle(face,s2);
		}
		else if(prev2[0]==face){//boundary
			s.removeFaceSideFromZykle(face,s1);
		}
		else{
			s.faceNext.get(prev1[0])[prev1[1]]=next2[0];
			s.faceNext.get(prev2[0])[prev2[1]]=next1[0];
			s.faceNextSide.get(prev1[0])[prev1[1]]=(short)next2[1];
			s.faceNextSide.get(prev2[0])[prev2[1]]=(short)next1[1];
		}
		// for safety:
		s.faceNext.set(face, new int[]{face,face,face});
		s.faceNextSide.set(face, new short[]{0,1,2});
	}
	/** Bestimmt die Facetten die an der zu entfernenden Kante liegen.
	 * Die Kante liegt an den gegebenen Vertice.
	 * @param v1 Vertex der Kante
	 * @param v2 Vertex der Kante
	 * @return Facetten an beiden Vertice
	 */
	private LinkedList<int[]> facesToRemove(int v1, int v2){
		ArrayList<Integer> facesV1=faceOfVert[v1];
		LinkedList<int[]> faceSides=new LinkedList<int[]>();
		for(int f: facesV1){
			int[] indis=s.faceIndis.get(f);
			for (int i = 0; i < 3; i++) 
				if(indis[i]==v2){
					if(indis[next(i)]==v1)
						faceSides.add(new int[]{f,i});
					else	
						faceSides.add(new int[]{f,prev(i)});
				}	
		}
		return faceSides;
	}
	/** Organisiert den Zeiger einer Facette neu,
	 * der auf seine kuerzeste entfernbare Kante verweist. 
	 * @param vert
	 */
	private void repointFacesOfMovedVert(int vert){
		ArrayList<Integer> faces=faceOfVert[vert];
		for (int f: faces){
			int worst=worstSide[f];
			if(worst<0)continue;// allready vorbidden
			if(changed[f]){ // keine weiteren entfernungen erlaubt.
				worstSide[f]=-1;
				continue;
			}	
			changed[f]=true;
			int[] indis=s.faceIndis.get(f);
			int pos=0;
			for (int i = 0; i < 3; i++) if(indis[i]==vert)pos=i;
			short obverseSide=next(pos);
			if(worstSide[f]==obverseSide)
				continue;// this side is uneffected
			if(s.faceSideLenSq.get(f)[obverseSide]<tolSq && removeable(f,obverseSide)){
				worstSide[f]=obverseSide;
			}
			else
				worstSide[f]=-1;// nichts mehr zu entfernen.
		}
	}
	/** Stellt fest, ob der Vertex am Rand liegt.
	 * @param vert Vertex
	 * @return true, falls er am Rand liegt.
	 */
	private boolean isAtBoundary(int vert){
		ArrayList<Integer> faces=faceOfVert[vert];
		for(int f:faces){
			for (int i = 0; i < 3; i++) 
				if(s.faceNext.get(f)[i]==f)//Rand
					if(s.faceIndis.get(f)[prev(i)]!=vert)//Seite nicht gegenueber von Vert
						return true;
		}
		return false;
	}
	/** Stellt nach verschiedenen Kriterien fest,
	 * ob es fuer die Flaeche vertraeglich ist diese Kante zu entfernen.
	 * @param face Facette
	 * @param side Seite der Facette
	 * @return true, falls sie entfernt werden darf.
	 */
	private boolean removeable(int face, short side){
		// check for sameFaces
		int[] next=s.nextFaceSide(new int[]{face,side});
		if(s.sameFaces(face, next[0]))// facetten benutzen die gleichen Punkte
			return false;
		//check boundary
		int v1=s.faceIndis.get(face)[side];
		int v2=s.faceIndis.get(face)[next(side)];
		if(isAtBoundary(v1))
			return false;
		if(isAtBoundary(v2))
			return false;
		//check angles/Normals
//		if(!checkAngles(v1)) TODO
//			return false;
//		if(!checkAngles(v2))
//			return false;
		return true;
	}
	/** Prueft ob der Winkel zwischen Vertexnormale und angrenzenden
	 * Facetten nicht zu gross ist.
	 * Vergleicht dazu den Winkel zwischen der (pseudo-)Vertexnormalen
	 * und den Orthogonalgeraden der Facetten. 
	 * Facettennormalen muessen daher nicht konsistent orientiert sein.
	 * sollte die Facette mehr als PI/2 aus der Tangentialebene des Vertex ragen.
	 * wird evtl. falsch geprueft. 
	 * @param v Vertex
	 * @return true, falls der Winkel kein Problem darstellt.
	 */
//	private boolean checkAngles(int v){
//		double[] vNormal=s.normals.get(v);
//		if(Calculator.lenSq(vNormal)==0)
//			return false;
//		for(int f:faceOfVert[v]){
//			double[] fNormal=s.faceNormals.get(f);
//			if(Calculator.isCosSmalerToleranceSq(fNormal, vNormal, tolNormalAngleSq,usePseudoNormals))
//				return false;
//		}
//		return true;
//	}		
	/** @return FaceCycleSubdivider der die zu bearbeitende Flaeche enthaelt. */
	public FaceCycleSubdivider getSubdivider() {
		return s;
	}
	/** Setzt die Toleranz fuer die Laenge, ab der Entschieden wird,
	 *  ob eine Kante zu kurz ist.
	 * @param tol Toleranz
	 */
	public void setTolerance(double tol) {
		this.tolSq = tol*tol;
	}
	/** @return Liefert die Toleranz nach der Entschieden wird, ob eine Kante zu kurz ist. */
	public double getTolerance() {
		return Math.sqrt(tolSq);
	}
	/** Entscheidet ob mit Pseudonormalen gerechnet werden soll.
	 * @param usePseudoNormals true, falls Pseudo erlaubt sind
	 */
	public void setUsePseudoNormals(boolean usePseudoNormals) {
		this.usePseudoNormals = usePseudoNormals;
	}
	/** @return Gibt an ob mit Pseudo-Normalen gerechnet wird. */
	public boolean isUsePseudoNormals() {
		return usePseudoNormals;
	}
	/** Setzt den Winkel, in Grad, ab dem Entschieden wird ob die Umgebung einer Kante nicht eben genug
	 * ist, um die Kante zu entfernen.  
	 * @param tolNormalAngle
	 */
	public void setTolNormalAngle(double tolNormalAngle) {
		double cos=Math.cos(tolNormalAngle*Math.PI/180);
		this.tolNormalAngleSq = cos*cos;
	}
	/**@return Liefert den Winkel, in Grad, der angibt ab wann die Umgebung einer Kante nicht eben genug
	 * ist, um die Kante zu entfernen. */
	public double getTolNormalAngle() {
		return Math.sqrt(tolNormalAngleSq)*180/Math.PI;  
	}
}