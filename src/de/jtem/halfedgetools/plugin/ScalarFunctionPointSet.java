package de.jtem.halfedgetools.plugin;

import java.awt.Color;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import de.jreality.geometry.PointSetFactory;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Node;
import de.jtem.halfedgetools.adapter.Adapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.generic.BaryCenter4d;

public class ScalarFunctionPointSet {

	private SceneGraphComponent 
		scalarFunctionComponent = new SceneGraphComponent();
	private double
		minValue = Double.MAX_VALUE,
		maxValue = 0;
	private ColorMap 
		colorMap = null;
	private double[][] 
	    coords = null;
	private double[]
	    radii = null;

	public ScalarFunctionPointSet(HalfEdgeDataStructure<?, ?, ?> hds, Adapter<Number> f, AdapterSet as) {
		scalarFunctionComponent.setName(f.toString().replace("Adapter", ""));
		Collection<Node<?,?,?>> nodes = getNodes(hds,f);
		coords = new double[nodes.size()][4];
		radii = new double[nodes.size()];
		int i = 0;
		for(Node<?,?,?> n : nodes) {
			coords[i] = getCoords(hds,n,as);
			radii[i] = f.get(n,as).doubleValue();
			if(radii[i] > maxValue) {
				maxValue = radii[i];
			}
			if(radii[i] < minValue) {
				minValue = radii[i];
			}
			++i;
		}
	}

	private void updateScalarFunctionComponent() {
		PointSetFactory psf = new PointSetFactory();
		psf.setVertexCount(radii.length);
		if(colorMap != null) {
			Color[] colors = new Color[radii.length];
			for(int i=0;i<radii.length;++i) {
				colors[i] = colorMap.getColor(radii[i],minValue,maxValue);
			}
			psf.setVertexColors(colors);
		}
		psf.setVertexCoordinates(coords);
		double[] absradii=new double[radii.length];
		for (int i = 0; i < absradii.length; i++) {
			absradii[i]= Math.abs(radii[i]);
		}
		psf.setVertexRelativeRadii(absradii);
		Appearance appearance = new Appearance();
		appearance.setAttribute(CommonAttributes.SPHERES_DRAW, true);
		appearance.setAttribute(CommonAttributes.VERTEX_DRAW, true);
		psf.update();
		scalarFunctionComponent.setAppearance(appearance);
		scalarFunctionComponent.setGeometry(psf.getGeometry());
	}
	
	private double[] getCoords(HalfEdgeDataStructure<?, ?, ?> hds, Node<?, ?, ?> n, AdapterSet as) {
		return as.getD(BaryCenter4d.class, n);
	}

	private Collection<Node<?,?,?>> getNodes(HalfEdgeDataStructure<?, ?, ?> hds, Adapter<Number> f) {
		List<Node<?,?,?>> nodes = new LinkedList<Node<?,?,?>>();
		if(f.canAccept(hds.getVertexClass())) {
			nodes.addAll(hds.getVertices());
		}
		if(f.canAccept(hds.getEdgeClass())) {
			nodes.addAll(hds.getEdges());
		}
		if(f.canAccept(hds.getFaceClass())) {
			nodes.addAll(hds.getFaces());
		}
		return nodes;
	}

	public SceneGraphComponent getComponent() {
		updateScalarFunctionComponent();
		return scalarFunctionComponent;
	}
	
	public void setColorMap(ColorMap sfcr) {
		colorMap = sfcr;
	}
}
