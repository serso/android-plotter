package org.solovyev.android.plotter.meshes;

import org.solovyev.android.plotter.Check;
import org.solovyev.android.plotter.Dimensions;
import org.solovyev.android.plotter.MeshConfig;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
public abstract class BaseSurface extends BaseMesh implements DimensionsAware {

	protected static enum Axes {
		XZ,
		YZ;
	}

	@Nonnull
	protected volatile SurfaceDimensions dimensions;
	protected final int widthVertices;
	protected final int heightVertices;
	private final float[] vertices;
	private final short[] indices;

	// create on the background thread and accessed from GL thread
	private volatile FloatBuffer verticesBuffer;
	private volatile ShortBuffer indicesBuffer;

	protected BaseSurface(float width, float height, int widthVertices, int heightVertices, boolean graph) {
		this(makeDimensions(width, height), widthVertices, heightVertices, graph);
	}

	@Nonnull
	private static Dimensions makeDimensions(float width, float height) {
		final Dimensions dimensions = new Dimensions();
		dimensions.graph.set(width, height);
		return dimensions;
	}

	@Nonnull
	public Dimensions getDimensions() {
		return dimensions.dimensions;
	}

	protected BaseSurface(@Nonnull Dimensions dimensions, int widthVertices, int heightVertices, boolean graph) {
		this(new SurfaceDimensions(dimensions, graph), widthVertices, heightVertices);
	}

	protected BaseSurface(@Nonnull SurfaceDimensions dimensions, int widthVertices, int heightVertices) {
		this.dimensions = dimensions;
		this.widthVertices = widthVertices;
		this.heightVertices = heightVertices;
		this.vertices = new float[this.heightVertices * this.widthVertices * 3];
		this.indices = new short[this.heightVertices * this.widthVertices];
	}

	public void setDimensions(@Nonnull Dimensions dimensions) {
		// todo serso: might be called on GL thread, requires synchronization
		if (!this.dimensions.dimensions.equals(dimensions)) {
			this.dimensions = new SurfaceDimensions(dimensions, this.dimensions.graph);
			setDirty();
		}
	}

	@Override
	public void onInit() {
		super.onInit();

		if (!dimensions.isEmpty()) {
			fillArrays(vertices, indices);
			verticesBuffer = Meshes.allocateOrPutBuffer(vertices, verticesBuffer);
			indicesBuffer = Meshes.allocateOrPutBuffer(indices, indicesBuffer);
		} else {
			setDirty();
		}
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
		final float xMin = dimensions.xMin;
		final float xMax = dimensions.xMax;
		final float yMin = dimensions.yMin;

		final float dx = dimensions.width / (widthVertices - 1);
		final float dy = dimensions.height / (heightVertices - 1);

		final Axes invertedAxes = getInvertedAxes();

		final float[] point = new float[3];

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

	protected static final class SurfaceDimensions {
		@Nonnull
		final Dimensions dimensions;
		final float xMin;
		final float xMax;
		final float yMin;
		final float width;
		final float height;
		final boolean graph;

		protected SurfaceDimensions(@Nonnull Dimensions dimensions, boolean graph) {
			this.dimensions = dimensions;
			this.graph = graph;
			if (graph) {
				this.xMin = dimensions.graph.getXMin(dimensions.camera);
				this.xMax = this.xMin + dimensions.graph.width;
				this.yMin = dimensions.graph.getYMin(dimensions.camera);
				this.width = dimensions.graph.width;
				this.height = dimensions.graph.height;
			} else {
				final float minAxis = Math.min(dimensions.view.width, dimensions.view.height);
				final float tickedAxisLength = minAxis - 4 * (minAxis) / (Axis.TICKS + 4 - 1);
				this.xMin = -tickedAxisLength / 2;
				this.xMax = tickedAxisLength / 2;
				this.yMin = -tickedAxisLength / 2;
				this.width = tickedAxisLength;
				this.height = tickedAxisLength;
			}
		}

		public boolean isEmpty() {
			return dimensions.graph.isEmpty();
		}

		public void scale(float[] point) {
			if (graph) {
				point[0] = dimensions.graph.toScreenX(point[0]);
				point[1] = dimensions.graph.toScreenY(point[1]);
				point[2] = dimensions.graph.toScreenZ(point[2]);
			}
		}
	}
}
