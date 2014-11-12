package org.solovyev.android.plotter.meshes;

import org.solovyev.android.plotter.Dimensions;
import org.solovyev.android.plotter.MeshConfig;

import javax.annotation.Nonnull;
import javax.microedition.khronos.opengles.GL11;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class Axis extends BaseMesh implements DimensionsAware {

	public static final int TICKS = 19;

	private static enum Direction {
		X(new int[]{1, 0, 0},
				new int[]{0, 1, 0}),
		Y(new int[]{0, 1, 0},
				new int[]{1, 0, 0}),
		Z(new int[]{0, 0, 1}, new int[]{1, 0, 0});

		final int[] vector;
		final int[] arrow;

		Direction(int[] vector, int[] arrow) {
			this.vector = vector;
			this.arrow = arrow;
		}
	}

	@Nonnull
	private final Direction direction;

	@Nonnull
	private final Arrays arrays = new Arrays();

	@Nonnull
	protected volatile Dimensions dimensions;

	@Nonnull
	private final ArrayInitializer initializer = new ArrayInitializer();

	// create on the background thread and accessed from GL thread
	private volatile FloatBuffer verticesBuffer;
	private volatile ShortBuffer indicesBuffer;

	private Axis(@Nonnull Direction direction, @Nonnull Dimensions dimensions) {
		this.direction = direction;
		this.dimensions = dimensions;
	}

	@Nonnull
	public static Axis x(@Nonnull Dimensions dimensions) {
		return new Axis(Direction.X, dimensions);
	}

	@Nonnull
	public static Axis y(@Nonnull Dimensions dimensions) {
		return new Axis(Direction.Y, dimensions);
	}

	@Nonnull
	public static Axis z(@Nonnull Dimensions dimensions) {
		return new Axis(Direction.Z, dimensions);
	}

	@Override
	protected void onInit() {
		super.onInit();

		if (!dimensions.scene.isEmpty()) {
			initializer.init();
			verticesBuffer = arrays.getVerticesBuffer(verticesBuffer);
			indicesBuffer = arrays.getIndicesBuffer(indicesBuffer);
		} else {
			setDirty();
		}
	}

	@Override
	protected void onInitGl(@Nonnull GL11 gl, @Nonnull MeshConfig config) {
		super.onInitGl(gl, config);

		setVertices(verticesBuffer);
		setIndices(indicesBuffer, IndicesOrder.LINES);
	}

	@Nonnull
	@Override
	protected BaseMesh makeCopy() {
		return new Axis(direction, dimensions);
	}

	@Override
	public void setDimensions(@Nonnull Dimensions dimensions) {
		// todo serso: might be called on GL thread, requires synchronization
		if (!this.dimensions.equals(dimensions)) {
			this.dimensions = dimensions;
			setDirty();
		}
	}

	@Nonnull
	@Override
	public Dimensions getDimensions() {
		return this.dimensions;
	}

	private class ArrayInitializer {

		private int ticksCount;
		private float ticksStep;

		public void init() {
			final float axisLength;
			if (direction == Direction.Y) {
				axisLength = dimensions.scene.height();
			} else {
				axisLength = dimensions.scene.width();
			}
			final float minAxisLength = Math.min(dimensions.scene.width(), dimensions.scene.height());
			final float arrowLength = minAxisLength / 30;
			final float graphWidth = dimensions.graph.width() - 4 * dimensions.graph.toGraphX(arrowLength);
			final float ticksDensity = Meshes.getTickStep(graphWidth, 10);
			ticksCount = (int) (dimensions.scene.width() / dimensions.graph.toScreenX(ticksDensity)) + 1;
			ticksStep = dimensions.scene.width() / ticksCount;

			float tickedAxisLength = (ticksCount - 1) * ticksStep;
			while (tickedAxisLength < axisLength) {
				ticksCount++;
				tickedAxisLength = (ticksCount - 1) * ticksStep;
			}
			arrays.init(3 * (2 + 2 + 2 * ticksCount), 2 + 2 * 2 + 2 * ticksCount);
			final float arrowWidth = minAxisLength / 40;
			final float tickWidth = arrowWidth / 3;

			initLine(axisLength);
			initArrow(arrowLength, arrowWidth);
			initTicks(tickedAxisLength, tickWidth);
		}

		private void initTicks(float tickedAxisLength, float tickWidth) {
			final int[] dv = direction.vector;
			final int[] da = direction.arrow;
			float x = -dv[0] * (tickedAxisLength / 2 + ticksStep) + da[0] * tickWidth / 2;
			float y = -dv[1] * (tickedAxisLength / 2 + ticksStep) + da[1] * tickWidth / 2;
			float z = -dv[2] * (tickedAxisLength / 2 + ticksStep) + da[2] * tickWidth / 2;
			for (int i = 0; i < ticksCount; i++) {
				x += dv[0] * ticksStep;
				y += dv[1] * ticksStep;
				z += dv[2] * ticksStep;

				arrays.add(arrays.vertex / 3, x, y, z);
				arrays.add(arrays.vertex / 3, x - da[0] * tickWidth, y - da[1] * tickWidth, z - da[2] * tickWidth);
			}
		}

		private void initArrow(float arrowLength, float arrowWidth) {
			final int[] dv = direction.vector;
			final int[] da = direction.arrow;
			arrays.add(0,
					arrays.vertices[0] - dv[0] * arrowLength - da[0] * arrowWidth / 2,
					arrays.vertices[1] - dv[1] * arrowLength - da[1] * arrowWidth / 2,
					arrays.vertices[2] - dv[2] * arrowLength - da[2] * arrowWidth / 2);
			arrays.indices[arrays.index++] = 2;

			arrays.add(0,
					arrays.vertices[0] - dv[0] * arrowLength + da[0] * arrowWidth / 2,
					arrays.vertices[1] - dv[1] * arrowLength + da[1] * arrowWidth / 2,
					arrays.vertices[2] - dv[2] * arrowLength + da[2] * arrowWidth / 2);
			arrays.indices[arrays.index++] = 3;
		}

		private void initLine(float axisLength) {
			arrays.add(0,
					direction.vector[0] * axisLength / 2,
					direction.vector[1] * axisLength / 2,
					direction.vector[2] * axisLength / 2);

			arrays.add(1,
					-arrays.vertices[0],
					-arrays.vertices[1],
					-arrays.vertices[2]);
		}
	}

	@Override
	public String toString() {
		return "Axis{" +
				"direction=" + direction +
				'}';
	}
}
