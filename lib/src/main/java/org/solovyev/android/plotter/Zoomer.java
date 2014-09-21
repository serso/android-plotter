package org.solovyev.android.plotter;

final class Zoomer {

	private float level = 1;
	private float target;
	private float step = 0;
	private float current;

	public float getLevel() {
		return level;
	}

	public float getCurrent() {
		return current;
	}

	public void reset() {
		current = level;
	}

	public boolean onFrame() {
		if (step < 0 && level > target) {
			level += step;
		} else if (step > 0 && level < target) {
			level += step;
		} else if (step != 0) {
			step = 0;
			level = target;
			return true;
		}

		return false;
	}
}
