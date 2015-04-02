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

	public static final float DISTANCE = 4f;

	@Nonnull
	public final Graph graph = new Graph();

	@Nonnull
	public final Scene scene = new Scene();

	@Nonnull
	public Zoom zoom = Zoom.one();

	@Nonnull
	public Dimensions copy() {
		return copy(new Dimensions());
	}

	@Nonnull
	public Dimensions copy(@Nonnull Dimensions that) {
		that.zoom = this.zoom;
		that.graph.rect.set(this.graph.rect);
		that.graph.zoom.set(this.graph.zoom);
		that.scene.rect.set(this.scene.rect);
		that.scene.view.set(this.scene.view);

		return that;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Dimensions that = (Dimensions) o;

		if (!graph.equals(that.graph)) return false;
		if (!scene.equals(that.scene)) return false;
		if (!zoom.equals(that.zoom)) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = graph.hashCode();
		result = 31 * result + scene.hashCode();
		result = 31 * result + zoom.hashCode();
		return result;
	}

	@Nonnull
	public Zoom setZoom(@Nonnull Zoom level) {
		if (!zoom.equals(level)) {
			final Zoom result = zoom.divideBy(level);
			zoom = level;
			return result;
		}

		return Zoom.one();
	}

	public void update(@Nonnull Zoom zoom, int viewWidth, int viewHeight) {
		if (shouldUpdate(zoom, viewWidth, viewHeight)) {
			final boolean viewChanged = scene.setViewDimensions(viewWidth, viewHeight);
			final Zoom zoomChange = setZoom(zoom);
			final boolean zoomChanged = !zoomChange.isOne();
			if (viewChanged) {
				scene.rect.left *= zoom.level;
				scene.rect.right *= zoom.level;
				scene.rect.bottom *= zoom.level;
				scene.rect.top *= zoom.level;

				final float mx = middle(scene.rect.right, scene.rect.left) * zoom.x;
				final float my = middle(scene.rect.top, scene.rect.bottom) * zoom.y;
				final float width = scene.rect.width() * zoom.x;
				final float height = scene.rect.height() * zoom.y;

				scene.rect.right = mx + width / 2f;
				scene.rect.left = mx - width / 2f;
				scene.rect.bottom = my + height / 2f;
				scene.rect.top = my - height / 2f;
			} else if (zoomChanged) {
				scene.rect.left /= zoomChange.level;
				scene.rect.right /= zoomChange.level;
				scene.rect.bottom /= zoomChange.level;
				scene.rect.top /= zoomChange.level;

				final float mx = middle(scene.rect.right, scene.rect.left) / zoomChange.x;
				final float my = middle(scene.rect.top, scene.rect.bottom) / zoomChange.y;
				final float width = scene.rect.width() / zoomChange.x;
				final float height = scene.rect.height() / zoomChange.y;

				scene.rect.right = mx + width / 2f;
				scene.rect.left = mx - width / 2f;
				scene.rect.bottom = my + height / 2f;
				scene.rect.top = my - height / 2f;
			}

			graph.update(scene.getAspectRatio(), zoom);
		}
	}

	private static float middle(float from, float to) {
		return (from + to) / 2f;
	}

	boolean shouldUpdate(@Nonnull Zoom zoom, int viewWidth, int viewHeight) {
		return !this.zoom.equals(zoom) || this.scene.view.width() != viewWidth || this.scene.view.height() != viewHeight;
	}

	public boolean isEmpty() {
		return graph.isEmpty() || scene.isEmpty();
	}

	public void moveCamera(float dx, float dy) {
	}

	public static final class Scene {

		public static final float WIDTH = 1.5f;

		@Nonnull
		public final RectF rect = new RectF();
		@Nonnull
		public final RectF view = new RectF();

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof Scene)) return false;

			Scene scene = (Scene) o;

			if (!rect.equals(scene.rect)) return false;
			if (!view.equals(scene.view)) return false;

			return true;
		}

		@Override
		public int hashCode() {
			int result = rect.hashCode();
			result = 31 * result + view.hashCode();
			return result;
		}

		public boolean isEmpty() {
			return rect.isEmpty();
		}

		public boolean setViewDimensions(int viewWidth, int viewHeight) {
			if (view.width() != viewWidth || view.height() != viewHeight) {
				view.right = viewWidth;
				view.bottom = viewHeight;

				final float aspectRatio = (float) viewHeight / (float) viewWidth;
				rect.left = -WIDTH / 2f;
				rect.right = WIDTH + rect.left;
				final float height = rect.width() * aspectRatio;
				rect.top = -height / 2;
				rect.bottom = height + rect.top;
				return true;
			}

			return false;
		}

		private float getAspectRatio() {
			return rect.height() / rect.width();
		}

		public float height() {
			return rect.height();
		}

		public float width() {
			return rect.width();
		}

		public float toSceneX(float x) {
			return x * rect.width() / view.width() - rect.width() / 2;
		}

		public float toSceneDx(float dx) {
			return dx * rect.width() / view.width();
		}

		public float toSceneY(float y) {
			return -(y * rect.height() / view.height() - rect.height() / 2);
		}

		public float toSceneDy(float dy) {
			return dy * rect.height() / view.height();
		}

		@Override
		public String toString() {
			return "Scene{" +
					"rect=" + rect +
					", view=" + view +
					'}';
		}
	}

	public static final class Graph {
		@Nonnull
		public final PointF zoom = new PointF(1f, 1f);

		@Nonnull
		public final RectF rect = new RectF();

		@Nonnull
		private final PointF center = new PointF();

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof Graph)) return false;

			Graph graph = (Graph) o;

			if (!rect.equals(graph.rect)) return false;
			if (!zoom.equals(graph.zoom)) return false;
			if (!center.equals(graph.center)) return false;

			return true;
		}

		@Override
		public int hashCode() {
			int result = zoom.hashCode();
			result = 31 * result + rect.hashCode();
			return result;
		}

		private boolean set(float width, float height) {
			if (rect.width() != width || rect.height() != height) {
				rect.left = center.x - width / 2;
				rect.right = width + rect.left;
				rect.top = center.y - height / 2;
				rect.bottom = height + rect.top;
				return true;
			}

			return false;
		}

		public boolean isEmpty() {
			return rect.isEmpty();
		}

		public void update(float aspectRatio, @Nonnull Zoom zoom) {
			final float requestedWidth = 20;
			final float requestedHeight = 20;
			final float width = requestedWidth * zoom.level;
			final float height = requestedHeight * zoom.level * aspectRatio;
			set(width, height);
			this.zoom.x = 1f / requestedWidth;
			this.zoom.y = 1f / (requestedHeight * aspectRatio);
		}

		public float toGraphX(float x) {
			return x / zoom.x;
		}

		public float toGraphY(float y) {
			return y / zoom.y;
		}

		public float toGraphZ(float z) {
			return toGraphY(z);
		}

		public float toScreenX(float x) {
			return x * zoom.x;
		}

		public float toScreenY(float y) {
			return y * zoom.y;
		}

		public float toScreenZ(float z) {
			return toScreenY(z);
		}

		public float width() {
			return rect.width();
		}

		public float height() {
			return rect.height();
		}

		@Override
		public String toString() {
			return "Graph{" +
					"zoom=" + zoom +
					", rect=" + rect +
					'}';
		}
	}

	@Nonnull
	public static Dimensions empty() {
		EMPTY.scene.rect.setEmpty();
		EMPTY.scene.view.setEmpty();
		EMPTY.graph.rect.setEmpty();
		return EMPTY;
	}

	@Override
	public String toString() {
		return "Dimensions{" +
				"graph=" + graph +
				", scene=" + scene +
				", zoom=" + zoom +
				'}';
	}
}
