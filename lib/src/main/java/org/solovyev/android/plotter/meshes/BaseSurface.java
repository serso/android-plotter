package org.solovyev.android.plotter.meshes;

import org.solovyev.android.plotter.Check;
import org.solovyev.android.plotter.Dimensions;
import org.solovyev.android.plotter.MeshConfig;

import javax.annotation.Nonnull;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

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

	@Nonnull
	protected Dimensions dimensions;
	protected final int widthVertices;
	protected final int heightVertices;
	private final float[] vertices;
	private final short[] indices;

	// create on the background thread and accessed from GL thread
	private volatile FloatBuffer verticesBuffer;
	private volatile ShortBuffer indicesBuffer;

	protected BaseSurface(float width, float height, int widthVertices, int heightVertices) {
		this.dimensions = new Dimensions();
		this.dimensions.setGraphDimensions(width, height);
		this.widthVertices = widthVertices;
		this.heightVertices = heightVertices;
		this.vertices = new float[this.heightVertices * this.widthVertices * 3];
		this.indices = new short[this.heightVertices * this.widthVertices];
	}

	public void setDimensions(@Nonnull Dimensions dimensions) {
		this.dimensions = dimensions;
	}

	@Override
	public void onInit() {
		super.onInit();

		fillArrays(vertices, indices);
		verticesBuffer = Meshes.allocateOrPutBuffer(vertices, verticesBuffer);
		indicesBuffer = Meshes.allocateOrPutBuffer(indices, indicesBuffer);
	}

	@Override
	public void onInitGl(@Nonnull GL11 gl, @Nonnull MeshConfig config) {
		super.onInitGl(gl, config);

		Check.isNotNull(verticesBuffer);
		Check.isNotNull(indicesBuffer);

		setVertices(verticesBuffer);
		setIndices(indicesBuffer, IndicesOrder.LINE_STRIP);
	}

	@Override
	protected void onPostDraw(@Nonnull GL11 gl) {
		super.onPostDraw(gl);
		gl.glDrawArrays(GL10.GL_LINE_STRIP, 0, heightVertices * widthVertices);
	}

	void fillArrays(@Nonnull float[] vertices, @Nonnull short[] indices) {
		final float xMin = dimensions.getXMin();
		final float xMax = xMin + dimensions.graph.width;
		final float yMin = dimensions.getYMin();

		final float dx = dimensions.graph.width / (widthVertices - 1);
		final float dy = dimensions.graph.height / (heightVertices - 1);

		int vertex = 0;
		for (int yi = 0; yi < heightVertices; yi++) {
			final float y = yMin + yi * dy;
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
					x = xMin + xi * dx;
				} else {
					// going left
					x = xMax - xi * dx;
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
