/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Contact details
 *
 * Email: se.solovyev@gmail.com
 * Site:  http://se.solovyev.org
 */

package org.solovyev.android.plotter;

import android.graphics.PointF;
import android.graphics.RectF;
import android.support.annotation.NonNull;

public final class Dimensions {
    @NonNull
    private static final Dimensions EMPTY = new Dimensions();

    @NonNull
    public final Graph graph = new Graph();

    @NonNull
    public final Scene scene = new Scene();

    @NonNull
    public static Dimensions empty() {
        EMPTY.scene.setEmpty();
        EMPTY.graph.setEmpty();
        return EMPTY;
    }

    @NonNull
    static String toString(@NonNull PointF point) {
        return "(x=" + point.x + ", y=" + point.y + ")";
    }

    @NonNull
    static String toString(@NonNull RectF rect) {
        return "[x=" + rect.left + ", y=" + rect.top + ", w=" + rect.width() + ", h=" + rect.height() + "]";
    }

    @NonNull
    public Dimensions copy() {
        return copy(new Dimensions());
    }

    @NonNull
    public Dimensions copy(@NonNull Dimensions that) {
        that.graph.copy(this.graph);
        that.scene.copy(this.scene);

        return that;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Dimensions that = (Dimensions) o;

        if (!graph.equals(that.graph)) return false;
        if (!scene.equals(that.scene)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = graph.hashCode();
        result = 31 * result + scene.hashCode();
        return result;
    }

    @NonNull
    public Dimensions updateScene(@NonNull RectSize viewSize, @NonNull RectSizeF sceneSize, @NonNull PointF sceneCenter) {
        if (scene.same(viewSize, sceneSize, sceneCenter)) {
            return this;
        }
        final Dimensions copy = copy();
        copy.setScene(viewSize, sceneSize, sceneCenter);
        return copy;
    }

    @NonNull
    public Dimensions updateGraph(@NonNull RectSizeF graphSize, @NonNull PointF graphCenter) {
        if (graph.same(graphSize, graphCenter)) {
            return this;
        }
        final Dimensions copy = copy();
        copy.setGraph(graphSize, graphCenter);
        return copy;
    }

    private void setGraph(@NonNull RectSizeF graphSize, @NonNull PointF graphCenter) {
        graph.set(graphSize, graphCenter, scene);
    }

    private void setScene(@NonNull RectSize viewSize, @NonNull RectSizeF sceneSize, @NonNull PointF sceneCenter) {
        scene.set(viewSize, sceneSize, sceneCenter);
        graph.update(sceneSize, sceneCenter);
    }

    public boolean isZero() {
        return graph.isEmpty() || scene.isEmpty();
    }

    @Override
    public String toString() {
        return "Dimensions{" +
                "graph=" + graph +
                ", scene=" + scene +
                '}';
    }

    public static final class Scene {

        @NonNull
        public final PointF center = new PointF();
        @NonNull
        public final RectSizeF size = new RectSizeF();
        @NonNull
        public final RectSize view = RectSize.empty();

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Scene)) return false;

            Scene scene = (Scene) o;

            if (!center.equals(scene.center)) return false;
            if (!size.equals(scene.size)) return false;
            if (!view.equals(scene.view)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = center.hashCode();
            result = 31 * result + size.hashCode();
            result = 31 * result + view.hashCode();
            return result;
        }

        public boolean isEmpty() {
            return size.isEmpty();
        }

        public boolean set(@NonNull RectSize viewSize, @NonNull RectSizeF sceneSize, @NonNull PointF sceneCenter) {
            if (same(viewSize, sceneSize, sceneCenter)) {
                return false;
            }
            view.set(viewSize);
            size.set(sceneSize);
            center.set(sceneCenter);
            return true;

        }

        private boolean same(@NonNull RectSize viewSize, @NonNull RectSizeF sceneSize, @NonNull PointF sceneCenter) {
            return view.equals(viewSize) && size.equals(sceneSize) && center.equals(sceneCenter);
        }

        public float toSceneX(float viewX) {
            return center.x + viewX * size.width / view.width - size.width / 2;
        }

        public float toSceneDx(float viewDx) {
            return viewDx * size.width / view.width;
        }

        public float toSceneY(float viewY) {
            return center.y + -(viewY * size.height / view.height - size.height / 2);
        }

        public float toSceneDy(float viewDy) {
            return viewDy * size.height / view.height;
        }

        @Override
        public String toString() {
            return "Scene{" +
                    "center=" + center +
                    ", size=" + size.stringSize() +
                    ", view=" + view.stringSize() +
                    '}';
        }

        public float centerXForStep(float step, boolean d3) {
            if (d3) {
                return 0;
            }
            return -center.x + center.x % step;
        }

        public float centerYForStep(float step, boolean d3) {
            if (d3) {
                return 0;
            }
            return -center.y + center.y % step;
        }

        public void copy(@NonNull Scene that) {
            center.set(that.center);
            size.set(that.size);
            view.set(that.view);
        }

        public void setEmpty() {
            center.set(0f, 0f);
            size.setEmpty();
            view.setEmpty();
        }
    }

    public static final class Graph {
        public static final float SIZE = 10f;
        @NonNull
        public final RectSizeF size = new RectSizeF();
        @NonNull
        public final RectSizeF original = new RectSizeF();
        @NonNull
        public final PointF center = new PointF();
        @NonNull
        public final PointF scale = new PointF();

        public Graph() {
            setEmpty();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final Graph that = (Graph) o;

            if (!scale.equals(that.scale)) return false;
            if (!size.equals(that.size)) return false;
            if (!original.equals(that.original)) return false;
            return center.equals(that.center);
        }

        @Override
        public int hashCode() {
            int result = size.hashCode();
            result = 31 * result + original.hashCode();
            result = 31 * result + center.hashCode();
            result = 31 * result + scale.hashCode();
            return result;
        }

        public boolean isEmpty() {
            return size.isEmpty();
        }

        public void update(@NonNull RectSizeF sceneSize, @NonNull PointF sceneCenter) {
            final float zoomLevel = sceneSize.width / Frustum.SCENE_WIDTH;
            size.set(zoomLevel * original.width, zoomLevel * original.height);
            scale.set(size.width / sceneSize.width, size.height / sceneSize.height);
            center.set(toGraphX(sceneCenter.x), toGraphY(sceneCenter.y));
        }

        public void set(@NonNull RectSizeF graphSize, @NonNull PointF graphCenter, @NonNull Scene scene) {
            original.set(graphSize);
            scene.size.set(Frustum.SCENE_WIDTH, Frustum.SCENE_WIDTH / scene.size.aspectRatio());
            scene.center.set(0f, 0f);
            update(scene.size, scene.center);

            center.set(graphCenter.x, graphCenter.y);
            scene.center.set(toScreenX(graphCenter.x), toScreenY(graphCenter.y));
        }

        public float toGraphX(float x) {
            return /*rect.centerX()*/ +scaleToGraphX(x);
        }

        public float scaleToGraphX(float x) {
            return x * scale.x;
        }

        public float toGraphY(float y) {
            return /*rect.centerY()*/ +scaleToGraphY(y);
        }

        public float scaleToGraphY(float y) {
            return y * scale.y;
        }

        public float toGraphZ(float z) {
            return toGraphY(z);
        }

        public float toScreenX(float x) {
            return /*scaleToScreenX(-rect.centerX())*/ +scaleToScreenX(x);
        }

        public float scaleToScreenX(float x) {
            return x / scale.x;
        }

        public float toScreenY(float y) {
            return /*scaleToScreenY(-rect.centerY())*/ +scaleToScreenY(y);
        }

        public float scaleToScreenY(float y) {
            return y / scale.y;
        }

        public float toScreenZ(float z) {
            return toScreenY(z);
        }

        public float width() {
            return size.width;
        }

        public float height() {
            return size.height;
        }

        @NonNull
        public RectF makeBounds() {
            return new RectF(xMin(), yMin(), xMax(), yMax());
        }

        @Override
        public String toString() {
            return "Graph{" +
                    "size=" + size.stringSize() +
                    ", original=" + original.stringSize() +
                    ", center=" + center +
                    ", scale=" + scale +
                    '}';
        }

        public float xMin() {
            return -size.width / 2 + center.x;
        }

        public float xMax() {
            return size.width / 2 + center.x;
        }

        public float yMin() {
            return -size.height / 2 + center.y;
        }

        public float yMax() {
            return size.height / 2 + center.y;
        }

        public void copy(@NonNull Graph that) {
            size.set(that.size);
            original.set(that.original);
            center.set(that.center);
            scale.set(that.scale);
        }

        public boolean same(@NonNull RectSizeF graphSize, @NonNull PointF graphCenter) {
            return size.equals(graphSize) && center.equals(graphCenter);
        }

        public void setEmpty() {
            original.set(SIZE, SIZE);
            size.set(original);
            center.set(0f, 0f);
            scale.set(1f, 1f);
        }
    }
}
