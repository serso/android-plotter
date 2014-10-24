package org.solovyev.android.plotter.meshes;

import org.solovyev.android.plotter.Dimensions;
import org.solovyev.android.plotter.MeshConfig;

import javax.annotation.Nonnull;
import javax.microedition.khronos.opengles.GL11;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class Axis extends BaseMesh implements DimensionsAware {

	private static final int TICKS = 20;

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
	private final float[] vertices = new float[3 * (2 + 2 + TICKS * 2)];
	@Nonnull
	private final short[] indices = new short[2 + 2 * 2 + 20 * 2];

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

		initializer.init();

		verticesBuffer = Meshes.allocateOrPutBuffer(vertices, verticesBuffer);
		indicesBuffer = Meshes.allocateOrPutBuffer(indices, indicesBuffer);
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

		private int vertex;
		private int index;

		public void init() {
			vertex = 0;
			index = 0;

			final float tickedAxisLength = dimensions.graph.width;
			final float axisLength = 1.2f * tickedAxisLength;
			final float arrowLength = axisLength / 30;
			final float arrowWidth = axisLength / 40;
			final float tickWidth = arrowWidth / 2;

			initLine(axisLength);
			initArrow(arrowLength, arrowWidth);
			initTicks(tickedAxisLength, tickWidth);
		}

		private void initTicks(float tickedAxisLength, float tickWidth) {
			final float step = tickedAxisLength / (TICKS - 1);
			final int[] dv = direction.vector;
			final int[] da = direction.arrow;
			float x = -dv[0] * (tickedAxisLength / 2 + step) + da[0] * tickWidth / 2;
			float y = -dv[1] * (tickedAxisLength / 2 + step) + da[1] * tickWidth / 2;
			float z = -dv[2] * (tickedAxisLength / 2 + step) + da[2] * tickWidth / 2;
			for (int i = 0; i < TICKS; i++) {
				x += dv[0] * step;
				y += dv[1] * step;
				z += dv[2] * step;

				indices[index++] = (short) (vertex / 3);
				vertices[vertex++] = x;
				vertices[vertex++] = y;
				vertices[vertex++] = z;

				indices[index++] = (short) (vertex / 3);
				vertices[vertex++] = x - da[0] * tickWidth;
				vertices[vertex++] = y - da[1] * tickWidth;
				vertices[vertex++] = z - da[2] * tickWidth;
			}
		}

		private void initArrow(float arrowLength, float arrowWidth) {
			final int[] dv = direction.vector;
			final int[] da = direction.arrow;
			vertices[vertex++] = vertices[0] - dv[0] * arrowLength - da[0] * arrowWidth / 2;
			vertices[vertex++] = vertices[1] - dv[1] * arrowLength - da[1] * arrowWidth / 2;
			vertices[vertex++] = vertices[2] - dv[2] * arrowLength - da[2] * arrowWidth / 2;
			indices[index++] = 0;
			indices[index++] = 2;

			vertices[vertex++] = vertices[0] - dv[0] * arrowLength + da[0] * arrowWidth / 2;
			vertices[vertex++] = vertices[1] - dv[1] * arrowLength + da[1] * arrowWidth / 2;
			vertices[vertex++] = vertices[2] - dv[2] * arrowLength + da[2] * arrowWidth / 2;
			indices[index++] = 0;
			indices[index++] = 3;
		}

		private void initLine(float axisLength) {
			vertices[vertex++] = direction.vector[0] * axisLength / 2;
			vertices[vertex++] = direction.vector[1] * axisLength / 2;
			vertices[vertex++] = direction.vector[2] * axisLength / 2;
			indices[index++] = 0;

			vertices[vertex++] = -vertices[0];
			vertices[vertex++] = -vertices[1];
			vertices[vertex++] = -vertices[2];
			indices[index++] = 1;
		}
	}
}
