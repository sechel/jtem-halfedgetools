package de.jtem.halfedgetools.algorithm.adaptivesubdivision;
import de.jtem.halfedgetools.algorithm.adaptivesubdivision.util.Calculator;
import de.jtem.halfedgetools.algorithm.delaunay.decorations.HasLengthSquared;
import de.jtem.halfedgetools.jreality.node.JREdge;
import de.jtem.halfedgetools.jreality.node.JRFace;
import de.jtem.halfedgetools.jreality.node.JRVertex;

/** Dient der Unterstuetzung der Subdivider Klasse
 * @author Bernd Gonska
 */ 
class Helper <
V extends JRVertex<V,E,F>,
E extends JREdge<V,E,F> & HasLengthSquared,
F extends JRFace<V,E,F>
> {
	/** Prueft ob mehr als zwei Facetten am gleichen Kantenpaar liegen. 
	 * So etwas kann durch abschnueren der Flaeche entstehen. 
	 * @param e
	 * @return true, falls mehr als 2 Facetten daran liegen.
	 */
	static <
	V extends JRVertex<V,E,F>,
	E extends JREdge<V,E,F> & HasLengthSquared,
	F extends JRFace<V,E,F>
	>boolean lamellenTest(E e){
		int n=0;
		V v=e.getStartVertex();
		E end=e;
		e=e.getNextEdge().getOppositeEdge();
		int i=0;
		while(e!=end){			
			e=e.getNextEdge().getOppositeEdge();
			if(e.getStartVertex()==v){
				n++;
			}
			i++;
		}
		return (n!=1);		
	}
	/** Gibt die Valenz eines Vertex zurueck.
	 * Anzahl der abgehenden Kanten.
	 */
	static <
	V extends JRVertex<V,E,F>,
	E extends JREdge<V,E,F> & HasLengthSquared,
	F extends JRFace<V,E,F>
	>int valence(E e){
		E t=e;
		t=t.getNextEdge().getOppositeEdge();
		int v=1;
		while (t!=e){
			t=t.getNextEdge().getOppositeEdge();
			v++;
		}
		return v;
	}
	/** Mehrere Kriterien ob von einem Flip der Kante abzuraten ist.
	 * @param e Kante
	 * @return true, es soll nicht gelfipt werden.
	 */
	static<
	V extends JRVertex<V,E,F>,
	E extends JREdge<V,E,F> & HasLengthSquared,
	F extends JRFace<V,E,F>
	> boolean doNotFlip(E e){
		E eo=e.getOppositeEdge();
		E e1= e.getNextEdge();
		E eo1= eo.getNextEdge();
		V vs=e.getStartVertex();
		V vt=e.getTargetVertex();		
		V ve=e1.getTargetVertex();
		V vo=eo1.getTargetVertex();		
		if (valence(e)<=3) return true;
		if (valence(e.getOppositeEdge())<=3) return true;
		if(ve==vo)return true;// degeneriert die Kante
		if(vs==vt)return true;// degenerierte Kante
		return false;
	}
	/** Mehrere Kriterien ob von eine Edge Remove der Kante abzuraten ist.
	 * @param e Kante
	 * @return true, es soll nicht entfernt werden.
	 */
	static<
	V extends JRVertex<V,E,F>,
	E extends JREdge<V,E,F> & HasLengthSquared,
	F extends JRFace<V,E,F>
	> boolean doNotRemove(E e, double curvAngle){
		E eo=e.getOppositeEdge();
		E e1= e.getNextEdge();
		E eo1= eo.getNextEdge();
		V vs=e.getStartVertex();
		V vt=e.getTargetVertex();		
		V ve=e1.getTargetVertex();
		V vo=eo1.getTargetVertex();		
		if(e.getLeftFace()==null)return true;
		if(eo.getLeftFace()==null)return true;
		if (valence(e1)<=3) return true;
		if (valence(eo1.getOppositeEdge())<=3) return true;
		if (ve==vo)return true;// degeneriert die Kante
		if (vs==vt)return true;// degenerierte Kante
		if (touchesBoundary(e))return true;
		if (lamellenTest(e))return true; //Sonderfaelle durch abschnueren
		//if (badRemoveAngle(vs,e,curvAngle))return true;
		//if (badRemoveAngle(vt,e,curvAngle))return true;
		return false;
	}
	/** Prueft ob die Winkel zwischen Vertexnormale und angrenzenden
	 * Facetten nicht zu gross sind.
	 * Vergleicht dazu den Winkel zwischen der (pseudo-)Vertexnormalen
	 * und den Orthogonalgeraden der Facetten.
	 * @param vs Der Vertex.
	 * @param e  Eine anliegende Kante.
	 * @param curvAngle Die Toleranz.
	 * @return true, falls die Winkel nicht ok sind.
	 */
	static <
	V extends JRVertex<V,E,F>,
	E extends JREdge<V,E,F> & HasLengthSquared,
	F extends JRFace<V,E,F>
	>boolean badRemoveAngle(V vs, E e, double curvAngle){
		double[] vNormal=vs.normal;
		if(Calculator.lenSq(vNormal)==0)
			return false;
		double[] fNormal=e.getLeftFace().normal;
		if(Calculator.isCosSmalerToleranceSq(fNormal, vNormal, curvAngle,false))
			return false;
		E t=e.getNextEdge().getOppositeEdge();
		while (t!=e){
			fNormal=e.getLeftFace().normal;
			if(Calculator.isCosSmalerToleranceSq(fNormal, vNormal, curvAngle,false))
				return false;
			t=t.getNextEdge().getOppositeEdge();
		}
		return true;
	}
	/** Prueft ob der Winkel zwischen den Facetten-Normalen der 
	 * beiden angrenzenden Facetten nicht zu gross ist.
	 * @param e Die Kante an beiden Facetten.
	 * @param tolerance Toleranz
	 * @return true, falls der Winkel nicht ok ist.
	 */
	static<
	V extends JRVertex<V,E,F>,
	E extends JREdge<V,E,F> & HasLengthSquared,
	F extends JRFace<V,E,F>
	> boolean badPairAngle(E e, double tolerance){
		double[] normal1=e.getLeftFace().normal;
		double[] normal2=e.getOppositeEdge().getLeftFace().normal;
		if(Calculator.isCosSmalerToleranceSq(normal1, normal2, tolerance,false))
			return false;
		return true;
	}
	/** Gibt zurueck, ob der Ziel-Vertex der Kanten am Rand liegt.
	 * @param e Die Kante. 
	 * @return true, liegt am Rand.
	 */
	static <
	V extends JRVertex<V,E,F>,
	E extends JREdge<V,E,F> & HasLengthSquared,
	F extends JRFace<V,E,F>
	>boolean isBoundaryVertex(E e){
		if(e.getLeftFace()==null)
			return true;
		E t=e.getNextEdge().getOppositeEdge();
		while (t!=e){
			if(t.getLeftFace()==null)
				return true;
			t=t.getNextEdge().getOppositeEdge();
			}
		return false;
	}
	/** Gibt an, ob einer der beiden Vertice der Kante am Rand liegt.
	 * @param e Kante
	 * @return true liegt am Rand.
	 */
	static <
	V extends JRVertex<V,E,F>,
	E extends JREdge<V,E,F> & HasLengthSquared,
	F extends JRFace<V,E,F>
	>boolean touchesBoundary(E e){
		if(isBoundaryVertex(e)|| isBoundaryVertex(e.getOppositeEdge()))
			return true;
		return false;
	}
	/** Prueft anhand unterschiedlicher Kriterien,  
	 * ob auf eine Paarbildung verzichtet werden soll. 
	 * @param midSide
	 * @param tolerancePairAngleSq
	 * @return true, falls keine Paarbildung empfohlen.
	 */
	static <
	V extends JRVertex<V,E,F>,
	E extends JREdge<V,E,F> & HasLengthSquared,
	F extends JRFace<V,E,F>
	>boolean doNotPair(E midSide,double tolerancePairAngleSq){
		if(badPairAngle(midSide, tolerancePairAngleSq)) return true;
		return false;
	}
	/** Das Quadrat der Hoehe h des Dreiecks ueber dieser Seite.
	 * Berechnet aus den Quadraten der Kantenlaengen.
	 * @param e
	 * @return Hoehe der Facette ueber der Kante.
	 */
	static<
	V extends JRVertex<V,E,F>,
	E extends JREdge<V,E,F> & HasLengthSquared,
	F extends JRFace<V,E,F>
	> double altitudeSq(E e){
		double cc=e.getLengthSquared();
		double bb=e.getNextEdge().getLengthSquared();
		double aa=e.getPreviousEdge().getLengthSquared();
		return bb*(4*bb*cc-(bb+cc-aa)*(bb+cc-aa))/((bb+cc-aa)*(bb+cc-aa));
	} 
}
