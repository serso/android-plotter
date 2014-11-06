package org.solovyev.android.plotter.meshes;

import org.solovyev.android.plotter.Color;
import org.solovyev.android.plotter.Dimensions;
import org.solovyev.android.plotter.Function0;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AxisGrid extends FunctionGraph3d {

	@Nullable
	private Axes axes;

	private AxisGrid(@Nonnull Dimensions dimensions, @Nullable Axes axes) {
		super(dimensions, Axis.TICKS, Axis.TICKS, Function0.ZERO);
		this.axes = axes;
		setColor(Color.DKGRAY);
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

	@Nullable
	@Override
	Axes getInvertedAxes() {
		return axes;
	}
}