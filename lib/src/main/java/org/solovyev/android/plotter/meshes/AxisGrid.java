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

	private final boolean d3;

	@Nonnull
	private Axes axes;

	private AxisGrid(@Nonnull Dimensions dimensions, @Nonnull Axes axes, @Nonnull Color color, boolean d3) {
		super(dimensions);
		this.axes = axes;
		this.d3 = d3;
		setColor(color);
	}

	@Nonnull
	public static AxisGrid yz(@Nonnull Dimensions dimensions, @Nonnull Color color, boolean d3) {
		return new AxisGrid(dimensions, Axes.YZ, color, d3);
	}

	@Nonnull
	public static AxisGrid xz(@Nonnull Dimensions dimensions, @Nonnull Color color, boolean d3) {
		return new AxisGrid(dimensions, Axes.XZ, color, d3);
	}

	@Nonnull
	public static AxisGrid xy(@Nonnull Dimensions dimensions, @Nonnull Color color, boolean d3) {
		return new AxisGrid(dimensions, Axes.XY, color, d3);
	}

	@Nonnull
	@Override
	protected BaseMesh makeCopy() {
		return new AxisGrid(dimensions, axes, getColor(), d3);
	}

	@Nonnull
	public DoubleBufferMesh<AxisGrid> toDoubleBuffer() {
		return DoubleBufferMesh.wrap(this, DimensionsAwareSwapper.INSTANCE);
	}

	@Nonnull
	@Override
	protected SurfaceInitializer createInitializer() {
		final Scene.AxisGrid grid = Scene.AxisGrid.create(dimensions, axes, d3);
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
				point[0] += (d3 ? dimensions.scene.center.x : 0);
				point[2] += (d3 ? dimensions.scene.center.y : 0);
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