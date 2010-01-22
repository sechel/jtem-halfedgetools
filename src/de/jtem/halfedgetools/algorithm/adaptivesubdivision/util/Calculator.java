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

package de.jtem.halfedgetools.algorithm.adaptivesubdivision.util;
/** Diese Klasse dient der Unterstuetzung der Klassen FaceCycleSubdivider und Remover.
 * Sie beinhaltet Vektorrechnung des R3,
 * Spline-Interpolation, Methoden zur Normalenberechnung 
 * und einige Tests gegen Geometrische Situationen.
 * 
 * @author Bernd Gonska
 */
public class Calculator {
	public static enum NormalType{AREA,AREA_INVERSE,ANGLE_WEIGHT,EQUAL_WEIGHT};
	//  --------------- Grundrechenarten des R3 ---------------
	/** 
	 * Gibt den Vektor normiert zurueck.
	 * Ist der Vekor nicht aus R3\{0} wird (0,0,0) zurueckgegeben.  
	 * @param v Ein Vektor der Laenge 3.
	 * @return v/|v| 
	 */
	public static double[] normalize(double[] v){
		if(isNotRealValued(v))
			return new double[]{0,0,0};
		double lensq=lenSq(v);
		if(lensq<1e-10) return new double[]{0,0,0};
		return times(1./Math.sqrt(lensq), v);
	} 
	/** Differenz zweier Vektoren aus R3
	 * @param u
	 * @param v
	 * @return u-v
	 */
	public static double[] sub(double[] u,double[] v){ // u - v
		return new double[]{u[0]-v[0],u[1]-v[1],u[2]-v[2]};
	}
	/** Kreutzprodukt(Vektorprodukt) zweier Vektoren aus R3
	 * @param u
	 * @param v
	 * @return u-v
	 */
	public static double[] cros(double[] u,double[] v){ 
		return new double[]{u[1]*v[2]-u[2]*v[1],u[2]*v[0]-u[0]*v[2],u[0]*v[1]-u[1]*v[0]};
	}
	/** Liefert das Negativ eines Vektors aus R3
	 * @param u
	 * @return -u
	 */
	public static double[] neg(double[] u){
		return new double[]{-u[0],-u[1],-u[2]};
	}
	/** Abstand zweier Vektoren aus R3
	 * @param u
	 * @param v
	 * @return |u-v|
	 */
	public static double dist(double[] u,double[] v){ 
		return len(sub(u, v));
	}
	/** Quadrat des Abstandes zweier Vektoren aus R3
	 * @param u
	 * @param v
	 * @return |u-v|*|u-v|
	 */
	public static double distSq(double[] u,double[] v){
		return lenSq(sub(u, v));
	}
	/** Die Laenge eines Vektors aus R3
	 * @param v
	 * @return |v|
	 */
	public static double len(double[] v){
		return  Math.sqrt(lenSq(v));
	}
	/** Das Quadrat der Laenge eines Vektors aus R3
	 * @param v
	 * @return |v|*|v|
	 */
	public static double lenSq(double[] v){
		return  v[0]*v[0]+v[1]*v[1]+v[2]*v[2];
	}
	/** Das vielfache eines Vektors aus R3
	 * @param fac
	 * @param u
	 * @return fac*u
	 */
	public static double[] times(double fac, double[] u){
		return new double[]{fac*u[0],fac*u[1],fac*u[2]};
	}
	/** Die Summe zweier Vektoren aus R3
	 * @param u
	 * @param v
	 * @return u+v
	 */
	public static double[] add(double[] u,double[] v){
		return new double[]{u[0]+v[0],u[1]+v[1],u[2]+v[2]};
	}
	/** das Skalarprodukt(Innere Produkt) zweier Vektoren aus R3 
	 * @param u
	 * @param v
	 * @return <u,v>
	 */
	public static double scalarProd(double[] u,double[] v){
		double d=0;
		for (int i = 0; i < u.length; i++) 		d+=u[i]*v[i];
		return d;
	}
	/** Setzt einem Vektor die Koordinaten eines anderen Vektors. Beide muessen aus R3 sein.
	 * @param u
	 * @param v
	 * u:=v 
	 */
	public static void set(double[] u,double[] v){
		for (int i = 0; i < v.length; i++) {
			u[i]=v[i];
		}
	}
	/** Linear-kombination zweiern Vektoren aus R3.
	 * @param uWeight
	 * @param u
	 * @param vWeight
	 * @param v
	 * @return uWeight*u + vWeight*v
	 */
	public static double[] linearCombination(double uWeight, double[] u,double vWeight,double[] v){// uWeight*u+vWeigth*v
		return new double[]{uWeight*u[0]+vWeight*v[0],uWeight*u[1]+vWeight*v[1],uWeight*u[2]+vWeight*v[2]}; 
	}
	/** Linear-kombination dreier Vektoren aus R3.
	 * @param uWeight
	 * @param u
	 * @param vWeight
	 * @param v
	 * @param wWeight
	 * @param w
	 * @return uWeight*u + vWeight*v + wWeight*w
	 */
	public static double[] linearCombination(double uWeight, double[] u,double vWeight,double[] v,double wWeight,double[] w){// uWeight*u+vWeigth*v+wWeigth*w
		return new double[]{uWeight*u[0]+vWeight*v[0]+wWeight*w[0],uWeight*u[1]+vWeight*v[1]+wWeight*w[1],uWeight*u[2]+vWeight*v[2]+wWeight*w[2]};
	}
	/** Linear-kombination aus vier Vektoren des R3.
	 * @param uWeight
	 * @param u
	 * @param vWeight
	 * @param v
	 * @param wWeight
	 * @param w
	 * @param xWeight
	 * @param x
	 * @return uWeight*u + vWeight*v + wWeight*w + xWeight*x
	 */
	public static double[] linearCombination(double uWeight, double[] u,double vWeight,double[] v,double wWeight,double[] w,double xWeight,double[] x){
		return new double[]{
				uWeight*u[0]+vWeight*v[0]+wWeight*w[0]+xWeight*x[0],
				uWeight*u[1]+vWeight*v[1]+wWeight*w[1]+xWeight*x[1],
				uWeight*u[2]+vWeight*v[2]+wWeight*w[2]+xWeight*x[2]};
	}
	// ------------------- Tests gegen Geometrische Situationen -------------------------
	/** Gibt an ob Winkel zwischen den Vektoren kleiner ist, als die Toleranz.
	 * Die Toleranz wird gegeben als Quadrat des cosinus des Winkels.
	 * Dieses hat das Vorzeichen wie der Cosinus. 
	 * Die Vektoren muessen nicht normiert sein.
	 * Diese Funktion stellt eine Erweiterung von isCosSmalerTolerance
	 * auf nicht normierte Vektoren dar.   
	 * @param u
	 * @param v
	 * @param tolSqSign  
	 * @return true, wenn der Winkel(u,v) ok ist.
	 */
	public static boolean isCosSmalerToleranceSqSign(double[] u,double[] v,double tolSqSign){
		double d=scalarProd(u, v);
		double lenSqU=lenSq(u);
		double lenSqV=lenSq(v);
		return d*d*Math.signum(d)<tolSqSign*lenSqU*lenSqV;
	}
	
	/** Gibt an ob der Kosinus des Winkels zwischen den Vektoren groesser 
	 * ist als die Toleranz.
	 * Die Vektoren muessen normiert sein.
	 * @param u
	 * @param v
	 * @param tol  
	 * @return true, wenn der Winkel(u,v) ok ist.
	 */
	static boolean isCosSmalerTolerance(double[] u,double[] v,double tol){
		return scalarProd(u, v)<tol;
	}
	/** Gibt an ob das Quadrat des Kosinus des Winkel zwischen den Geraden, die durch die Vektoren 
	 * gegeben sind, groesser ist als die Toleranz.
	 * Die Toleranz muss positiv sein.
	 * Die Vektoren muessen nicht normiert sein.
	 * @param u
	 * @param v
	 * @param tolSq  
	 * @param usePseudoNormals Gibt an, ob die Vektoren nicht normiert sind.
	 * @return true, wenn der Winkel(u,v) ok ist.
	 */
	public static boolean isCosSmalerToleranceSq(double[] u,double[] v,double tolSq, boolean usePseudoNormals){
		double d=scalarProd(u, v);
		if(usePseudoNormals){
			double lenSqU=lenSq(u);
			double lenSqV=lenSq(v);
			return d*d<tolSq*lenSqU*lenSqV;
		}
		else return d*d<tolSq;
	}
	/** Gibt an, ob die geflippte Kante kuerzer ist als die originale.
	 * v1,v2,v3 bilden das eine, v1,v3,v4 das andere Dreieck im Ursprungszustand. 
	 * @param v1
	 * @param v2
	 * @param v3
	 * @param v4
	 * @return true, falls die geflippte Kante kuerzer ist.
	 */
	public static double shouldFlip(double[] v1,double[] v2,double[] v3,double[] v4){
		double now=distSq(v1,v3);
		double alt=distSq(v2,v4);
		return now-alt;
	}
	/** Gibt an, ob die Dreiecke die aus dem Flip hervorgehen,
	 * in einem Winkel kleiner als PI/2 zueinander stehen. 
	 * c,d,b bilden das eine, c,a,d das andere Dreieck im Ursprungszustand.
	 * @param a
	 * @param b
	 * @param c
	 * @param d
	 * @return true, falls der Flip eine Tasche bilden wuerde.
	 */
	public static boolean flipCreatesOverlap(double[] a,double[] b,double[] c,double[] d){
		double[] ac=sub(c, a);
		double[] ab=sub(b, a);
		double[] da=sub(a, d);
		double[] c_=projectedVectorPseudoNormal(ab, ac);
		return scalarProd(c_, da)<0;
	}
	/** Vergleicht die Laenge der gemeinsamen Seite mit der, innerhalb der Flaeche gelflipten Alternative.
	 * Die innerlich geflippte Kante darf dabei ausserhalb der beiden Dreiecke liegen. 
	 * Die innerlich geflipte Kante verlaeuft immer innerhalb der Ebenen in denen die beiden Dreiecke liegen.   
	 * v1,v2,v3 bilden das eine, v1,v3,v4 das andere Dreieck im Ursprungszustand. 
	 * Falls die neue Kante jedoch Laenge 0 hat, degeneriert sie Dreiecke. Deshalb wird dann 0 zureuckgegeben
	 * @param v1
	 * @param v2
	 * @param v3
	 * @param v4
	 * @return Differenz der Quadrate der Laengen. Kleiner 0 bedeutet Vorteil beim Flippen.
	 */
	public static double flipIntrinsicLenValue(double[] v1,double[] v2,double[] v3,double[] v4){
		double aa=distSq(v1,v2);
		double bb=distSq(v2,v3);
		double cc=distSq(v1,v3);
		double ee=distSq(v1, v4);
		double ff  =distSq(v4, v3);
		// intrinsische Distanz(Quadriert)
		double p1p1=(aa+cc-bb)*(aa+cc-bb)/(4*cc);
		double p2p2=(ee+cc-ff)*(ee+cc-ff)/(4*cc);
		double h1h1=aa-p1p1;
		double h2h2=ee-p2p2;
		double dd=h1h1+h2h2+2*Math.sqrt(h1h1*h2h2)+(aa-bb-ee+ff)*(aa-bb-ee+ff)/(4*cc);
		// flippen wenn <0:
		if(dd==0)
			return 0;// du sollst nicht flippen!
		return cc-dd;
	}
	// --------------- Normalen berechnung -----------------
	/** Setzt einem Vertex in der mitgegebenen Normale den Anteil den es aus dieser Facette erhaelt.
	 *  Das Gewicht der Facettennormale ist immer gleich. 
	 *  Die Summe Dieser Anteile faehrt zu einer Pseudo-Normalen.
	 *  Diese Funktion gibt nebenbei eine Facettennormale zurueck.  
	 * @param vNormal Normale die den Anteil erhaelt
	 * @param v1 Der Vertex der Normale.
	 * @param v2 Ein anderer Vertex der Facette. 
	 * @param v3 Ein anderer Vertex der Facette.
	 * @return Facettennormale
	 */
	public static double[] setEqualWeightNormal(double[] vNormal, double[] v1, double[] v2, double[] v3){
		double[] u=sub(v2,v1);
		double[] v=sub(v3,v1);
		double[] n=cros(u,v);
		n=normalize(n);
		set(vNormal,add(vNormal,n));
		return n;
	}
	/** Setzt einem Vertex in der mitgegebenen Normale den Anteil den es aus dieser Facette erhaelt.
	 *  Das Gewicht der Facettennormale entspricht dem an dem Vertex anliegenden Winkel. 
	 *  Die Summe Dieser Anteile faehrt zu einer Pseudo-Normalen.
	 *  Diese Funktion gibt nebenbei eine Facettennormale zurueck.  
	 * @param vNormal Normale die den Anteil erhaelt
	 * @param v1 Der Vertex der Normale.
	 * @param v2 Ein anderer Vertex der Facette.
	 * @param v3 Ein anderer Vertex der Facette.
	 * @return Facettennormale
	 */
	public static double[] setAngleWeightNormal(double[] vNormal, double[] v1, double[] v2, double[] v3){
		double[] u=sub(v2,v1);
		double[] v=sub(v3,v1);
		double[] n=cros(u,v);
		n=normalize(n);
		if(lenSq(n)==0)
			return n; // add nothing
		double[] w=sub(v3, v2);
		double l1l1=distSq(u,v);//laengen
		double l2=dist(neg(u),w);
		double l3=dist(v, w);
		double a1=Math.acos((l2*l2+l3*l3-l1l1)/(2*l2*l3));//Winkel
		if (isNotReal(a1))
			return n; // add nothing
		n=times(a1, n);// gewichten
		set(vNormal,add(vNormal,n));
		return n;
	}
	/** Setzt einem Vertex in der mitgegebenen Normale den Anteil den es aus dieser Facette erhaelt.
	 *  Die Summe Dieser Anteile faehrt zu einer Pseudo-Normalen.
	 *  Das Gewicht entspricht dem (halben)Flaecheninhalt der Facette. 
	 *  Diese Funktion gibt nebenbei eine pseudo-Facettennormale zurueck.  
	 * @param vNormal Normale die den Anteil erhaelt
	 * @param v1 Der Vertex der Normale.
	 * @param v2 Ein anderer Vertex der Facette. 
	 * @param v3 Ein anderer Vertex der Facette.
	 * @return pseudo-Facettennormale
	 */
	public static double[] setAreaWeigthNormal(double[] vNormal, double[] v1, double[] v2, double[] v3){
		double[] u=sub(v2,v1);
		double[] v=sub(v3,v1);
		double[] n=cros(u,v);
		if(isNotRealValued(n))
			return new double[]{0,0,0}; // add nothing
		set(vNormal,add(vNormal,n));
		return n;
	}
	/** Setzt einem Vertex in der mitgegebenen Normale den Anteil den es aus dieser Facette erhaelt.
	 *  Die Summe Dieser Anteile faehrt zu einer Pseudo-Normalen.
	 *  Das Gewicht entspricht dem inversen des (halben) Flaecheninhaltes der Facette. 
	 *  Diese Funktion gibt nebenbei eine pseudo-Facettennormale zurueck.  
	 * @param vNormal Normale die den Anteil erhaelt
	 * @param v1 Der Vertex der Normale.
	 * @param v2 Ein anderer Vertex der Facette. 
	 * @param v3 Ein anderer Vertex der Facette.
	 * @return pseudo-Facettennormale
	 */
	public static double[] setAreaInverseWeightNormal(double[] vNormal, double[] v1, double[] v2, double[] v3){
		double[] u=sub(v2,v1);
		double[] v=sub(v3,v1);
		double[] n=cros(u,v);
		if(lenSq(n)==0||isNotRealValued(n))
			return new double[]{0,0,0}; // add nothing
		n=times(1./lenSq(n), n);// invert Area
		set(vNormal,add(vNormal,n));
		return n;
	}
	/** Gibt eine Normierte Normale der Facette zurueck.
	 * Falls so eine Normale nicht existiert gibt er (0,0,0) zurueck.
	 * @param v1 Ein Vertex der Facette.
	 * @param v2 Ein Vertex der Facette.
	 * @param v3 Ein Vertex der Facette.
	 * @return Facettennormale
	 */
	public static double[] getFaceNormal(double[] v1, double[] v2, double[] v3){
		double[] u=sub(v2,v1);
		double[] v=sub(v3,v1);
		double[] n=cros(u,v);
		n=normalize(n);
		return n;
	}
	// -------------------- Spline Interpolation ---------------
	/** Berechnet die Projektion von v auf die Ebene gegeben durch die Normale n.
	 * Ist n gleich (0,0,0) so gibt er (0,0,0) zurueck.    
	 * @param n Die Normale.
	 * @param v Der zu projezierende Vektor.
	 * @return Projektion von v auf Orthogonalebene zu n.
	 */
	public static double[] projectedVector(double[] n,double[] v){
		double nv=scalarProd(n, v);
		double[] sub=times(nv,n);
		return sub(v,sub); 
	}
	/** Berechnet die Projektion von v auf die Ebene gegeben durch die Normale n.
	 * Ist n gleich (0,0,0) so gibt er (0,0,0) zurueck.    
	 * @param n Die pseudo-Normale.
	 * @param v Der zu projezierende Vektor.
	 * @return Projektion von v auf Orthogonalebene zu n.
	 */
	public static double[] projectedVectorPseudoNormal(double[] n,double[] v){
		if(isNotReal(1./lenSq(n))) return new double[]{0,0,0};
		double nv=scalarProd(n, v);
		double[] sub=times(nv/lenSq(n),n);
		return sub(v,sub); 
	}
	/** Interpoliert einen Vertex in der Mitte eines Splines, 
	 * der durch die gegebenen Punkte geht, und zu den dortigen Normalen passt.
	 * Kann auch mit (0,0,0)-Normalen umgehen.
	 * @param uCoord Kordinaten vom Vertex u.
	 * @param vCoord Kordinaten vom Vertex v.
	 * @param uNormal Vertex-Normale zum Vertex u.
	 * @param vNormal Vertex-Normale zum Vertex v.
	 * @return Spline Interpolation zwischen u und v.
	 */
	public static double[] interpolate(double[] uCoord,double[] vCoord,double[] uNormal, double[] vNormal) {
		double[] edge=new double[]{vCoord[0]-uCoord[0],vCoord[1]-uCoord[1],vCoord[2]-uCoord[2]};
		double[] ant0=projectedVector(uNormal, edge);
		double[] ant1=projectedVector(vNormal, edge);
		double[] a0=uCoord;// koeffitienten
		double[] a1=ant0;
		double[] a2=linearCombination(3, edge, -1,ant1 , -2, ant0);
		double[] a3=linearCombination(-2, edge, 1,ant1 , 1, ant0);
		double[] ret=new double[]{
				a0[0]+a1[0]/2+a2[0]/4+a3[0]/8,
				a0[1]+a1[1]/2+a2[1]/4+a3[1]/8,
				a0[2]+a1[2]/2+a2[2]/4+a3[2]/8,
				};
		return ret;
	}
	/** Interpoliert einen Vertex in der Mitte eines Splines 
	 * der durch die gegebenen Punkte geht und zu den dortigen Normalen passt.
	 * Kann auch mit pseudo-Normalen rechnen.  
	 * Kann auch mit (0,0,0)-Normalen umgehen.
	 * @param uCoord Kordinaten vom Vertex u.
	 * @param vCoord Kordinaten vom Vertex v.
	 * @param uNormal Pseudo-Vertex-Normale zum Vertex u.
	 * @param vNormal Pseudo-Vertex-Normale zum Vertex v.
	 * @return Spline Interpolation zwischen u und v.
	 */
	public static double[] interpolateWithPseudoNormal(double[] uCoord,double[] vCoord,double[] uNormal, double[] vNormal) {
		double[] edge=new double[]{vCoord[0]-uCoord[0],vCoord[1]-uCoord[1],vCoord[2]-uCoord[2]};
		double[] ant0=projectedVectorPseudoNormal(uNormal, edge);
		double[] ant1=projectedVectorPseudoNormal(vNormal, edge);
		double[] a0=uCoord;// koeffitienten
		double[] a1=ant0;
		double[] a2=linearCombination(3, edge, -1,ant1 , -2, ant0);
		double[] a3=linearCombination(-2, edge, 1,ant1 , 1, ant0);
		double[] ret=new double[]{
				a0[0]+a1[0]/2+a2[0]/4+a3[0]/8,
				a0[1]+a1[1]/2+a2[1]/4+a3[1]/8,
				a0[2]+a1[2]/2+a2[2]/4+a3[2]/8,
				};
		return ret;
	}
	/** Testet ob der Vektor nicht aus R3 ist. 
	 * @param v
	 */
	public static boolean isNotRealValued(double[] v){
		for (int i = 0; i < v.length; i++) {
			if(Double.isInfinite(v[i])) return true;
			if(Double.isNaN(v[i])) return true;
		}
		return false;
	}
	/** Testet ob ein Wert keine reelle Zahl ist
	 * @param v
	 */
	public static boolean isNotReal(double v){
		if(Double.isInfinite(v)) return true;
		if(Double.isNaN(v)) return true;
		return false;
	}
}

