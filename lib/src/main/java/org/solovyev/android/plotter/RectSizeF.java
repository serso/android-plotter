package org.solovyev.android.plotter;

import android.support.annotation.NonNull;

public final class RectSizeF {
	public float width;
	public float height;

	public RectSizeF() {
	}

	public RectSizeF(float width, float height) {
		this.width = width;
		this.height = height;
	}

	public void setEmpty() {
		width = 0f;
		height = 0f;
	}

	public void multiplyBy(float value) {
		width *= value;
		height *= value;
	}

	@Override
	public String toString() {
		return "Size" + stringSize();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		final RectSizeF that = (RectSizeF) o;

		if (Float.compare(that.width, width) != 0) return false;
		return Float.compare(that.height, height) == 0;

	}

	@Override
	public int hashCode() {
		int result = (width != +0.0f ? Float.floatToIntBits(width) : 0);
		result = 31 * result + (height != +0.0f ? Float.floatToIntBits(height) : 0);
		return result;
	}

	public void set(@NonNull RectSizeF that) {
		set(that.width, that.height);
	}

	public void set(float width, float height) {
		this.width = width;
		this.height = height;
	}

	public boolean isEmpty() {
		return width == 0f || height == 0f;
	}

	public float aspectRatio() {
		if (isEmpty()) {
			return 0;
		}
		return width / height;
	}

	@NonNull
	public String stringSize() {
		return "[w=" + width + ", h=" + height + "]";
	}
}
