package org.solovyev.android.plotter.meshes;

import org.solovyev.android.plotter.Dimensions;
import org.solovyev.android.plotter.Function;

import javax.annotation.Nonnull;

public class FunctionGraph2d extends BaseCurve implements FunctionGraph {

	@Nonnull
	private volatile Function function;

	private FunctionGraph2d(@Nonnull Dimensions dimensions, @Nonnull Function function) {
		super(dimensions);
		this.function = function;
	}

	@Nonnull
	public static FunctionGraph2d create(@Nonnull Dimensions dimensions, @Nonnull Function function) {
		return new FunctionGraph2d(dimensions, function);
	}

	@Override
	protected float y(float x) {
		final Function f = function;
		switch (f.getArity()) {
			case 0:
				return f.evaluate();
			case 1:
				return f.evaluate(x);
			case 2:
				return f.evaluate(x, 0);
			default:
				throw new IllegalArgumentException();
		}
	}

	@Override
	public void setFunction(@Nonnull Function function) {
		// todo serso: might be called on GL thread, requires synchronization
		if (!this.function.equals(function)) {
			this.function = function;
			setDirty();
		}
	}

	@Nonnull
	@Override
	public Function getFunction() {
		return function;
	}

	@Nonnull
	@Override
	protected BaseMesh makeCopy() {
		return create(dimensions.get(), function);
	}

	@Override
	public String toString() {
		return function.toString();
	}
}
