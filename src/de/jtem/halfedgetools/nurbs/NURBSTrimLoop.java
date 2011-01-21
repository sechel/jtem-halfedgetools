package de.jtem.halfedgetools.nurbs;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class NURBSTrimLoop {
	
	protected List<NURBSCurve> curves = new LinkedList<NURBSCurve>();
	protected List<double[]> domains = new LinkedList<double[]>();
	
	public NURBSTrimLoop() {
	}
	
	public NURBSTrimLoop(List<NURBSCurve> c, List<double[]> d) {
		curves = c;
		domains = d;
	}

	public void addCurve(NURBSCurve c, double[] domain){
		curves.add(c);
		domains.add(domain);
	}
	
	public void removeCurve(NURBSCurve c) {
		int i = curves.indexOf(c);
		if (i == -1) return;
		curves.remove(c);
		domains.remove(i);
	}
	
	
	public String toString(){
		String str = "TRIMM LOOP: " + "\n";
		for (NURBSCurve c : curves) {
			int i = curves.indexOf(c);
			str += "domain: " + Arrays.toString(domains.get(i));
			str += "\n"+ c.toString();
		}		
		return str;
	}
}
