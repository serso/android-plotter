package org.solovyev.android.plotter.meshes;

import org.solovyev.android.plotter.Check;
import org.solovyev.android.plotter.Dimensions;
import org.solovyev.android.plotter.MeshConfig;

import javax.annotation.Nonnull;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import java.nio.FloatBuffer;

public abstract class BaseCurve extends BaseMesh {

	@Nonnull
	protected volatile Dimensions dimensions;

	// create on the background thread and accessed from GL thread
	private volatile FloatBuffer verticesBuffer;

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

	public void setDimensions(@Nonnull Dimensions dimensions) {
		// todo serso: might be called on GL thread, requires synchronization
		if (!this.dimensions.equals(dimensions)) {
			this.dimensions = dimensions;
			setDirty();
		}
	}

	@Nonnull
	public Dimensions getDimensions() {
		return dimensions;
	}

	@Override
	public void onInit() {
		super.onInit();

		fillGraph(graph);
		verticesBuffer = Meshes.allocateOrPutBuffer(graph.vertices, graph.start, graph.length(), verticesBuffer);
	}

	@Override
	public void onInitGl(@Nonnull GL11 gl, @Nonnull MeshConfig config) {
		super.onInitGl(gl, config);

		Check.isNotNull(verticesBuffer);

		setVertices(verticesBuffer);
	}

	@Override
	protected void onPostDraw(@Nonnull GL11 gl) {
		super.onPostDraw(gl);
		gl.glDrawArrays(GL10.GL_LINES, 0, verticesBuffer.capacity());
	}

	void fillGraph(@Nonnull Graph graph) {
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

		compute(newXMin, newXMax, graph);
	}

	protected abstract float y(float x);

	void compute(float newXMin,
				 float newXMax,
				 @Nonnull Graph graph) {
		final float xMin = graph.xMin();
		final float xMax = graph.xMax();

		final float step = (newXMax - newXMin) / 10f;

		float x = xMin - step;
		while (x > newXMin) {
			graph.prepend(x, y(x));
			x -= step;
		}

		x = xMax + step;
		while (x < newXMax) {
			graph.append(x, y(x));
			x += step;
		}
	}

}
