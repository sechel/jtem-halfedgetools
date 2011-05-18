package de.jtem.halfedgetools.plugin.data.color;

import java.awt.Color;

public abstract class ColorMap {

	public abstract Color getColor(double val, double minValue, double maxValue);
	
}
