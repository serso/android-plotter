package org.solovyev.android.plotter.meshes;

import org.solovyev.android.plotter.Dimensions;
import org.solovyev.android.plotter.Function;

import javax.annotation.Nonnull;

public class FunctionGraph3d extends BaseSurface implements FunctionGraph {

	@Nonnull
	private volatile Function function;

	FunctionGraph3d(float width, float height, int widthVertices, int heightVertices, @Nonnull Function function) {
		super(width, height, widthVertices, heightVertices);
		this.function = function;
	}

	FunctionGraph3d(@Nonnull Dimensions dimensions, int widthVertices, int heightVertices, @Nonnull Function function) {
		super(dimensions, widthVertices, heightVertices);
		this.function = function;
	}

	@Nonnull
	public static FunctionGraph3d create(float width, float height, int widthVertices, int heightVertices, @Nonnull Function function) {
		return new FunctionGraph3d(width, height, widthVertices, heightVertices, function);
	}

	@Nonnull
	public static FunctionGraph3d create(@Nonnull Dimensions dimensions, int widthVertices, int heightVertices, @Nonnull Function function) {
		return new FunctionGraph3d(dimensions, widthVertices, heightVertices, function);
	}

	@Nonnull
	public static FunctionGraph3d create(@Nonnull Function function) {
		return new FunctionGraph3d(5f, 5f, 10, 10, function);
	}

	@Override
	protected float z(float x, float y, int xi, int yi) {
		final Function f = function;
		switch (f.getArity()) {
			case 0:
				return f.evaluate();
			case 1:
				return f.evaluate(x);
			case 2:
				return f.evaluate(x, y);
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
		return new FunctionGraph3d(dimensions.graph.width, dimensions.graph.height, widthVertices, heightVertices, function);
	}

	@Override
	public String toString() {
		return function.toString();
	}
}
