package org.solovyev.android.plotter;

import android.os.Bundle;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import javax.annotation.Nonnull;

import static android.os.SystemClock.uptimeMillis;

final class Zoomer {

	private static final long DURATION = 1000L;
	private static final float ZOOM_IN = .625f;
	private static final float ZOOM_OUT = 1.6f;

	@Nonnull
	private final Interpolator interpolator = new AccelerateDecelerateInterpolator();

	private float level = 1f;

	private float from = -1;
	private float to = level;

	private long startTime = -1;

	public Zoomer(@Nonnull Bundle bundle) {
		level = bundle.getFloat("zoom.level", 1f);
		to = level;
	}

	public Zoomer() {
	}

	public float getLevel() {
		return level;
	}

	/**
	 * @return true if current zoom level has changed
	 */
	public boolean onFrame() {
		if (isZooming()) {
			final long now = uptimeMillis();
			final float position = (now - startTime) / (float) DURATION;
			if (position >= 1f) {
				startTime = -1;
				from = -1;
				level = to;
				return true;
			}

			if (from > to) {
				level = from - (from - to) * interpolator.getInterpolation(position);
				return true;
			}

			if (from < to) {
				level = from + (to - from) * interpolator.getInterpolation(position);
				return true;
			}
		}

		return false;
	}

	public boolean zoom(boolean in) {
		if ((isZoomingIn() && in) || (isZoomingOut() && !in)) {
			return false;
		}

		if (in) {
			if (level * ZOOM_IN == 0f) {
				return false;
			}
			if (from == -1) {
				zoomTo(level * ZOOM_IN);
			} else {
				reverseZoom();
			}
		} else {
			if (level >= Float.MAX_VALUE / ZOOM_OUT) {
				return false;
			}
			if (from == -1) {
				zoomTo(level * ZOOM_OUT);
			} else {
				reverseZoom();
			}
		}

		return true;
	}

	private void zoomTo(float newLevel) {
		to = newLevel;
		from = level;
		startTime = uptimeMillis();
	}

	public boolean reset() {
		if (isZooming()) {
			if ((to == 1f) || (from == 1f)) {
				reverseZoom();
				return true;
			}
			return false;
		}

		if (level != 1) {
			zoomTo(1);
			return true;
		}

		return false;
	}

	private void reverseZoom() {
		final float tmp = to;
		to = from;
		from = tmp;

		final long now = uptimeMillis();
		final long remaining = DURATION - (now - startTime);
		startTime = now - remaining;
	}

	boolean isZooming() {
		return to != level;
	}

	boolean isZoomingIn() {
		return to < level;
	}

	boolean isZoomingOut() {
		return to > level;
	}

	public void saveState(@Nonnull Bundle bundle) {
		if (isZooming()) {
			bundle.putFloat("zoom.level", to);
		} else {
			bundle.putFloat("zoom.level", level);
		}
	}
}
