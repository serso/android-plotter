package org.solovyev.android.plotter;

final class RectSizeF {
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
		return "Size[" +
				"w=" + width +
				", h=" + height +
				']';
	}
}
