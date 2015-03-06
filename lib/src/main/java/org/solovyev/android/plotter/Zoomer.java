package org.solovyev.android.plotter;

import android.os.Bundle;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static android.os.SystemClock.uptimeMillis;

final class Zoomer {

	private static final long DURATION = 1000L;
	private long duration = DURATION;
	@Nonnull
	private final Interpolator interpolator = new AccelerateDecelerateInterpolator();
	@Nonnull
	private Zoom level = Zoom.one();
	@Nonnull
	private Zoom to = level;
	@Nullable
	private Zoom from = null;
	private long startTime = -1;

	public Zoomer(@Nonnull Bundle bundle) {
		final float value = bundle.getFloat("zoom.level", 1f);
		level = new Zoom(value, value);
		to = level;
	}

	public Zoomer() {
	}

	@Nonnull
	public Zoom getLevel() {
		return level;
	}

	/**
	 * @return true if current zoom level has changed
	 */
	public boolean onFrame() {
		if (isZooming()) {
			Check.isTrue(from != null);
			final long now = uptimeMillis();
			final float position = (now - startTime) / (float) duration;
			if (position >= 1f) {
				startTime = -1;
				from = null;
				level = to;
				return true;
			}

			if (!from.equals(to)) {
				level = Zoom.between(from, to, interpolator.getInterpolation(position));
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
			if (!level.canZoomIn()) {
				return false;
			}
			if (from == null) {
				zoomTo(level.zoomIn());
			} else {
				reverseZoom();
			}
		} else {
			if (!level.canZoomOut()) {
				return false;
			}
			if (from == null) {
				zoomTo(level.zoomOut());
			} else {
				reverseZoom();
			}
		}

		return true;
	}

	public boolean zoomBy(float level) {
		if (isZooming()) {
			return false;
		}
		if (level == 1f) {
			return false;
		}
		zoomTo(this.level.multiplyBy(level));
		duration = 1;
		return true;
	}

	private void zoomTo(@Nonnull Zoom newZoom) {
		to = newZoom;
		from = level;
		duration = DURATION;
		startTime = uptimeMillis();
	}

	public boolean reset() {
		if (isZooming()) {
			if (to.isOne() || (from != null && from.isOne())) {
				reverseZoom();
				return true;
			}
			return false;
		}

		if (!level.isOne()) {
			zoomTo(Zoom.one());
			return true;
		}

		return false;
	}

	private void reverseZoom() {
		final Zoom tmp = to;
		to = from;
		from = tmp;

		final long now = uptimeMillis();
		final long remaining = duration - (now - startTime);
		startTime = now - remaining;
	}

	boolean isZooming() {
		return to != level;
	}

	boolean isZoomingIn() {
		return to.smallerThan(level);
	}

	boolean isZoomingOut() {
		return to.biggerThan(level);
	}

	public void saveState(@Nonnull Bundle bundle) {
		// todo serso: fix this
		bundle.putFloat("zoom.level", isZooming() ? to.x : level.x);
	}

	@Override
	public String toString() {
		return "Zoomer{" +
				"level=" + (isZooming() ? to : level) +
				'}';
	}
}
