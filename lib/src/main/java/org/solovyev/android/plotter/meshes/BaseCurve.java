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

}
