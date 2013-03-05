package de.jtem.halfedgetools.plugin.algorithm;

public enum AlgorithmCategory {

	Subdivision("Subdivision"),
	Simplification("Simplification"),
	Editing("Editing"),
	Generator("Generators"),
	File("File"),
	Selection("Selection"),
	Topology("Topology"),
	Geometry("Geometry"),
	VectorField("Vector Fields"),
	TextureRemeshing("Texture Remeshing"),
	DDG("DDG"),
	Custom("Custom");
	
	private String
		name = null;
	
	private AlgorithmCategory(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
}
