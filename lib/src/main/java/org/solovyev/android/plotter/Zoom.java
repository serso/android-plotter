package org.solovyev.android.plotter;

import android.support.annotation.NonNull;

public final class Zoom {

	private static final float ZOOM_IN = .625f;
	private static final float ZOOM_OUT = 1.6f;

	final float x;
	final float y;

	public Zoom(float x, float y) {
		this.x = x;
		this.y = y;
	}

	@NonNull
	public static Zoom one() {
		return new Zoom(1f, 1f);
	}

	@NonNull
	public static Zoom between(@NonNull Zoom from, @NonNull Zoom to, float interpolation) {
		final float x = between(from.x, to.x, interpolation);
		final float y = between(from.y, to.y, interpolation);
		return new Zoom(x, y);
	}

	private static float between(float from, float to, float interpolation) {
		if (from > to) {
			return from - (from - to) * interpolation;
		} else {
			return to - (to - from) * interpolation;
		}
	}

	public static Zoom zero() {
		return new Zoom(0, 0);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Zoom zoom = (Zoom) o;

		if (Float.compare(zoom.x, x) != 0) return false;
		if (Float.compare(zoom.y, y) != 0) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = (x != +0.0f ? Float.floatToIntBits(x) : 0);
		result = 31 * result + (y != +0.0f ? Float.floatToIntBits(y) : 0);
		return result;
	}

	@NonNull
	public Zoom multiplyBy(float zoom) {
		return new Zoom(zoom * x, zoom * y);
	}

	boolean canZoomIn() {
		return ZOOM_IN * x != 0f && ZOOM_IN * y != 0f;
	}

	boolean isZero() {
		return x == 0f || y == 0f;
	}

	boolean isOne() {
		return x == 1f && y == 1f;
	}

	@NonNull
	public Zoom zoomIn() {
		return multiplyBy(ZOOM_IN);
	}

	public boolean canZoomOut() {
		final float max = Float.MAX_VALUE / ZOOM_OUT;
		return x < max && y < max;
	}

	@NonNull
	public Zoom zoomOut() {
		return multiplyBy(ZOOM_OUT);
	}

	public boolean smallerThan(@NonNull Zoom that) {
		return x < that.x && y < that.y;
	}

	public boolean biggerThan(@NonNull Zoom that) {
		return that.smallerThan(this);
	}

	@NonNull
	public Zoom divideBy(@NonNull Zoom that) {
		return new Zoom(x / that.x, y / that.y);
	}
}
