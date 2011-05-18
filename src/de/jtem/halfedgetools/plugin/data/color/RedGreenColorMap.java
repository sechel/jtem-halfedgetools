package de.jtem.halfedgetools.plugin.data.color;

import java.awt.Color;

public class RedGreenColorMap extends ColorMap {

	@Override
	public Color getColor(double val, double minValue, double maxValue) {
		float relativeValue = (float)((val-minValue)/(maxValue-minValue));
		return new Color(relativeValue,1-relativeValue,0);
	}

}
