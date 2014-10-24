package org.solovyev.android.plotter.meshes;

import javax.annotation.Nonnull;

public final class FunctionGraphSwapper implements DoubleBufferMesh.Swapper<FunctionGraph> {

	@Nonnull
	public static final DoubleBufferMesh.Swapper<FunctionGraph> INSTANCE = new FunctionGraphSwapper();

	private FunctionGraphSwapper() {
	}

	@Override
	public void swap(@Nonnull FunctionGraph current, @Nonnull FunctionGraph next) {
		DimensionsAwareSwapper.INSTANCE.swap(current, next);
		next.setFunction(current.getFunction());
	}
}
