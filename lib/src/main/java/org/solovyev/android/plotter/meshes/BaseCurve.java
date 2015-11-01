package org.solovyev.android.plotter.meshes;

import android.util.Log;
import org.solovyev.android.plotter.Check;
import org.solovyev.android.plotter.Dimensions;
import org.solovyev.android.plotter.MeshConfig;

import javax.annotation.Nonnull;
import javax.microedition.khronos.opengles.GL11;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public abstract class BaseCurve extends BaseMesh implements DimensionsAware {

	private static final boolean CUTOFF = true;

	@Nonnull
	protected final MeshDimensions dimensions;

	// create on the background thread and accessed from GL thread
	private volatile FloatBuffer verticesBuffer;
	private volatile ShortBuffer indicesBuffer;

	@Nonnull
	private final Graph graph = Graph.create();

	protected BaseCurve(@Nonnull Dimensions dimensions) {
		this.dimensions = new MeshDimensions(dimensions);
	}

	@Override
	public void setDimensions(@Nonnull Dimensions dimensions) {
		if (this.dimensions.set(dimensions)) {
			Log.d(TAG, this + ": dimensions=" + dimensions);
			setDirty();
		}
	}

	@Override
	@Nonnull
	public Dimensions getDimensions() {
		return dimensions.get();
	}

	@Override
	public void onInit() {
		super.onInit();

		final Dimensions dimensions = this.dimensions.get();
		if (!dimensions.isZero()) {
			fillGraph(graph, dimensions);
			verticesBuffer = Meshes.allocateOrPutBuffer(graph.vertices, graph.start, graph.length(), verticesBuffer);
			final short[] indices;
			if (CUTOFF) {
				final Scene.AxisGrid grid = Scene.AxisGrid.create(dimensions, AxisGrid.Axes.XY, false);
				indices = graph.getIndices(grid.rect.top, grid.rect.bottom);
			} else {
				indices = graph.getIndices();
			}
			indicesBuffer = Meshes.allocateOrPutBuffer(indices, 0, graph.getIndicesCount(), indicesBuffer);
		} else {
			setDirty();
		}
	}

	@Override
	public void onInitGl(@Nonnull GL11 gl, @Nonnull MeshConfig config) {
		super.onInitGl(gl, config);

		Check.isNotNull(verticesBuffer);

		setVertices(verticesBuffer);
		setIndices(indicesBuffer, CUTOFF ? IndicesOrder.LINES : IndicesOrder.LINE_STRIP);
	}

	void fillGraph(@Nonnull Graph graph, @Nonnull Dimensions dimensions) {
		final float add = dimensions.graph.size.width;
		final float newXMin = dimensions.graph.xMin() - add;
		final float newXMax = dimensions.graph.xMax() + add;
		final int maxPoints = 4 * dimensions.scene.view.width;
		final float points = maxPoints / 2;
		final float step = Math.abs(newXMax - newXMin) / points;

		if (graph.step < 0 || graph.step > step || graph.center.x != dimensions.graph.center.x || graph.center.y != dimensions.graph.center.y) {
			graph.clear();
		}
		graph.step = step;
		graph.center.x = dimensions.graph.center.x;
		graph.center.y = dimensions.graph.center.y;

		if (!graph.isEmpty()) {
			final float screenXMin = dimensions.graph.toScreenX(newXMin);
			final float screenXMax = dimensions.graph.toScreenX(newXMax);
			if (screenXMin >= graph.xMin()) {
				// |------[---erased---|------data----|---erased--]------ old data
				// |-------------------[------data----]------------------ new data
				//                    xMin           xMax
				//
				// OR
				//
				// |------[---erased---|------data----]----------- old data
				// |-------------------[------data----<---->]----- new data
				//                    xMin                 xMax
				graph.moveStartTo(screenXMin);
				if (screenXMax <= graph.xMax()) {
					graph.moveEndTo(screenXMax);
				} else {
					if (fillGraphIfCantGrow(graph, newXMin, newXMax, step, maxPoints, dimensions)) return;
					calculate(dimensions.graph.toGraphX(graph.xMax()) + step, newXMax, step, graph, dimensions.graph);
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
				if (fillGraphIfCantGrow(graph, newXMin, newXMax, step, maxPoints, dimensions)) return;
				calculate(dimensions.graph.toGraphX(graph.xMin()) - step, newXMin, -step, graph, dimensions.graph);
				if (screenXMax <= graph.xMax()) {
					graph.moveEndTo(screenXMax);
				} else {
					calculate(dimensions.graph.toGraphX(graph.xMax()) + step, newXMax, step, graph, dimensions.graph);
				}
			}
		} else {
			calculate(newXMin, newXMax, step, graph, dimensions.graph);
		}
	}

	private boolean fillGraphIfCantGrow(Graph graph, float newXMin, float newXMax, float step, int maxPoints, @Nonnull Dimensions dimensions) {
		if (!graph.canGrow(maxPoints)) {
			// if we can't grow anymore we must clear the graph and recalculate all values
			graph.clear();
			calculate(newXMin, newXMax, step, graph, dimensions.graph);
			return true;
		}
		return false;
	}

	protected abstract float y(float x);

	private void calculate(float from, float to, float step, @Nonnull Graph graph, @Nonnull Dimensions.Graph g) {
		float x = from;
		while (x < to) {
			float y = y(x);
			if (step > 0) {
				graph.append(g.toScreenX(x), g.toScreenY(y));
			} else {
				graph.prepend(g.toScreenX(x), g.toScreenY(y));
			}
			x += step;
		}
	}
}
