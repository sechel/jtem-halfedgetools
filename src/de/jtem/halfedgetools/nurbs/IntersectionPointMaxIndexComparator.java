package de.jtem.halfedgetools.nurbs;

import java.util.Comparator;

public class IntersectionPointMaxIndexComparator implements
	Comparator<IntersectionPoint>{
	@Override
	public int compare(IntersectionPoint o1, IntersectionPoint o2) {
		return (int)Math.signum(o1.indexOnMaxCurve-o2.indexOnMaxCurve);
	}
}
