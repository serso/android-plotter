package org.solovyev.android.plotter.meshes;

import android.graphics.RectF;

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
	private final List<FormatInterval> labelFormats = new ArrayList<>();

	{
		labelFormats.add(new FormatInterval(Math.pow(10, -5), Math.pow(10, -4), new DecimalFormat("##0.####")));
		labelFormats.add(new FormatInterval(Math.pow(10, -4), Math.pow(10, -3), new DecimalFormat("##0.###")));
		labelFormats.add(new FormatInterval(Math.pow(10, -3), Math.pow(10, -2), new DecimalFormat("##0.##")));
		labelFormats.add(new FormatInterval(Math.pow(10, -2), Math.pow(10, 2), new DecimalFormat("##0.#")));
		labelFormats.add(new FormatInterval(Math.pow(10, 2), Math.pow(10, 4), new DecimalFormat("##0")));
	}

	@Nonnull
	private final DecimalFormat defaultFormat = new DecimalFormat("##0.##E0");

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

		final boolean isY = direction == AxisDirection.Y;
		final Scene.Axis axis = Scene.Axis.create(dimensions.scene, isY);
		final Scene.Ticks ticks = Scene.Ticks.create(dimensions.graph, axis);
		final float fontVerticalOffset = calculateFontVerticalOffset(ticks);
		final float fontScale = 3f * ticks.width / fontAtlas.getFontHeight();
		final int[] dv = direction.vector;
		final int[] da = direction.arrow;
		float x = -dv[0] * (ticks.axisLength / 2 + ticks.step) + da[0] * ticks.width / 2;
		if (isY) {
			x += ticks.width / 2;
		}
		float y = -dv[1] * (ticks.axisLength / 2 + ticks.step) + da[1] * ticks.width / 2;
		if (isY) {
			// as digits usually occupy only lower part of the glyph cell visually text appears
			// to be not centered. Let's fix this by offsetting Y coordinate. Note that this
			// offset is unique for font used in the font atlas
			y += fontVerticalOffset;
		}
		float z = -dv[2] * (ticks.axisLength / 2 + ticks.step) + da[2] * ticks.width / 2;
		final DecimalFormat format = getFormatter(ticks.step);
		for (int tick = 0; tick < ticks.count; tick++) {
			x += dv[0] * ticks.step;
			y += dv[1] * ticks.step;
			z += dv[2] * ticks.step;

			final boolean middle = tick == ticks.count / 2;
			if (middle && direction != AxisDirection.X) {
				// center is reserved for X coordinate
				continue;
			}

			final String label = getLabel(x, y, z, format);
			FontAtlas.MeshData meshData = fontAtlas.getMeshData(label, x, y, z, fontScale, !isY, isY);
			if (!middle && direction != AxisDirection.Z && !meshDataList.isEmpty()) {
				final RectF bounds = meshData.getBounds();
				final FontAtlas.MeshData lastMeshData = meshDataList.get(meshDataList.size() - 1);
				if (lastMeshData.getBounds().intersect(bounds)) {
					if (direction == AxisDirection.X) {
						meshData = fontAtlas.getMeshData(label, x, y - (ticks.width + bounds.height() - fontVerticalOffset), z, fontScale, !isY, isY);
					} else {
						// new label intersects old, let's skip it
						continue;
					}
				}
			}
			meshDataList.add(meshData);
		}

		final FontAtlas.MeshData meshData = new FontAtlas.MeshData(meshDataList, false, false);

		setIndices(meshData.indices, meshData.indicesOrder);
		setVertices(meshData.vertices);
		setTexture(meshData.textureId, meshData.textureCoordinates);
	}

	@Nonnull
	private String getLabel(float x, float y, float z, @Nonnull DecimalFormat format) {
		final float value;
		switch (direction) {
			case X:
				value = dimensions.graph.toGraphX(x);
				break;
			case Y:
				value = dimensions.graph.toGraphY(y);
				break;
			default:
				value = dimensions.graph.toGraphZ(z);
		}

		return format.format(value);
	}

	@Nonnull
	private DecimalFormat getFormatter(float step) {
		for (AxisLabels.FormatInterval labelFormat : labelFormats) {
			if (labelFormat.l <= step && step < labelFormat.r) {
				return labelFormat.format;
			}
		}
		return defaultFormat;
	}

	private float calculateFontVerticalOffset(Scene.Ticks ticks) {
		return ticks.width / 4;
	}

	@Nonnull
	@Override
	protected BaseMesh makeCopy() {
		return new AxisLabels(direction, fontAtlas, dimensions);
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

	@Override
	public String toString() {
		return "AxisLabels{" +
				"direction=" + direction +
				'}';
	}

	private static final class FormatInterval {
		final float l;
		final float r;
		@Nonnull
		final DecimalFormat format;

		private FormatInterval(double l, double r, @Nonnull DecimalFormat format) {
			this.l = (float) l;
			this.r = (float) r;
			this.format = format;
		}
	}
}
