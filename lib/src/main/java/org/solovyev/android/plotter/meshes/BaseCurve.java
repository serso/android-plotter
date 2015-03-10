package org.solovyev.android.plotter.meshes;

import android.util.Log;

import org.solovyev.android.plotter.Check;
import org.solovyev.android.plotter.Dimensions;
import org.solovyev.android.plotter.MeshConfig;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.*;

import javax.annotation.Nonnull;
import javax.microedition.khronos.opengles.GL11;

public abstract class BaseCurve extends BaseMesh implements DimensionsAware {

	@Nonnull
	private static final String TAG = Meshes.getTag("BaseCurve");

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
		dimensions.graph.set(width, height);
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

		if (!dimensions.isEmpty()) {
			fillGraph(graph);
			verticesBuffer = Meshes.allocateOrPutBuffer(graph.vertices, graph.start, graph.length(), verticesBuffer);
			indicesBuffer = Meshes.allocateOrPutBuffer(graph.getIndices(), 0, graph.getIndicesCount(), indicesBuffer);
		} else {
			setDirty();
		}
	}

	@Override
	public void onInitGl(@Nonnull GL11 gl, @Nonnull MeshConfig config) {
		super.onInitGl(gl, config);

		Check.isNotNull(verticesBuffer);

		setVertices(verticesBuffer);
		setIndices(indicesBuffer, IndicesOrder.LINE_STRIP);
	}

	void fillGraph(@Nonnull Graph graph) {
		final float add = 0;
		final float newXMin = dimensions.graph.rect.left - add;
		final float newXMax = dimensions.graph.rect.right + 2 * add;
		final float density = dimensions.scene.view.width() / 10f;
		final float step = Math.abs((newXMax - newXMin) / density);

		Log.d(TAG, "Requesting [" + newXMin + ", " + newXMax + "]");
		if (!graph.isEmpty()) {
			Log.d(TAG, "Old values [" + graph.xMin() + ", " + graph.xMax() + "]");
		}

		if (graph.step < 0 || graph.step > step) {
			Log.d(TAG, "Clearing... Old step = " + graph.step + ", new step = " + step);
			graph.step = step;
			graph.clear();
		}

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
				} else {
					append(graph.xMax() + step, newXMax, step, graph, dimensions.graph);
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

				prepend(newXMin, graph.xMin() - step, step, graph, dimensions.graph);
				if (newXMax <= graph.xMax()) {
					graph.moveEndTo(newXMax);
				} else {
					append(graph.xMax() + step, newXMax, step, graph, dimensions.graph);
				}
			}
		} else {
			append(newXMin, newXMax, step, graph, dimensions.graph);
		}

		List<float[]> value = java.util.Arrays.asList(graph.vertices);
		Log.d(TAG, String.valueOf(value));
	}

	protected abstract float y(float x);

	private void append(float from, float to, float step, @Nonnull Graph graph, @Nonnull Dimensions.Graph g) {
		Log.d(TAG, "Appending [" + from + ", " + to + "]");
		float x = from;
		while (x < to) {
			graph.append(g.toScreenX(x), g.toScreenY(y(x)));
			x += step;
		}
	}

	private void prepend(float from, float to, float step, @Nonnull Graph graph, @Nonnull Dimensions.Graph g) {
		Log.d(TAG, "Prepending [" + from + ", " + to + "]");
		float x = to;
		while (x > from) {
			graph.prepend(g.toScreenX(x), g.toScreenY(y(x)));
			x -= step;
		}
	}

}
