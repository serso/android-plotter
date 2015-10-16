package org.solovyev.android.plotter.meshes;

import org.solovyev.android.plotter.Dimensions;

import javax.annotation.Nonnull;

public interface DimensionsAware extends Mesh {

	@Nonnull
	Dimensions getDimensions();

	void setDimensions(@Nonnull Dimensions dimensions);
}
