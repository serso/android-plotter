package org.solovyev.android.plotter.meshes;

import org.solovyev.android.plotter.Function;

import javax.annotation.Nonnull;

public class FunctionGraph extends BaseSurface {

	@Nonnull
	private Function function;

	public FunctionGraph(float width, float height, int widthVertices, int heightVertices, @Nonnull Function function) {
		super(width, height, widthVertices, heightVertices);
		this.function = function;
	}

	@Override
	protected float z(float x, float y, int xi, int yi) {
		switch (function.getArity()) {
			case 0:
				return function.evaluate();
			case 1:
				return function.evaluate(x);
			case 2:
				return function.evaluate(x, y);
			default:
				throw new IllegalArgumentException();
		}
	}

	public void setFunction(@Nonnull Function function) {
		this.function = function;
	}
}
