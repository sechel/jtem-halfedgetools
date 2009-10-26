package de.jtem.halfedgetools.algorithm.subdivision.util;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;


/** Diese Klasse dient der Unterteilung eine Triangulierte Flaeche
 *  anhand von Koordinaten und Facettenindice mit Hilfe von Splines. 
 * 
 * Zur intern verwendeten Datenstruktur:
 *  Vertice haben einen Index aufgrund ihrer Position im <code>coords</code> Array.
 *  Die Facetten haben einen Index aufgrund ihrer Position im <code>faceIndis</code> Array.
 *  Die Eckpunkte einer Facette sind innerhalb der Facette indiziert mit ihrer Position
 *   im <code>faceIndis</code> Array an der Stelle der Facette. Also bei der Facettendefinition.
 *  Die Seiten einer Facette sind innerhalb der Facette indiziert mit dem Zyklisch niedrigeren Index
 *   ihrer beiden Eckpunkte. 
 *  Die Kante zwischen dem letzten und dem ersten Vertex einer Facette hat demnach Index 2.
 *  Bei den auf einen illegalen Vertex folgenen Facetten gilt folgende Reihenfolge:
 *   Der Illegale Vertex ging aus einer Kante hervor die zwischen 2 Vertice lag.
 *   Die neue Facette, die an dem Vertex liegt, der den niedrigeren Index traegt, 
 *   wird zuerst genannt.
 *  Die Objekte wie Facetten und Vertice existieren als solche nur anhand ihres Index.
 *  Ihre Daten sind in anderen Listen unter dem gleichen Index gespeichert.  
 * 
 * Naeheres in meiner Diplomarbeit.
 * 
 * @author Bernd Gonska
 */

public class FaceCycleSubdivider {
	
	/// ----------------- FaceCycle Data ----------------- 
	// Vertex-Koordinaten
	ArrayList<double[]> coords;
	// Vertex-Normalen
	ArrayList<double[]> normals;
	// Facetten-Normalen 
	ArrayList<double[]> faceNormals;
	// fuer jede Facette die Laengen ihrer drei Seiten
	ArrayList<double[]> faceSideLenSq;  // <double[3]>
	// Der Typ einer Facette wird waehrend der Unterteilung festgelegt um
	private ArrayList<FaceType> faceType;  // <double[3]>
	// Liste aller Facetten die mindestens eine zu langen Kante haben.
	private List<Integer> badFaces;
	
	// definition der Facetten 
	ArrayList<int[]> faceIndis; // <int[3]>
	// fuer jede Seite einer Facette wird die Nachbarfacette(bzw Vertex) gespeichert 
	ArrayList<int[]> faceNext;  // <int[3]>
	// fuer jede Seite einer Facette wird der Index der anliegenden Seite der 
	// Nachbarfacette gespeichert. ist der Nachbar ein Vertex wird dies durch -1 vermerkt.
	ArrayList<short[]> faceNextSide; // <short[3]> 
	// ein illegaler vertex verweist auf zwei anliegende Facetten. 
	private ArrayList<int[]> illegalVertNext; // <int[2]> ; haben nur illegale Vertice
	// ein illegaler vertex verweist auf je eine Seite der zwei Facetten auf die er zeigt. 
	private ArrayList<short[]> illegalVertNextSide; // <short[2]> ; haben nur illegale Vertice
	// Anzahl der regulaeren Vertice. Dies wird benoetigt um aus dem
	// Index eines irregulaeren vertex die position seiner Daten aus
	// <code> illegalVertNext </code> und <code> illegalVertNextSide </code> zu ermitteln.
	private int legalVertexCount;// 
	/// ----------------- settings----------------- 
	// gibt an normalen berechnet werden muessen
	private boolean needNormals=true;
	// Toleranz fuer die Laenge einer Kante (Unterteilungskriterium)
	private double tolerance=0.3;
	// Toleranz fuer die ebenmaesigkeit um Facetten zusammenzufassen.
	// Dient dem Erhalt der Form der Flaeche. 
	private double curvTol=0;
	// Eine andere Form des selben Wertes.
	private double curvTolSqSign=0;
	// Typ der Normalen-Berechnung
	private Calculator.NormalType normalType=Calculator.NormalType.AREA;
	// Einstellung ob mit pseudo-Normalen gerechnet wird.
	private boolean usePseudoNormals=false;
	// ----------------- Graphendarstellung -------------------
	// Vertex-Koordinaten aus dem Graph der Funktion
	ArrayList<double[]> coordsImage;
	private RnRmFunktion mapper=null; // maps a  parameter to the Image (if given)
	double[] getPosition(int vertex){return (mapper==null)?coords.get(vertex):coordsImage.get(vertex);}
	void setImage(int vertex){if(mapper!=null)coordsImage.set(vertex, mapper.map(coords.get(vertex)));}
	public boolean useImages(){return mapper!=null;}
	public void setMapper(RnRmFunktion mapper) {this.mapper = mapper;}
	public RnRmFunktion getMapper() {	return mapper;}
	private TwoPointInterpolator pointInterp=	new TwoPointInterpolator(){
		public double[] interpolate(double[] a, double[] b) {
			return Calculator.linearCombination(.5, a, .5, b);
		}
	};
	public void setPointInterpolator(TwoPointInterpolator pointInterpolator) {	this.pointInterp= pointInterpolator;}
	public TwoPointInterpolator getPointInterpolator() {	return pointInterp;}
	boolean useExtraflip=true;
	boolean useSimple=true;
	// ----------------- essentielle Kombinatorik -------------
	/** Dient dem Einlesen von Vertice. 
	 * Darf nur beim Einlesen von Daten genutzt werden.
	 * @param coord Koordinaten des Vertex
	 * @return Index des Vertex 
	 */
	private int addCommonVertex(double[] coord){
		if(coord==null) coord=new double[]{0,0,0};
		coords.add(new double[]{coord[0],coord[1],coord[2]});// std coordinaten
		if(mapper!=null)
			coordsImage.add(mapper.map(coord));// calculate new image
		else 
			coordsImage.add(new double[]{0,0,0});// std coordinaten
		legalVertexCount++;
		return coords.size()-1;
	}
	/** Fuegt einen neuen (illegalen) Vertex hinzu.
	 * Wird waerend der Unterteilung bemutzt.  
	 * @param coord VertexKoordinaten
	 * @return Index des Vertex
	 */
	private int addIllegalVertex(double[] coord){
		if(coord==null) coord=new double[]{0,0,0};
		coords.add(new double[]{coord[0],coord[1],coord[2]});// std coordinaten
		if(mapper!=null)
			coordsImage.add(mapper.map(coord));// calculate new image
		normals.add(new double[]{0,0,0});// Vertex normalen
		illegalVertNext.add(new int[]{-1,-1});// no node attached
		illegalVertNextSide.add(new short[]{-2,-2});// -2 means unset
		return coords.size()-1;
	}
	/** Dient dem Erstellen einer Facette. 
	 * @param indices Indice der definierenden Vertice
	 * @return Index der Facette 
	 */
	private int addFace(int[] indices, FaceType ft){
		if(indices==null) indices=new int[]{-1,-1,-1};
		faceIndis.add(new int[]{indices[0],indices[1],indices[2]});// no Verticess attached !no change will cause crashes!!!
		faceNext.add(new int[]{-1,-1,-1});// no node attached
		faceType.add(ft);
		faceNextSide.add(new short[]{-2,-2,-2});// -2 means unset
		return faceIndis.size()-1;
	}	
	/** Liefert die beiden Facetten auf die der illegale Vertex verweist. 
	 * Nur fuer illegale Vertices anwendbar. Reihenfolge siehe oben.
	 * @param vertex Index des Vertex
	 * @return <code>int[2]</code> Die beiden folgenden Facetten   
	 */
	private int[] getFacesNextOfVertex(int vertex){
		if(vertex<legalVertexCount){// normal Vertex has no faceNext
			System.err.println("JRSubdivider.getFacesNextOfVertex(legal Vertex has no faceNext)");
			return null;
		}
		return illegalVertNext.get(vertex-legalVertexCount);
	}
	/** Liefert die beiden anliegenden Seiten der Facetten auf die der illegale Vertex verweist. 
	 * Nur fuer illegale Vertices anwendbar. Reihenfolge wie bei <code> getFacesNextOfVertex </code>.
	 * @param vertex Index des Vertex.
	 * @return <code>int[2]</code> Die beiden folgenden Facetten-Seiten   
	 */
	private short[] getFacesNextSidesOfVertex(int vertex){
		if(vertex<legalVertexCount){// normal Vertex has no faceNext
			System.err.println("JRSubdivider.getFacesNextSidesOfVertex(legal Vertex has no faceNext)");
			return null;
		}
		return illegalVertNextSide.get(vertex-legalVertexCount);
	}
	/** Setzt einem Illegalen Vertex eine nachfolgende Facette mit ihrer Seite.  
	 * Die position ob als erstes oder zweites muss genannt werden. 
	 * Dabei gilt es die Konvention des niedriger indizierten Vertex zu beachten.(s.o.)
	 * @param vertex 
	 * @param pointer Als erstes oder zweites genannt.
	 * @param face 
	 * @param side
	 */
	private void setFaceSideNextOfVertex(int vertex, short pointer, int face, short side){
		if(vertex<legalVertexCount){// normal Vertex has no faceNext
			System.err.println("JRSubdivider.setFaceSideNextOfVertex(legal Vertex has no faceNext)");
			return;
		} 
		illegalVertNext.get(vertex-legalVertexCount)[pointer]=face;
		illegalVertNextSide.get(vertex-legalVertexCount)[pointer]=side;
	}
	/** Es wird der illegale Vertex zu dieser Kante gesucht.
	 *  Gibt es keinen wird -1 zurueckgegeben.
	 * @param face Facette
	 * @param side Interne Seite der Facette
	 * @return Index des Vertex
	 */
	private int findIllegalVertex(int face, short side){
		if(side==-1)// ist bereits vertex
			return face;
		int[] nodeSide=nextFaceSide(new int[]{face,side});
		while( (nodeSide[0]!=face || nodeSide[1]!=side)
				// darf eigentlich keine 2. seite geben(kommt ja nur durch Vertex)
				//&&(nodeSide.length!=4 || nodeSide[2]!=face || nodeSide[3]!=side)
				){//Abbruch: einer ist startFace
			if(nodeSide[1]==-1){
				return nodeSide[0]; 
			}
			nodeSide=nextFaceSide(nodeSide);
		}
		return -1; // no Vertex found 
	}
	/**  Ein illegaler Vertex zeigt auf 2 Facetten.
	 * Gibt zurueck welche position die gegebene Facette an diesem Vertex hat. 
	 * @param face Facette
	 * @param side Seite der Facette auf die der Vertex zeigen sollte
	 * @param vert Vertex
	 * @return 
	 *  "0" fuer die erste Position.
	 *  "1" fuer die zweite Position.
	 *  -1 falls nicht vorhanden.
	 */
	private short getVertexPointerOfFace(int face, int side, int vert){
		int [] faces= getFacesNextOfVertex(vert);
		short [] nextSides= getFacesNextSidesOfVertex(vert);
		if(faces[0]==face && nextSides[0]==side)
			return 0;
		if(faces[1]==face && nextSides[1]==side)
			return 1;
		System.err.println("JRSubdivider.getVertexPointerOfFace(vertex does not point to this Face side)");
		return -1;  
	}
	/** Liefert den Nachfolger einer Seite einer Facette.
	 * Falls der Nachfolger ein illegaler Vertex ist, 
	 * Wird als Seite -1 zurueckgegeben.
	 * @param faceSide Hat die Form <code>int[]{face, side}</code>.
	 * @return <code>int[]{face, side}</code> Facette und Seite des Nachfolgers.
	 */	
	int[] nextFaceSide(int[] faceSide){
		if(faceSide.length==4){// two faceSides
			int[] a=nextFaceSide(new int[]{faceSide[0],faceSide[1]}); 
			int[] b=nextFaceSide(new int[]{faceSide[2],faceSide[3]});
			if(a[0]==b[0]&& a[1]==b[1])
				return a;
			return new int[]{a[0],a[1],b[0],b[1]};
		}
		if(faceSide[1]==-1){//vertex
			int[] faces=getFacesNextOfVertex(faceSide[0]);
			short[] sides=getFacesNextSidesOfVertex(faceSide[0]);
			return new int[]{faces[0],sides[0],faces[1],sides[1]};
		}
		return new int[]{faceNext.get(faceSide[0])[faceSide[1]],
				faceNextSide.get(faceSide[0])[faceSide[1]]};
	}
	/** Liefert die Vorgaenger zur Seite einer Facette.
	 * @param face Facette
	 * @param side Seite
	 * @return
	 *  <code>int[]{face, side}</code> Facette und Seite des Vorgaengers.
	 *  Falls der Zykel einen illegalen Vertex enthaelt, gibt er immer 2 Vorgaenger zurueck:
	 *  <code>{face1, side1,  face2, side2}</code>
	 *  Diese sind aber identisch falls es nur einen Vorgaenger gibt.
	 *   
	 */	
	int[] getPrevFaceSides(int face, short side){
		int[] nodeSide= new int[]{face,side};
		int[] nodeSidesNext=nextFaceSide(nodeSide);
		while( (nodeSidesNext[0]!=face || nodeSidesNext[1]!=side)// TODO hier haengt er!!!
				&&(nodeSidesNext.length==2 || nodeSidesNext[2]!=face || nodeSidesNext[3]!=side)
				){//Abbruch: ein(!)Nachfolger ist startFace
			nodeSide=nodeSidesNext;
			nodeSidesNext=nextFaceSide(nodeSide);
		}
		if(nodeSide.length==4 && nodeSidesNext.length==4){// nicht beide vorgaenger von face!
			if(nodeSidesNext[0]==face && nodeSidesNext[1]==side)
				return new int[]{nodeSide[0],nodeSide[1]};
			else
				return new int[]{nodeSide[2],nodeSide[3]};
		}	
		return nodeSide;
	}
	/** Ersetzt eine Facettenseite in an einem FacettenSeiten-Zyklus
	 * durch eine andere. Nachbarschaften werden dabei umgehaengt.
	 * Die entnommene Factten-Seite zeigt auf sich selbst.
	 * @param faceOld 
	 * @param sideOld Die zu ersetzende Facetten-Seite.
	 * @param faceNew
	 * @param sideNew Die einzusetzende Facetten-Seite.
	 */
	void replaceFaceSideInZykle(int faceOld, short sideOld,int faceNew, short sideNew){// fine
		if(sideOld==-1||sideNew==-1){
			System.err.println("JRSubdivider.replaceFaceSideInZykle(darf keine vertices replacen)");
			return;
		}
		// beachte das die faceOld Seite bereits unterteilt sein kann
		int[] nodeSideNext=nextFaceSide(new int[]{faceOld,sideOld});
		if(nodeSideNext[0]==faceOld && nodeSideNext[1]==sideOld){// liegt am Rand
			faceNext.get(faceNew)[sideNew]=faceNew;
			faceNextSide.get(faceNew)[sideNew]=sideNew;
			return;
		}
		int[] prev= getPrevFaceSides(faceOld, sideOld);// {face,side} or {face1,side1,face2,side2}
		if(prev[1]==-1){// Vorgaenger prev ist ein Vertex
			short  pointer = getVertexPointerOfFace(faceOld, sideOld, prev[0]);
			setFaceSideNextOfVertex(prev[0], pointer, faceNew, sideNew);
		}
		else{// Vorgaenger prev sind ein oder zwei Faces
			if(prev.length==4){ // es sind 2 vorgänger
				faceNext.get(prev[2])[prev[3]]=faceNew;
				faceNextSide.get(prev[2])[prev[3]]=sideNew;
			}
			faceNext.get(prev[0])[prev[1]]=faceNew;
			faceNextSide.get(prev[0])[prev[1]]=sideNew;
		}
		faceNext.get(faceNew)[sideNew]=nodeSideNext[0];
		faceNextSide.get(faceNew)[sideNew]=(short)nodeSideNext[1];
		// for Savety:
		faceNext.get(faceOld)[sideOld]=faceOld;
		faceNextSide.get(faceOld)[sideOld]=sideOld;
	}
	/** Setzt eine Facettenseite in an einen FacettenSeiten-Zyklus ein.
	 * Fuer die Position wird der Nachfolger angegeben.
	 *  Nachbarschaften werden dabei umgehaengt.
	 * @param nextface 
	 * @param nextSide Die gewuenschte Nachfolger Facetten-Seite. 
	 * @param faceNew
	 * @param sideNew Die einzusetzende FacettenSeite.
	 */
	void insertFaceSideInZykle(int nextface, short nextSide,int faceNew, short sideNew){
		if(sideNew==-1){// cannot insert Vertex!
			System.err.println("JRSubdivider.insertFaceSideInZykle(cannot insert Vertex!)");
			return;
		}
		int[] prev= getPrevFaceSides(nextface, nextSide);// {face,side} or {face1,side1,face2,side2}
		if(prev[1]==-1){// Vorgaenger prev ist ein Vertex
			short  pointer = getVertexPointerOfFace(nextface, nextSide, prev[0]);
			setFaceSideNextOfVertex(prev[0], pointer, faceNew, sideNew);
		}
		else{// Vorgaenger prev ist ein oder 2 Faces
			if(prev.length==4){ // es sind 2 vorgänger
				faceNext.get(prev[2])[prev[3]]=faceNew;
				faceNextSide.get(prev[2])[prev[3]]=sideNew;
			}
			faceNext.get(prev[0])[prev[1]]=faceNew;
			faceNextSide.get(prev[0])[prev[1]]=sideNew;
		}
		faceNext.get(faceNew)[sideNew]=nextface;
		faceNextSide.get(faceNew)[sideNew]=nextSide;		
	}
	/** Entfernt eine Facetten-Seite aus einem Facetten-Seiten-Zyklus. 
	 * Nachbarschaften werden dabei umgehaengt.
	 * Die entnommene Factten-Seite zeigt danach auf sich selbst.
	 * @param face 
	 * @param side Die zu isolierende Facetten-Seite.
	 */
	void removeFaceSideFromZykle(int face, short side){
		int[] next= nextFaceSide(new int[]{face,side});
		if(next[0]==face && next[1]==side){// boundary faceSide
			System.err.println("JRSubdivider.removeFaceSideFromZykle(boundary faceSide)");
			return;
		}
		if(side==-1){// cannot remove Vertex!
			System.err.println("JRSubdivider.removeFaceSideFromZykle(cannot remove Vertex!)");
			return;
		}
		int[] prev= getPrevFaceSides(face, side);// {face,side} or {face1,side1,face2,side2}
		if(prev[1]==-1){// Vorgaenger prev ist ein Vertex
			System.err.println("JRSubdivider.removeFaceSideFromZykle(Vorgaenger prev ist ein Vertex)");
			return;
		}
		else{// Vorgaenger prev sind ein oder zwei Faces
			if(prev.length==4){ // es sind 2 vorgänger
				faceNext.get(prev[2])[prev[3]]=next[0];
				faceNextSide.get(prev[2])[prev[3]]=(short)next[1];
			}
			faceNext.get(prev[0])[prev[1]]=next[0];
			faceNextSide.get(prev[0])[prev[1]]=(short)next[1];
		}
		// for Savety:
		faceNext.get(face)[side]=face;
		faceNextSide.get(face)[side]=side;
	}
	/** Zaehlt den Facettentyp hoch.
	 * Der Facettentyp entspricht dem Grad im Subdivisionsgraphen.
	 * @param f Der alter Facettentyp.
	 * @return Facettentyp des naechsten Grades.
	 */
	FaceType increaseType(FaceType f){
		if(f==FaceType.FINE)return FaceType.END;		 
		if(f==FaceType.END)return FaceType.WAY;		 
		return FaceType.NODE;		 
	}
	/** Erstellt intern aus den gegebenen Daten <code>faceIndis</code>
	 * die FaceCycle-Datenstruktur mit Nachbarschaftsbeziehungen.
	 */
	public void generateStructure(){
		illegalVertNext= new ArrayList<int[]>();
		illegalVertNextSide= new ArrayList<short[]>();
		HashMap<VertexPair, int[]> edge=new HashMap<VertexPair, int[]>();
		List<int[]> firstFaceSides = new LinkedList<int[]>();
		for (int face = 0; face < faceIndis.size(); face++) {
			int[] faceVerts=faceIndis.get(face);
			for (short side = 0; side < 3; side++) {
				int[] faceSide=new int[]{face,side};
				VertexPair p= new VertexPair(faceVerts[side],faceVerts[next(side)]);
				if (edge.containsKey(p)){ // weiter verlinken
					int[] nextFaceSide=edge.get(p);
					faceNext.get(face)[side]=nextFaceSide[0];// face
					faceNextSide.get(face)[side]=(short)nextFaceSide[1];// side
				}
				else{
					firstFaceSides.add(faceSide);
				}
				edge.put(p, faceSide);
			}
		}
		for(int[] faceSide: firstFaceSides){// den Kreis schliessen
			int face=faceSide[0];
			short side=(short)faceSide[1];
			int[] faceVerts=faceIndis.get(face);
			VertexPair p= new VertexPair(faceVerts[side],faceVerts[next(side)]);
			int[] nextFaceSide=edge.get(p);
			faceNext.get(face)[side]=nextFaceSide[0];// face
			faceNextSide.get(face)[side]=(short)nextFaceSide[1];// side
		}
	}
	
	/** Raeumt nach der Subdivision alle unnoetigen Daten auf.
	 * Dabei werden alle illegalen Vertice zu legalen Vertice.
	 */
	void cleanDataStructure(){
		// clean Data:
		faceSideLenSq=null; 
		badFaces=null; 
		normals=null;
		faceNormals=null;
		// make illegal Vertice legal:
		illegalVertNext=new ArrayList<int[]>(); 
		illegalVertNextSide=new ArrayList<short[]>();
		legalVertexCount= coords.size();
		// thats all :)
	}
	// ----------------- check ---------------
	/** Vergleicht ob zwei Facetten die drei gleichen Vertice refferenzieren,
	 *  und somit strukturell identisch sind. 
 	 * @param f1 Index der einen Facette
 	 * @param f2 Index der anderen Facette
 	 * @return true, falls sie gleich sind.
	 */
	boolean sameFaces(int f1, int f2){
		int n=0;
		int[] vertsF1=faceIndis.get(f1);
		int[] vertsF2=faceIndis.get(f2);
		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 3; j++) 
				if(vertsF1[i]==vertsF2[j])
					n++;
		return (n==3);
	}
	/** Gibt die Differenz an zwischen den Laengequadraten dieser Facettenseite
	 * und des Laengenquadrates ihrer geflipten Alternative an.  
	 * @param face Die zu pruefende Facette.
	 * @param side Die zu pruefende Seite der Facette. 
	 * @return LaengeLaenge(Seite)-LaengeLaenge(geflipte Seite)
	 */
	private double flipTrueLenValue(int face, short side){ 
		int[] next= nextFaceSide(new int[]{face,side});
		int[] verts= faceIndis.get(face);
		double[] v1=getPosition(verts[next(side)]);
		double[] v2=getPosition(verts[prev(side)]);
		double[] v3=getPosition(verts[side]);
		double[] v4=getPosition(faceIndis.get(next[0])[prev((short)next[1])]);
		return Calculator.shouldFlip(v1,v2,v3,v4);
	}
	/** Gibt die Differenz zwischen dem Laengenquadrat dieser Facettenseite
	 * und der Laengenquadrat ihrer intrinsisch geflipten Alternative an.
	 * Intrinsisch meint die Laenge wird innerhalb der Flaeche gemessen.
	 * (Genauer: In der Vereinigung der Ebenen in denen die 2 
	 *  angrenzenden Facetten liegen)   
	 * @param face Die zu pruefende Facette.
	 * @param side Die zu pruefende Seite der Facette. 
	 * @return Laengenquadrat(Seite)-Laengenquadrat(intrinsisch geflipte Seite)
	 */
	private double flipIntrinsicLenValue(int face, short side){
		int[] next= nextFaceSide(new int[]{face,side});
		int[] verts= faceIndis.get(face);
		double[] v1=getPosition(verts[next(side)]);
		double[] v2=getPosition(verts[prev(side)]);
		double[] v3=getPosition(verts[side]);
		double[] v4=getPosition(faceIndis.get(next[0])[prev((short)next[1])]);
		return Calculator.flipIntrinsicLenValue(v1, v2, v3, v4); 
	}
	// ----------------- subdiv ---------------
	/** Kombinatorische Operation:
	 *  Einseitiger Edge-Split.
	 *  @param face Index der zu unterteilende Facette. 
	 *  @param side Seite die gespalten werden soll. 
	 *  @return Index der neue hinzugewonnenen Facette.
	 *  (Die alte Facette wird mitbenutzt. 
	 *  Sie bleibt beim niedrigeren Vertex der Seite.)
	 */
	private int edgeSplit(int face, short side, FaceType ft){
		short s0=side;	
		short s1=next(s0);
		short s2=next(s1);
		int[] faceVerts=faceIndis.get(face);
		int newVert=findIllegalVertex(face, side);
		int[] next=nextFaceSide(new int[]{face,side});
		int[] prev=getPrevFaceSides(face, side);
		int v0=faceVerts[s0];
		int v1=faceVerts[s1];
		int v2=faceVerts[s2];
		if(newVert==-1){//have to add vertex
			// new vertex:
			double[] coord=spline(v0,v1,usePseudoNormals);
			newVert=addIllegalVertex(coord);
			// new Face and VertexReffernces
			int[] vertsOfNew=new int[3];
			vertsOfNew[s0]=newVert;
			vertsOfNew[s1]=v1;
			vertsOfNew[s2]=v2;
			int face2=addFace(vertsOfNew,ft);
			faceVerts[s1]=newVert; // vertices of face
			// link faces (without side)
			replaceFaceSideInZykle(face, s1, face2, s1);
			faceNext.get(face)[s1]=face2;
			faceNext.get(face2)[s2]=face;
			faceNextSide.get(face)[s1]=s2;
			faceNextSide.get(face2)[s2]=s1;
			// link side
			if(next[0]==face && next[1]==side){// boundary faceSide
				faceNext.get(face2)[side]=face2;
				faceNextSide.get(face2)[side]=side;	
			}
			else{
				// link side (next) 
				faceNext.get(face2)[side]=faceNext.get(face)[side];
				faceNextSide.get(face2)[side]=faceNextSide.get(face)[side];
				// link side (prev) // ordne nach Vertex index: kleiner index erste Facette!
				if(v0<v1)
					illegalVertNext.set(newVert-legalVertexCount,new int[]{face,face2});
				else
					illegalVertNext.set(newVert-legalVertexCount,new int[]{face2,face});
				illegalVertNextSide.set(newVert-legalVertexCount,new short[]{side,side});
				// wer zeigt auf newVert?
				faceNext.get(prev[0])[prev[1]]=newVert;
				faceNextSide.get(prev[0])[prev[1]]=-1;// is a Vertex				
			}				
			return face2;
		}
		// else:
		// find both sides and link carefully
		boolean last=false;
		if(prev.length==4 && next[1]==-1)// last unsplit side!
			last=true;
		// new Face and VertexReffernces
		int[] vertsOfNew=new int[3];
		vertsOfNew[s0]=newVert;
		vertsOfNew[s1]=v1;
		vertsOfNew[s2]=v2;
		int face2=addFace(vertsOfNew,ft);
		faceVerts[s1]=newVert; // vertices of face
		// link faces (without side)
		replaceFaceSideInZykle(face, s1, face2, s1);
		faceNext.get(face)[s1]=face2;
		faceNext.get(face2)[s2]=face;
		faceNextSide.get(face)[s1]=s2;
		faceNextSide.get(face2)[s2]=s1;
		// link side: remove
		removeFaceSideFromZykle(face, side);
		// link side: insert behind Vertex
		next=nextFaceSide(new int[]{newVert,-1});// {face1,side1,face2,side2}
		int faceNew=face2;
		if(v1<v0){// face got the lower Vertex face2 the higher(of side) 
			face2=face;
			face=faceNew;
		}
		if(last){
			prev=getPrevFaceSides(newVert,(short)-1);// {face1,side1,face2,side2}
			faceNext.get(prev[0])[prev[1]]=face;
			faceNext.get(prev[2])[prev[3]]=face2;
			faceNext.get(face)[side]=next[0];
			faceNext.get(face2)[side]=next[2];
			faceNextSide.get(prev[0])[prev[1]]=side;
			faceNextSide.get(prev[2])[prev[3]]=side;
			faceNextSide.get(face)[side]=(short)next[1];
			faceNextSide.get(face2)[side]=(short)next[3];
			// for savety:
			illegalVertNext.set(newVert-legalVertexCount,new int[]{-1,-1});
			illegalVertNextSide.set(newVert-legalVertexCount,new short[]{-2,-2});
		}
		else{
			illegalVertNext.set(newVert-legalVertexCount,new int[]{face,face2});
			illegalVertNextSide.set(newVert-legalVertexCount,new short[]{side,side});
			faceNext.get(face)[side]=next[0];
			faceNext.get(face2)[side]=next[2];
			faceNextSide.get(face)[side]=(short)next[1];
			faceNextSide.get(face2)[side]=(short)next[3];
		}
		return faceNew; 
	}
/** Kombinatorische Operation: Edge-Flip.
 *  Der Flip ist nur zulaessig wenn die zu flipende Kante Valenz 2 hat.
 * @param face Facette die zu der zu flipenden Seite gehoert.
 * @param side Seite der Facette die geflipt werden soll.
 */
	private void edgeFlip(int face, short side){
		int[] next=nextFaceSide(new int[]{face,side});
		int other=next[0];
		if(side==-1||next[1]==-1){// einer ist ein Vertex!
			System.err.println("JRSubdivider.edgeFlip(cannot flip a Vertex)");
			return; 
		}
		if (other==face){
			System.err.println("JRSubdivider.edgeFlip(cannot flip boundary edge)");
			return; 	
		}
		int[] nextNext=nextFaceSide(next);
		if(nextNext[0]!=face||nextNext[1]!=side){
			System.err.println("JRSubdivider.edgeFlip(cannot flip edge with valence >2)");
			return;
		}
		short s0=side;
		short s1=next(s0);
		short s2=next(s1);
		short sn0=(short)next[1];
		short sn1=next(sn0);
		short sn2=next(sn1);
		int v1=faceIndis.get(face)[s1];
		int v2=faceIndis.get(face)[s2];
		int v3=faceIndis.get(other)[sn2];
		if(v1==faceIndis.get(other)[sn0]){//gleiche Orientierung
			// Vertices
			faceIndis.get(face)[s1]=v3;
			faceIndis.get(other)[sn1]=v2;
			// link Neighborhood
			replaceFaceSideInZykle(other, sn1, face, s0);				
			replaceFaceSideInZykle(face, s1, other, sn0);
			// link together
			faceNext.get(face)[s1]=other;
			faceNextSide.get(face)[s1]=sn1;
			faceNext.get(other)[sn1]=face;
			faceNextSide.get(other)[sn1]=s1;
		}
		else{//entgegengesetzte Orientierung
			// Vertices
			faceIndis.get(face)[s1]=v3;
			faceIndis.get(other)[sn0]=v2;
			// link Neighborhood
			replaceFaceSideInZykle(other, sn2, face, s0);				
			replaceFaceSideInZykle(face, s1, other, sn0);
			// link together
			faceNext.get(face)[s1]=other;
			faceNextSide.get(face)[s1]=sn2;
			faceNext.get(other)[sn2]=face;
			faceNextSide.get(other)[sn2]=s1;			
		}
	}
	/** Wendet zwei Splits auf unterschiedliche Seiten einer Facette an.
	 * Entscheidet anschliessend durch einen Flip ueber die optimale Konstelation.  
	 * Benoetigt eine Facette die genau 2 zu lange Kanten hat. Unterteilt diese beiden Kanten.
	 * @param face Facette die gedrittelt werden soll.
	 */
	private void splitFaceTwice(int face){
		short firstSide=findNextBadEdge(face, (short)0);
		short seccondSide=findNextBadEdge(face, (short)firstSide);
		if(next(firstSide)!=seccondSide){
			short temp=firstSide;
			firstSide=seccondSide;
			seccondSide=temp;
		}
		short thirdSide=next(seccondSide);
		edgeSplit(face,seccondSide,FaceType.HANDLED_WAY);
		edgeSplit(face,firstSide,FaceType.HANDLED_WAY);
		if(useExtraflip){
			if(flipTrueLenValue(face,thirdSide)>0)
				edgeFlip(face, thirdSide);
		}
	}
	/** Optimierung von <code>splitFaceTwice</code> 
	 * Entscheidet gleich anhand der Laenge was zuerst gesplited wird. 
	 * @param face Facette die gedrittelt werden soll.
	 */
//	private void splitFaceTwiceOpti(int face){
//		double[] len=faceSideLenSq.get(face);
//		short longest=0;
//		if(len[longest]<len[1])	longest=1;
//		if(len[longest]<len[2])	longest=2;
//		int newFace=edgeSplit(face,longest,FaceType.HANDLED_WAY);
//		short vice=next(longest);
//		if(len[vice]<len[next(vice)]){
//			vice=next(vice);
//			edgeSplit(face,vice,FaceType.HANDLED_WAY);	
//		}
//		else{
//			edgeSplit(newFace,vice,FaceType.HANDLED_WAY);
//		}
//		return;
//	}
	/**Findet den Nachfolger dieser Facette im Weg des Subdivisionsgraphen.
	 * Prueft dabei gleichzeitig ob der Weg enden muss.
	 * @param faceSide Facette und Startseite des Restweges.
	 * Diese Seite liegt nicht an der Facette die gesucht wird! 
	 * @return naechste Facetten-Seite des Weges. Gibt <code>null</code>
	 * zurueck, falls der Weg endet. 
	 */
	private int[] NextWayPart(int[] faceSide){
		int otherSide=findNextBadEdge(faceSide[0],(short)faceSide[1]);
		int[] next=new int[]{faceSide[0],otherSide};
		next=nextFaceSide(next);
		if(next[1]==otherSide && next[0]==faceSide[0])
			return null;// boundary edge
		if(next[1]==-1 || faceType.get(next[0])!=FaceType.WAY)
			return null;// no WayPart following
		int [] nextNext=nextFaceSide(next);
		if(nextNext[1]!=otherSide || nextNext[0]!=faceSide[0])
			return null;// edgeValenz > 2 (no pair)
		return next;
	}
	/** Prueft ob die beiden Facettenseiten ein Paerchen 
	 * des Subdivisionsgraphen-Weges bilden duerfen.
	 * Verfaehrt unterschiedlich, je nach dem ob die Normalen normiert sind oder nicht. 
	 * @param curr Die erste Facette mit start-Seite des Weges.
	 * @param oth Die zweite Facette mit mittel-Seite der beiden.  
	 * @return true, falls die Paerchen gebildet werden duerfen.
	 */
	private boolean checkPairable(int[] curr, int[] oth){
		int s=findNextBadEdge(curr[0],(short)curr[1]);
		double[] fNormo=faceNormals.get(oth[0]);
		if(faceIndis.get(curr[0])[s]==faceIndis.get(oth[0])[oth[1]])// nicht die gleiche Orientierung!
			fNormo=Calculator.times(-1, fNormo);
//		if(usePseudoNormals){ //TODO turn off for image
//			if(Calculator.isCosSmalerToleranceSqSign(faceNormals.get(curr[0]), fNormo, curvTolSqSign))
//				return false;
//		}
//		else{
//			if(Calculator.isCosSmalerTolerance(faceNormals.get(curr[0]), fNormo, curvTol))
//				return false;
//		}
		return true;
	}
	/** Unterteilt einen gesamten Subdivisionsgraph-Weg.
	 * @param face Eine beliebige Facette aus diesem Weg.
	 */
	private void way(int face){
		// find start of complete way
		int[] startEdge=new int[]{face, findNextBadEdge(face, (short)0)};
		int[] other=nextFaceSide(startEdge);
		while( other[1]!=-1 // grenzt an Vertex
				&& other[0]!=face // laufen nicht endlos im kreis
				&& faceNext.get(other[0])[other[1]]!=other[0]// Boundary
				&& faceType.get(other[0])==FaceType.WAY // ist Wegstueck
				&& faceNext.get(other[0])[other[1]]==startEdge[0]
		){ // Edge hat valenz 2
			other[1]=findNextBadEdge(other[0], (short)other[1]);
			startEdge=other;
			other=nextFaceSide(startEdge);
		}
		int[] current=startEdge;
		while (current!=null){
			other=NextWayPart(current);
			if(	other!=null 
					&& !sameFaces(current[0],other[0])
					&&  checkPairable(current,other)	// checke ob das Paaerchen gutartig ist
					){// have a pair
				int[] nextCurr=NextWayPart(other);
				int leftFace=current[0];
				int rightFace=other[0];
				short left=(short)current[1];
				short middRight=(short)other[1];
				short middLeft=findNextBadEdge(leftFace, left);
				short right=findNextBadEdge(rightFace, middRight);
				int newLeft=edgeSplit(leftFace,left,FaceType.HANDLED_WAY2);
				int newRight=edgeSplit(rightFace, right,FaceType.HANDLED_WAY2);
				if(next(middRight)==right) // which is now the correct side to flip
					edgeFlip(rightFace,middRight);
				else edgeFlip(newRight,middRight);
				// -------- extra flip:
				if(useExtraflip){
					int[] thirdTestFaceSide=null;
					if(next(middRight)==right){// which is the correct side to try flip?
						if(flipIntrinsicLenValue(newRight,middRight)>0){
							edgeFlip(newRight, middRight);
							thirdTestFaceSide=nextFaceSide(new int[]{newRight,next(middRight)});
						}
						else thirdTestFaceSide=new int[]{newRight,middRight};
					}
					else{
						if(flipIntrinsicLenValue(rightFace,middRight)>0){
							edgeFlip(rightFace, middRight);
							thirdTestFaceSide=new int[]{rightFace,next(middRight)};
						}
						else thirdTestFaceSide=new int[]{rightFace,middRight};
					}
					if(next(left)==middLeft){
						if(flipIntrinsicLenValue(leftFace,middLeft)>0){
							edgeFlip(leftFace, middLeft);
						}
					}
					else{
						if(flipIntrinsicLenValue(newLeft,middLeft)>0){
							edgeFlip(newLeft, middLeft);
						}
					}
					if(thirdTestFaceSide!=null // nur wichtig fuer den Parallelen Fall  
							&& flipIntrinsicLenValue(thirdTestFaceSide[0],(short)thirdTestFaceSide[1])>0){
						edgeFlip(thirdTestFaceSide[0], (short)thirdTestFaceSide[1]);
					}
				}
				// -------- extra flip end
				current=nextCurr;
				faceType.set(leftFace,FaceType.HANDLED_WAY2);
				faceType.set(rightFace,FaceType.HANDLED_WAY2);
			}
			else{// make only one 
				faceType.set(current[0],FaceType.HANDLED_WAY);
				splitFaceTwice(current[0]);
				current=other;
			}
		}
	}
	/** Unterteilt eine Facette vom Typ Node. (viertelt optimal)
	 * @param face Die zu unterteilende Facette.  
	 */
	private void faceSplit(int face){
		int other=edgeSplit(face,(short)0,FaceType.HANDLED_NODE);
		edgeSplit(face,(short)2,FaceType.HANDLED_NODE);					
		edgeSplit(other,(short)1,FaceType.HANDLED_NODE); 
		edgeFlip(face, (short)1);
		// extra flip: TODO turn off for image?
		if(useExtraflip){
			double r0=flipIntrinsicLenValue(face,(short)0);
			double r1=flipIntrinsicLenValue(face,(short)1);
			double r2=flipIntrinsicLenValue(face,(short)2);
			double maxR=Math.max(r0,Math.max(r1, r2));
			if(maxR>0){
				if (r0==maxR) edgeFlip(face, (short)0);
				else if (r1==maxR)edgeFlip(face, (short)1);
				else if (r2==maxR)edgeFlip(face, (short)2);
			}
		}
	}
	/** Sucht zur gegebenen Seite einer Facette, die der zyklischen Reihenfolge 
	 * nach, naechste, zu lange Kante. 
	 * Sollte nur eine Kante zu lang sein, wird diese so gefunden.  
	 * @param face Die Facette.
	 * @param side Die Startseite (kommt als letztes in Frage).
	 * @return Die Lange Seite nach der Startseite. 
	 *  -1, falls es keine lange Seite gibt.
	 */
	private short findNextBadEdge(int face, short side){
		double[] lens=faceSideLenSq.get(face);
		short n;
		for(short i=side;i<side+3;i++){
			n=next(i);
			if(lens[n]>=tolerance*tolerance)
				return n;
		}
		System.err.println("JRSubdivider.findBadEdge(no bad edge found)");
		return -1;// no bad edge found
	}
	// ----------------- normalen -------------------
	/** Berechnet die Pseudo-Vertexnormalen eines Vertex. 
	 * Nebenbei setzt sie pseudoFacettennormalen der angrenzenden Facetten.
	 * Die Normale wird anhand der Nachbarschaft ausgerechnet und ist somit immer richtig.
	 * Vorausgesetzt sie ist bestimmbar. Sonst wird sie auf (0,0,0) gesetzt.
	 */
	private void makeVertexNormal(int face,int vert,short side,boolean[] usedVerts, boolean[][] usedFaceVerts,boolean change){
		/// die Orientierung pro Vertex beibehalten
		int[] verts=faceIndis.get(face);
		short otherSide;
		if(verts[side]==vert){// andere orientierung
			if(usedFaceVerts[face][side])
				return; // allready done
			otherSide=prev(side);
			// calc normal, add
			int v2=faceIndis.get(face)[next(side)];
			int v3=faceIndis.get(face)[prev(side)];
			if(change){
				faceNormals.set(face,Calculator.setAngleWeightNormal(normals.get(vert), getPosition(vert), getPosition(v3),getPosition(v2)));
				faceNormals.set(face,Calculator.times(-1, faceNormals.get(face)));// umdrehen da negativ berechnet!				
			}
			else
				faceNormals.set(face,Calculator.setAngleWeightNormal(normals.get(vert), getPosition(vert), getPosition(v2),getPosition(v3)));
			// mark as done
			usedFaceVerts[face][side]=true;
		}
		else{// gleiche orientierung
			if(usedFaceVerts[face][next(side)])
				return; // allready done
			otherSide=next(side);
			// calc normal, add
			int v3=faceIndis.get(face)[prev(side)];
			int v2=faceIndis.get(face)[side];
			if(change)
				faceNormals.set(face,Calculator.setAngleWeightNormal(normals.get(vert), getPosition(vert), getPosition(v3),getPosition(v2)));
			else{
				faceNormals.set(face,Calculator.setAngleWeightNormal(normals.get(vert), getPosition(vert), getPosition(v2),getPosition(v3)));
				faceNormals.set(face,Calculator.times(-1, faceNormals.get(face)));// umdrehen da negativ berechnet!
			}
			// mark as done
			usedFaceVerts[face][next(side)]=true;
		}
		/// gegen mehrere surfaces absichern!
		if(usedVerts[vert]){
			normals.set(vert,new double[]{0,0,0});// Part more than one surfaces
			return;
		}
		// liegt am Verzweigungspunkt
		int[] next=nextFaceSide(new int[]{face,otherSide});
		if(nextFaceSide(next)[0]!=face){// der Vertex liegt an einem verzweigungspunkt
			usedVerts[vert]=true;
			normals.set(vert,new double[]{0,0,0});
		}
		makeVertexNormal(next[0], vert, (short)next[1], usedVerts, usedFaceVerts,change);
	}
	/** Setzt die drei Laengen einer jeden Facettenseite. 
	 * Zaehlt nebenbei den Grad einer Facette mit. */
	void calcLenAndFaceType(){
		faceType=new ArrayList<FaceType>(faceIndis.size());
		faceSideLenSq=new ArrayList<double[]>(faceIndis.size());
		for (int i = 0; i < faceIndis.size(); i++){ 
			faceType.add(FaceType.FINE);
			faceSideLenSq.add(new double[]{-1,-1,-1});
		}
		badFaces=new LinkedList<Integer>();
		for (int face = 0; face < faceIndis.size(); face++) {
			int[] faceVerts=faceIndis.get(face);
			for (short side = 0; side < 3; side++) {
				double[] u=getPosition(faceVerts[side]);
				double[] v=getPosition(faceVerts[next(side)]);
				double len=Calculator.distSq(u, v);
				if(len>=tolerance*tolerance){
					FaceType ft= faceType.get(face);
					if(ft==FaceType.FINE)
						badFaces.add(face);
					faceType.set(face,increaseType(ft));
				}
				faceSideLenSq.get(face)[side]=len;
			}
		}
	}
	/** Interpoliert mit Hilfe von Splines auf der Kante zwischen
	 * dem Vertex u und v. Benutzt dabei deren Vertexnormalen.  
	 * @param u Vertex
	 * @param v Vertex
	 * @param usePseudoNormals Gibt an, ob die VertexNormalen normiert sind.
	 * @return Interpolierte Koordinaten. 
	 */
	double[] spline(int u,int v,boolean usePseudoNormals){
		double[] ret;
		if(mapper==null){
			if(usePseudoNormals)
				ret=Calculator.interpolateWithPseudoNormal(coords.get(u), coords.get(v), normals.get(u), normals.get(v));
			else ret=Calculator.interpolate(coords.get(u), coords.get(v), normals.get(u), normals.get(v));
		}
		else{
			ret=pointInterp.interpolate(coords.get(u), coords.get(v));
		}
		return ret;
	}
	/** Berechnet die Vertex- und Face-Normalen der Flaeche. 
	 * @param usePseudoNormals Gibt an, ob die Normalen Pseudonormalen sein duerfen.
	 */
	void calcNormals(boolean usePseudoNormals){
		// init some data
		faceNormals=new ArrayList<double[]>(faceIndis.size());
		normals=new ArrayList<double[]>(coords.size());
		boolean[][] usedFaceVerts= new boolean[faceIndis.size()][];
		boolean[] usedVerts= new boolean[coords.size()];
		for (int i = 0; i < faceIndis.size(); i++)	faceNormals.add(new double[]{0,0,0});
		for (int v = 0; v < coords.size();v++)		normals.add(new double[]{0,0,0});
		for (int i = 0; i < faceIndis.size(); i++)	usedFaceVerts[i]=new boolean[]{false,false,false};
		for (int i = 0; i < coords.size(); i++)		usedVerts[i]=false;
		// try make every Vertex of every Face
		for (int f = 0; f < faceIndis.size(); f++) {
			for (short s = 0; s < 3; s++) {
				int vert=faceIndis.get(f)[s];
				int[] next=nextFaceSide(new int[]{f,s});
				makeVertexNormal(f,vert,s,usedVerts,usedFaceVerts,false);
				makeVertexNormal(next[0],vert,(short)next[1],usedVerts,usedFaceVerts,true);
				usedVerts[vert]=true;
				}
			}
		// normalise Normals
		if (!usePseudoNormals){
			for (int v = 0; v < normals.size();v++) 
				normals.set(v, Calculator.normalize(normals.get(v)));
			for (int f = 0; f < faceNormals.size();f++) 
				faceNormals.set(f, Calculator.normalize(faceNormals.get(f)));
		}
	}		
	// ----------------- public-----------------
	/** Klasse zum Subdividieren von triangulierten Flaechen im R3.
	 *  Benoetigt die Flaechendaten in Form einer Koordinatenliste 
	 *  und eine Facettenindex-Liste.
	 */
	public FaceCycleSubdivider(double[][] vertexCoords, int[][] faceIndices) {
		importData(vertexCoords, faceIndices);
	}
	/** Klasse zum Subdividieren von triangulierten Flaechen im R3.
	 */
	public FaceCycleSubdivider() {
	}
	/** Setzt dem FaceCycleSubdivider eine neue Flaeche. 
	 *  Benoetigt die Flaechendaten in Form einer Koordinatenliste 
	 *  und einer Facettenindex-Liste.
	 * @param coordinates
	 * @param faceIndices
	 */
	public void importData(double[][] coordinates, int[][] faceIndices){
		badFaces=null;
		normals=null;
		int numVerts=coordinates.length;
		int numFaces= faceIndices.length;
		coords= new ArrayList<double[]>(numVerts);
		coordsImage= new ArrayList<double[]>(numVerts);
		legalVertexCount=0;
		for (int i = 0; i < numVerts; i++) 
			addCommonVertex(coordinates[i]);
		faceNext=new ArrayList<int[]>(numFaces);
		faceNextSide=new ArrayList<short[]>(numFaces);
		faceIndis= new ArrayList<int[]>(numFaces);
		faceType= new ArrayList<FaceType>(numFaces);
		for (int i = 0; i < numFaces; i++)
			addFace(faceIndices[i], FaceType.FINE);
		generateStructure();
	}
	/** VertexNormalen koennen auch gezielt uebergeben werden.
	 * Die Laenge und Reihenfolge der Liste muss der Anzahl 
	 * und Reihenfolge der bereits gesetzten Vertice entsprechen. 
	 * @param vertexNormals
	 */
	public void setVertexNormals(double[][] vertexNormals){
		if(vertexNormals.length==coords.size()){
			normals=new ArrayList<double[]>(coords.size());
			for (int i = 0; i < vertexNormals.length; i++) 
				normals.add(vertexNormals[i]);
		}
		else	System.err.println("number off normals differ from number of Vertices(nothing done)");
	}
	/** Art der diskreten Normalen-Berechnung. 
	 * @param normalType */
	public void setNormalType(Calculator.NormalType normalType) {
		this.normalType = normalType;
	}
	/** @return Art der diskreten Normalen-Berechnung. */
	public Calculator.NormalType getNormalType() {
		return normalType;
	}
	/** Ob Pseudonormalen benutzt werden sollen. 
	 * @param usePseudoNormals
	 */
	public void setUsePseudoNormals(boolean usePseudoNormals) {
		this.usePseudoNormals = usePseudoNormals;
	}
	/** @return Ob Pseudonormalen benutzt werden sollen. */
	public boolean isUsePseudoNormals() {
		return usePseudoNormals;
	}
	/** Diese Funktion dient dazu, die bestehenden Vertice mit neuen Koordinaten
	 *  zu belegen. Dabei bleibt die Struktur der Flaeche gleich.
	 *  Die Laenge und Reihenfolge des uebergebenen Arrays muss der Anzahl 
	 *  der bestehenden bereits gesetzten Vertice entsprechen.  
	 * @param c Koordinaten
	 */
	public void setCoords(double[][] c){
		if(c.length==coords.size()){
			coords=new ArrayList<double[]>(coords.size());
			for (int i = 0; i < c.length; i++) 
				coords.add(new double[]{c[i][0],c[i][1],c[i][2]});
		}
		else	System.err.println("number off coords differ from number of Vertices(nothing done)");
	}
	/** Liefert die Vertex-Koordinaten.
	 *  Dient dem Auslesen der unterteilten Flaeche.
	 * @return Koordinaten
	 */
	public double[][] getCoords(){
		double[][] co=new double[coords.size()][];
		for (int i = 0; i < coords.size(); i++) {
			co[i]=coords.get(i);
		}
		return co;
	}
	/** Liefert die Vertex-Bild-Koordinaten.
	 *  Dient dem Auslesen der unterteilten Flaeche.
	 * @return Koordinaten
	 */
	public double[][] getImageCoords(){
		double[][] co=new double[coords.size()][];
		for (int i = 0; i < coords.size(); i++) {
			co[i]=getPosition(i);
		}
		return co;
	}
	/** Liefert die intern verwendeten Daten der Vertex-Koordinaten der Flaeche. 
	 * Dient dem ungesicherten Zugriff auf die Koordinatendaten.
	 * Aenderungen dieser Daten beneinflussen direkt die eingelesene Flaeche.
	 * Aenderungen der Laenge der Liste sind verboten.  
	 * Kann benutzt werden um ohne unnoetiges kopieren der Daten,
	 * verformungen auf die Flaeche anzuwenden.
	 * @return Refferenz auf die VertexKoordinaten der Flaeche.
	 */
	public ArrayList<double[]> getOriginalCoords(){
		return coords;
	}
	/** Liefert die FacettenIndice der Flaeche.
	 *  Dient dem Auslesen der unterteilten Flaeche.
	 * @return Facetten-Indices
	 */
	public int[][] getFaceIndices(){
		int[][] ind=new int[faceIndis.size()][];
		for (int i = 0; i < faceIndis.size(); i++) {
			ind[i]=faceIndis.get(i);
		}
		return ind;
	}
	/** Liefert die NachbarFacetten der Flaeche.
	 * @return Facetten-NachbarIndices
	 */
	public int[][] getFaceNeighborIndices(){
		int[][] neighb=new int[faceIndis.size()][];
		for (int i = 0; i < faceIndis.size(); i++) {
			neighb[i]=faceNext.get(i);
		}
		return neighb;
	}
	/** Liefert die NachbarFacettenSeiten der Flaeche.
	 * @return Facetten-NachbarIndices
	 */
	public int[][] getFaceNeighborSideIndices(){
		int[][] neighb=new int[faceIndis.size()][];
		for (int i = 0; i < faceIndis.size(); i++) {
			neighb[i]=new int[]{faceNextSide.get(i)[0],faceNextSide.get(i)[1],faceNextSide.get(i)[2]};
		}
		return neighb;
	}
	/** Diese Liste ist als Faerbung (RGB) der Facette gedacht.
	 * Jedem Facettentyp wird eine Farbe zugeordnet.
	 * Dies dient der Veranschaulichung des Algorithmus.
	 * @return Facetten Farben nach Facetten-Typ
	 */
	public double[][] getTypeAsFaceColor(){
		double[][] co=new double[faceType.size()][];
		double[] fine=new double[]{.8,1,1};
		double[] end=new double[]{1,0,1};
		double[] handEnd=new double[]{.7,.5,.7};
		double[] way=new double[]{0,1,0};
		double[] handWay1=new double[]{0,.8,.5};
		double[] handWay2=new double[]{.5,.8,0};
		double[] node=new double[]{1,0,0};
		double[] handNode=new double[]{.7,.3,.3};		
		//double[] way=new double[]{0,.8,.5};
		for (int i = 0; i < faceType.size(); i++) {
			FaceType ft=faceType.get(i);
			switch (ft) {
			case END:							co[i]=end;				break;
			case HANDLED_END:		co[i]=handEnd;		break;
			case WAY:							co[i]=way;				break;
			case HANDLED_WAY:		co[i]=handWay1;	break;
			case HANDLED_WAY2:	co[i]=handWay2;	break;
			case NODE:						co[i]=node;				break;
			case HANDLED_NODE:	co[i]=handNode;		break;
			default:								co[i]=fine;				break;
			}
		}
		return co;
	}
	/** Setzt eine Toleranz mit der Entschieden wird, ob die Normalen zweier 
	 * Facetten einen zu grosen Winkel zueinander haben.
	 * Ist dies der Fall duerfen sie nicht im Paerchen zusammengefasst werde.
	 * @param curvTol Toleranz-Winkel in Grad.
	 */
	public void setCurvTol(double curvTol) {
		double cos=Math.cos(curvTol*Math.PI/180);
		this.curvTolSqSign = Math.signum(cos)*cos*cos;
		this.curvTol=cos;
	}
	/** Gibt die Toleranz zurueck mit der Entschieden wird,
	 * ob die Normalen zweier Facetten einen zu grosen Winkel zueinander haben.
	 * Waehre dies der Fall duerften sie nicht im Paerchen zusammengefasst werde.
	 * @return Toleranz-Winkel in Grad 
	 */
	public double getCurvTol() {
		return Math.acos(curvTol)*180/Math.PI;
	}
	/** Setzt die Toleranz mit der Entschieden wird, ob eine Kante 
	 * als zu lang gilt.
	 * @param tolerance
	 */
	public void setTolerance(double tolerance) {
		this.tolerance = tolerance;
	}
	/** Gibt die Toleranz zurueck mit der entschieden wird, 
	 * ob eine Kante zu lang ist. 
	 * @return Toleranz
	 */
	public double getTolerance() {
		return tolerance;
	}
	/** Unterteilt die bereits eingelesene Flaeche mit einem 
	 * Schritt meines Algorithmus.	 */
	public void subdivide() {
		if(needNormals&&normals==null)
				calcNormals(usePseudoNormals);
		if(normals==null)
			normals=new ArrayList<double[]>();
		calcLenAndFaceType();
		for (int face: badFaces) {
			FaceType type=faceType.get(face);
			if(type==FaceType.END){// splitten
				short badEdge=findNextBadEdge(face, (short)0);
				edgeSplit(face,badEdge,FaceType.HANDLED_END);
			}
			if(type==FaceType.WAY){// waySplit
				if(useSimple)	splitFaceTwice(face);
				else way(face);
			}
			if(type==FaceType.NODE){// face splitten
				faceSplit(face);
			}
		}
		cleanDataStructure();
	}
	/** Markiert in der bereits eingelesenen Flaeche den Grad einer Facette.
	 *  (Facetten mit 2 zu langen Kanten haben Grad 2) 
	 */
	public void mark() { 
		calcLenAndFaceType();
	}
	// ----------------- Extra DataStruckure ------------
	/** Steht fuer den Grad einer Facette und 
	 * ggf.die Regel mit der sie unterteilt wurde.
	 */
	static enum FaceType{
		FINE,END,WAY,NODE,
		HANDLED_END,
		HANDLED_WAY,
		HANDLED_WAY2,
		HANDLED_NODE
	}
	/** Datenstruktur zur bestimmung der 
	 * Nachbarschaftsverhaeltnisse der Facetten.
	 */
	private static final class VertexPair {
		final int l, h;
		VertexPair(int a, int b) {
			if(a<=b) { l=a; h=b; }
			else     { h=a; l=b; }
		}
		public boolean equals(Object obj) {
			if(this==obj) return true;
			try {
				final VertexPair p=(VertexPair)obj;
				return l == p.l && h == p.h;
			} catch(ClassCastException ex) {
				return false;
			}
		}
		public int hashCode() {
			return (l<<16)^h;
		}
	}
	/** Zyklischer Nachfolger einer Zahl von 0 bis 2. */
	private short next(short i){return (short)((i+1)%3); }
	/** Zyklischer Vorgaenger einer Zahl von 0 bis 2. */
	private short prev(short i){return (short)((i+2)%3); }
	public boolean check(boolean tell){
		faceType=new ArrayList<FaceType>(faceIndis.size());
		for (int i = 0; i < faceIndis.size(); i++) {
			faceType.add(FaceType.FINE);
		}
		int v=0;
		for (int[] nextFace:illegalVertNext ) {
			if(nextFace[0]!=-1||nextFace[1]!=-1)
				v++;
		}
		int s=0;
		int single=0;
		int singleFailure=0;
		int dualfailure=0;
		int multi=0;
		int n=0;
		for (int i = 0; i < faceIndis.size(); i++) {
			short [] nextSide=faceNextSide.get(i);
			int[] nextFace=faceNext.get(i);
			for(int k=0;k<3;k++){
				int nex= nextFace[k];
				short nexts=nextSide[k];
				if(nexts==-1){
					s++;
					faceType.set(i, increaseType(faceType.get(i)));
					continue;
				}
				if(nex==-1){
					n++;
					faceType.set(i, increaseType(faceType.get(i)));
					continue;
				}
				int nextnextFace=faceNext.get(nex)[nexts];
				short nextnextS=faceNextSide.get(nex)[nexts];
				if(nex==i){ //pruefe ob der Nachbar ich bin und ich auf mich zeige
					single++;
					if(nexts!=k){
						singleFailure++;
						faceType.set(i, increaseType(faceType.get(i)));
					}
					continue;
				}
				// pruefe ob der Nachbar nicht bin er aber auf mich zeigt
				if(nextnextFace==i){
					if(nextnextS!=k){
						dualfailure++;
						faceType.set(i, increaseType(faceType.get(i)));
					}
					continue;
				}
				multi++;
				faceType.set(i, increaseType(faceType.get(i)));
//				System.out.println("FaceCycleSubdivider.check("+k+")=multi nextFace("+i+")="+nex+" nextSide="+nexts);
			}
		}
		if(tell){
			System.out.println("JRSubdivider.check(Raender)"+single+" |Fehler:"+singleFailure);
			System.out.println("JRSubdivider.check(KantenFehler)"+dualfailure);
			System.out.println("JRSubdivider.check(multikanten)"+multi);
			System.out.println("JRSubdivider.check(freie illegale Vertices)"+v);
			System.out.println("JRSubdivider.check(illegale Vertex Refferenzen)"+s);
			System.out.println("JRSubdivider.check(freie Facetten Refferenzen)"+n);
			System.out.println("FaceCycleSubdivider.check(num Vertice:)"+coords.size());
			System.out.println("FaceCycleSubdivider.check(num Faces:)"+faceIndis.size());
			//TODO: es muessen gleiche Objekte sein sonnst wuerde die konnektivitaet sich nicht ueberlappen.
			// gleiche Anzahl Vertice aber ploetzlich mehr nachbarschaft!! -->> generateStrukture
		}
		if(v>0|s>0|n>0|singleFailure>0|dualfailure>0)
			return false;
		return true;
	}

}
