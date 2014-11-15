package org.solovyev.android.plotter.meshes;

import org.solovyev.android.plotter.Dimensions;
import org.solovyev.android.plotter.MeshConfig;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
public abstract class BaseSurface extends BaseMesh implements DimensionsAware {

	protected static enum Axes {
		XZ,
		YZ;
	}

	@Nonnull
	protected volatile MeshDimensions dimensions;

	@Nonnull
	private final Arrays arrays = new Arrays();

	protected BaseSurface(float width, float height, boolean graph) {
		this(makeDimensions(width, height), graph);
	}

	@Nonnull
	private static Dimensions makeDimensions(float width, float height) {
		final Dimensions dimensions = new Dimensions();
		dimensions.graph.set(width, height);
		return dimensions;
	}

	@Nonnull
	public Dimensions getDimensions() {
		return dimensions.d;
	}

	protected BaseSurface(@Nonnull Dimensions dimensions, boolean graph) {
		this(new MeshDimensions(dimensions, graph));
	}

	protected BaseSurface(@Nonnull MeshDimensions dimensions) {
		this.dimensions = dimensions;
	}

	public void setDimensions(@Nonnull Dimensions dimensions) {
		// todo serso: might be called on GL thread, requires synchronization
		if (!this.dimensions.d.equals(dimensions)) {
			this.dimensions = new MeshDimensions(dimensions, this.dimensions.graph);
			setDirty();
		}
	}

	@Override
	public void onInit() {
		super.onInit();

		if (!dimensions.isEmpty()) {
			dimensions.init();
			arrays.init(3 * dimensions.totalVertices(), dimensions.totalVertices());
			fillArrays(arrays.vertices, arrays.indices);
			arrays.createBuffers();
		} else {
			setDirty();
		}
	}

	@Override
	public void onInitGl(@Nonnull GL11 gl, @Nonnull MeshConfig config) {
		super.onInitGl(gl, config);

		setVertices(arrays.getVerticesBuffer());
		setIndices(arrays.getIndicesBuffer(), IndicesOrder.LINE_STRIP);
	}

	@Override
	protected void onPostDraw(@Nonnull GL11 gl) {
		super.onPostDraw(gl);
		gl.glDrawArrays(GL10.GL_LINE_STRIP, 0, dimensions.totalVertices());
	}

	void fillArrays(@Nonnull float[] vertices, @Nonnull short[] indices) {
		final float xMin = dimensions.xMin;
		final float xMax = dimensions.xMax;
		final float yMin = dimensions.yMin;

		final float dx = dimensions.width / (dimensions.widthVertices - 1);
		final float dy = dimensions.height / (dimensions.heightVertices - 1);

		final Axes invertedAxes = getInvertedAxes();

		final float[] point = new float[3];

		int vertex = 0;
		for (int yi = 0; yi < dimensions.heightVertices; yi++) {
			final float y = yMin + yi * dy;
			final boolean yEven = yi % 2 == 0;

			for (int xi = 0; xi < dimensions.widthVertices; xi++) {
				final boolean xEven = xi % 2 == 0;
				int ii = xi * (dimensions.heightVertices - 1) + xi;
				int iv = yi * (dimensions.widthVertices - 1) + yi;
				if (xEven) {
					ii += yi;
				} else {
					ii += (dimensions.heightVertices - 1 - yi);
				}
				if (yEven) {
					iv += xi;
				} else {
					iv += (dimensions.widthVertices - 1 - xi);
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

				point[0] = x;
				point[1] = y;
				point[2] = z;

				dimensions.scale(point);

				final float sx = point[0];
				final float sy = point[1];
				final float sz = point[2];

				if (invertedAxes != null) {
					switch (invertedAxes) {
						case XZ:
							vertices[vertex++] = sz;
							vertices[vertex++] = sx;
							vertices[vertex++] = sy;
							break;
						case YZ:
							vertices[vertex++] = sx;
							vertices[vertex++] = sy;
							vertices[vertex++] = sz;
							break;
					}
				} else {
					vertices[vertex++] = sx;
					vertices[vertex++] = sz;
					vertices[vertex++] = sy;
				}
			}
		}
	}

	protected abstract float z(float x, float y, int xi, int yi);

	@Nullable
	Axes getInvertedAxes() {
		return null;
	}
}
