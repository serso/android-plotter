package org.solovyev.android.plotter.meshes;

import android.graphics.RectF;
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

	private AxisGrid(@Nonnull Dimensions dimensions, @Nonnull Axes axes) {
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
		return new AxisGrid(dimensions, Axes.XY);
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
		final Scene.Ticks widthTicks;
		final Scene.Ticks heightTicks;
		switch (axes) {
			case XZ:
				widthTicks = xTicks;
				heightTicks = yTicks;
				break;
			case YZ:
				widthTicks = yTicks;
				heightTicks = xTicks;
				break;
			case XY:
				widthTicks = xTicks;
				heightTicks = xTicks;
				break;
			default:
				throw new AssertionError();
		}
		bounds.left = -widthTicks.axisLength / 2;
		bounds.right = widthTicks.axisLength / 2;
		bounds.bottom = -heightTicks.axisLength / 2;
		bounds.top = heightTicks.axisLength / 2;
		return new SurfaceInitializer(this, SurfaceInitializer.Data.create(bounds, widthTicks.count, heightTicks.count)) {
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