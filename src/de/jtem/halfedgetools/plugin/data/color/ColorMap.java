package de.jtem.halfedgetools.plugin.data.color;

import java.awt.Color;

public enum ColorMap {

	Mono("Mono"), Hue("Hue"), HueInverse("Hue Inverse"), RedGreen("Red Green");

	private String name = "Color Map";

	private ColorMap(String name) {
		this.name = name;
	}

	public Color getColor(double val, double minValue, double maxValue) {
		float relativeValue = (float) ((val - minValue) / (maxValue - minValue));
		switch (this) {
		case RedGreen:
			return new Color(relativeValue, 1 - relativeValue, 0);
		case Mono:
			return Color.BLUE;
		case HueInverse:
			return Color.getHSBColor(2f * relativeValue / 3f, 1f, 1f);			
		case Hue:
		default:
			return Color.getHSBColor(2f/3f - 2f * relativeValue / 3f, 1f, 1f);
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
