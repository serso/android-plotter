package org.solovyev.android.plotter.meshes;

import org.solovyev.android.plotter.Dimensions;

import javax.annotation.Nonnull;

final class MeshDimensions {
	@Nonnull
	final Dimensions d;
	float xMin;
	float xMax;
	float yMin;
	float width;
	float height;
	boolean graph;
	int widthVertices;
	int heightVertices;

	protected MeshDimensions(@Nonnull Dimensions d, boolean graph) {
		this.d = d;
		this.graph = graph;
	}

	public void init() {
		if (graph) {
			this.xMin = d.graph.getXMin();
			this.xMax = this.xMin + d.graph.width();
			this.yMin = d.graph.getYMin();
			this.width = d.graph.width();
			this.height = d.graph.height();
			this.widthVertices = 20;
			this.heightVertices = 20;
		} else {
			final Scene.Axis xAxis = Scene.Axis.create(d.scene, false);
			final Scene.Axis yAxis = Scene.Axis.create(d.scene, true);
			final Scene.Ticks xTicks = Scene.Ticks.create(d.graph, xAxis);
			final Scene.Ticks yTicks = Scene.Ticks.create(d.graph, yAxis);
			this.xMin = -xTicks.axisLength / 2;
			this.xMax = xTicks.axisLength / 2;
			this.yMin = -yTicks.axisLength / 2;
			this.width = xTicks.axisLength;
			this.height = yTicks.axisLength;
			this.widthVertices = xTicks.count;
			this.heightVertices = yTicks.count;
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

	int totalVertices() {
		return widthVertices * heightVertices;
	}
}
