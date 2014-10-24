package org.solovyev.android.plotter.meshes;

import org.solovyev.android.plotter.Color;
import org.solovyev.android.plotter.Dimensions;

import javax.annotation.Nonnull;

public interface DimensionsAware extends Mesh {

	void setDimensions(@Nonnull Dimensions dimensions);

	@Nonnull
	Dimensions getDimensions();

	void setColor(@Nonnull Color color);
	@Nonnull
	Color getColor();
}
