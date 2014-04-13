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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import de.jreality.ui.LayoutFactory;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.algorithm.subdivision.CatmullClark;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmDialogPlugin;
import de.jtem.halfedgetools.plugin.image.ImageHook;
import de.jtem.halfedgetools.selection.Selection;
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

	private JRadioButton
		NoBoundary = new JRadioButton("Delete"),
		BSplineButton = new JRadioButton("B-Spline Boundary"),
		FixedButton = new JRadioButton("Fixed Boundary"),
		BSplineLines = new JRadioButton("B-Spline Lines"),
		NoLines = new JRadioButton("No featured Lines");
	private JCheckBox
		useLinearChecker = new JCheckBox("Linear Interpolation");

	private CatmullClark 
		cc = new CatmullClark();

	
	public CatmullClarkPlugin() {
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = LayoutFactory.createRightConstraint();
		boundaryPanel.setLayout(new GridBagLayout());
//		boundaryPanel.setPreferredSize(new Dimension(250, 120));
		boundaryPanel.setBorder(BorderFactory.createTitledBorder("Boundary"));
		
		BSplineButton.setSelected(true);
		BoundaryGroup.add(BSplineButton);
		BoundaryGroup.add(FixedButton);
		BoundaryGroup.add(NoBoundary);
		
		boundaryPanel.add(BSplineButton,c);
		boundaryPanel.add(FixedButton,c);
		boundaryPanel.add(NoBoundary,c);
		
		featuredLines.setLayout(new GridBagLayout());
//		featuredLines.setPreferredSize(new Dimension(250, 120));
		featuredLines.setBorder(BorderFactory.createTitledBorder("Feature Lines"));
		
		BSplineLines.setSelected(true);
		LinesGroup.add(BSplineLines);
		LinesGroup.add(NoLines);
		
		featuredLines.add(BSplineLines,c);
		featuredLines.add(NoLines,c);
		
		panel.add(useLinearChecker, c);
		panel.add(boundaryPanel,c);
		panel.add(featuredLines,c);

	}
		
	@Override
	public < 
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void executeAfterDialog(HDS hds, AdapterSet a, HalfedgeInterface hcp) {
		HDS hds2 = hcp.createEmpty(hds);
		boolean bSplineFeatures = BSplineLines.isSelected();
		boolean bSplineBoundary = BSplineButton.isSelected();
		boolean removeBoundary = NoBoundary.isSelected();
		boolean linearInterpolation = useLinearChecker.isSelected();
		Selection sel = new Selection(hcp.getSelection());
		
		Map<F, V> oldFnewVMap = new HashMap<F, V>();
		Map<E, V> oldEnewVMap = new HashMap<E, V>();
		Map<V, V> oldVnewVMap = new HashMap<V, V>();
		cc.subdivide(
			hds, 
			hds2, 
			a, 
			sel, 
			bSplineBoundary,
			bSplineFeatures, 
			removeBoundary,
			linearInterpolation,
			oldFnewVMap, 
			oldEnewVMap, 
			oldVnewVMap
		);
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
