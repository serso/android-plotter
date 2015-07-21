package org.solovyev.android.plotter;

import android.graphics.PointF;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import javax.annotation.Nonnull;

public class CameraMan {
	private final long duration = Zoomer.DURATION;
	@Nonnull
	private final Interpolator interpolator = new DecelerateInterpolator();

	@Nonnull
	private final PointF current = new PointF();
	@Nonnull
	private final PointF from = new PointF();
	@Nonnull
	private final PointF to = new PointF();
	private long startTime;

	void move(@Nonnull PointF from, @Nonnull PointF to) {
		startTime = Plot.animationTime();
		this.from.set(from);
		this.to.set(to);
		this.current.set(from);
	}

	private boolean isAnimating() {
		return startTime != 0 && Plot.animationTime() - startTime < duration;
	}

	boolean onFrame() {
		if (!isAnimating()) {
			return false;
		}
		final long elapsed = Math.min(Plot.animationTime() - startTime, duration);
		final float position = elapsed / (float) duration;
		final float interpolation = interpolator.getInterpolation(position);
		current.x = from.x + interpolation * (to.x - from.x);
		current.y = from.y + interpolation * (to.y - from.y);

		return isAnimating();
	}

	@Nonnull
	public PointF getPosition() {
		if (isAnimating()) {
			return current;
		} else {
			return to;
		}
	}
}
