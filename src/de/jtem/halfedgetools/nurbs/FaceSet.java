package de.jtem.halfedgetools.nurbs;

import java.util.Arrays;

import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.scene.IndexedFaceSet;

public class FaceSet {
	protected double[][] verts;
	protected int[][] faces;
	
	public FaceSet(){
		
	}

	public double[][] getVerts() {
		return verts;
	}

	public void setVerts(double[][] verts) {
		this.verts = verts;
	}

	public int[][] getFaces() {
		return faces;
	}

	public void setFaces(int[][] faces) {
		this.faces = faces;
	}

	@Override
	public String toString() {
		String str = new String();
		str = str + "verts" + '\n';
		for (int i = 0; i < verts.length; i++) {
			str = str + Arrays.toString(verts[i]) + '\n';
		}
		str = str + "faces" + '\n';
		for (int i = 0; i < faces.length; i++) {
			str = str + Arrays.toString(faces[i]) + '\n';
		}
		return str;
	}
	
	public IndexedFaceSet getIndexedFaceSet() {
		IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
		ifsf.setVertexCount(verts.length);
		ifsf.setVertexCoordinates(verts);
		ifsf.setGenerateEdgesFromFaces(true);
		ifsf.setGenerateFaceNormals(true);
		ifsf.setGenerateVertexNormals(true);
		ifsf.setFaceCount(faces.length);
		ifsf.setFaceIndices(faces);
		ifsf.update();
		return ifsf.getIndexedFaceSet();
		
	}
	
}
