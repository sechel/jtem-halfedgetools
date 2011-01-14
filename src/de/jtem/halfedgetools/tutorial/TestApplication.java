package de.jtem.halfedgetools.tutorial;

import de.jreality.plugin.JRViewer;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.plugin.VectorFieldManager;

public class TestApplication {

	public static void main(String[] args) {
		// Halfedge abstrakt
		HalfEdgeDataStructure<?, ?, ?> hds = null;
		
		// Halfedge konkret
		VHDS vhds = new VHDS();
		VV v = vhds.addNewVertex();
		VE e = vhds.addNewEdge();
		VE e2 = vhds.addNewEdge();
		VF f = vhds.addNewFace();
		e.setTargetVertex(v);
		e2.setTargetVertex(v);
		e.linkOppositeEdge(e2);
		e.linkNextEdge(e2);
		e.setLeftFace(f);
		
		// Das Adapter Konzept
		TestPositionAdapter pa = new TestPositionAdapter();
		
		// Das Adapter Set
		AdapterSet a = new AdapterSet(pa);
		
		// Die generischen Adapter
		a.addAll(AdapterSet.createGenericAdapters());
		
		// Ein Beispiel Algorithmus
		double area = TestAlgorithm.doSomething(vhds, a);
		
		// Beispiel Applikation mit Plugin
		JRViewer jv = new JRViewer();
		jv.addContentUI();
		jv.registerPlugin(new TestPlugin());
		jv.registerPlugin(new VectorFieldManager());
		jv.registerPlugin(new TestVisualizer());
		jv.startup();
	}
	
}
