package org.solovyev.android.plotter.meshes;

import org.solovyev.android.plotter.Dimensions;
import org.solovyev.android.plotter.MeshConfig;
import org.solovyev.android.plotter.text.FontAtlas;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.microedition.khronos.opengles.GL11;

public class AxisLabels extends BaseMesh implements DimensionsAware {

	@Nonnull
	private final AxisDirection direction;

	@Nonnull
	private final FontAtlas fontAtlas;

	@Nonnull
	private volatile Dimensions dimensions;
	
	private AxisLabels(@Nonnull AxisDirection direction, @Nonnull FontAtlas fontAtlas, @Nonnull Dimensions dimensions) {
		this.direction = direction;
		this.fontAtlas = fontAtlas;
		this.dimensions = dimensions;
	}

	@Nonnull
	public static AxisLabels x(@Nonnull FontAtlas fontAtlas, @Nonnull Dimensions dimensions) {
		return new AxisLabels(AxisDirection.X, fontAtlas, dimensions);
	}

	@Nonnull
	public static AxisLabels y(@Nonnull FontAtlas fontAtlas, @Nonnull Dimensions dimensions) {
		return new AxisLabels(AxisDirection.Y, fontAtlas, dimensions);
	}

	@Nonnull
	public static AxisLabels z(@Nonnull FontAtlas fontAtlas, @Nonnull Dimensions dimensions) {
		return new AxisLabels(AxisDirection.Z, fontAtlas, dimensions);
	}

	@Nonnull
	public DoubleBufferMesh<AxisLabels> toDoubleBuffer() {
		return DoubleBufferMesh.wrap(this, DimensionsAwareSwapper.INSTANCE);
	}

	@Override
	protected void onInit() {
		super.onInit();

		if (dimensions.scene.isEmpty()) {
			setDirty();
		}
	}

	@Override
	protected void onInitGl(@Nonnull GL11 gl, @Nonnull MeshConfig config) {
		super.onInitGl(gl, config);
		final List<FontAtlas.MeshData> meshDataList = new ArrayList<>();
		final DecimalFormat tickFormat = new DecimalFormat("##0.##E0");

		final Scene.Axis axis = Scene.Axis.create(dimensions.scene, false);
		final Scene.Ticks ticks = Scene.Ticks.create(dimensions.graph, axis);
		final float fontScale = 3f * ticks.width / fontAtlas.getFontHeight();
		final boolean centerX = direction != AxisDirection.Y;
		final boolean centerY = direction == AxisDirection.Y;
		final int[] dv = direction.vector;
		final int[] da = direction.arrow;
		float x = -dv[0] * (ticks.axisLength / 2 + ticks.step) + da[0] * ticks.width / 2;
		float y = -dv[1] * (ticks.axisLength / 2 + ticks.step) + da[1] * ticks.width / 2;
		float z = -dv[2] * (ticks.axisLength / 2 + ticks.step) + da[2] * ticks.width / 2;
		for (int i = 0; i < ticks.count; i++) {
			x += dv[0] * ticks.step;
			y += dv[1] * ticks.step;
			z += dv[2] * ticks.step;

			meshDataList.add(fontAtlas.getMeshData(tickFormat.format(x), x, y, z, fontScale, centerX, centerY));
		}
		
		final FontAtlas.MeshData meshData = new FontAtlas.MeshData(meshDataList, false, false);

		setIndices(meshData.indices, meshData.indicesOrder);
		setVertices(meshData.vertices);
		setTexture(meshData.textureId, meshData.textureCoordinates);
	}

	@Nonnull
	@Override
	protected BaseMesh makeCopy() {
		return new AxisLabels(direction, fontAtlas, dimensions);
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

	@Override
	public String toString() {
		return "AxisLabels{" +
				"direction=" + direction +
				'}';
	}
}
