package org.solovyev.android.plotter;

public class ZoomLevels {

	private static final int K = 3;

	public float x;
	public float y;

	boolean isChanged() {
		return x != 1f || y != 1f;
	}

	@Override
	public String toString() {
		return "ZoomLevels{" +
				"x=" + round(x) +
				", y=" + round(y) +
				'}';
	}

	private float round(float value) {
		return ((int) (1000 * value) / 1000f);
	}

	public float getLevel() {
		if ((x > 1f && y < 1f) || (y > 1f && x < 1f)) {
			return 1f;
		}

		if (x == 1f) {
			return y;
		}

		if (y == 1f) {
			return x;
		}
		return x > 1f ? Math.max(x, y) : Math.min(x, y);
	}

	void reset() {
		x = 1f;
		y = 1f;
	}

	void update(float x, float y) {
		setX(x);
		setY(y);
	}

	void setY(float y) {
		this.y = round(y);
	}

	void setX(float x) {
		this.x = round(x);
	}
}
