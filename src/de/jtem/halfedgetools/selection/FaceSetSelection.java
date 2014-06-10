package de.jtem.halfedgetools.selection;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;

public class FaceSetSelection {

	private static Logger
		log = Logger.getLogger(FaceSetSelection.class.getName());
	
	public FaceSetSelection() {
		vertexSelection = new VertexSelection();
		edgeSelection = new EdgeSelection();
		faceSelection = new FaceSelection();
	}
	
	public static FaceSetSelection toFaceSetSelection(Selection s) {
		FaceSetSelection ems = new FaceSetSelection();
		VertexSelection vs = new VertexSelection();
		EdgeSelection es = new EdgeSelection();
		FaceSelection fs = new FaceSelection();
		ems.setVertexSelection(vs);
		ems.setEdgeSelection(es);
		ems.setFaceSelection(fs);
		for (Vertex<?, ?, ?> v : s.getVertices()) {
			VertexSelection.Vertex vv = new VertexSelection.Vertex();
			vv.setIndex(v.getIndex());
			vv.setChannel(s.getChannel(v));
			vs.getVertices().add(vv);
		}
		for (Edge<?, ?, ?> e : s.getEdges()) {
			EdgeSelection.Edge ee = new EdgeSelection.Edge();
			if (e.getLeftFace() != null) {
				ee.setFace(e.getLeftFace().getIndex());
			} else {
				ee.setFace(-1);
			}
			ee.setVertex1(e.getStartVertex().getIndex());
			ee.setVertex2(e.getTargetVertex().getIndex());
			ee.setChannel(s.getChannel(e));
			es.getEdges().add(ee);
		}
		for (Face<?, ?, ?> f : s.getFaces()) {
			FaceSelection.Face ff = new FaceSelection.Face();
			ff.setIndex(f.getIndex());
			ff.setChannel(s.getChannel(f));
			fs.getFaces().add(ff);
		}
		return ems;
	}
	
	
	public static <
		V extends Vertex<V,E,F>,
		E extends Edge<V,E,F>,
		F extends Face<V,E,F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> Selection toSelection(FaceSetSelection es, HDS hds) {
		Selection s = new Selection();
		for (VertexSelection.Vertex v : es.getVertexSelection().getVertices()) {
			V vv = hds.getVertex(v.getIndex());
			s.add(vv, v.getChannel());
		}
		for (EdgeSelection.Edge e : es.getEdgeSelection().getEdges()) {
			List<E> candidates = null;
			if (e.getFace() >= 0) {
				F f = hds.getFace(e.getFace());
				candidates = HalfEdgeUtils.boundaryEdges(f);
			} else {
				candidates = HalfEdgeUtils.boundaryEdges(hds);
			}
			boolean found = false;
			for (E be : candidates) {
				if (
					be.getTargetVertex().getIndex() == e.getVertex1() && be.getStartVertex().getIndex() == e.getVertex2() ||
					be.getTargetVertex().getIndex() == e.getVertex2() && be.getStartVertex().getIndex() == e.getVertex1() 
				) {
					s.add(be, e.getChannel());
					found = true;
					break;
				}
			}
			if (!found) {
				log.warning("edge selection not found: " + e);
			}
		}
		for (FaceSelection.Face f : es.getFaceSelection().getFaces()) {
			F ff = hds.getFace(f.getIndex());
			s.add(ff, f.getChannel());
		}
		return s;
	}
	
	
    protected FaceSetSelection.VertexSelection vertexSelection;
    protected FaceSetSelection.EdgeSelection edgeSelection;
    protected FaceSetSelection.FaceSelection faceSelection;

    public FaceSetSelection.VertexSelection getVertexSelection() {
        return vertexSelection;
    }

    public void setVertexSelection(FaceSetSelection.VertexSelection value) {
        this.vertexSelection = value;
    }

    public FaceSetSelection.EdgeSelection getEdgeSelection() {
        return edgeSelection;
    }

    public void setEdgeSelection(FaceSetSelection.EdgeSelection value) {
        this.edgeSelection = value;
    }

    public FaceSetSelection.FaceSelection getFaceSelection() {
        return faceSelection;
    }

    public void setFaceSelection(FaceSetSelection.FaceSelection value) {
        this.faceSelection = value;
    }

    public static class EdgeSelection {

        protected List<FaceSetSelection.EdgeSelection.Edge> edges;

        public List<FaceSetSelection.EdgeSelection.Edge> getEdges() {
            if (edges == null) {
                edges = new ArrayList<FaceSetSelection.EdgeSelection.Edge>();
            }
            return this.edges;
        }


        public static class Edge {

            protected int face;
            protected int vertex1;
            protected int vertex2;
            protected Integer channel;

            public int getFace() {
                return face;
            }

            public void setFace(int value) {
                this.face = value;
            }

            public int getVertex1() {
                return vertex1;
            }

            public void setVertex1(int value) {
                this.vertex1 = value;
            }

            public int getVertex2() {
                return vertex2;
            }

            public void setVertex2(int value) {
                this.vertex2 = value;
            }

            public int getChannel() {
                if (channel == null) {
                    return  0;
                } else {
                    return channel;
                }
            }

            public void setChannel(Integer value) {
                this.channel = value;
            }

        }

    }


    public static class FaceSelection {

        protected List<FaceSetSelection.FaceSelection.Face> faces;

        public List<FaceSetSelection.FaceSelection.Face> getFaces() {
            if (faces == null) {
                faces = new ArrayList<FaceSetSelection.FaceSelection.Face>();
            }
            return this.faces;
        }


        public static class Face {

            protected int index;
            protected Integer channel;

            public int getIndex() {
                return index;
            }

            public void setIndex(int value) {
                this.index = value;
            }

            public int getChannel() {
                if (channel == null) {
                    return  0;
                } else {
                    return channel;
                }
            }

            public void setChannel(Integer value) {
                this.channel = value;
            }

        }

    }

    public static class VertexSelection {

        protected List<FaceSetSelection.VertexSelection.Vertex> vertices;

        public List<FaceSetSelection.VertexSelection.Vertex> getVertices() {
            if (vertices == null) {
                vertices = new ArrayList<FaceSetSelection.VertexSelection.Vertex>();
            }
            return this.vertices;
        }


        public static class Vertex {

            protected int index;
            protected Integer channel;

            public int getIndex() {
                return index;
            }

            public void setIndex(int value) {
                this.index = value;
            }

            public int getChannel() {
                if (channel == null) {
                    return  0;
                } else {
                    return channel;
                }
            }

            public void setChannel(Integer value) {
                this.channel = value;
            }

        }

    }

}
