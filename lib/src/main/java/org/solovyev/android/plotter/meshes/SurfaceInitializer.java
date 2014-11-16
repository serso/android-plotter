package org.solovyev.android.plotter.meshes;

import android.graphics.RectF;
import org.solovyev.android.plotter.Dimensions;

import javax.annotation.Nonnull;

class SurfaceInitializer {

	@Nonnull
	private final BaseSurface surface;

	@Nonnull
	private final Data data;

	SurfaceInitializer(@Nonnull BaseSurface surface, @Nonnull Data data) {
		this.data = data;
		this.surface = surface;
	}

	@Nonnull
	public static SurfaceInitializer createForGraph(@Nonnull BaseSurface surface, @Nonnull Dimensions.Graph graph) {
		return new SurfaceInitializer(surface, Data.create(graph.rect));
	}

	@Nonnull
	public static SurfaceInitializer create(@Nonnull BaseSurface surface, @Nonnull RectF bounds) {
		return new SurfaceInitializer(surface, Data.create(bounds));
	}

	public void init(@Nonnull Arrays arrays) {
		arrays.init(3 * data.totalVertices(), data.totalVertices());

		final float dx = data.dx();
		final float dy = data.dy();

		final float[] point = new float[3];

		int vertex = 0;
		for (int yi = 0; yi < data.yVertices; yi++) {
			final float y = data.bounds.top + yi * dy;
			final boolean yEven = yi % 2 == 0;

			for (int xi = 0; xi < data.xVertices; xi++) {
				final boolean xEven = xi % 2 == 0;
				int ii = xi * (data.yVertices - 1) + xi;
				int iv = yi * (data.xVertices - 1) + yi;
				if (xEven) {
					ii += yi;
				} else {
					ii += (data.yVertices - 1 - yi);
				}
				if (yEven) {
					iv += xi;
				} else {
					iv += (data.xVertices - 1 - xi);
				}

				final float x;
				if (yEven) {
					// going right
					x = data.bounds.left + xi * dx;
				} else {
					// going left
					x = data.bounds.right - xi * dx;
				}

				final float z = surface.z(x, y, xi, yi);

				point[0] = x;
				point[1] = y;
				point[2] = z;

				scale(point);
				rotate(point);

				arrays.indices[ii] = (short) iv;
				arrays.vertices[vertex++] = point[0];
				arrays.vertices[vertex++] = point[2];
				arrays.vertices[vertex++] = point[1];
			}
		}
	}

	protected void rotate(float[] point) {
	}

	protected void scale(float[] point) {
	}

	final static class Data {

		@Nonnull
		final RectF bounds = new RectF();
		int xVertices;
		int yVertices;

		@Nonnull
		public static Data create(@Nonnull RectF bounds) {
			return create(bounds, 20, 20);
		}

		@Nonnull
		public static Data create(@Nonnull RectF bounds, int xVertices, int yVertices) {
			final Data data = new Data();
			data.bounds.set(bounds);
			data.xVertices = xVertices;
			data.yVertices = yVertices;
			return data;
		}

		private float dy() {
			return bounds.height() / (yVertices - 1);
		}

		private float dx() {
			return bounds.width() / (xVertices - 1);
		}

		private int totalVertices() {
			return xVertices *yVertices;
		}
	}

	public static class GraphSurfaceInitializer extends SurfaceInitializer {

		@Nonnull
		private final Dimensions.Graph graph;

		public GraphSurfaceInitializer(@Nonnull BaseSurface surface, @Nonnull Dimensions.Graph graph) {
			super(surface, Data.create(graph.rect));
			this.graph = graph;
		}

		@Override
		protected void scale(float[] point) {
			point[0] = graph.toScreenX(point[0]);
			point[1] = graph.toScreenY(point[1]);
			point[2] = graph.toScreenZ(point[2]);
		}
	}
}
