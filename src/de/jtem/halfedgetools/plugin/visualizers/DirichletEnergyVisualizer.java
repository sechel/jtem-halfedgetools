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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.jreality.math.Rn;
import de.jtem.halfedgetools.functional.DomainValue;
import de.jtem.halfedgetools.jreality.adapter.Adapter;
import de.jtem.halfedgetools.jreality.adapter.LabelAdapter2Ifs;
import de.jtem.halfedgetools.jreality.node.JREdge;
import de.jtem.halfedgetools.plugin.VisualizerPlugin;

public class DirichletEnergyVisualizer extends VisualizerPlugin implements ChangeListener {

	private DecimalFormat
		format = new DecimalFormat("0.000");
	private SpinnerNumberModel
		placesModel = new SpinnerNumberModel(3, 0, 20, 1);
	private JSpinner	
		placesSpinner = new JSpinner(placesModel);
	private JPanel
		panel = new JPanel();
	
	
	public DirichletEnergyVisualizer() {
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
		panel.add(new JLabel("Decimal Places"), gbc1);
		panel.add(placesSpinner, gbc2);
		
		placesSpinner.addChangeListener(this);
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
	
	public class EdgeLengthAdapter <E extends JREdge<?, E, ?>> implements  LabelAdapter2Ifs<E> {

		@Override
		public AdapterType getAdapterType() {
			return AdapterType.EDGE_ADAPTER;
		}

		@Override
		public String getLabel(E e) {
			double l = w(e)*getLengthSquared(e);
			return format.format(l);
		}
		
	}
	
	public static double cot(Double phi) {
		return 1.0 / StrictMath.tan(phi);
	}
	
	public<E extends JREdge<?, E, ?>> double getLengthSquared(E e) {
		return Rn.euclideanDistanceSquared(e.getTargetVertex().position, e.getStartVertex().position);
	}
	
	public <E extends JREdge<?, E, ?>> double getAlpha(E e){
		double a = getLengthSquared(e.getNextEdge());
		double b = getLengthSquared(e.getPreviousEdge());
		double c = getLengthSquared(e);
	
		return Math.acos((a + b - c) / (2.0 * Math.sqrt(a * b)));
	}
	

	private <E extends JREdge<?, E, ?>> double w(E e) {
		E e1 = e;
		E e2 = e.getOppositeEdge();
		double val = 0.0;
		double w = 0.5;
		
		if(e.getLeftFace() == null) {
			val = w * cot(getAlpha(e2));
		} else if(e.getRightFace() == null) {
			val = w * cot(getAlpha(e1));
		} else { // interior edge
			double alpha_ij = cot(getAlpha(e1));
			double alpha_ji = cot(getAlpha(e2));

			// optimize
			val = w * (cot(alpha_ij) + cot(alpha_ji));
		}
		
		return val;
	}
	
	@Override
	public JPanel getOptionPanel() {
		return panel;
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public Set<? extends Adapter> getAdapters() {
		return Collections.singleton(new EdgeLengthAdapter());
	}


	@Override
	public String getName() {
		return "Edge Dirichlet Energy";
	}

}
