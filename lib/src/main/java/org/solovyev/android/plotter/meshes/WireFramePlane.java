package org.solovyev.android.plotter.meshes;

import org.solovyev.android.plotter.Dimensions;
import org.solovyev.android.plotter.Function0;

import javax.annotation.Nonnull;

public class WireFramePlane extends FunctionGraph3d {

	public WireFramePlane() {
		this(1, 1, 2, 2);
	}

	public WireFramePlane(float width, float height) {
		this(width, height, 2, 2);
	}

	public WireFramePlane(float width, float height, int widthVertices, int heightVertices) {
		super(width, height, widthVertices, heightVertices, Function0.ZERO);
	}

	public WireFramePlane(@Nonnull Dimensions dimensions, int widthVertices, int heightVertices) {
		super(dimensions, widthVertices, heightVertices, Function0.ZERO);
	}

	@Nonnull
	public static WireFramePlane create(@Nonnull Dimensions dimensions, int widthVertices, int heightVertices) {
		return new WireFramePlane(dimensions, widthVertices, heightVertices);
	}

	@Nonnull
	@Override
	protected BaseMesh makeCopy() {
		return new WireFramePlane(dimensions.graph.width, dimensions.graph.height, widthVertices, heightVertices);
	}
}