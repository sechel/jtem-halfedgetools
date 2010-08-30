package de.jtem.halfedgetools.functional;

import static de.jtem.halfedge.util.HalfEdgeUtils.constructFaceByVertices;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;

public class FunctionalTestData {

	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void createCombinatorialTriangle(HDS hds) {
	
		V v1 = hds.addNewVertex();
		V v2 = hds.addNewVertex();
		V v3 = hds.addNewVertex();
	
		constructFaceByVertices(hds, v1, v2, v3);
	}
		
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void createCombinatorialTetrahedron(HDS hds) {

		V v1 = hds.addNewVertex();
		V v2 = hds.addNewVertex();
		V v3 = hds.addNewVertex();
		V v4 = hds.addNewVertex();

		constructFaceByVertices(hds, v1, v2, v3);
		constructFaceByVertices(hds, v3, v2, v4);
		constructFaceByVertices(hds, v1, v3, v4);
		constructFaceByVertices(hds, v1, v4, v2);
	}
	
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void createCombinatorialBiPyrTriangle(HDS hds) 
	{

		V v1 = hds.addNewVertex();
		V v2 = hds.addNewVertex();
		V v3 = hds.addNewVertex();
		V v4 = hds.addNewVertex();
		V v5 = hds.addNewVertex();

		constructFaceByVertices(hds, v1, v2, v4);
		constructFaceByVertices(hds, v2, v3, v4);
		constructFaceByVertices(hds, v3, v1, v4);

		constructFaceByVertices(hds, v1, v3, v5);
		constructFaceByVertices(hds, v3, v2, v5);
		constructFaceByVertices(hds, v2, v1, v5);
	}
 
	
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void createCombinatorialOctahedron(HDS hds) 
	{

		V v1 = hds.addNewVertex();
		V v2 = hds.addNewVertex();
		V v3 = hds.addNewVertex();
		V v4 = hds.addNewVertex();
		V v5 = hds.addNewVertex();
		V v6 = hds.addNewVertex();

		constructFaceByVertices(hds, v1, v3, v5);
		constructFaceByVertices(hds, v3, v2, v5);
		constructFaceByVertices(hds, v2, v4, v5);
		constructFaceByVertices(hds, v4, v1, v5);

		constructFaceByVertices(hds, v3, v1, v6);
		constructFaceByVertices(hds, v1, v4, v6);
		constructFaceByVertices(hds, v4, v2, v6);
		constructFaceByVertices(hds, v2, v3, v6);
	}
	
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void createCombinatorialPyrWithBnd(HDS hds) 
	{
		V v1 = hds.addNewVertex();
		V v2 = hds.addNewVertex();
		V v3 = hds.addNewVertex();
		V v4 = hds.addNewVertex();
		V v5 = hds.addNewVertex();
	
		constructFaceByVertices(hds, v1, v3, v5);
		constructFaceByVertices(hds, v3, v2, v5);
		constructFaceByVertices(hds, v2, v4, v5);
		constructFaceByVertices(hds, v4, v1, v5);
	}

}
