package de.jtem.halfedgetools.plugin.texturespace;

import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;

import de.jreality.math.Matrix;
import de.jreality.scene.SceneGraphPath;
import de.jreality.shader.EffectiveAppearance;

public class AffineTexturePaint extends TexturePaint {

	private SceneGraphPath
		transformationPath = new SceneGraphPath();
	
	public AffineTexturePaint(BufferedImage txtr, Rectangle2D anchor) {
		super(txtr, anchor);
	}

	public void setTransformationPath(SceneGraphPath path) {
		this.transformationPath = path;
	}
	public AffineTransform getTextureTransform() {
		EffectiveAppearance app = EffectiveAppearance.create(transformationPath);
		Matrix texMatrix = (Matrix)app.getAttribute("polygonShader.texture2d:textureMatrix", new Matrix());
		double[] texArr = texMatrix.getArray();
		return new AffineTransform(texArr[0], texArr[4], texArr[1], texArr[5], texArr[3], texArr[7]);
	}
	
	@Override
	public PaintContext createContext(
		ColorModel cm, 
		Rectangle deviceBounds,
		Rectangle2D userBounds, 
		AffineTransform xform, 
		RenderingHints hints
	) {
		AffineTransform T = getTextureTransform();
		try {
			T.invert();
		} catch (NoninvertibleTransformException e) { }
        if (xform == null) {
            xform = T;
        } else {
            xform = (AffineTransform) xform.clone();
            xform.concatenate(T);
        }
		return super.createContext(cm, deviceBounds, userBounds, xform, hints);
	}
	
	
}
