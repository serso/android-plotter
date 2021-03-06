package org.solovyev.android.plotter.meshes;

import android.graphics.RectF;
import android.support.annotation.NonNull;

import org.solovyev.android.plotter.Dimensions;

class SurfaceInitializer {

    @NonNull
    private final BaseSurface surface;

    @NonNull
    private final Data data;

    SurfaceInitializer(@NonNull BaseSurface surface, @NonNull Data data) {
        this.data = data;
        this.surface = surface;
    }


    public void init(@NonNull Arrays arrays) {
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

        @NonNull
        final RectF bounds = new RectF();
        int xVertices;
        int yVertices;

        @NonNull
        public static Data create(@NonNull RectF bounds, int size) {
            return create(bounds, size, size);
        }

        @NonNull
        public static Data create(@NonNull RectF bounds, int xVertices, int yVertices) {
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
            return xVertices * yVertices;
        }
    }

    public static class GraphSurfaceInitializer extends SurfaceInitializer {

        @NonNull
        private final Dimensions.Graph graph;

        public GraphSurfaceInitializer(@NonNull BaseSurface surface, @NonNull Dimensions.Graph graph, int size) {
            super(surface, Data.create(graph.makeBounds(), size));
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
