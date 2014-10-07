package org.solovyev.android.plotter;

import android.graphics.PointF;

import javax.annotation.Nonnull;

final class PinchZoomTracker {

	private static final float EPS = 1.5f;
	// todo serso: dpi!!!
	private static final float MIN_DISTANCE = distance(0f, 25f);

	@Nonnull
	private final PointF distance = new PointF();

	@Nonnull
	private final ZoomLevels current = new ZoomLevels();

	void reset(float x1, float y1,
			   float x2, float y2) {
		distance.x = distance(x1, x2);
		distance.y = distance(y1, y2);
	}

	@Nonnull
	ZoomLevels update(float x1, float y1, float x2, float y2) {
		current.reset();

		if (distance.x > MIN_DISTANCE) {
			final float dx = distance(x1, x2);
			if (dx > EPS) {
				current.x = distance.x / dx;
			}
		}

		if (distance.y > MIN_DISTANCE) {
			final float dy = distance(y1, y2);
			if (dy > EPS) {
				current.y = distance.y / dy;
			}
		}

		current.adjust();
		return current;
	}

	private static float distance(float x1, float x2) {
		final float dx = x1 - x2;
		return dx * dx;
	}
}
