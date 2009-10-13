package de.jtem.halfedgetools.algorithm.subdivision.util;
public interface RnRmFunktion {
	public double[] map(double[] param);
	/** Normiert den gegebenen Vektor (0 bleibt 0).
	 * Beliebige Dimension moeglich.
	 *  @author Bernd Gonska
	 */
	public static class SphereProjector implements RnRmFunktion{
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
		public double[] map(double[] param) {
			double[] ret=new double[param.length];
			for (int i = 0; i < ret.length; i++) 
				ret[i]=param[i];
			return ret;
		}
	}
}
