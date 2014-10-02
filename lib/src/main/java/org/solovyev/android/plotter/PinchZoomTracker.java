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
	private final PointF current = new PointF();

	void reset(float x1, float y1,
			   float x2, float y2) {
		distance.x = distance(x1, x2);
		distance.y = distance(y1, y2);
	}

	@Nonnull
	PointF update(float x1, float y1, float x2, float y2) {
		current.x = 1f;
		current.y = 1f;

		if (distance.x > MIN_DISTANCE) {
			final float d = distance(x1, x2);
			if (d > EPS) {
				current.x = distance.x / d;
			}
		}

		if (distance.y > MIN_DISTANCE) {
			final float d = distance(y1, y2);
			if (d > EPS) {
				current.y = distance.y / d;
			}
		}

		return current;
	}

	private static float distance(float x1, float x2) {
		final float dx = x1 - x2;
		return dx * dx;
	}
}
