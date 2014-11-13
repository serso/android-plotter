package org.solovyev.android.plotter.meshes;

import org.solovyev.android.plotter.Color;
import org.solovyev.android.plotter.Dimensions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AxisGrid extends BaseSurface {

	@Nullable
	private Axes axes;

	private AxisGrid(@Nonnull Dimensions dimensions, @Nullable Axes axes) {
		super(dimensions, false);
		this.axes = axes;
		setColor(Color.create(0xFF333333));
	}

	private AxisGrid(@Nonnull MeshDimensions dimensions, @Nullable Axes axes) {
		super(dimensions);
		this.axes = axes;
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

	@Override
	protected float z(float x, float y, int xi, int yi) {
		return 0;
	}

	@Nullable
	@Override
	Axes getInvertedAxes() {
		return axes;
	}

	@Override
	public String toString() {
		return "AxisGrid{" +
				"axes=" + axes +
				'}';
	}
}