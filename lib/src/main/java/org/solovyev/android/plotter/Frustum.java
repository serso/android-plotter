package org.solovyev.android.plotter;

import javax.annotation.Nonnull;

public final class Frustum {
	public float width;
	public float height;
	public float near;
	public float far;
	@Nonnull
	public Zoom zoom = Zoom.zero();
	public float aspectRatio;

	private Frustum(@Nonnull Zoom zoom, float aspectRatio) {
		update(zoom, aspectRatio);
	}

	boolean update(@Nonnull Zoom zoom, float aspectRatio) {
		if (!this.zoom.equals(zoom) || this.aspectRatio != aspectRatio) {
			this.zoom = zoom;
			this.aspectRatio = aspectRatio;
			this.near = zoom.level / 3f;
			this.far = zoom.level * 3f;
			this.width = zoom.x * 2 * near / 5f;
			this.height = zoom.y * width * aspectRatio;
			return true;
		}

		return false;
	}

	@Nonnull
	static Frustum create(@Nonnull Zoom zoom, float aspectRatio) {
		return new Frustum(zoom, aspectRatio);
	}

	@Nonnull
	static Frustum empty() {
		return new Frustum(Zoom.zero(), 1);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Frustum frustum = (Frustum) o;

		if (Float.compare(frustum.aspectRatio, aspectRatio) != 0) return false;
		if (!zoom.equals(frustum.zoom)) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = zoom.hashCode();
		result = 31 * result + (aspectRatio != +0.0f ? Float.floatToIntBits(aspectRatio) : 0);
		return result;
	}

	public boolean isEmpty() {
		return zoom.isZero();
	}
}
