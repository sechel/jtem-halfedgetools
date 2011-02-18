package de.jtem.halfedgetools.nurbs;

import static java.awt.GridBagConstraints.BOTH;
import static javax.swing.JFileChooser.FILES_ONLY;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.ListSelectionModel.SINGLE_SELECTION;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;

import de.jreality.geometry.PointSetFactory;
import de.jreality.plugin.basic.View;
import de.jreality.scene.SceneGraphComponent;
import de.jtem.halfedgetools.io.NurbsIO;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.image.ImageHook;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.jtem.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;

public class NurbsManagerPlugin extends ShrinkPanelPlugin implements ActionListener{

	private HalfedgeInterface 
		hif = null;
	
	private JFileChooser 
		chooser = new JFileChooser();
	
	private Action
		importAction = new ImportAction();
	
	private JButton
		importButton = new JButton(importAction),
		updateButton = new JButton("update"),
		integralCurveButton = new JButton("Curve");

	private JTable
		surfacesTable= new JTable(new SurfaceTableModel());
	
	private JScrollPane
		layersScroller = new JScrollPane(surfacesTable, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER);
	
	private JToolBar
		surfaceToolbar = new JToolBar();
	
	private ArrayList<NURBSSurface>
		surfaces = new ArrayList<NURBSSurface>();

	private SpinnerNumberModel
		uSpinnerModel = new SpinnerNumberModel(10,0,100,2),
		vSpinnerModel = new SpinnerNumberModel(10,0,100,2);
	
	private JSpinner
		uSpinner = new JSpinner(uSpinnerModel),
		vSpinner = new JSpinner(vSpinnerModel);

	private JCheckBox
		vectorFieldBox = new JCheckBox("vf");
	
	private int activeSurfaceIndex = 0;
	
	public NurbsManagerPlugin() {
		GridBagConstraints c = new GridBagConstraints();
		c.fill = BOTH;
		c.weightx = 1.0;
		c.insets = new Insets(2, 2, 2, 2);
		c.gridwidth = GridBagConstraints.REMAINDER;

		configureFileChooser();
		importButton.addActionListener(this);
		importButton.setToolTipText("Load Nurbs surface");
		updateButton.addActionListener(this);
		integralCurveButton.addActionListener(this);
		
		JPanel tablePanel = new JPanel();
		tablePanel.setLayout(new GridLayout());
		tablePanel.add(layersScroller);
		layersScroller.setMinimumSize(new Dimension(30, 150));
		surfacesTable.getTableHeader().setPreferredSize(new Dimension(10, 0));
		surfacesTable.setRowHeight(22);
		surfacesTable.getSelectionModel().setSelectionMode(SINGLE_SELECTION);
		
		surfaceToolbar.add(importAction);
		surfaceToolbar.add(new JToolBar.Separator());
		surfaceToolbar.add(vectorFieldBox);
		surfaceToolbar.add(new JToolBar.Separator());
		surfaceToolbar.add(uSpinner);
		surfaceToolbar.add(vSpinner);
		surfaceToolbar.add(new JToolBar.Separator());
		surfaceToolbar.add(updateButton);
		surfaceToolbar.setFloatable(false);
		
		c.weighty = 0.0;
		shrinkPanel.add(surfaceToolbar, c);
		c.weighty = 1.0;
		shrinkPanel.add(tablePanel, c);
		c.weighty = 0.0;
		shrinkPanel.add(integralCurveButton,c);
	}

	private void configureFileChooser() {
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setFileSelectionMode(FILES_ONLY);
		chooser.setMultiSelectionEnabled(false);
		chooser.addChoosableFileFilter(new FileFilter(){
			@Override
			public boolean accept(File f) {
				return f.isDirectory() || f.getName().toLowerCase().endsWith(".obj");
			}

			@Override
			public String getDescription() {
				return "Wavefront OBJ (*.obj)";
			}
			
			@Override
			public String toString() {
				return getDescription();
			}
		});

		chooser.addChoosableFileFilter(new FileFilter(){
			@Override
			public boolean accept(File f) {
				return f.isDirectory() || f.getName().toLowerCase().endsWith(".obj");
			}

			@Override
			public String getDescription() {
				return "Halfedge Geomtry (*.obj)";
			}
			
			@Override
			public String toString() {
				return getDescription();
			}
		});
	}	
	
	private class ImportAction extends AbstractAction {
		
		private static final long 
			serialVersionUID = 1L;

		public ImportAction() {
			putValue(NAME, "Import");
			putValue(SMALL_ICON, ImageHook.getIcon("folder.png"));
			putValue(SHORT_DESCRIPTION, "Import");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			Window w = SwingUtilities.getWindowAncestor(shrinkPanel);
			chooser.setDialogTitle("Import Into Layer");
			int result = chooser.showOpenDialog(w);
			if (result != JFileChooser.APPROVE_OPTION) return;
			File file = chooser.getSelectedFile();
			try {
				if (file.getName().toLowerCase().endsWith(".obj")) {
					NURBSSurface surface = NurbsIO.readNURBS(new FileReader(file));
					surface.setName(file.getName());
					surfaces.add(surface);
					activeSurfaceIndex = surfaces.size()-1;
				} 
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(w, ex.getMessage(), ex.getClass().getSimpleName(), ERROR_MESSAGE);
			}
			updateStates();
		}
	}

	private class SurfaceTableModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		private String[] columnNames = new String[]{"Name"};
		
		@Override
		public String getColumnName(int col) {
	        return columnNames[col].toString();
	    }
		
	    @Override
		public int getRowCount() { 
	    	return (surfaces==null)?0:surfaces.size();
	    }
	    
	    @Override
		public int getColumnCount() { 
	    	return 1; 
	    }
	    
	    @Override
		public Object getValueAt(int row, int col) {
	        return surfaces.get(row).getName();
	    }
	    
	    @Override
		public boolean isCellEditable(int row, int col) {
	    	return true; 
	    }
	    
	    @Override
		public void setValueAt(Object value, int row, int col) {
	        surfaces.get(row).setName((String)value);
	    }
	}
	
	private void updateStates() {
		surfacesTable.revalidate();
		surfacesTable.getSelectionModel().setSelectionInterval(activeSurfaceIndex,activeSurfaceIndex);
	}
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		hif = c.getPlugin(HalfedgeInterface.class);
		surfacesTable.setModel(new SurfaceTableModel());
		surfacesTable.setSelectionMode(SINGLE_SELECTION);
		surfacesTable.getSelectionModel().setSelectionInterval(activeSurfaceIndex,activeSurfaceIndex);
	}
	
	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		return View.class;
	}

	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Nurbs Manager", "Nurbs Team");
		return info;
	}
	@Override
	public void storeStates(Controller c) throws Exception {
		super.storeStates(c);
		String chooserDir = chooser.getCurrentDirectory().getAbsolutePath();
		c.storeProperty(getClass(), "importExportLocation", chooserDir);
	}
	
	@Override
	public void restoreStates(Controller c) throws Exception {
		super.restoreStates(c);
		String chooserDir = System.getProperty("user.dir");
		chooserDir = c.getProperty(getClass(), "importExportLocation", chooserDir);
		File chooserDirFile = new File(chooserDir);
		if (chooserDirFile.exists()) {
			chooser.setCurrentDirectory(chooserDirFile);
		}
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		Object src = ae.getSource();
		if(src == updateButton) {
			NURBSSurfaceFactory qmf = new NURBSSurfaceFactory();
			qmf.setGenerateVertexNormals(true);
			qmf.setGenerateFaceNormals(true);
			qmf.setGenerateEdgesFromFaces(true);
			qmf.setULineCount(uSpinnerModel.getNumber().intValue());
			qmf.setVLineCount(vSpinnerModel.getNumber().intValue());
			qmf.setSurface(surfaces.get(surfacesTable.getSelectedRow()));
			qmf.update();

			hif.set(qmf.getGeometry());
			hif.update();
			hif.addLayerAdapter(qmf.getUVAdapter(), false);
			if(vectorFieldBox.isSelected()) {
				hif.addLayerAdapter(qmf.getMinCurvatureVectorField(),false);
				hif.addLayerAdapter(qmf.getMaxCurvatureVectorField(),false);
			}
		} else if(src == integralCurveButton) {
			double[] tspan = {0,1.527};
			double[] y0 = {0.25,0.5};
			double tol = 0.001;
//			IntegralCurves.rungeKutta(surfaces.get(surfacesTable.getSelectedRow()), tspan, y0, tol, true);
			PointSetFactory psf = new PointSetFactory();
			psf.setVertexCount(1);
			psf.setVertexCoordinates(new double[]{0,0,0});
			
			psf.update();
			SceneGraphComponent sgc = new SceneGraphComponent("Integral Curves");
			SceneGraphComponent minCurveComp = new SceneGraphComponent("Min Curve");
			sgc.addChild(minCurveComp);
			sgc.setGeometry(psf.getGeometry());
			hif.getActiveLayer().addTemporaryGeometry(sgc);
		}
	}
}
