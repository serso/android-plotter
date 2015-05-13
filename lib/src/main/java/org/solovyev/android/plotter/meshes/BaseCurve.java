package org.solovyev.android.plotter.meshes;

import org.solovyev.android.plotter.Check;
import org.solovyev.android.plotter.Dimensions;
import org.solovyev.android.plotter.MeshConfig;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.annotation.Nonnull;
import javax.microedition.khronos.opengles.GL11;

public abstract class BaseCurve extends BaseMesh implements DimensionsAware {

	@Nonnull
	protected volatile Dimensions dimensions;

	// create on the background thread and accessed from GL thread
	private volatile FloatBuffer verticesBuffer;
	private volatile ShortBuffer indicesBuffer;

	@Nonnull
	private final Graph graph = Graph.create();

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

		if (!dimensions.isZero()) {
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
		final float add = dimensions.graph.rect.width();
		final float newXMin = dimensions.graph.rect.left - add;
		final float newXMax = dimensions.graph.rect.right + add;
		final float points = dimensions.scene.view.width() / 2;
		final int maxPoints = (int) (2 * dimensions.scene.view.width());
		final float step = Math.abs(newXMax - newXMin) / points;

		if (graph.step < 0 || graph.step > step || graph.center.x != dimensions.graph.rect.centerX() || graph.center.y != dimensions.graph.rect.centerY()) {
			graph.clear();
		}
		graph.step = step;
		graph.center.x = dimensions.graph.rect.centerX();
		graph.center.y = dimensions.graph.rect.centerY();

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
					if (fillGraphIfCantGrow(graph, newXMin, newXMax, step, maxPoints)) return;
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
				if (fillGraphIfCantGrow(graph, newXMin, newXMax, step, maxPoints)) return;
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

	private boolean fillGraphIfCantGrow(Graph graph, float newXMin, float newXMax, float step, int maxPoints) {
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
