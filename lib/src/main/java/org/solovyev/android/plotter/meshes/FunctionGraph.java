package org.solovyev.android.plotter.meshes;

import org.solovyev.android.plotter.Function;

import javax.annotation.Nonnull;

public interface FunctionGraph extends DimensionsAware {
	void setFunction(@Nonnull Function function);
	@Nonnull
	Function getFunction();
}
