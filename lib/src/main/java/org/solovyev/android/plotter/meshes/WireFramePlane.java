package org.solovyev.android.plotter.meshes;

import org.solovyev.android.plotter.Dimensions;

import javax.annotation.Nonnull;

public class WireFramePlane extends BaseSurface {

	public WireFramePlane() {
		this(1, 1, 2, 2);
	}

	public WireFramePlane(float width, float height) {
		this(width, height, 2, 2);
	}

	public WireFramePlane(float width, float height, int widthVertices, int heightVertices) {
		super(width, height, widthVertices, heightVertices, true);
	}

	public WireFramePlane(@Nonnull Dimensions dimensions, int widthVertices, int heightVertices) {
		super(dimensions, widthVertices, heightVertices, true);
	}

	@Nonnull
	public static WireFramePlane create(@Nonnull Dimensions dimensions, int widthVertices, int heightVertices) {
		return new WireFramePlane(dimensions, widthVertices, heightVertices);
	}

	@Nonnull
	@Override
	protected BaseMesh makeCopy() {
		return new WireFramePlane(dimensions.dimensions.graph.width, dimensions.dimensions.graph.height, widthVertices, heightVertices);
	}

	@Override
	protected float z(float x, float y, int xi, int yi) {
		return 0;
	}
}