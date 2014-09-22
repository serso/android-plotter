package org.solovyev.android.plotter.meshes;

import org.solovyev.android.plotter.Function0;

import javax.annotation.Nonnull;
import javax.microedition.khronos.opengles.GL11;

public class WireFramePlane extends FunctionGraph {

	public WireFramePlane(@Nonnull GL11 gl11) {
		this(gl11, 1, 1, 2, 2);
	}

	public WireFramePlane(@Nonnull GL11 gl11, float width, float height) {
		this(gl11, width, height, 2, 2);
	}

	public WireFramePlane(@Nonnull GL11 gl11, float width, float height, int widthVertices, int heightVertices) {
		super(gl11, width, height, widthVertices, heightVertices, new Function0() {
			@Override
			public float evaluate() {
				return 0;
			}
		});
	}
}