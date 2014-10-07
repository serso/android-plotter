package org.solovyev.android.plotter.meshes;

import org.solovyev.android.plotter.Check;
import org.solovyev.android.plotter.Dimensions;
import org.solovyev.android.plotter.Function;
import org.solovyev.android.plotter.MeshConfig;

import javax.annotation.Nonnull;
import javax.microedition.khronos.opengles.GL11;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public abstract class BaseCurve extends BaseMesh {

	@Nonnull
	protected volatile Dimensions dimensions;
	private final float[] vertices;
	private final short[] indices;

	// create on the background thread and accessed from GL thread
	private volatile FloatBuffer verticesBuffer;
	private volatile ShortBuffer indicesBuffer;

	protected BaseCurve(float width, float height) {
		this(makeDimensions(width, height));
	}

	@Nonnull
	private static Dimensions makeDimensions(float width, float height) {
		final Dimensions dimensions = new Dimensions();
		dimensions.setGraphDimensions(width, height);
		return dimensions;
	}

	protected BaseCurve(@Nonnull Dimensions dimensions) {
		this.dimensions = dimensions;
		this.vertices = new float[0];
		this.indices = new short[0];
	}

	public void setDimensions(@Nonnull Dimensions dimensions) {
		// todo serso: might be called on GL thread, requires synchronization
		if (!this.dimensions.equals(dimensions)) {
			this.dimensions = dimensions;
			setDirty();
		}
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
		setIndices(indicesBuffer, IndicesOrder.LINES);
	}

	void fillArrays(@Nonnull float[] vertices, @Nonnull short[] indices) {

	}

	protected abstract float z(float x, float y, int xi, int yi);

	void compute(@Nonnull Function f, @Nonnull Graph graph) {
		final float newXMin = dimensions.getXMin();
		final float newXMax = newXMin + dimensions.graph.width;

		// prepare graph
		if (!graph.isEmpty()) {
			if (newXMin >= graph.xMin()) {
				// |------[---erased---|------data----|---erased--]------ old data
				// |-------------------[------data----]------------------ new data
				//                    xMin           xMax
				//
				// OR
				//
				// |------[---erased---|------data----]----------- old data
				// |-------------------[------data----<---->]----- new data
				//                    xMin                 xMax
				graph.moveStartTo(newXMin);
				if (newXMax <= graph.xMax()) {
					graph.moveEndTo(newXMax);
					// nothing to compute
				}
			} else {
				// |--------------------[-----data----|---erased----]-- old data
				// |------[<------------>-----data----]---------------- new data
				//       xMin                        xMax
				//
				// OR
				//
				// |--------------------[------data--]----|----------- old data
				// |-------[<----------->------data--<--->]-----------new data
				//        xMin                           xMax

				if (newXMax <= graph.xMax()) {
					graph.moveEndTo(newXMax);
				}
			}
		}

		compute(f, newXMin, newXMax, graph);
	}

	void compute(@Nonnull Function f,
				 float newXMin,
				 float newXMax,
				 @Nonnull Graph graph) {
		final float xMin = graph.xMin();
		final float xMax = graph.xMax();

		final float step = (newXMax - newXMin) / 100f;

		float x = xMin - step;
		while (x > newXMin) {
			graph.prepend(x, f.evaluate(x));
			x -= step;
		}

		x = xMax + step;
		while (x < newXMax) {
			graph.append(x, f.evaluate(x));
			x += step;
		}
	}

	static class Graph {

		int start = 0;
		int end = 0;

		int capacity = 4 * 3;
		float[] vertices = new float[capacity];

		private Graph() {
		}

		@Nonnull
		static Graph create() {
			return new Graph();
		}

		void prepend(float x, float y) {
			ensureCanPrepend();

			vertices[--start] = 0;
			vertices[--start] = y;
			vertices[--start] = x;
		}

		private void ensureCanPrepend() {
			if (start < 3) {
				makeSpaceAtTheStart();
			}
		}

		void append(float x, float y) {
			ensureCanAppend();

			vertices[end++] = x;
			vertices[end++] = y;
			vertices[end++] = 0;
		}

		private void ensureCanAppend() {
			if (end + 3 > capacity) {
				makeSpaceAtTheEnd();
			}
		}

		void makeSpaceAtTheEnd() {
			if (start > capacity / 2) {
				final int length = length();
				System.arraycopy(vertices, start, vertices, 0, length);
				start = 0;
				end = length;
				return;
			}

			final int newCapacity = 5 * capacity / 4;
			final float[] newVertices = new float[newCapacity];
			System.arraycopy(vertices, start, newVertices, start, length());
			vertices = newVertices;
			capacity = newCapacity;
		}

		private void makeSpaceAtTheStart() {
			if (end != 0 && end < capacity / 2) {
				final int newStart = start + capacity - end;
				final int length = length();
				System.arraycopy(vertices, start, vertices, newStart, length);
				start = newStart;
				end = newStart + length;
				return;
			}

			final int newCapacity = 5 * capacity / 4;
			final float[] newVertices = new float[newCapacity];
			final int offset = newCapacity - capacity;
			System.arraycopy(vertices, start, newVertices, start + offset, length());
			start += offset;
			end += offset;
			vertices = newVertices;
			capacity = newCapacity;
		}


		int length() {
			return end - start;
		}

		boolean isEmpty() {
			return start == end;
		}

		void clear() {
			start = 0;
			end = 0;
		}

		public float x(int position) {
			return vertices[position];
		}

		public void moveStartTo(float x) {
			while (start < end && vertices[start] < x) {
				start += 3;
			}

			if (start > end) {
				start = end;
			}
		}

		public void moveEndTo(float x) {
			while (start < end && vertices[end] > x) {
				end -= 3;
			}

			if (start > end) {
				end = start;
			}
		}

		public float xMin() {
			return vertices[start];
		}

		public float xMax() {
			return vertices[end];
		}


	}
}
