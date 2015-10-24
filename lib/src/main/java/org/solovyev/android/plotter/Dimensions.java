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

import javax.annotation.Nonnull;

public final class Dimensions {
	@Nonnull
	private static final Dimensions EMPTY = new Dimensions();
	@Nonnull
	public static final PointF ZERO = new PointF();

	@Nonnull
	public final Graph graph = new Graph();

	@Nonnull
	public final Scene scene = new Scene();

	@Nonnull
	public Dimensions copy() {
		return copy(new Dimensions());
	}

	@Nonnull
	public Dimensions copy(@Nonnull Dimensions that) {
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

	@Nonnull
	public Dimensions update(@Nonnull RectSize viewSize, @Nonnull RectSizeF sceneSize, @Nonnull PointF sceneCenter) {
		if (scene.same(viewSize, sceneSize, sceneCenter)) {
			return this;
		}
		final Dimensions copy = copy();
		copy.set(viewSize, sceneSize, sceneCenter);
		return copy;
	}

	private void set(@Nonnull RectSize viewSize, @Nonnull RectSizeF sceneSize, @Nonnull PointF sceneCenter) {
		scene.set(viewSize, sceneSize, sceneCenter);
		graph.update(sceneSize, sceneCenter);
	}

	public boolean isZero() {
		return graph.isEmpty() || scene.isEmpty();
	}

	public static final class Scene {

		@Nonnull
		public final PointF center = new PointF();
		@Nonnull
		public final RectSizeF size = new RectSizeF();
		@Nonnull
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

		public boolean set(@Nonnull RectSize viewSize, @Nonnull RectSizeF sceneSize, @Nonnull PointF sceneCenter) {
			if (same(viewSize, sceneSize, sceneCenter)) {
				return false;
			}
			view.set(viewSize);
			size.set(sceneSize);
			center.set(sceneCenter);
			return true;

		}

		private boolean same(@Nonnull RectSize viewSize, @Nonnull RectSizeF sceneSize, @Nonnull PointF sceneCenter) {
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

		public float centerXForStep(float step) {
			return -center.x + center.x % step;
		}

		public float centerYForStep(float step) {
			return -center.y + center.y % step;
		}

		public void copy(@Nonnull Scene that) {
			center.set(that.center);
			size.set(that.size);
			view.set(that.view);
		}
	}

	public static final class Graph {
		@Nonnull
		public final RectSizeF size = new RectSizeF(20f, 20f);
		@Nonnull
		public final RectSizeF original = new RectSizeF(20f, 20f);
		@Nonnull
		public final PointF center = new PointF(0f, 0f);
		public float scale = original.width / Frustum.SCENE_WIDTH;

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			final Graph that = (Graph) o;

			if (Float.compare(that.scale, scale) != 0) return false;
			if (!size.equals(that.size)) return false;
			if (!original.equals(that.original)) return false;
			return center.equals(that.center);
		}

		@Override
		public int hashCode() {
			int result = size.hashCode();
			result = 31 * result + original.hashCode();
			result = 31 * result + center.hashCode();
			result = 31 * result + (scale != +0.0f ? Float.floatToIntBits(scale) : 0);
			return result;
		}

		public boolean isEmpty() {
			return size.isEmpty();
		}

		public void update(@Nonnull RectSizeF sceneSize, @Nonnull PointF sceneCenter) {
			final float zoomLevel = sceneSize.width / Frustum.SCENE_WIDTH;
			size.set(zoomLevel * original.width, zoomLevel * original.height);
			scale = size.width / sceneSize.width;
			center.set(toGraphX(sceneCenter.x), toGraphY(sceneCenter.y));
		}

		public float toGraphX(float x) {
			return /*rect.centerX()*/ + scaleToGraphX(x);
		}

		public float scaleToGraphX(float x) {
			return x * scale;
		}

		public float toGraphY(float y) {
			return /*rect.centerY()*/ + scaleToGraphY(y);
		}

		public float scaleToGraphY(float y) {
			return y * scale;
		}

		public float toGraphZ(float z) {
			return toGraphY(z);
		}

		public float toScreenX(float x) {
			return /*scaleToScreenX(-rect.centerX())*/ + scaleToScreenX(x);
		}

		public float scaleToScreenX(float x) {
			return x / scale;
		}

		public float toScreenY(float y) {
			return /*scaleToScreenY(-rect.centerY())*/ + scaleToScreenY(y);
		}

		public float scaleToScreenY(float y) {
			return y / scale;
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

		@Nonnull
		public RectF makeBounds() {
			return new RectF(xMin(), yMax(), xMax(), yMin());
		}

		@Override
		public String toString() {
			return "Graph{" +
					"size=" + size +
					", original=" + original +
					", center=" + center +
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

		public void copy(@Nonnull Graph that) {
			size.set(that.size);
			original.set(that.original);
			center.set(that.center);
			scale = that.scale;
		}
	}

	@Nonnull
	public static Dimensions empty() {
		EMPTY.scene.size.setEmpty();
		EMPTY.scene.view.setEmpty();
		EMPTY.graph.size.setEmpty();
		return EMPTY;
	}

	@Override
	public String toString() {
		return "Dimensions{" +
				"graph=" + graph +
				", scene=" + scene +
				'}';
	}

	@Nonnull
	static String toString(@Nonnull PointF point) {
		return "(x=" + point.x + ", y=" + point.y + ")";
	}

	@Nonnull
	static String toString(@Nonnull RectF rect) {
		return "[x=" + rect.left + ", y=" + rect.top + ", w=" + rect.width() + ", h=" + rect.height() + "]";
	}
}
