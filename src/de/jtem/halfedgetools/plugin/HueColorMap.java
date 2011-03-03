package de.jtem.halfedgetools.plugin;

import java.awt.Color;

public class HueColorMap extends ColorMap {

	@Override
	public Color getColor(double val, double minValue, double maxValue) {
		float relativeValue = (float)((val-minValue)/(maxValue-minValue));
		return Color.getHSBColor(2.0f*relativeValue/3.0f, 1f, 1f);
	}

}
