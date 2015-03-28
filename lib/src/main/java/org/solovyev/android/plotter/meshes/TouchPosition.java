package org.solovyev.android.plotter.meshes;

import android.support.annotation.Nullable;

import org.solovyev.android.plotter.Dimensions;
import org.solovyev.android.plotter.MeshConfig;

import javax.annotation.Nonnull;
import javax.microedition.khronos.opengles.GL11;

public class TouchPosition extends BaseMesh implements DimensionsAware {
	private static final float EMPTY = Float.MAX_VALUE;
	private volatile float x = EMPTY;
	private volatile float y = EMPTY;
	private final float vertices[] = {
			x, 0, 0,
			x, 0, 0,
			0, y, 0,
			0, y, 0};
	private final short indices[] = {
			0, 1,
			2, 3};
	@Nonnull
	private volatile Dimensions dimensions;
	@Nullable
	private volatile Data data;

	public TouchPosition(@Nonnull Dimensions dimensions) {
		this.dimensions = dimensions;
		setColor(AxisGrid.COLOR);
	}

	@Override
	protected void onInitGl(@Nonnull GL11 gl, @Nonnull MeshConfig config) {
		super.onInitGl(gl, config);

		if (isEmpty() || dimensions.isEmpty()) {
			setDirtyGl();
			return;
		}

		final Data data = getData();

		if (x < -data.xTicks.axisLength / 2 || x > data.xTicks.axisLength / 2 || y < -data.yTicks.axisLength / 2 || y > data.yTicks.axisLength / 2) {
			setDirtyGl();
			return;
		}

		setIndices(indices, IndicesOrder.LINES);

		int vertex = 0;
		vertices[vertex] = x;
		vertices[vertex + 1] = -data.yTicks.axisLength / 2;
		vertex += 3;
		vertices[vertex] = x;
		vertices[vertex + 1] = data.yTicks.axisLength / 2;
		vertex += 3;
		vertices[vertex] = -data.xTicks.axisLength / 2;
		vertices[vertex + 1] = y;
		vertex += 3;
		vertices[vertex] = data.xTicks.axisLength / 2;
		vertices[vertex + 1] = y;
		setVertices(vertices);
	}

	@Nonnull
	private Data getData() {
		Data localData = data;
		if (localData == null) {
			localData = new Data(dimensions);
			data = localData;
		}
		return localData;
	}

	private boolean isEmpty() {
		return x == EMPTY || y == EMPTY;
	}

	public void set(float x, float y) {
		if (this.x != x || this.y != y) {
			this.x = x;
			this.y = y;
			setDirtyGl();
		}
	}

	public void setScreenXY(float x, float y) {
		set(dimensions.scene.toSceneX(x), dimensions.scene.toSceneY(y));
	}

	@Nonnull
	@Override
	protected BaseMesh makeCopy() {
		return this;
	}

	@Nonnull
	@Override
	public Dimensions getDimensions() {
		return this.dimensions;
	}

	@Override
	public void setDimensions(@Nonnull Dimensions dimensions) {
		// todo serso: might be called on GL thread, requires synchronization
		if (!this.dimensions.equals(dimensions)) {
			this.dimensions = dimensions;
			this.data = null;
		}
	}

	public void clear() {
		set(EMPTY, EMPTY);
	}

	@Override
	public String toString() {
		return "TouchPosition{" +
				"y=" + y +
				", x=" + x +
				'}';
	}

	private static final class Data {
		@Nonnull
		final Scene.Axis xAxis;
		@Nonnull
		final Scene.Axis yAxis;
		@Nonnull
		final Scene.Ticks xTicks;
		@Nonnull
		final Scene.Ticks yTicks;

		private Data(@Nonnull Dimensions dimensions) {
			xAxis = Scene.Axis.create(dimensions.scene, false);
			yAxis = Scene.Axis.create(dimensions.scene, true);
			xTicks = Scene.Ticks.create(dimensions.graph, xAxis);
			yTicks = Scene.Ticks.create(dimensions.graph, yAxis);
		}
	}
}
