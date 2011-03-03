package de.jtem.halfedgetools.plugin;

import java.awt.Color;

public abstract class ColorMap {

	public abstract Color getColor(double val, double minValue, double maxValue);
	
}
