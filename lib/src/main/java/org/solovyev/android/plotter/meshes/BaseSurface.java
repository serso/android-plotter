package org.solovyev.android.plotter.meshes;

import javax.annotation.Nonnull;
import javax.microedition.khronos.opengles.GL11;

public abstract class BaseSurface extends BaseMesh {

	protected final float width;
	protected final float height;
	protected final int widthVertices;
	protected final int heightVertices;

	protected BaseSurface(@Nonnull GL11 gl, float width, float height, int widthVertices, int heightVertices) {
		super(gl);
		this.width = width;
		this.height = height;
		this.widthVertices = widthVertices;
		this.heightVertices = heightVertices;
	}

	@Override
	public void init(@Nonnull GL11 gl) {
		super.init(gl);

		final float[] vertices = new float[widthVertices * heightVertices * 3];

		final float x0 = -width / 2;
		final float y0 = -height / 2;

		final float dx = width / (widthVertices - 1);
		final float dy = height / (heightVertices - 1);

		int vertex = 0;
		for (int yi = 0; yi < heightVertices; yi++) {
			for (int xi = 0; xi < widthVertices; xi++) {
				final float x = x0 + xi * dx;
				final float y = y0 + yi * dy;
				final float z = z(x, y, xi, yi);

				vertices[vertex++] = x;
				vertices[vertex++] = y;
				vertices[vertex++] = z;
			}
		}

		setVertices(vertices);
	}

	protected abstract float z(float x, float y, int xi, int yi);
}
