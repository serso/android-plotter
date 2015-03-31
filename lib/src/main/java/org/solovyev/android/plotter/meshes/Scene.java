package org.solovyev.android.plotter.meshes;

import org.solovyev.android.plotter.Dimensions;

import javax.annotation.Nonnull;

// Spx = Scene pixels
// Gpx = Graph pixels
// Vpx = View pixels
final class Scene {
	private Scene() {
	}

	static final class Axis {

		final float length;
		final float lengthX;
		final float arrowLength;
		final float arrowWidth;
		final float multiplier = 3f;

		public Axis(@Nonnull Dimensions.Scene scene, boolean y) {
			final float width = multiplier * scene.width();
			final float height = multiplier * scene.height();

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
		public static Axis create(@Nonnull Dimensions.Scene scene, boolean y) {
			return new Axis(scene, y);
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
			final float sidePaddingGpx = graph.toGraphX(sidePaddingSpx);
			final float tickWidthSpx = axis.arrowWidth / 3;

			final float graphWidthGpx = graph.width() - 2 * sidePaddingGpx;
			final float tickStepGpx = Meshes.getTickStep(graphWidthGpx, 10);

			int ticksCount = Math.max((int) (axis.lengthX / graph.toScreenX(tickStepGpx)), MIN_COUNT);
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
