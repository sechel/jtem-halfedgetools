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
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import de.jreality.ui.LayoutFactory;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.TypedAdapterSet;
import de.jtem.halfedgetools.algorithm.subdivision.DooSabin;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmDialogPlugin;
import de.jtem.halfedgetools.plugin.image.ImageHook;
import de.jtem.jrworkspace.plugin.PluginInfo;

/**
 * 
 * @author Jens-Peter Rohrlack (jens.peter.rohrlack@gmail.com)
 *
 */
public class DooSabinPlugin extends AlgorithmDialogPlugin {
	
	private JPanel
		panel = new JPanel();

	private JCheckBox
		interactiveMode = new JCheckBox("Interactive mode");

	private DooSabin.BoundaryMode 
		selectedMode = DooSabin.BoundaryMode.PROPOSED;
	
	private DooSabin 
		ds = new DooSabin();

	
	public DooSabinPlugin() {
		
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = LayoutFactory.createRightConstraint();	
		panel.add(interactiveMode,c);
		
		panel.add(new JLabel("boundary edges:"), c);
		
	    JRadioButton proposed = new JRadioButton("proposed");
	    proposed.setActionCommand("PROPOSED");
	    proposed.setSelected(true);

	    JRadioButton fixed = new JRadioButton("fixed");
	    fixed.setActionCommand("FIXED");
	    
	    JRadioButton custom = new JRadioButton("custom");
	    custom.setActionCommand("CUSTOM");
	    
	    //Group the radio buttons.
	    ButtonGroup group = new ButtonGroup();
	    group.add(proposed);
	    group.add(fixed);
	    group.add(custom);

	    ActionListener aListener = new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals("PROPOSED")) {
					selectedMode = DooSabin.BoundaryMode.PROPOSED;
				} else if (e.getActionCommand().equals("FIXED")) {
					selectedMode = DooSabin.BoundaryMode.FIXED;
				} else if (e.getActionCommand().equals("CUSTOM")) {
					selectedMode = DooSabin.BoundaryMode.CUSTOM;
				}
			}
		};

		proposed.addActionListener(aListener);
		custom.addActionListener(aListener);
		fixed.addActionListener(aListener);
 
		panel.add(proposed, c);
		panel.add(fixed, c);
		panel.add(custom, c);

	}
		
	@Override
	public < 
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void executeAfterDialog(HDS hds, AdapterSet a, final HalfedgeInterface hcp) {
		
		HDS hds2 = hcp.createEmpty(hds);
		
		final ArrayList<HDS> hdsSteps = new ArrayList<HDS>(hds.getVertices().size() + 1); //+1 for initial state
	
		TypedAdapterSet<double[]> da = a.querySet(double[].class);

		
		ds.subdivide(
			hds, 
			hds2, 
			da,
			hcp,
			(interactiveMode.isSelected() ? hdsSteps : null),
			getCurrentJob(),
			selectedMode
		);
		
		
		if (!interactiveMode.isSelected()) {
			
			hcp.set(hds2);
			
		} else {
			
			//work around for control frame
			class ControlFrame extends JFrame {
	
				private static final long serialVersionUID = 1L;
				
				ArrayList<HDS> hdsSteps;
				int currentIndex;
				HalfedgeInterface hcp;
				
				JLabel infoLabel;
				
				public ControlFrame(String title, ArrayList<HDS> hdsSteps, int currentIndex,
						HalfedgeInterface hcp) throws HeadlessException {
					super(title);
					this.hdsSteps = hdsSteps;
					this.currentIndex = currentIndex;
					this.hcp = hcp;
					
					setSize(new Dimension(200,100));
					setPreferredSize(new Dimension(200,100));
					setResizable(false);
					setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					
					hcp.set(hdsSteps.get(0));
					
					class MyButton {
						
						//0 inc, 1 dec, 2 start, 3 end
						
						public JButton button;
						
						public MyButton(String text, final ControlFrame cf, final int mode) {
							
							button = new JButton(text);
							
							button.addActionListener(new ActionListener(){
	
								@Override
								public void actionPerformed(ActionEvent e) {
									switch (mode) {
										case 0 : {
											if (cf.currentIndex < cf.hdsSteps.size()-1) {
												cf.currentIndex++;		
											}	
											break;
										} 
										case 1 : {
											if (cf.currentIndex > 0) {
												cf.currentIndex--;							
											}	
											break;
										}
										case 2 : {
											cf.currentIndex = 0;
											break;
										}
										case 3 : {								
											cf.currentIndex = cf.hdsSteps.size()-1;																	
											break;
										}
									}
									cf.hcp.set(cf.hdsSteps.get(cf.currentIndex));
									infoLabel.setText("<html><table><tr><td width=190 align=center>step "+(cf.currentIndex+1)+" of "+ cf.hdsSteps.size() +"</td></tr></table></html>");
								}
								
							});
							
						}
		
					}
								
					MyButton forward = new MyButton(">", this, 0);
					MyButton back = new MyButton("<", this, 1);
					MyButton start = new MyButton("|<", this, 2);
					MyButton end = new MyButton(">|", this, 3);
					
					JPanel buttonPanel = new JPanel();
					buttonPanel.setSize(new Dimension(200,100));
					buttonPanel.setPreferredSize(new Dimension(200,100));
		
					infoLabel = new JLabel("<html><table><tr><td width=190 align=center>step 0 of "+ hdsSteps.size() +"</td></tr></table></html>");
					infoLabel.setSize(new Dimension(190,30));
					infoLabel.setPreferredSize(new Dimension(190,30));
					
					buttonPanel.add(infoLabel);
					buttonPanel.add(start.button);
					buttonPanel.add(back.button);
					buttonPanel.add(forward.button);
					buttonPanel.add(end.button);
					getContentPane().add(buttonPanel);
				}
	
			}
			
			ControlFrame controlFrame = new ControlFrame("DooSabin Control (work around)", hdsSteps, 0, hcp);
			
			controlFrame.setVisible(true);
			controlFrame.requestFocus();
		}
	}
	
	@Override
	public AlgorithmCategory getAlgorithmCategory() {
		return AlgorithmCategory.Subdivision;
	}
	
	@Override
	public String getAlgorithmName() {
		return "Doo Sabin";
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Doo Sabin Subdivision");
		info.icon = ImageHook.getIcon("DooSabin.png", 16, 16);
		return info;
	}
	
	@Override
	protected JPanel getDialogPanel() {
		return panel;
	}


}
