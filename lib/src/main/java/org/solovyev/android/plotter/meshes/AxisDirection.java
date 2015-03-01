package org.solovyev.android.plotter.meshes;

enum AxisDirection {
	X(new int[]{1, 0, 0},
			new int[]{0, 1, 0}),
	Y(new int[]{0, 1, 0},
			new int[]{1, 0, 0}),
	Z(new int[]{0, 0, 1}, new int[]{0, 1, 0});

	final int[] vector;
	final int[] arrow;

	AxisDirection(int[] vector, int[] arrow) {
		this.vector = vector;
		this.arrow = arrow;
	}
}
