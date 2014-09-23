package org.solovyev.android.plotter.meshes;

import org.solovyev.android.plotter.Function0;

public class WireFramePlane extends FunctionGraph {

	public WireFramePlane() {
		this(1, 1, 2, 2);
	}

	public WireFramePlane(float width, float height) {
		this(width, height, 2, 2);
	}

	public WireFramePlane(float width, float height, int widthVertices, int heightVertices) {
		super(width, height, widthVertices, heightVertices, new Function0() {
			@Override
			public float evaluate() {
				return 0;
			}
		});
	}
}