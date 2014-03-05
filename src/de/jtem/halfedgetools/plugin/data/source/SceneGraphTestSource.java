package de.jtem.halfedgetools.plugin.data.source;

import de.jreality.geometry.Primitives;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AbstractAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.generic.BaryCenter3d;
import de.jtem.halfedgetools.plugin.data.DataSourceProvider;
import de.jtem.jrworkspace.plugin.Plugin;

public class SceneGraphTestSource extends Plugin implements DataSourceProvider {

	public SceneGraphTestSource() {
	}

	private class TestGeometryAdapter extends AbstractAdapter<SceneGraphNode> {

		private IndexedFaceSet
			geometry = Primitives.coloredCube();
		
		public TestGeometryAdapter() {
			super(SceneGraphNode.class, true, false);
		}

		@Override
		public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
			return true;
		}
		
		@Override
		public <
			V extends Vertex<V, E, F>, 
			E extends Edge<V, E, F>, 
			F extends Face<V, E, F>, 
			N extends Node<V, E, F>
		> SceneGraphNode get(N n, AdapterSet a) {
			SceneGraphComponent root = new SceneGraphComponent("Cube at node " + n.getIndex());
			MatrixBuilder mb = MatrixBuilder.euclidean();
			mb.translate(a.getD(BaryCenter3d.class, n));
			mb.scale(0.05);
			mb.assignTo(root);
			root.setGeometry(geometry);
			return root;
		}
		
	}
	
	@Override
	public AdapterSet getDataSources() {
		return new AdapterSet(new TestGeometryAdapter());
	}

}
