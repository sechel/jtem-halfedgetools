/**
 * 
 */
package de.jtem.halfedgetools.adapter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author josefsso
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Bundle {
	
	public static enum BundleType {
		Tensor,
		Color,
		Direction,
		Affine,
		Text,
		Value,
		Transformation
	}
	
	public enum DisplayType {
		Debug,
		Geometry,
		List,
		Label
	}

	public int dimension();
	
	public BundleType type();
	
	public DisplayType display();
	
	public String name();
	
}
