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

package de.jtem.halfedgetools.plugin.algorithm.subdivision;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.CalculatorException;
import de.jtem.halfedgetools.adapter.CalculatorSet;
import de.jtem.halfedgetools.algorithm.calculator.EdgeAverageCalculator;
import de.jtem.halfedgetools.algorithm.calculator.FaceBarycenterCalculator;
import de.jtem.halfedgetools.algorithm.calculator.VertexPositionCalculator;
import de.jtem.halfedgetools.algorithm.subdivision.CatmullClarkAll;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.HalfedgeSelection;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmDialogPlugin;
import de.jtem.halfedgetools.plugin.image.ImageHook;
import de.jtem.jrworkspace.plugin.PluginInfo;


public class CatmullClarkPlugin extends AlgorithmDialogPlugin {
	
	private JPanel
		panel = new JPanel();
	private JPanel
		boundaryPanel = new JPanel();
	private JPanel
		featuredLines = new JPanel();
	private ButtonGroup
		BoundaryGroup = new ButtonGroup();
	private ButtonGroup
		LinesGroup = new ButtonGroup();
	private boolean
		b1,b3,b4;

	private JRadioButton
		NoBoundary = new JRadioButton("Delete"),
		BSplineButton = new JRadioButton("B-Spline Boundary"),
		FixedButton = new JRadioButton("Fixed Boundary"),
		BSplineLines = new JRadioButton("B-Spline Lines"),
		NoLines = new JRadioButton("No featured Lines");

	private CatmullClarkAll 
		cc = new CatmullClarkAll();

	
	public CatmullClarkPlugin() {
		panel.setLayout(new GridBagLayout());
		GridBagConstraints gbc1 = new GridBagConstraints();
		gbc1.fill = GridBagConstraints.BOTH;
		gbc1.weightx = 1.0;
		gbc1.gridwidth = GridBagConstraints.RELATIVE;
		gbc1.insets = new Insets(2, 2, 2, 2);
		GridBagConstraints gbc2 = new GridBagConstraints();
		gbc2.fill = GridBagConstraints.BOTH;
		gbc2.weightx = 1.0;
		gbc2.gridwidth = GridBagConstraints.REMAINDER;
		gbc2.insets = new Insets(2, 2, 2, 2);
		
		boundaryPanel.setLayout(new GridBagLayout());
		boundaryPanel.setPreferredSize(new Dimension(250, 120));
		boundaryPanel.setBorder(BorderFactory.createTitledBorder("boundary"));
		
		BSplineButton.setSelected(true);
		BoundaryGroup.add(BSplineButton);
		BoundaryGroup.add(FixedButton);
		BoundaryGroup.add(NoBoundary);
		
		boundaryPanel.add(BSplineButton,gbc2);
		boundaryPanel.add(FixedButton,gbc2);
		boundaryPanel.add(NoBoundary,gbc2);
		
		featuredLines.setLayout(new GridBagLayout());
		featuredLines.setPreferredSize(new Dimension(250, 120));
		featuredLines.setBorder(BorderFactory.createTitledBorder("featured lines"));
		
		BSplineLines.setSelected(true);
		LinesGroup.add(BSplineLines);
		LinesGroup.add(NoLines);
		
		featuredLines.add(BSplineLines,gbc2);
		featuredLines.add(NoLines,gbc2);
		
		panel.add(boundaryPanel,gbc2);
		panel.add(featuredLines,gbc2);
	}
		
	@Override
	public < 
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void executeAfterDialog(HDS hds, CalculatorSet c, HalfedgeInterface hcp) throws CalculatorException {
		HDS hds2 = hcp.createEmpty(hds);
		VertexPositionCalculator vc = c.get(hds.getVertexClass(), VertexPositionCalculator.class);
		EdgeAverageCalculator ec = c.get(hds.getEdgeClass(), EdgeAverageCalculator.class);
		FaceBarycenterCalculator fc = c.get(hds.getFaceClass(), FaceBarycenterCalculator.class);
		if (vc == null || ec == null || fc == null) {
			throw new CalculatorException("No Subdivision calculators found for " + hds);
		}
		b4 = BSplineLines.isSelected();
		b1 = BSplineButton.isSelected();
		b3 = NoBoundary.isSelected();
		HalfedgeSelection sel = new HalfedgeSelection(hcp.getSelection());
		cc.subdivide(hds, hds2, vc, ec, fc, sel, b1, b3, b4);
		hcp.set(hds2);
		hcp.setSelection(sel);
	}
	
	@Override
	public AlgorithmCategory getAlgorithmCategory() {
		return AlgorithmCategory.Subdivision;
	}
	
	@Override
	public String getAlgorithmName() {
		return "Catmull Clark";
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Catmull Clark Subdivision");
		info.icon = ImageHook.getIcon("CatmullClark.png", 16, 16);
		return info;
	}
	
	@Override
	protected JPanel getDialogPanel() {
		return panel;
	}


}
