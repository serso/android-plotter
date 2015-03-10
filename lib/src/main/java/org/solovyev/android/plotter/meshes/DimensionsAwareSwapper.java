package org.solovyev.android.plotter.meshes;

import javax.annotation.Nonnull;

public final class DimensionsAwareSwapper implements DoubleBufferMesh.Swapper<DimensionsAware> {

	@Nonnull
	public static final DoubleBufferMesh.Swapper<DimensionsAware> INSTANCE = new DimensionsAwareSwapper();

	private DimensionsAwareSwapper() {
	}

	@Override
	public void swap(@Nonnull DimensionsAware current, @Nonnull DimensionsAware next) {
		next.setColor(current.getColor());
		next.setWidth(current.getWidth());
		next.setDimensions(current.getDimensions());
	}
}
