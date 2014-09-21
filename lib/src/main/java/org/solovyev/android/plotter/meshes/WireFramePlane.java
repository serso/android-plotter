package org.solovyev.android.plotter.meshes;

import org.solovyev.android.plotter.Color;

import javax.annotation.Nonnull;
import javax.microedition.khronos.opengles.GL11;

public class WireFramePlane extends BaseMesh {

	private final float width;
	private final float height;
	private final int widthVertices;
	private final int heightVertices;

	public WireFramePlane(@Nonnull GL11 gl11) {
		this(gl11, 1, 1, 2, 2);
	}

	public WireFramePlane(@Nonnull GL11 gl11, float width, float height) {
		this(gl11, width, height, 2, 2);
	}

	public WireFramePlane(@Nonnull GL11 gl11, float width, float height, int widthVertices, int heightVertices) {
		super(gl11);
		this.width = width;
		this.height = height;
		this.widthVertices = widthVertices;
		this.heightVertices = heightVertices;

		final float[] vertices = new float[widthVertices * heightVertices * 3];
		final short[] indices = new short[widthVertices * heightVertices];

		final float x0 = -width / 2;
		final float y0 = -height / 2;

		final float dx = width / (widthVertices - 1);
		final float dy = height / (heightVertices - 1);

		int vertex = 0;
		int index = 0;
		for (int yi = 0; yi < heightVertices; yi++) {
			for (int xi = 0; xi < widthVertices; xi++) {
				vertices[vertex++] = x0 + xi * dx;
				vertices[vertex++] = y0 + yi * dy;
				vertices[vertex++] = (float) (Math.sin(vertices[vertex - 2]) + Math.sin(vertices[vertex - 1]));

				final float rowyi = widthVertices * yi;
				if (yi % 2 == 0) {
					indices[index++] = (short) (rowyi + xi);
				} else {
					indices[index++] = (short) (rowyi + widthVertices - xi - 1);
				}
			}
		}

		setIndices(indices, IndicesOrder.LINE_STRIP);
		setVertices(vertices);
	}

	@Override
	protected void onDraw(@Nonnull GL11 gl) {
		super.onDraw(gl);

		//gl.glDrawArrays(GL10.GL_LINE_STRIP, 0, widthVertices * heightVertices);
	}
}