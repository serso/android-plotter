package org.solovyev.android.plotter.meshes;

import android.graphics.RectF;
import org.solovyev.android.plotter.Color;
import org.solovyev.android.plotter.Dimensions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AxisGrid extends BaseSurface {

	protected static enum Axes {
		XZ,
		YZ;
	}

	@Nullable
	private Axes axes;

	private AxisGrid(@Nonnull Dimensions dimensions, @Nullable Axes axes) {
		super(dimensions);
		this.axes = axes;
		setColor(Color.create(0xFF222222));
	}

	@Nonnull
	public static AxisGrid yz(@Nonnull Dimensions dimensions) {
		return new AxisGrid(dimensions, Axes.YZ);
	}

	@Nonnull
	public static AxisGrid xz(@Nonnull Dimensions dimensions) {
		return new AxisGrid(dimensions, Axes.XZ);
	}

	@Nonnull
	public static AxisGrid xy(@Nonnull Dimensions dimensions) {
		return new AxisGrid(dimensions, null);
	}

	@Nonnull
	@Override
	protected BaseMesh makeCopy() {
		return new AxisGrid(dimensions, axes);
	}

	@Nonnull
	public DoubleBufferMesh<AxisGrid> toDoubleBuffer() {
		return DoubleBufferMesh.wrap(this, DimensionsAwareSwapper.INSTANCE);
	}

	@Nonnull
	@Override
	protected SurfaceInitializer createInitializer() {
		final Scene.Axis xAxis = Scene.Axis.create(dimensions.scene, false);
		final Scene.Axis yAxis = Scene.Axis.create(dimensions.scene, true);
		final Scene.Ticks xTicks = Scene.Ticks.create(dimensions.graph, xAxis);
		final Scene.Ticks yTicks = Scene.Ticks.create(dimensions.graph, yAxis);
		final RectF bounds = new RectF();
		bounds.left = -xTicks.axisLength / 2;
		bounds.right = xTicks.axisLength / 2;
		bounds.bottom = -yTicks.axisLength / 2;
		bounds.top = xTicks.axisLength / 2;
		return new SurfaceInitializer(this, SurfaceInitializer.Data.create(bounds, xTicks.count, yTicks.count)) {
			@Override
			protected void rotate(float[] point) {
				if (axes != null) {
					final float x = point[0];
					final float y = point[1];
					final float z = point[2];
					switch (axes) {
						case XZ:
							point[0] = x;
							point[1] = z;
							point[2] = y;
							break;
						case YZ:
							point[0] = z;
							point[1] = y;
							point[2] = x;
							break;
					}
				}
			}
		};
	}

	@Override
	protected float z(float x, float y, int xi, int yi) {
		return 0;
	}

	@Override
	public String toString() {
		return "AxisGrid{" +
				"axes=" + axes +
				'}';
	}
}