package org.solovyev.android.plotter.meshes;

import org.solovyev.android.plotter.Dimensions;
import org.solovyev.android.plotter.Function;

import javax.annotation.Nonnull;

public class FunctionGraph extends BaseSurface {

	@Nonnull
	public static final DoubleBufferMesh.Swapper<FunctionGraph> SWAPPER = new Swapper();

	@Nonnull
	private volatile Function function;

	FunctionGraph(float width, float height, int widthVertices, int heightVertices, @Nonnull Function function) {
		super(width, height, widthVertices, heightVertices);
		this.function = function;
	}

	FunctionGraph(@Nonnull Dimensions dimensions, int widthVertices, int heightVertices, @Nonnull Function function) {
		super(dimensions, widthVertices, heightVertices);
		this.function = function;
	}

	@Nonnull
	public static FunctionGraph create(float width, float height, int widthVertices, int heightVertices, @Nonnull Function function) {
		return new FunctionGraph(width, height, widthVertices, heightVertices, function);
	}

	@Nonnull
	public static FunctionGraph create(@Nonnull Dimensions dimensions, int widthVertices, int heightVertices, @Nonnull Function function) {
		return new FunctionGraph(dimensions, widthVertices, heightVertices, function);
	}

	@Nonnull
	public static FunctionGraph create(@Nonnull Function function) {
		return new FunctionGraph(5f, 5f, 10, 10, function);
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

	public void setFunction(@Nonnull Function function) {
		// todo serso: might be called on GL thread, requires synchronization
		if (!this.function.equals(function)) {
			this.function = function;
			setDirty();
		}
	}

	@Nonnull
	@Override
	protected BaseMesh makeCopy() {
		return new FunctionGraph(dimensions.graph.width, dimensions.graph.height, widthVertices, heightVertices, function);
	}

	private static class Swapper extends BaseMesh.Swapper<FunctionGraph> {
		@Override
		public void swap(@Nonnull FunctionGraph current, @Nonnull FunctionGraph next) {
			super.swap(current, next);
			next.setFunction(current.function);
			next.setDimensions(current.dimensions);
		}
	}

	@Override
	public String toString() {
		return function.toString();
	}
}
