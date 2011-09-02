package de.jtem.halfedgetools.nurbs;

import java.util.Comparator;

public class EventPointYComparator implements Comparator<EventPoint> {
	
	@Override
	public int compare(EventPoint p, EventPoint q) {
		if(p.point[1] > q.point[1] || (p.point[1] == q.point[1] && p.point[0] < q.point[0])){
			return -1;
		}
		else if(p.point[1] == q.point[1] && p.point[0] == q.point[0]){
			return 0;
		}
		else{
			return 1;
		}
	}

}