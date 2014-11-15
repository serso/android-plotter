package org.solovyev.android.plotter;

import javax.annotation.Nonnull;

public final class Frustum {
	public float width;
	public float height;
	public float near;
	public float far;
	public float distance;
	public float aspectRatio;

	private Frustum(float distance, float aspectRatio) {
		update(distance, aspectRatio);
	}

	boolean update(float distance, float aspectRatio) {
		if (this.distance != distance || this.aspectRatio != aspectRatio) {
			this.distance = distance;
			this.aspectRatio = aspectRatio;
			this.near = distance / 3f;
			this.far = distance * 3f;
			this.width = 2 * near / 5f;
			this.height = width * aspectRatio;
			return true;
		}

		return false;
	}

	@Nonnull
	static Frustum create(float distance, float aspectRatio) {
		return new Frustum(distance, aspectRatio);
	}

	@Nonnull
	static Frustum empty() {
		return new Frustum(0, 1);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Frustum frustum = (Frustum) o;

		if (Float.compare(frustum.aspectRatio, aspectRatio) != 0) return false;
		if (Float.compare(frustum.distance, distance) != 0) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = (distance != +0.0f ? Float.floatToIntBits(distance) : 0);
		result = 31 * result + (aspectRatio != +0.0f ? Float.floatToIntBits(aspectRatio) : 0);
		return result;
	}

	public boolean isEmpty() {
		return distance == 0f;
	}
}
