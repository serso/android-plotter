package org.solovyev.android.plotter.meshes;

import org.solovyev.android.plotter.Dimensions;

import javax.annotation.Nonnull;

final class MeshDimensions {
	@Nonnull
	final Dimensions d;
	final float xMin;
	final float xMax;
	final float yMin;
	final float width;
	final float height;
	final boolean graph;

	protected MeshDimensions(@Nonnull Dimensions d, boolean graph) {
		this.d = d;
		this.graph = graph;
		if (graph) {
			this.xMin = d.graph.getXMin(d.camera);
			this.xMax = this.xMin + d.graph.width();
			this.yMin = d.graph.getYMin(d.camera);
			this.width = d.graph.width();
			this.height = d.graph.height();
		} else {
			final float minAxis = Math.min(d.scene.width(), d.scene.height());
			final float tickedAxisLength = minAxis - 4 * (minAxis) / (Axis.TICKS + 4 - 1);
			this.xMin = -tickedAxisLength / 2;
			this.xMax = tickedAxisLength / 2;
			this.yMin = -tickedAxisLength / 2;
			this.width = tickedAxisLength;
			this.height = tickedAxisLength;
		}
	}

	public boolean isEmpty() {
		return d.graph.isEmpty();
	}

	public void scale(float[] point) {
		if (graph) {
			point[0] = d.graph.toScreenX(point[0]);
			point[1] = d.graph.toScreenY(point[1]);
			point[2] = d.graph.toScreenZ(point[2]);
		}
	}
}
