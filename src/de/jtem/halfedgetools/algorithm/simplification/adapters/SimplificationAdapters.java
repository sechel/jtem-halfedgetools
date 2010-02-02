package de.jtem.halfedgetools.algorithm.simplification.adapters;

import de.jtem.halfedge.Face;

public class SimplificationAdapters {

      
       public interface NormalAdapter<F extends Face<?, ?, F>>{
               public double[] getNormal(F f);
       }
       
       public interface AreaAdapter<F extends Face<?, ?, F>>{
               public double getArea(F f);
       }
       
}
