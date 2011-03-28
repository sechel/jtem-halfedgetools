package de.jtem.halfedgetools.nurbs;

import static java.awt.GridBagConstraints.BOTH;
import static javax.swing.JFileChooser.FILES_ONLY;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.ListSelectionModel.SINGLE_SELECTION;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
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
import de.jreality.plugin.JRViewer;
import de.jreality.plugin.basic.View;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultPointShader;
import de.jreality.shader.ShaderUtility;
import de.jreality.ui.LayoutFactory;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.io.NurbsIO;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.image.ImageHook;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.jtem.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;
import de.jtem.jrworkspace.plugin.sidecontainer.widget.ShrinkPanel;

public class NurbsManagerPlugin extends ShrinkPanelPlugin implements ActionListener{
	
	

	private HalfedgeInterface 
		hif = null;
	
	private JFileChooser 
		chooser = new JFileChooser();
	
	private Action
		importAction = new ImportAction();
	
	private GeodesicPanel
		geodesicPanel = new GeodesicPanel();
	
	private JButton
		importButton = new JButton(importAction),
		updateButton = new JButton("update"),
		integralCurveButton = new JButton("Integral curve");

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
		shrinkPanel.add(integralCurveButton, c);
		c.weighty = 0.0;
		shrinkPanel.add(geodesicPanel, c);
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
	
	private class GeodesicPanel extends ShrinkPanel implements ActionListener{
		
		private static final long 
			serialVersionUID = 1L;
		private SpinnerNumberModel
			tolExpModel = new SpinnerNumberModel(-3, -30.0, 0, 1),
			epsExpModel = new SpinnerNumberModel(-3, -30.0, 0, 1),
			dirXModel = new SpinnerNumberModel(1.0, -100.0, 100.0, 0.01),
			dirYModel = new SpinnerNumberModel(0.0, -100.0, 100.0, 0.01);
		private JSpinner
			tolSpinner = new JSpinner(tolExpModel),
			epsSpinner = new JSpinner(epsExpModel),
			xSpinner = new JSpinner(dirXModel),
			ySpinner = new JSpinner(dirYModel);
		private JButton
			goButton = new JButton("Go");
		
		public GeodesicPanel() {
			super("Geodesic");
			setShrinked(true);
			setLayout(new GridBagLayout());
			GridBagConstraints lc = LayoutFactory.createLeftConstraint();
			GridBagConstraints rc = LayoutFactory.createRightConstraint();
			add(new JLabel("Direction X"), lc);
			add(xSpinner, rc);
			add(new JLabel("Direction Y"), lc);
			add(ySpinner, rc);
			add(new JLabel("Tolerance Exp"), lc);
			add(tolSpinner, rc);
			add(new JLabel("Eps Exp"), lc);
			add(epsSpinner, rc);
			add(goButton, rc);
			
			goButton.addActionListener(this);
		}
		
		public void actionPerformed(ActionEvent e){
			double tol = tolExpModel.getNumber().doubleValue();
			tol = Math.pow(10, tol);
			double eps = epsExpModel.getNumber().doubleValue();
			eps = Math.pow(10, eps);
			double dirX = dirXModel.getNumber().doubleValue();
			double dirY = dirYModel.getNumber().doubleValue();
			
			Set<Vertex<?,?,?>> verts = hif.getSelection().getVertices();
			AdapterSet as = hif.getAdapters();
			for(Vertex<?,?,?> v : verts) {
				double[] y0 = new double[4];
					y0[0] = as.getD(NurbsUVCoordinate.class, v)[0];
					y0[1] = as.getD(NurbsUVCoordinate.class, v)[1];
					y0[2] = dirX;
					y0[3] = dirY;
				LinkedList<double[]> all = new LinkedList<double[]>();
				
				LinkedList<double[]> pts = IntegralCurves.geodesicExponential(surfaces.get(surfacesTable.getSelectedRow()), y0, 0.01, tol);
				all.addAll(pts);
				PointSetFactory psf = new PointSetFactory();
				int p = surfaces.get(surfacesTable.getSelectedRow()).p;
				int q = surfaces.get(surfacesTable.getSelectedRow()).q;
				double[] U = surfaces.get(surfacesTable.getSelectedRow()).U;
				double[] V = surfaces.get(surfacesTable.getSelectedRow()).V;
				double[][][]Pw = surfaces.get(surfacesTable.getSelectedRow()).getControlMesh();
				double[][] u = new double[all.size()][];
				double[][] points = new double[all.size()][];
				for (int i = 0; i < u.length; i++) {
					u[i] = all.get(i);
				}

				psf.setVertexCount(u.length);
				for (int i = 0; i < u.length; i++) {
					double[] S = new double[4];
					NURBSAlgorithm.SurfacePoint(p, U, q, V, Pw, u[i][0], u[i][1], S);
					points[i] = S;
//						System.out.println(Arrays.toString(points[i]));
				}
				psf.setVertexCoordinates(points);
				psf.update();
				SceneGraphComponent sgc = new SceneGraphComponent("Integral Curves");
				SceneGraphComponent minCurveComp = new SceneGraphComponent("Geodesic");
				sgc.addChild(minCurveComp);
				sgc.setGeometry(psf.getGeometry());
				Appearance labelAp = new Appearance();
				sgc.setAppearance(labelAp);
				DefaultGeometryShader dgs = ShaderUtility.createDefaultGeometryShader(labelAp, false);
				DefaultPointShader pointShader = (DefaultPointShader)dgs.getPointShader();
					pointShader.setDiffuseColor(Color.orange);
				hif.getActiveLayer().addTemporaryGeometry(sgc);
			}
	
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
			hif.addLayerAdapter(qmf.getUVAdapter(), false);
			if(vectorFieldBox.isSelected()) {
				hif.addLayerAdapter(qmf.getMinCurvatureVectorField(),false);
				hif.addLayerAdapter(qmf.getMaxCurvatureVectorField(),false);
			}
			hif.update();
		} else if(src == integralCurveButton) {
	
			Set<Vertex<?,?,?>> verts = hif.getSelection().getVertices();
			AdapterSet as = hif.getAdapters();
			for(Vertex<?,?,?> v : verts) {
				double[] y0 = as.getD(NurbsUVCoordinate.class, v);
				double tol = 0.1;
				boolean max;

				for (int j = 0; j < 2; j++) {
					LinkedList<double[]> all = new LinkedList<double[]>();
					if(j==0){
						max = false;
					}else{
						max = true;
					}
					IntObjects intObj = IntegralCurves.rungeKutta(surfaces.get(surfacesTable.getSelectedRow()), y0, tol,false, max,0.01);
					all.addAll(intObj.getPoints());
					if(!intObj.isNearby()){
						intObj = IntegralCurves.rungeKutta(surfaces.get(surfacesTable.getSelectedRow()), y0, tol,true, max,0.01);
					}
					all.addAll(intObj.getPoints());
					PointSetFactory psf = new PointSetFactory();
					int p = surfaces.get(surfacesTable.getSelectedRow()).p;
					int q = surfaces.get(surfacesTable.getSelectedRow()).q;
					double[] U = surfaces.get(surfacesTable.getSelectedRow()).U;
					double[] V = surfaces.get(surfacesTable.getSelectedRow()).V;
					double[][][]Pw = surfaces.get(surfacesTable.getSelectedRow()).getControlMesh();
					double[][] u = new double[all.size()][];
					double[][] points = new double[all.size()][];
					for (int i = 0; i < u.length; i++) {
						u[i] = all.get(i);
					}

					psf.setVertexCount(u.length);
					for (int i = 0; i < u.length; i++) {
						double[] S = new double[4];
						NURBSAlgorithm.SurfacePoint(p, U, q, V, Pw, u[i][0], u[i][1], S);
						points[i] = S;
					}
					psf.setVertexCoordinates(points);
					psf.update();
					SceneGraphComponent sgc = new SceneGraphComponent("Integral Curves");
					SceneGraphComponent minCurveComp = new SceneGraphComponent("Min Curve");
					sgc.addChild(minCurveComp);
					sgc.setGeometry(psf.getGeometry());
					Appearance labelAp = new Appearance();
					sgc.setAppearance(labelAp);
					DefaultGeometryShader dgs = ShaderUtility.createDefaultGeometryShader(labelAp, false);
					DefaultPointShader pointShader = (DefaultPointShader)dgs.getPointShader();
					if(max){
						pointShader.setDiffuseColor(Color.red);
					}else{
						pointShader.setDiffuseColor(Color.cyan);
					}
					hif.getActiveLayer().addTemporaryGeometry(sgc);
				}
	
			}
		} 

//			int n = 151;
//			LinkedList<double[]> umb = IntegralCurves.umbilicPoints1(surfaces.get(surfacesTable.getSelectedRow()), n);
//			double[][]umbPoints = new double[umb.size()][];
//			for (int i = 0; i < umb.size(); i++) {
//				//System.out.println(Arrays.toString(umb.get(i)));
//
//				umbPoints[i] = umb.get(i);
//				
//			}
//			if(umbPoints.length > 0){
//				PointSetFactory psfu = new PointSetFactory();
//				int pu = surfaces.get(surfacesTable.getSelectedRow()).p;
//				int qu = surfaces.get(surfacesTable.getSelectedRow()).q;
//				double[] Uu = surfaces.get(surfacesTable.getSelectedRow()).U;
//				double[] Vu = surfaces.get(surfacesTable.getSelectedRow()).V;
//				double[][][]Pwu = surfaces.get(surfacesTable.getSelectedRow()).getControlMesh();
//				double[][] surfaceUmbilics = new double[umb.size()][];
//				psfu.setVertexCount(umbPoints.length);
//				for (int i = 0; i < umbPoints.length; i++) {
//					double[] S = new double[4];
//					NURBSAlgorithm.SurfacePoint(pu, Uu, qu, Vu, Pwu, umbPoints[i][0], umbPoints[i][1], S);
//					surfaceUmbilics[i] = S;
//				}
//				psfu.setVertexCoordinates(surfaceUmbilics);
//				psfu.update();
//				SceneGraphComponent sgcu = new SceneGraphComponent("Umbilics");
//				SceneGraphComponent uPointComp = new SceneGraphComponent("U points");
//				sgcu.addChild(uPointComp);
//				sgcu.setGeometry(psfu.getGeometry());
//				Appearance labelApu = new Appearance();
//				sgcu.setAppearance(labelApu);
//				DefaultGeometryShader dgsu = ShaderUtility.createDefaultGeometryShader(labelApu, false);
//				DefaultPointShader pointShaderu = (DefaultPointShader)dgsu.getPointShader();
//				pointShaderu.setDiffuseColor(Color.black);
//				pointShaderu.setPointRadius(0.08);
//				hif.getActiveLayer().addTemporaryGeometry(sgcu);
//			}
	}
	
	@Override
	public void mainUIChanged(String uiClass) {
		super.mainUIChanged(uiClass);
		SwingUtilities.updateComponentTreeUI(chooser);
	}
	
	public static void main(String[] args) {
		JRViewer v = new JRViewer();
		v.addContentUI();
		v.registerPlugin(new NurbsManagerPlugin());
		v.startup();
	}
}
