package org.solovyev.android.plotter;

public class ZoomLevels {
	public float x;
	public float y;

	void check() {
		if ((x > 1f && y < 1f) || (y > 1f && x < 1f)) {
			reset();
		}
	}

	boolean isChanged() {
		return x != 1f || y != 1f;
	}

	@Override
	public String toString() {
		return "ZoomLevels{" +
				"x=" + x +
				", y=" + y +
				'}';
	}

	public float getLevel() {
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
}
