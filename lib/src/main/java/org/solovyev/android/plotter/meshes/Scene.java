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
		final float minLength;
		final float arrowLength;
		final float arrowWidth;

		public Axis(@Nonnull Dimensions.Scene scene, boolean y) {
			lengthX = scene.width();
			if (y) {
				length = scene.height();
			} else {
				length = scene.width();
			}
			minLength = Math.min(scene.width(), scene.height());
			arrowLength = minLength / 30;
			arrowWidth = minLength / 40;
		}

		@Nonnull
		public static Axis create(@Nonnull Dimensions.Scene scene, boolean y) {
			return new Axis(scene, y);
		}
	}

	static final class Ticks {

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

			int ticksCount = (int) (axis.lengthX / graph.toScreenX(tickStepGpx));
			if (ticksCount > 20) {
				ticksCount /= 2;
			}
			if (ticksCount < 10) {
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
