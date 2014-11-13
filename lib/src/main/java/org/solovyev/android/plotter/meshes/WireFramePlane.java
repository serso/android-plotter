package org.solovyev.android.plotter.meshes;

import org.solovyev.android.plotter.Dimensions;

import javax.annotation.Nonnull;

public class WireFramePlane extends BaseSurface {

	public WireFramePlane() {
		this(1, 1);
	}

	public WireFramePlane(float width, float height) {
		super(width, height, true);
	}

	public WireFramePlane(@Nonnull Dimensions dimensions, int widthVertices, int heightVertices) {
		super(dimensions, true);
	}

	@Nonnull
	public static WireFramePlane create(@Nonnull Dimensions dimensions, int widthVertices, int heightVertices) {
		return new WireFramePlane(dimensions, widthVertices, heightVertices);
	}

	@Nonnull
	@Override
	protected BaseMesh makeCopy() {
		return new WireFramePlane(dimensions.d.graph.width(), dimensions.d.graph.height());
	}

	@Override
	protected float z(float x, float y, int xi, int yi) {
		return 0;
	}
}