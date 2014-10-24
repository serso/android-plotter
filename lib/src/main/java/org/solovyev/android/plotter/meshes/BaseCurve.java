package org.solovyev.android.plotter.meshes;

import org.solovyev.android.plotter.Check;
import org.solovyev.android.plotter.Dimensions;
import org.solovyev.android.plotter.MeshConfig;

import javax.annotation.Nonnull;
import javax.microedition.khronos.opengles.GL11;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public abstract class BaseCurve extends BaseMesh implements DimensionsAware {

	@Nonnull
	protected volatile Dimensions dimensions;

	// create on the background thread and accessed from GL thread
	private volatile FloatBuffer verticesBuffer;
	private volatile ShortBuffer indicesBuffer;

	@Nonnull
	private final Graph graph = Graph.create();

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
	}

	@Override
	public void setDimensions(@Nonnull Dimensions dimensions) {
		// todo serso: might be called on GL thread, requires synchronization
		if (!this.dimensions.equals(dimensions)) {
			this.dimensions = dimensions;
			setDirty();
		}
	}

	@Override
	@Nonnull
	public Dimensions getDimensions() {
		return dimensions;
	}

	@Override
	public void onInit() {
		super.onInit();

		fillGraph(graph);
		verticesBuffer = Meshes.allocateOrPutBuffer(graph.vertices, graph.start, graph.length(), verticesBuffer);
		indicesBuffer = Meshes.allocateOrPutBuffer(graph.getIndices(), 0, graph.getIndicesCount(), indicesBuffer);
	}

	@Override
	public void onInitGl(@Nonnull GL11 gl, @Nonnull MeshConfig config) {
		super.onInitGl(gl, config);

		Check.isNotNull(verticesBuffer);

		setVertices(verticesBuffer);
		setIndices(indicesBuffer, IndicesOrder.LINE_STRIP);
	}

	void fillGraph(@Nonnull Graph graph) {
		final float add = dimensions.graph.width;
		final float newXMin = dimensions.getXMin() - add;
		final float newXMax = newXMin + dimensions.graph.width + 2 * add;

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

		compute(newXMin, newXMax, graph);
	}

	protected abstract float y(float x);

	void compute(final float newXMin,
				 final float newXMax,
				 @Nonnull Graph graph) {
		float x;

		final float step = Math.abs((newXMax - newXMin) / 50f);
		if (graph.accuracy / step > 1.2f) {
			graph.accuracy = step;
			graph.clear();
		}

		final float xMin;
		final float xMax;
		if (!graph.isEmpty()) {
			xMin = graph.xMin();
			xMax = graph.xMax();
		} else {
			xMin = newXMin;
			xMax = newXMin;
		}

		x = graph.isEmpty() ? xMin : xMin - step;
		while (x > newXMin) {
			graph.prepend(x, y(x));
			x -= step;
		}

		x = graph.isEmpty() ? xMax : xMax + step;
		while (x < newXMax) {
			graph.append(x, y(x));
			x += step;
		}
	}

}
