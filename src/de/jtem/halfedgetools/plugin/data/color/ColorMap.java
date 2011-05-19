package de.jtem.halfedgetools.plugin.data.color;

import java.awt.Color;

public enum ColorMap {

	Hue("Hue"),
	RedGreen("Red Green");
	
	private String 
		name = "Color Map";
	
	private ColorMap(String name) {
		this.name = name;
	}
	
	public Color getColor(double val, double minValue, double maxValue) {
		switch (this) {
		case RedGreen:
			float relativeValue = (float) ((val - minValue) / (maxValue - minValue));
			return new Color(relativeValue, 1 - relativeValue, 0);
		default:
			relativeValue = (float) ((val - minValue) / (maxValue - minValue));
			return Color.getHSBColor(2.0f * relativeValue / 3.0f, 1f, 1f);
		}
	}
	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
}
