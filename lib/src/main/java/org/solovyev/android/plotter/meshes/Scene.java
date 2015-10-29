package org.solovyev.android.plotter.meshes;

import android.graphics.RectF;

import org.solovyev.android.plotter.Dimensions;

import javax.annotation.Nonnull;

// Spx = Scene pixels
// Gpx = Graph pixels
// Vpx = View pixels
final class Scene {
	private Scene() {
	}

	static final class AxisGrid {

		@Nonnull
		final RectF rect;
		@Nonnull
		final Scene.Ticks widthTicks;
		@Nonnull
		final Scene.Ticks heightTicks;

		AxisGrid(@Nonnull RectF rect, @Nonnull Ticks widthTicks, @Nonnull Ticks heightTicks) {
			this.rect = rect;
			this.widthTicks = widthTicks;
			this.heightTicks = heightTicks;
		}

		@Nonnull
		public static AxisGrid create(@Nonnull Dimensions dimensions, @Nonnull org.solovyev.android.plotter.meshes.AxisGrid.Axes axes, boolean d3) {
			final Scene.Axis xAxis = Scene.Axis.create(dimensions.scene, false, d3);
			final Scene.Axis yAxis = Scene.Axis.create(dimensions.scene, true, d3);
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
			bounds.left = -widthTicks.axisLength / 2 - dimensions.scene.centerXForStep(widthTicks.step, d3);
			bounds.right = widthTicks.axisLength / 2 - dimensions.scene.centerXForStep(widthTicks.step, d3);
			bounds.bottom = -heightTicks.axisLength / 2 - dimensions.scene.centerYForStep(heightTicks.step, d3);
			bounds.top = heightTicks.axisLength / 2 - dimensions.scene.centerYForStep(heightTicks.step, d3);
			return new AxisGrid(bounds, widthTicks, heightTicks);
		}
	}
	static final class Axis {

		final float length;
		final float lengthX;
		final float arrowLength;
		final float arrowWidth;
		final float multiplier;

		public Axis(@Nonnull Dimensions.Scene scene, boolean y, boolean d3) {
			multiplier = d3 ? 1 : 5;
			final float width = multiplier * scene.size.width;
			final float height = multiplier * scene.size.height;

			lengthX = width;
			if (y) {
				length = height;
			} else {
				length = width;
			}
			final float minLength = Math.min(width, height);
			arrowLength = minLength / (multiplier * 30);
			arrowWidth = minLength / (multiplier * 40);
		}

		@Nonnull
		public static Axis create(@Nonnull Dimensions.Scene scene, boolean y, boolean d3) {
			return new Axis(scene, y, d3);
		}
	}

	static final class Ticks {

		private static final int MIN_COUNT = 3;

		// always odd
		final int count;
		final float step;
		final float width;
		final float axisLength;

		Ticks(int count, float step, float width) {
			this.count = count;
			this.step = step;
			this.width = width;
			this.axisLength = (count - 1) * step;
		}

		@Nonnull
		public static Ticks create(@Nonnull Dimensions.Graph graph, @Nonnull Axis axis) {
			final float sidePaddingSpx = 1.2f * axis.arrowLength;
			final float sidePaddingGpx = graph.scaleToGraphX(sidePaddingSpx);
			final float tickWidthSpx = axis.arrowWidth / 3;

			final float graphWidthGpx = graph.width() - 2 * sidePaddingGpx;
			final float tickStepGpx = Meshes.getTickStep(graphWidthGpx, 10);

			int ticksCount = Math.max((int) (axis.lengthX / graph.scaleToScreenX(tickStepGpx)), MIN_COUNT);
			while (ticksCount > axis.multiplier * 20) {
				ticksCount /= 2;
			}
			while (ticksCount < axis.multiplier * 10) {
				ticksCount *= 2;
			}
			if (ticksCount % 2 == 0) {
				ticksCount++;
			}
			float ticksStepSpx = axis.lengthX / ticksCount;
			float tickedAxisLengthSpx = (ticksCount - 1) * ticksStepSpx;

			float maxTickedAxisLengthSpx = axis.length - 2 * sidePaddingSpx;
			if (tickedAxisLengthSpx < maxTickedAxisLengthSpx) {
				while (tickedAxisLengthSpx < maxTickedAxisLengthSpx) {
					ticksCount += 2;
					tickedAxisLengthSpx = (ticksCount - 1) * ticksStepSpx;
				}
				ticksCount -= 2;
			} else if (tickedAxisLengthSpx > maxTickedAxisLengthSpx) {
				while (tickedAxisLengthSpx > maxTickedAxisLengthSpx) {
					ticksCount -= 2;
					tickedAxisLengthSpx = (ticksCount - 1) * ticksStepSpx;
				}
			}
			return new Ticks(ticksCount, ticksStepSpx, tickWidthSpx);
		}
	}
}
