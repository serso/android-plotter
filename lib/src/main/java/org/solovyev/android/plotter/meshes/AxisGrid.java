package org.solovyev.android.plotter.meshes;

import org.solovyev.android.plotter.Color;
import org.solovyev.android.plotter.Dimensions;

import javax.annotation.Nonnull;

public class AxisGrid extends BaseSurface {

	protected static enum Axes {
		XY,
		XZ,
		YZ;
	}

	@Nonnull
	private Axes axes;

	private AxisGrid(@Nonnull Dimensions dimensions, @Nonnull Axes axes, @Nonnull Color color) {
		super(dimensions);
		this.axes = axes;
		setColor(color);
	}

	@Nonnull
	public static AxisGrid yz(@Nonnull Dimensions dimensions, @Nonnull Color color) {
		return new AxisGrid(dimensions, Axes.YZ, color);
	}

	@Nonnull
	public static AxisGrid xz(@Nonnull Dimensions dimensions, @Nonnull Color color) {
		return new AxisGrid(dimensions, Axes.XZ, color);
	}

	@Nonnull
	public static AxisGrid xy(@Nonnull Dimensions dimensions, @Nonnull Color color) {
		return new AxisGrid(dimensions, Axes.XY, color);
	}

	@Nonnull
	@Override
	protected BaseMesh makeCopy() {
		return new AxisGrid(dimensions, axes, getColor());
	}

	@Nonnull
	public DoubleBufferMesh<AxisGrid> toDoubleBuffer() {
		return DoubleBufferMesh.wrap(this, DimensionsAwareSwapper.INSTANCE);
	}

	@Nonnull
	@Override
	protected SurfaceInitializer createInitializer() {
		final Scene.AxisGrid grid = Scene.AxisGrid.create(dimensions, axes);
		return new SurfaceInitializer(this, SurfaceInitializer.Data.create(grid.rect, grid.widthTicks.count, grid.heightTicks.count)) {
			@Override
			protected void rotate(float[] point) {
				if (axes != Axes.XY) {
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