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

package de.jtem.halfedgetools.plugin;


import static de.jtem.halfedgetools.jreality.adapter.Adapter.AdapterType.VERTEX_ADAPTER;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.BevelBorder;

import de.jreality.plugin.JRViewer;
import de.jreality.plugin.JRViewer.ContentType;
import de.jreality.plugin.basic.View;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedgetools.algorithm.Coord3DAdapter;
import de.jtem.halfedgetools.algorithm.catmullclark.CatmullClarkSubdivision;
import de.jtem.halfedgetools.algorithm.subdivision.Subdivider;
import de.jtem.halfedgetools.algorithm.subdivision.Subdivider.InterpolFunk;
import de.jtem.halfedgetools.algorithm.subdivision.Subdivider.SubdivType;
import de.jtem.halfedgetools.algorithm.subdivision.node.SubDivEdge;
import de.jtem.halfedgetools.algorithm.subdivision.node.SubDivFace;
import de.jtem.halfedgetools.algorithm.subdivision.node.SubDivHDS;
import de.jtem.halfedgetools.algorithm.subdivision.node.SubDivVertex;
import de.jtem.halfedgetools.jreality.adapter.standard.StandardCoordinateAdapter;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.jtem.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;

public class HalfedgeSubdivisonPlugin extends ShrinkPanelPlugin implements ActionListener {

	private HalfedgeConnectorPlugin<SubDivVertex,SubDivEdge,SubDivFace,SubDivHDS>
		hcp = null;
		
	// ----------------------------- Gui -----------------------------
		
	private JLabel maxL=new JLabel("max length:",JLabel.LEFT);
	private JLabel minL=new JLabel("min length:",JLabel.LEFT);
	private SpinnerNumberModel maxSpM= new SpinnerNumberModel(0.2,0,100,0.01);
	private SpinnerNumberModel minSpM= new SpinnerNumberModel(0.05,0,100,0.01);
	private JSpinner maxSp=new JSpinner(maxSpM);
	private JSpinner minSp=new JSpinner(minSpM);
	private JCheckBox extraFlipCB=new JCheckBox("extra flip",true);
	private JCheckBox linearCB=new JCheckBox("linear",false);

	private ButtonGroup typeBG= new ButtonGroup();
	private JRadioButton sqrtRB=new JRadioButton("Squareroot");
	private JRadioButton butterflyRB=new JRadioButton("Butterfly");
	private JRadioButton stockRB=new JRadioButton("Stock");
	private JRadioButton gonskaRB=new JRadioButton("Gonska");
	private JRadioButton catmullRB = new JRadioButton("Catmull-Clark");
		
	private JButton	subdivideB= new JButton("Subdivide");
	private JButton	unrefineB= new JButton("Unrefine");
		
	public HalfedgeSubdivisonPlugin() {
		shrinkPanel.setLayout(new BorderLayout());
		JPanel subP= new JPanel(new GridLayout(6,2));
		JPanel unrefP= new JPanel(new GridLayout(2,2));
		shrinkPanel.add(subP,BorderLayout.NORTH);
		shrinkPanel.add(unrefP,BorderLayout.SOUTH);
		subP.setBorder(BorderFactory.createTitledBorder(	BorderFactory.createBevelBorder(BevelBorder.RAISED),
		"Subdivision"));
		unrefP.setBorder(BorderFactory.createTitledBorder(	BorderFactory.createBevelBorder(BevelBorder.RAISED),
		"Unrefinement"));

		subP.add(maxL);
		subP.add(maxSp);
		subP.add(sqrtRB);
		subP.add(butterflyRB);
		subP.add(stockRB);
		subP.add(catmullRB);
		subP.add(gonskaRB);
		subP.add(new JPanel());
		subP.add(extraFlipCB);
		subP.add(linearCB);
		subP.add(subdivideB);
		unrefP.add(minL);
		unrefP.add(minSp);
		unrefP.add(unrefineB);
		
		typeBG.add(sqrtRB);
		typeBG.add(butterflyRB);
		typeBG.add(stockRB);
		typeBG.add(gonskaRB);
		typeBG.add(catmullRB);
		
		catmullRB.addActionListener(this);
		sqrtRB.addActionListener(this);
		butterflyRB.addActionListener(this);
		stockRB.addActionListener(this);
		gonskaRB.addActionListener(this);
		subdivideB.addActionListener(this);
		unrefineB.addActionListener(this);
		extraFlipCB.setSelected(true);
		linearCB.setSelected(false);
		
		sqrtRB.setSelected(true);
		extraFlipCB.setEnabled(false);
		linearCB.setEnabled(false);	
	}
	// ------------------------------ Listeners ------------------------
	public void actionPerformed(ActionEvent e) {
		linearCB.setEnabled(gonskaRB.isSelected());
		extraFlipCB.setEnabled(gonskaRB.isSelected());
		if (e.getSource()==subdivideB)	subdivide();
		if (e.getSource()==unrefineB) 		unrefine();
	}
	private void unrefine(){
		// convert:
		SubDivHDS heds = new SubDivHDS();
		hcp.convertActiveGeometryToHDS(heds,new StandardCoordinateAdapter(VERTEX_ADAPTER));
		// perform:
		Subdivider<SubDivVertex,SubDivEdge,SubDivFace> s = new Subdivider(SubDivVertex.class, SubDivEdge.class, SubDivFace.class);
		s.setHeds(heds);
		s.setToleranceShort(minSpM.getNumber().doubleValue());
		s.removeShortEdges();
		// reconvert:
		heds=s.getHeds();
		hcp.updateHalfedgeContentAndActiveGeometry(heds, true, 
				new StandardCoordinateAdapter(VERTEX_ADAPTER)
//				,new SubDivTypeColorAdapter(VERTEX_ADAPTER)
//				,new SubDivTypeColorAdapter(FACE_ADAPTER)
		);
	}
	private void subdivide(){
		// convert:
		HalfEdgeDataStructure<SubDivVertex, SubDivEdge,SubDivFace> heds= new HalfEdgeDataStructure<SubDivVertex, SubDivEdge, SubDivFace>(
				SubDivVertex.class,SubDivEdge.class,SubDivFace.class);
		hcp.convertActiveGeometryToHDS(heds,new StandardCoordinateAdapter(VERTEX_ADAPTER));

		// settings and perform:
		Subdivider s= new Subdivider(SubDivVertex.class, SubDivEdge.class, SubDivFace.class);
		s.setHeds(heds);
		s.setToleranceLong(maxSpM.getNumber().doubleValue());
		boolean extraFlip=false;
		
		if(sqrtRB.isSelected()){
			s.setCalcNormals(false);
			s.subdivideSqrt();
		}
		if(butterflyRB.isSelected()){
			s.setCalcNormals(false);
			s.setExtraFlip(false);
			s.usePredefinedInterpolFunk(InterpolFunk.BUTTERFLY);
			s.subdivide();
		}
		if(stockRB.isSelected()){
			s.setCalcNormals(true);
			s.subdivideSplit();
		}
		if(gonskaRB.isSelected()){
			s.setExtraFlip(extraFlip);
			s.setCalcNormals(true);
			s.setExtraFlip(extraFlipCB.isSelected());
			s.setType(SubdivType.EXTENDED);
			if(linearCB.isSelected())
				s.usePredefinedInterpolFunk(InterpolFunk.LINEAR);
			else
				s.usePredefinedInterpolFunk(InterpolFunk.SPLINE);
			s.subdivide();
		}
		if (catmullRB.isSelected()) {
			HalfEdgeDataStructure<SubDivVertex, SubDivEdge,SubDivFace> heds2= new HalfEdgeDataStructure<SubDivVertex, SubDivEdge, SubDivFace>(
					SubDivVertex.class,SubDivEdge.class,SubDivFace.class);
			CatmullClarkSubdivision<SubDivVertex, SubDivEdge, SubDivFace> ccs = new CatmullClarkSubdivision<SubDivVertex, SubDivEdge, SubDivFace>();
			
			Coord3DAdapter<SubDivVertex> ca = new Coord3DAdapter<SubDivVertex>() {
				@Override
				public double[] getCoord(SubDivVertex v) {
					return v.position;
				}
				@Override
				public void setCoord(SubDivVertex v, double[] c) {
					v.position = c;
				}
			};
			ccs.subdivide(heds, heds2, ca);
			s.setHeds(heds2);
		}
		
		// reconvert:
		heds=s.getHeds();
		hcp.updateHalfedgeContentAndActiveGeometry(heds, true, 
				new StandardCoordinateAdapter(VERTEX_ADAPTER)
//				,new SubDivTypeColorAdapter(VERTEX_ADAPTER)
//				,new SubDivTypeColorAdapter(EDGE_ADAPTER)
//				,new SubDivTypeColorAdapter(FACE_ADAPTER)
				);
	}
	
	// ---------------------------------------------------------
	
	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		return View.class;
	}
	
	
	@Override
	public PluginInfo getPluginInfo() {
		return new PluginInfo("gonskas subdivider and others");
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		hcp = c.getPlugin(HalfedgeConnectorPlugin.class);
	}

	
	@Override
	public void uninstall(Controller c) throws Exception {
		super.uninstall(c);
	}
	
	
	public static void main(String[] args) {
		JRViewer v = new JRViewer();
		v.addBasicUI();
		v.addContentSupport(ContentType.CenteredAndScaled);
		v.registerPlugin(new HalfedgeSubdivisonPlugin());
		v.startup();
	}
	
	
}
