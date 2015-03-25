package org.solovyev.android.plotter.meshes;

import org.solovyev.android.plotter.Dimensions;
import org.solovyev.android.plotter.MeshConfig;

import javax.annotation.Nonnull;
import javax.microedition.khronos.opengles.GL11;

public class TouchPosition extends BaseMesh implements DimensionsAware {
	private static final float EMPTY = Float.MAX_VALUE;
	private volatile float x = EMPTY;
	private volatile float y = EMPTY;
	@Nonnull
	private volatile Dimensions dimensions;

	private final short indices[] = {
			0, 1,
			2, 3};

	private final float vertices[] = {
			x, 0, 0,
			x, 0, 0,
			0, y, 0,
			0, y, 0};

	public TouchPosition(@Nonnull Dimensions dimensions) {
		this.dimensions = dimensions;
		setColor(AxisGrid.COLOR);
	}

	@Override
	protected void onInitGl(@Nonnull GL11 gl, @Nonnull MeshConfig config) {
		super.onInitGl(gl, config);

		setIndices(indices, IndicesOrder.LINES);

		int vertex = 0;
		vertices[vertex] = x;
		vertices[vertex + 1] = dimensions.graph.toScreenY(dimensions.graph.rect.bottom);
		vertex+=3;
		vertices[vertex] = x;
		vertices[vertex + 1] = dimensions.graph.toScreenY(dimensions.graph.rect.top);
		vertex+=3;
		vertices[vertex] = dimensions.graph.toScreenX(dimensions.graph.rect.left);
		vertices[vertex + 1] = y;
		vertex+=3;
		vertices[vertex] = dimensions.graph.toScreenX(dimensions.graph.rect.right);
		vertices[vertex + 1] = y;
		setVertices(vertices);
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
		return new TouchPosition(dimensions);
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
			setDirty();
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
}
