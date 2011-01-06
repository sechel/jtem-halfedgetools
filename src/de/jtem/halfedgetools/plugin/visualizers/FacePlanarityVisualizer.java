/**
This file is part of a jTEM project.
All jTEM projects are licensed under the FreeBSD license 
or 2-clause BSD license (see http://www.opensource.org/licenses/bsd-license.php). 

Copyright (c) 2002-2010, Technische Universität Berlin, jTEM
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, 
are permitted provided that the following conditions are met:

-	Redistributions of source code must retain the above copyright notice, 
	this list of conditions and the following disclaimer.

-	Redistributions in binary form must reproduce the above copyright notice, 
	this list of conditions and the following disclaimer in the documentation 
	and/or other materials provided with the distribution.
 
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, 
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS 
BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, 
OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT 
OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING 
IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY 
OF SUCH DAMAGE.
**/

package de.jtem.halfedgetools.plugin.visualizers;

import static de.jreality.math.Rn.crossProduct;
import static de.jreality.math.Rn.determinant;
import static de.jreality.math.Rn.euclideanNorm;
import static de.jreality.math.Rn.subtract;
import static java.lang.Math.abs;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AbstractAdapter;
import de.jtem.halfedgetools.adapter.Adapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Color;
import de.jtem.halfedgetools.adapter.type.Label;
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.VisualizerPlugin;
import de.jtem.jrworkspace.plugin.Controller;

public class FacePlanarityVisualizer extends VisualizerPlugin implements ChangeListener, ActionListener {

	private DecimalFormat
		format = new DecimalFormat("0.000");
	private SpinnerNumberModel
		placesModel = new SpinnerNumberModel(3, 0, 20, 1);
	private JSpinner	
		placesSpinner = new JSpinner(placesModel);
	private JCheckBox	
		showLabels = new JCheckBox("Labels", false),
		showColors = new JCheckBox("Colors", true);
	private JPanel
		panel = new JPanel();
	private double
		maxUnevenness = 0.0;
	
	
	public FacePlanarityVisualizer() {
		panel.setLayout(new GridBagLayout());
		GridBagConstraints gbc1 = new GridBagConstraints();
		gbc1.fill = GridBagConstraints.BOTH;
		gbc1.weightx = 1.0;
		gbc1.gridwidth = 1;
		gbc1.insets = new Insets(2, 2, 2, 2);
		GridBagConstraints gbc2 = new GridBagConstraints();
		gbc2.fill = GridBagConstraints.BOTH;
		gbc2.weightx = 1.0;
		gbc2.gridwidth = GridBagConstraints.REMAINDER;
		gbc2.insets = new Insets(2, 2, 2, 2);
		
		panel.add(showColors, gbc1);
		panel.add(showLabels, gbc2);
		panel.add(new JLabel("Decimal Places"), gbc1);
		panel.add(placesSpinner, gbc2);
		
		showColors.addActionListener(this);
		showLabels.addActionListener(this);
		placesSpinner.addChangeListener(this);
	}
	
	
	@Override
	public void storeStates(Controller c) throws Exception {
		super.storeStates(c);
		c.storeProperty(getClass(), "showLabels", showLabels.isSelected());
		c.storeProperty(getClass(), "showColors", showColors.isSelected());
		c.storeProperty(getClass(), "decimalPlaces", placesModel.getNumber().intValue());
	}
	
	@Override
	public void restoreStates(Controller c) throws Exception {
		super.restoreStates(c);
		showLabels.setSelected(c.getProperty(getClass(), "showLabels", showLabels.isSelected()));
		showColors.setSelected(c.getProperty(getClass(), "showColors", showColors.isSelected()));
		placesModel.setValue(c.getProperty(getClass(), "decimalPlaces", placesModel.getNumber().intValue()));
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		String fs = "0.";
		for (int i = 0; i < placesModel.getNumber().intValue(); i++) {
			fs += "0";
		}
		format = new DecimalFormat(fs);
		updateContent();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		updateContent();
	}
	
	
	@Override
	public  < 
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void initVisualization(HDS hds, AdapterSet a, HalfedgeInterface hif) {
		maxUnevenness = getMaxUnevenness(hds, a);
	}

	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> double getMaxUnevenness(HDS hds, AdapterSet a){
		double maxUneven = 0.0;
		for (F f : hds.getFaces()) {
			double vol = getRelativeUnevenness(f, a);
			if (vol > maxUneven)
				maxUneven = vol;
		}
		return maxUneven;
	}


	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> double getMinUnevenness(HDS hds, AdapterSet a){
		double minUneven = Double.MAX_VALUE;
		for (F f : hds.getFaces()) {
			double vol = getRelativeUnevenness(f, a);
			if (vol < minUneven)
				minUneven = vol;
		}
		return minUneven;
	}


	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> double getMeanUnevenness(HDS hds, AdapterSet a){
		double meanVol = 0.0;
		int count = 0;
		for (F f : hds.getFaces()) {
			meanVol += getRelativeUnevenness(f, a);
			count++;
		}
		return meanVol / count;
	}


	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> double getRelativeUnevenness(F f, AdapterSet ad) {
		List<E> boundary = HalfEdgeUtils.boundaryEdges(f);
		if (boundary.size() != 4)
			return 0.0;
		double[] a = ad.get(Position.class, boundary.get(0).getTargetVertex(), double[].class);
		double[] b = ad.get(Position.class, boundary.get(1).getTargetVertex(), double[].class);
		double[] c = ad.get(Position.class, boundary.get(2).getTargetVertex(), double[].class);
		double[] d = ad.get(Position.class, boundary.get(3).getTargetVertex(), double[].class);
		double[] Mtetraeder = {c[0] - a[0], c[1] - a[1], c[2] - a[2], b[0] - a[0], b[1] - a[1], b[2] - a[2], d[0] - a[0], d[1] - a[1], d[2] - a[2]};
		double vol = determinant(Mtetraeder);
		double[][] point = {a,b,c,d};
		double maxHeight = 0.0;
		double meanLength = 0.0;
		for (int i = 1; i <= 4; i++) {
			meanLength += Rn.euclideanDistance(point[i - 1], point[i % 4]);
			double[] v = crossProduct(null, subtract(null, point[i - 1], point[i % 4]), subtract(null, point[(i + 1) % 4], point[i % 4]));
			double area = euclideanNorm(v);
			if (area == 0.0) continue;
			double offset = abs(vol / area);
			if (offset > maxHeight)
				maxHeight = offset;
		}
		meanLength /= 4;
		return maxHeight / meanLength;
	}
	
	
	private class PlanarityValueAdapter extends AbstractAdapter<Double> {
		
		public PlanarityValueAdapter() {
			super(Double.class, true, false);
		}

		@Override
		public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
			return Face.class.isAssignableFrom(nodeClass);
		}
		
		@Override
		public double getPriority() {
			return 0;
		}
		
		@Override
		public <
			V extends Vertex<V, E, F>,
			E extends Edge<V, E, F>,
			F extends Face<V, E, F>
		> Double getF(F f, AdapterSet a) {
			return getRelativeUnevenness(f, a);
		}
		
	}
	
	@Label
	private class PlanarityLabelAdapter extends AbstractAdapter<String> {
		
		public PlanarityLabelAdapter() {
			super(String.class, true, false);
		}

		@Override
		public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
			return Face.class.isAssignableFrom(nodeClass);
		}
		
		@Override
		public double getPriority() {
			return 0;
		}
		
		@Override
		public <
			V extends Vertex<V, E, F>,
			E extends Edge<V, E, F>,
			F extends Face<V, E, F>
		> String getF(F f, AdapterSet a) {
			double u = getRelativeUnevenness(f, a) * 100;
			return format.format(u) + "%";
		}
		
	}

	
	
	@Color
	private class PlanarityColorAdapter extends AbstractAdapter<double[]> {
		
		private final double[]
		    colorGreen = {0, 1, 0};
		
		public PlanarityColorAdapter() {
			super(double[].class, true, false);
		}

		@Override
		public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
			return Face.class.isAssignableFrom(nodeClass);
		}
		
		@Override
		public double getPriority() {
			return 0;
		}
		
		@Override
		public <
			V extends Vertex<V, E, F>,
			E extends Edge<V, E, F>,
			F extends Face<V, E, F>
		> double[] getF(F f, AdapterSet a) {
			if (maxUnevenness == 0) {
				return colorGreen;
			}
			double col = getRelativeUnevenness(f, a) / maxUnevenness;
			return new double[] {col, 1 - col, 0};
		}
		
	}
	
	
	
	@Override
	public JPanel getOptionPanel() {
		return panel;
	}
	
	
	@Override
	public Set<? extends Adapter<?>> getAdapters() {
		Set<Adapter<?>> result = new HashSet<Adapter<?>>();
		if (showColors.isSelected()) {
			result.add(new PlanarityColorAdapter());
		}
		if (showLabels.isSelected()) {
			result.add(new PlanarityLabelAdapter());
		}
		result.add(new PlanarityValueAdapter());
		return result;
	}


	@Override
	public String getName() {
		return "Face Planarity";
	}

}
