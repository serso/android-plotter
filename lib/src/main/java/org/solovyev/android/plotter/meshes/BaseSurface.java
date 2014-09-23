package org.solovyev.android.plotter.meshes;

import org.solovyev.android.plotter.MeshConfig;

import javax.annotation.Nonnull;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

/*
 0     1     2     3     4     5
  +----->---->v----->---->v-----+
  |     ^     |     ^     |     ^
  |     |     |     |     |     |
11v   10|    9v    8|    7v    6|
  +------------------------------
  |     ^     |     ^     |     ^
  |     |     |     |     |     |
12v   13|   14v   15|   16v   17|
  +------------------------------
  |     ^     |     ^     |     ^
  |     |     |     |     |     |
23v   22|   21v   20|   19v   18|
  +------------------------------
  |     ^     |     ^     |     ^
  |     |     |     |     |     |
24v   25|   26v   27|   28v   29|
  +---->^----->---->^----->---->^
 */
public abstract class BaseSurface extends BaseMesh {

	protected final float width;
	protected final float height;
	protected final int widthVertices;
	protected final int heightVertices;

	protected BaseSurface(float width, float height, int widthVertices, int heightVertices) {
		this.width = width;
		this.height = height;
		this.widthVertices = widthVertices;
		this.heightVertices = heightVertices;
	}

	@Override
	public void init(@Nonnull GL11 gl, @Nonnull MeshConfig config) {
		super.init(gl, config);

		final float[] vertices = new float[heightVertices * widthVertices * 3];
		final short[] indices = new short[heightVertices * widthVertices];

		fillArrays(vertices, indices);

		setVertices(vertices);
		setIndices(indices, IndicesOrder.LINE_STRIP);
	}

	@Override
	protected void onPostDraw(@Nonnull GL11 gl) {
		super.onPostDraw(gl);
		gl.glDrawArrays(GL10.GL_LINE_STRIP, 0, heightVertices * widthVertices);
	}

	void fillArrays(@Nonnull float[] vertices, @Nonnull short[] indices) {
		final float x0 = -width / 2;
		final float xn = width / 2;
		final float y0 = -height / 2;

		final float dx = width / (widthVertices - 1);
		final float dy = height / (heightVertices - 1);

		int vertex = 0;
		for (int yi = 0; yi < heightVertices; yi++) {
			final float y = y0 + yi * dy;
			final boolean yEven = yi % 2 == 0;

			for (int xi = 0; xi < widthVertices; xi++) {
				final boolean xEven = xi % 2 == 0;
				int ii = xi * (heightVertices - 1) + xi;
				int iv = yi * (widthVertices - 1) + yi;
				if (xEven) {
					ii += yi;
				} else {
					ii += (heightVertices - 1 - yi);
				}
				if (yEven) {
					iv += xi;
				} else {
					iv += (widthVertices - 1 - xi);
				}
				indices[ii] = (short) iv;

				final float x;
				if (yEven) {
					// going right
					x = x0 + xi * dx;
				} else {
					// going left
					x = xn - xi * dx;
				}

				final float z = z(x, y, xi, yi);

				vertices[vertex++] = x;
				vertices[vertex++] = y;
				vertices[vertex++] = z;
			}
		}
	}

	protected abstract float z(float x, float y, int xi, int yi);
}
