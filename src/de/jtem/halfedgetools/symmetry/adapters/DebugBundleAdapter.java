package de.jtem.halfedgetools.symmetry.adapters;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;

import de.jtem.halfedge.Node;
import de.jtem.halfedgetools.jreality.Bundle;
import de.jtem.halfedgetools.jreality.Bundle.DisplayType;
import de.jtem.halfedgetools.jreality.adapter.LabelAdapter2Ifs;
public class DebugBundleAdapter implements LabelAdapter2Ifs<Node<?, ?, ?>>{
	private final AdapterType typ;
	private static String newline = System.getProperty("line.separator");
	private DecimalFormat twoPlaces = new DecimalFormat("0.00");

	public DebugBundleAdapter(AdapterType typ) {
		this.typ=typ;
	}
	public AdapterType getAdapterType() {
		return typ;
	}
	/** the color of the node
	 */
	@SuppressWarnings("unchecked")
	public String getLabel(Node<?, ?, ?> node) {
		
		String s = "";
		Class c = node.getClass();
		
		for(Method m : c.getMethods()) {
			if(m.isAnnotationPresent(Bundle.class)) {
				if(m.getAnnotation(Bundle.class).display() == DisplayType.Debug) {
					String toAdd = "";
					String name = m.getAnnotation(Bundle.class).name();
//					BundleType bt = m.getAnnotation(Bundle.class).type();
					
					Object ret = null;
					try {
						ret = m.invoke(node, (Object[])null);
					} catch (IllegalArgumentException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IllegalAccessException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (InvocationTargetException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
	//				switch(bt){
	//					case Value:
	//						toAdd = name + " " + twoPlaces.format(ret) + newline;
	//					default:
	//						toAdd = name + " " + ret.toString() + newline;			
	//				}
	
					if(ret instanceof Double) {
						toAdd = name + " " + twoPlaces.format(ret) + newline;
					} if(ret == null){
	
					} else {
						toAdd = name + " " + ret.toString() + newline;
					}
					s += toAdd;
				}
			}
		}
		
		return s;
	}
}
