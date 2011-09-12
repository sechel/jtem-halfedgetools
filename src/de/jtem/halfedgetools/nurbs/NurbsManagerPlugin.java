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
import java.util.Collections;
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
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;

import de.jreality.geometry.PointSetFactory;
import de.jreality.math.Rn;
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
import de.jtem.halfedgetools.plugin.HalfedgeLayer;
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
	
	private CurvatureLinesPanel
		curvatureLinesPanel = new CurvatureLinesPanel();
	
	private JButton
		importButton = new JButton(importAction),
		updateButton = new JButton("update");
//		integralCurveButton = new JButton("Integral curve");

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
		shrinkPanel.add(curvatureLinesPanel, c);
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
			tolExpModel = new SpinnerNumberModel(-2, -30.0, 0, 1),
			epsExpModel = new SpinnerNumberModel(-2, -30.0, 0, 1),
			nearbyModel = new SpinnerNumberModel(-3, -30.0, 0, 1);
		private JSpinner
			tolSpinner = new JSpinner(tolExpModel),
			epsSpinner = new JSpinner(epsExpModel),
			nearbySpinner = new JSpinner(nearbyModel);
		private JButton
			goButton = new JButton("Go");
		private JRadioButton
			segmentButton = new JRadioButton("Geodesic Segment");
		
		
		public GeodesicPanel() {
			super("Geodesic");
			setShrinked(true);
			setLayout(new GridBagLayout());
			GridBagConstraints lc = LayoutFactory.createLeftConstraint();
			GridBagConstraints rc = LayoutFactory.createRightConstraint();
			add(new JLabel("Tolerance Exp"), lc);
			add(tolSpinner, rc);
			add(new JLabel("Eps Exp"), lc);
			add(epsSpinner, rc);
			add(new JLabel("nearby target Exp"), lc);
			add(nearbySpinner, rc);
			add(segmentButton, rc);
			add(goButton, rc);
			
			goButton.addActionListener(this);
			
		}
		
		public void actionPerformed(ActionEvent e){
			double tol = tolExpModel.getNumber().doubleValue();
			tol = Math.pow(10, tol);
			double eps = epsExpModel.getNumber().doubleValue();
			eps = Math.pow(10, eps);
			double nearby = nearbyModel.getNumber().doubleValue();
			nearby = Math.pow(10, nearby);
			
			Set<Vertex<?,?,?>> verts = hif.getSelection().getVertices();
			AdapterSet as = hif.getAdapters();
			double[] a = new double [2];
			double[] b = new double [2];
			if(verts.size() != 2){
				System.out.println("Select only two vertices!");
			}else{
				int index = 0;
				for (Vertex<?,?,?> v : verts) {
					index = index + 1;
					if(index == 1){
						a = as.getD(NurbsUVCoordinate.class, v);
					}else{
						b = as.getD(NurbsUVCoordinate.class, v);
					}
				}
			if(segmentButton.isSelected()){
				LinkedList<double[]> points = IntegralCurves.geodesicSegmentBetweenTwoPoints(surfaces.get(surfacesTable.getSelectedRow()), a, b, eps, tol,nearby);
				PointSetFactory psf = new PointSetFactory();
				int p = surfaces.get(surfacesTable.getSelectedRow()).p;
				int q = surfaces.get(surfacesTable.getSelectedRow()).q;
				double[] U = surfaces.get(surfacesTable.getSelectedRow()).U;
				double[] V = surfaces.get(surfacesTable.getSelectedRow()).V;
				double[][][]Pw = surfaces.get(surfacesTable.getSelectedRow()).getControlMesh();
				double[][] u = new double[points.size()][];
				double[][] surfacePoints = new double[points.size()][];
				for (int i = 0; i < u.length; i++) {
					u[i] = points.get(i);
				}
				psf.setVertexCount(u.length);
				for (int i = 0; i < u.length; i++) {
					double[] S = new double[4];
					NURBSAlgorithm.SurfacePoint(p, U, q, V, Pw, u[i][0], u[i][1], S);
					surfacePoints[i] = S;
//						System.out.println(Arrays.toString(surfacePoints[i]));
				}
				psf.setVertexCoordinates(surfacePoints);
				psf.update();
				SceneGraphComponent sgc = new SceneGraphComponent("geodesic segment");
				SceneGraphComponent minCurveComp = new SceneGraphComponent("Geodesic Segment");
				sgc.addChild(minCurveComp);
				sgc.setGeometry(psf.getGeometry());
				Appearance labelAp = new Appearance();
				sgc.setAppearance(labelAp);
				DefaultGeometryShader dgs = ShaderUtility.createDefaultGeometryShader(labelAp, false);
				DefaultPointShader pointShader = (DefaultPointShader)dgs.getPointShader();
				pointShader.setDiffuseColor(Color.orange);
				hif.getActiveLayer().addTemporaryGeometry(sgc);
				double length = 0;
				for (int i = 0; i < surfacePoints.length - 1; i++) {
					double [] realPoint1 = new double[3];
					realPoint1[0] = surfacePoints[i][0];
					realPoint1[1] = surfacePoints[i][1];
					realPoint1[2] = surfacePoints[i][2];
					double [] realPoint2 = new double[3];
					realPoint2[0] = surfacePoints[i + 1][0];
					realPoint2[1] = surfacePoints[i + 1][1];
					realPoint2[2] = surfacePoints[i + 1][2];
					length = length + Rn.euclideanDistance(realPoint1, realPoint2);
				}
				System.out.println("Geodesic segment length: " + length);
			}else{
				LinkedList<double[]> points = IntegralCurves.geodesicExponentialGivenByTwoPoints(surfaces.get(surfacesTable.getSelectedRow()), a, b, eps, tol,nearby);
				points.addAll(IntegralCurves.geodesicExponentialGivenByTwoPoints(surfaces.get(surfacesTable.getSelectedRow()), b, a, eps, tol, nearby));
				PointSetFactory psf = new PointSetFactory();
				int p = surfaces.get(surfacesTable.getSelectedRow()).p;
				int q = surfaces.get(surfacesTable.getSelectedRow()).q;
				double[] U = surfaces.get(surfacesTable.getSelectedRow()).U;
				double[] V = surfaces.get(surfacesTable.getSelectedRow()).V;
				double[][][]Pw = surfaces.get(surfacesTable.getSelectedRow()).getControlMesh();
				double[][] u = new double[points.size()][];
				double[][] surfacePoints = new double[points.size()][];
				for (int i = 0; i < u.length; i++) {
					u[i] = points.get(i);
				}
				psf.setVertexCount(u.length);
				for (int i = 0; i < u.length; i++) {
					double[] S = new double[4];
					NURBSAlgorithm.SurfacePoint(p, U, q, V, Pw, u[i][0], u[i][1], S);
					surfacePoints[i] = S;
//						System.out.println(Arrays.toString(surfacePoints[i]));
				}
				psf.setVertexCoordinates(surfacePoints);
				psf.update();
				SceneGraphComponent sgc = new SceneGraphComponent("geodesic segment");
				SceneGraphComponent minCurveComp = new SceneGraphComponent("Geodesic Segment");
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
	}
	
	private class CurvatureLinesPanel extends ShrinkPanel implements ActionListener{
		
		private static final long 
			serialVersionUID = 1L;
		private SpinnerNumberModel
			tolExpModel = new SpinnerNumberModel(-2, -30.0, 0, 1),
			epsExpModel = new SpinnerNumberModel(-2, -30.0, 0, 1),
			stepSizeModel = new SpinnerNumberModel(-2, -30.0, 0, 1);
			
		private JSpinner
			tolSpinner = new JSpinner(tolExpModel),
			epsSpinner = new JSpinner(epsExpModel),
			stepSizeSpinner = new JSpinner(stepSizeModel);
		private JButton
			goButton = new JButton("Go");
		private JRadioButton
			maxButton = new JRadioButton("Max Curvature (red)"),
			minButton = new JRadioButton("Min Curvature (cyan)"),
			intersectionButton = new JRadioButton("Intersection points");
		
		public CurvatureLinesPanel() {
			super("Curvature Lines");
			setShrinked(true);
			setLayout(new GridBagLayout());
			GridBagConstraints lc = LayoutFactory.createLeftConstraint();
			GridBagConstraints rc = LayoutFactory.createRightConstraint();
			add(new JLabel("Tolerance Exp"), lc);
			add(tolSpinner, rc);
			add(new JLabel("Eps Exp"), lc);
			add(epsSpinner, rc);
			add(new JLabel("Step Size Exp"), lc);
			add(stepSizeSpinner, rc);
			add(maxButton, rc);
			add(minButton, rc);
			add(intersectionButton, rc);
			add(goButton, rc);
			
			goButton.addActionListener(this);
		}
		
		
		public void actionPerformed(ActionEvent e){
			double tol = tolExpModel.getNumber().doubleValue();
			tol = Math.pow(10, tol);
			double eps = epsExpModel.getNumber().doubleValue();
			eps = Math.pow(10, eps);
			double stepSize = stepSizeModel.getNumber().doubleValue();
			stepSize = Math.pow(10, stepSize);
			Set<Vertex<?,?,?>> verts = hif.getSelection().getVertices();
			AdapterSet as = hif.getAdapters();
			int n = 201;
			LinkedList<double[]> umbilics = IntegralCurves.umbilicPoints(surfaces.get(surfacesTable.getSelectedRow()), n);
			PointSetFactory psfu = new PointSetFactory();
			int p = surfaces.get(surfacesTable.getSelectedRow()).p;
			int q = surfaces.get(surfacesTable.getSelectedRow()).q;
			double[] U = surfaces.get(surfacesTable.getSelectedRow()).U;
			double[] V = surfaces.get(surfacesTable.getSelectedRow()).V;
			double[][][]Pw = surfaces.get(surfacesTable.getSelectedRow()).getControlMesh();
			double[][] uu = new double[umbilics.size()][];
			double[][] upoints = new double[umbilics.size()][];
			for (int i = 0; i < uu.length; i++) {
				uu[i] = umbilics.get(i);
			}
			psfu.setVertexCount(uu.length);

			for (int i = 0; i < uu.length; i++) {
				double[] S = new double[4];
				NURBSAlgorithm.SurfacePoint(p, U, q, V, Pw, uu[i][0], uu[i][1], S);
				upoints[i] = S;
			}
			if(umbilics.size()>0){
			psfu.setVertexCoordinates(upoints);
			psfu.update();
			SceneGraphComponent sgcu = new SceneGraphComponent("umbilics");
			SceneGraphComponent umbilicComp = new SceneGraphComponent("Max Curve");
			sgcu.addChild(umbilicComp);
			sgcu.setGeometry(psfu.getGeometry());
			Appearance uAp = new Appearance();
			sgcu.setAppearance(uAp);
			DefaultGeometryShader udgs = ShaderUtility.createDefaultGeometryShader(uAp, false);
			DefaultPointShader upointShader = (DefaultPointShader)udgs.getPointShader();
			upointShader.setDiffuseColor(Color.black);
			hif.getActiveLayer().addTemporaryGeometry(sgcu);
			}
			boolean max = maxButton.isSelected();
			boolean min = minButton.isSelected();
			boolean inter = intersectionButton.isSelected();
			LinkedList<LineSegmentIntersection> segments = new LinkedList<LineSegmentIntersection>();
			int counter = 1;
			LinkedList<Integer> umbilicIndex = new LinkedList<Integer>();
			for(Vertex<?,?,?> v : verts) {
				double[] y0 = as.getD(NurbsUVCoordinate.class, v);
					if (max){
						counter = curveLine(tol, eps, stepSize, umbilics, p, q,
								U, V, Pw, segments, counter, umbilicIndex, y0, true);
					}
					if (min){
						counter = curveLine(tol, eps, stepSize, umbilics, p, q,
								U, V, Pw, segments, counter, umbilicIndex, y0, false);
					}
			}
			hif.clearSelection();
			if(inter){
				// default patch
				LinkedList<LineSegmentIntersection> boundarySegments = new LinkedList<LineSegmentIntersection>();
				double[][] seg1 = {{0.001,0.001},{0.999,0.001}};
				LineSegmentIntersection b1 = new LineSegmentIntersection(seg1, 1, 1, true);
				double[][] seg2 = {{0.999,0.001},{0.999,0.999}};
				LineSegmentIntersection b2 = new LineSegmentIntersection(seg2, 1, 2, true);
				double[][] seg3 = {{0.999,0.999},{0.001,0.999}};
				LineSegmentIntersection b3 = new LineSegmentIntersection(seg3, 1, 3, true);
				double[][] seg4 = {{0.001,0.999},{0.001,0.001}};
				LineSegmentIntersection b4 = new LineSegmentIntersection(seg4, 1, 4, true);
				boundarySegments.add(b1);
				boundarySegments.add(b2);
				boundarySegments.add(b3);
				boundarySegments.add(b4);
				int shiftedIndex = boundarySegments.size();
				for (LineSegmentIntersection s : segments) {
					s.setCurveIndex(s.curveIndex + shiftedIndex);
				}
				segments.addAll(boundarySegments);
				LinkedList<IntersectionPoint> curveCurveIntersections = LineSegmentIntersection.findIntersections(segments);
				LinkedList<HalfedgePoint> hp1 = LineSegmentIntersection.findAllNbrs1(curveCurveIntersections);
				curveCurveIntersections = LineSegmentIntersection.bruteForceCurveCurveIntersection(segments);
				LinkedList<IntersectionPoint> intersections = new LinkedList<IntersectionPoint>();
				intersections.addAll(curveCurveIntersections);
				LinkedList<HalfedgePoint> hp = LineSegmentIntersection.findAllNbrs(intersections);
				System.out.println("HALFEDGE points:");
				for (HalfedgePoint halfedgePoints : hp) {
					System.out.println(halfedgePoints.toString());
				}
				System.out.println("Fertige Punkte");
//				LinkedList<HalfedgePoint> H = LineSegmentIntersection.orientedNbrs(hp);
				LinkedList<HalfedgePoint> H1 = LineSegmentIntersection.orientedNbrs1(hp1);
				System.out.println("INTERSECTION SIZE "+intersections.size());
//				FaceSet fS = LineSegmentIntersection.createFaceSet(H);
				FaceSet fS = LineSegmentIntersection.createFaceSet1(H1);
				for (int i = 0; i < fS.verts.length; i++) {
					double[] S = new double[4];
					NURBSAlgorithm.SurfacePoint(p, U, q, V, Pw, fS.verts[i][0], fS.verts[i][1], S);
					fS.verts[i] = S;
				}
				System.out.println("FACESET:");
				System.out.println(fS.toString());
				HalfedgeLayer hel = new HalfedgeLayer(hif);
				hel.setName("Curvature Geometry");
				hel.set(fS.getIndexedFaceSet());
				hif.addLayer(hel);
				hif.update();
				PointSetFactory psfi = new PointSetFactory();
				
				double[][] iu = new double[intersections.size()][];
				double[][] ipoints = new double[intersections.size()][];
				int c = 0;
				for (IntersectionPoint ip : intersections) {
					iu[c] = ip.point;
					c++;
				}
				psfi.setVertexCount(iu.length);

				for (int i = 0; i < iu.length; i++) {
					double[] S = new double[4];
					NURBSAlgorithm.SurfacePoint(p, U, q, V, Pw, iu[i][0], iu[i][1], S);
					ipoints[i] = S;
				}
				if(curveCurveIntersections.size()>0){
				psfi.setVertexCoordinates(ipoints);
				psfi.update();
				SceneGraphComponent sgci = new SceneGraphComponent("intersection");
				SceneGraphComponent intersectionComp = new SceneGraphComponent("Intersection");
				sgci.addChild(intersectionComp);
				sgci.setGeometry(psfi.getGeometry());
				Appearance iAp = new Appearance();
				sgci.setAppearance(iAp);
				DefaultGeometryShader idgs = ShaderUtility.createDefaultGeometryShader(iAp, false);
				DefaultPointShader ipointShader = (DefaultPointShader)idgs.getPointShader();
				ipointShader.setDiffuseColor(Color.black);
				hif.getActiveLayer().addTemporaryGeometry(sgci);
				}
			}
		}

		private int curveLine(double tol, double eps, double stepSize,
				LinkedList<double[]> umbilics, int p, int q, double[] U,
				double[] V, double[][][] Pw,
				LinkedList<LineSegmentIntersection> segments, int curveIndex,
				LinkedList<Integer> umbilicIndex, double[] y0, boolean maxMin) {
			IntObjects intObj;
			int noSegment;
			LinkedList<double[]> all = new LinkedList<double[]>();
			intObj = IntegralCurves.rungeKutta(surfaces.get(surfacesTable.getSelectedRow()), y0, tol,false, maxMin,eps,stepSize,umbilics);
			if(intObj.umbilicIndex != 0){
				umbilicIndex.add(intObj.umbilicIndex);
			}
			Collections.reverse(intObj.getPoints());
			all.addAll(intObj.getPoints());
			noSegment = all.size();
			System.out.println("first size" + noSegment);
			if(!intObj.isNearby()){
				intObj = IntegralCurves.rungeKutta(surfaces.get(surfacesTable.getSelectedRow()), y0, tol,true, maxMin,eps,stepSize, umbilics);
				if(intObj.umbilicIndex != 0){
					umbilicIndex.add(intObj.umbilicIndex);
				}
				all.addAll(intObj.getPoints());
			}else{
				//add the first element of a closed curve
				System.out.println("add first");
				double[] first = new double [2];
				first[0] = all.getFirst()[0];
				first[1] = all.getFirst()[1];
				all.add(first);
				noSegment = all.size();
			}
			int index = 0;
			double[] firstcurvePoint = all.getFirst();
			for (double[] secondCurvePoint : all) {
				index ++;
				if(index != 1){
					double[][]seg = new double[2][];
					seg[0] = firstcurvePoint;
					seg[1] = secondCurvePoint;
					LineSegmentIntersection lsi = new  LineSegmentIntersection();
					lsi.indexOnCurve = index ;
					lsi.segment = seg;
					lsi.curveIndex = curveIndex;
					lsi.max = maxMin;
					if(index != noSegment + 1){
						segments.add(lsi);
						if(index == noSegment){
							System.out.println("last segment " + segments.getLast().toString());
						}
					}else{
						System.out.println("count "+index+ " stelle " + noSegment);
					}
					firstcurvePoint = secondCurvePoint;
				}
			}
			curveIndex ++;
			PointSetFactory psf = new PointSetFactory();
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
			SceneGraphComponent sgc = new SceneGraphComponent("Integral Curve");
			SceneGraphComponent maxCurveComp = new SceneGraphComponent("Max Curve");
			sgc.addChild(maxCurveComp);
			sgc.setGeometry(psf.getGeometry());
			Appearance labelAp = new Appearance();
			sgc.setAppearance(labelAp);
			DefaultGeometryShader dgs = ShaderUtility.createDefaultGeometryShader(labelAp, false);
			DefaultPointShader pointShader = (DefaultPointShader)dgs.getPointShader();
			if(maxMin){
				pointShader.setDiffuseColor(Color.red);
			}else{
				pointShader.setDiffuseColor(Color.cyan);
			}
			hif.getActiveLayer().addTemporaryGeometry(sgc);
			return curveIndex;
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
