package de.jtem.halfedgetools.plugin.visualizers;

import static de.jreality.math.Rn.crossProduct;
import static de.jreality.math.Rn.determinant;
import static de.jreality.math.Rn.euclideanNorm;
import static de.jreality.math.Rn.subtract;
import static java.lang.Math.abs;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.jreality.math.Rn;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.jreality.adapter.Adapter;
import de.jtem.halfedgetools.jreality.adapter.ColorAdapter2Ifs;
import de.jtem.halfedgetools.jreality.adapter.LabelAdapter2Ifs;
import de.jtem.halfedgetools.jreality.node.JREdge;
import de.jtem.halfedgetools.jreality.node.JRFace;
import de.jtem.halfedgetools.plugin.VisualizerPlugin;

public class FacePlanarityVisualizer extends VisualizerPlugin implements ChangeListener {

	private DecimalFormat
		format = new DecimalFormat("0.000");
	private SpinnerNumberModel
		placesModel = new SpinnerNumberModel(3, 0, 20, 1);
	private JSpinner	
		placesSpinner = new JSpinner(placesModel);
	private JPanel
		panel = new JPanel();
	
	
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
	

	public static <
		E extends JREdge<?, E, F>, 
		F extends JRFace<?, E, F>
	> double getMaxUnevenness(HalfEdgeDataStructure<?, E, F> hds){
		double maxUneven = 0.0;
		for (F f : hds.getFaces()) {
			double vol = getRelativeUnevenness(f);
			if (vol > maxUneven)
				maxUneven = vol;
		}
		return maxUneven;
	}


	public static <
		E extends JREdge<?, E, F>, 
		F extends JRFace<?, E, F>
	> double getMinUnevenness(HalfEdgeDataStructure<?, E, F> hds){
		double minUneven = Double.MAX_VALUE;
		for (F f : hds.getFaces()) {
			double vol = getRelativeUnevenness(f);
			if (vol < minUneven)
				minUneven = vol;
		}
		return minUneven;
	}


	public static <
		E extends JREdge<?, E, F>, 
		F extends JRFace<?, E, F>
	> double getMeanUnevenness(HalfEdgeDataStructure<?, E, F> hds){
		double meanVol = 0.0;
		int count = 0;
		for (F f : hds.getFaces()) {
			meanVol += getRelativeUnevenness(f);
			count++;
		}
		return meanVol / count;
	}


	public static <
		E extends JREdge<?, E, F>, 
		F extends JRFace<?, E, F>
	> double getRelativeUnevenness(F f) {
		List<E> boundary = HalfEdgeUtils.boundaryEdges(f);
		if (boundary.size() != 4)
			return 0.0;
		double[] a = boundary.get(0).getTargetVertex().position;
		double[] b = boundary.get(1).getTargetVertex().position;
		double[] c = boundary.get(2).getTargetVertex().position;
		double[] d = boundary.get(3).getTargetVertex().position;
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
	
	
	public class PlanarityAdapter <
		E extends JREdge<?, E, F>, 
		F extends JRFace<?, E, F>
	>  implements LabelAdapter2Ifs<F>, ColorAdapter2Ifs<F> {

		@Override
		public AdapterType getAdapterType() {
			return AdapterType.FACE_ADAPTER;
		}

		@Override
		public String getLabel(F f) {
			return format.format(getRelativeUnevenness(f) * 100) + "%";
		}
		
		@Override
		public double[] getColor(F f) {
			double maxUnevenness = getMaxUnevenness(f.getHalfEdgeDataStructure());
			double col = getRelativeUnevenness(f) / maxUnevenness;
			return new double[]{col, 1 - col, 0};
		}
		
	}
	
	@Override
	public JPanel getOptionPanel() {
		return panel;
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public Set<? extends Adapter> getAdapters() {
		return Collections.singleton(new PlanarityAdapter());
	}


	@Override
	public String getName() {
		return "Face Planarity";
	}

}
