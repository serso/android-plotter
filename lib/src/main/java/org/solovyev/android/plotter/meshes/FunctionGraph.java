package org.solovyev.android.plotter.meshes;

import org.solovyev.android.plotter.Color;
import org.solovyev.android.plotter.Dimensions;
import org.solovyev.android.plotter.Function;

import javax.annotation.Nonnull;

public interface FunctionGraph extends Mesh {
	void setFunction(@Nonnull Function function);
	void setColor(@Nonnull Color color);
	void setDimensions(@Nonnull Dimensions dimensions);

	@Nonnull
	Function getFunction();
	@Nonnull
	Dimensions getDimensions();
	@Nonnull
	Color getColor();
}
