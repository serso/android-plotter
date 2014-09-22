package org.solovyev.android.plotter.meshes;

import org.solovyev.android.plotter.Function;

import javax.annotation.Nonnull;
import javax.microedition.khronos.opengles.GL11;

public class FunctionGraph extends BaseSurface {

	@Nonnull
	private final Function function;

	public FunctionGraph(@Nonnull GL11 gl, float width, float height, int widthVertices, int heightVertices, @Nonnull Function function) {
		super(gl, width, height, widthVertices, heightVertices);
		this.function = function;
	}

	@Override
	public void init(@Nonnull GL11 gl) {
		super.init(gl);

		setIndices(makeIndices(widthVertices, heightVertices), IndicesOrder.LINE_STRIP);
	}

	protected static short[] makeIndices(int widthVertices, int heightVertices) {
		final short[] indices = new short[heightVertices * widthVertices];

		int index = 0;
		for (int yi = 0; yi < heightVertices; yi++) {
			for (int xi = 0; xi < widthVertices; xi++) {
				final float rowyi = widthVertices * yi;
				if (yi % 2 == 0) {
					indices[index++] = (short) (rowyi + xi);
				} else {
					indices[index++] = (short) (rowyi + widthVertices - xi - 1);
				}
			}
		}
		return indices;
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
}
