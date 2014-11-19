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

	static final float DISTANCE = 4f;

	@Nonnull
	public final Graph graph = new Graph();

	@Nonnull
	public final Scene scene = new Scene();

	public float zoom = 1f;

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
		if (!(o instanceof Dimensions)) return false;

		Dimensions that = (Dimensions) o;

		if (Float.compare(that.zoom, zoom) != 0) return false;
		if (!graph.equals(that.graph)) return false;
		if (!scene.equals(that.scene)) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = graph.hashCode();
		result = 31 * result + (zoom != +0.0f ? Float.floatToIntBits(zoom) : 0);
		return result;
	}

	public float setZoom(float level) {
		if (zoom != level) {
			final float result = zoom / level;
			zoom = level;
			return result;
		}

		return 1;
	}

	public void update(float zoom, int viewWidth, int viewHeight) {
		if (shouldUpdate(zoom, viewWidth, viewHeight)) {
			final boolean viewChanged = scene.setViewDimensions(viewWidth, viewHeight);
			final float zoomChange = setZoom(zoom);
			final boolean zoomChanged = zoomChange != 1;
			if (viewChanged) {
				scene.rect.left *= zoom;
				scene.rect.right *= zoom;
				scene.rect.bottom *= zoom;
				scene.rect.top *= zoom;
			} else if (zoomChanged) {
				scene.rect.left /= zoomChange;
				scene.rect.right /= zoomChange;
				scene.rect.bottom /= zoomChange;
				scene.rect.top /= zoomChange;
			}
			graph.update(this);
		}
	}

	boolean shouldUpdate(float zoom, int viewWidth, int viewHeight) {
		return this.zoom != zoom || this.scene.view.width() != viewWidth || this.scene.view.height() != viewHeight;
	}

	public boolean isEmpty() {
		return graph.isEmpty() || scene.isEmpty();
	}

	public static final class Scene {
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
				rect.left = -1.5f / 2f;
				rect.right = 1.5f + rect.left;
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

		public void multiplyBy(float w, float h) {
			rect.right *= w;
			rect.bottom *= h;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof Graph)) return false;

			Graph graph = (Graph) o;

			if (!rect.equals(graph.rect)) return false;
			if (!zoom.equals(graph.zoom)) return false;

			return true;
		}

		@Override
		public int hashCode() {
			int result = zoom.hashCode();
			result = 31 * result + rect.hashCode();
			return result;
		}

		public boolean set(float width, float height) {
			if (rect.width() != width || rect.height() != height) {
				rect.left = -width / 2;
				rect.right = width + rect.left;
				rect.top = -height / 2;
				rect.bottom = height + rect.top;
				return true;
			}

			return false;
		}

		public boolean isEmpty() {
			return rect.isEmpty();
		}

		public void update(@Nonnull Dimensions dimensions) {
			final float requestedWidth = 20;
			final float requestedHeight = 20;
			final float aspectRatio = dimensions.scene.getAspectRatio();
			final float width = requestedWidth * dimensions.zoom;
			final float height = requestedHeight * dimensions.zoom * aspectRatio;
			set(width, height);
			zoom.x = dimensions.zoom / width();
			zoom.y = dimensions.zoom / height();
		}

		public float toGraphX(float x) {
			return x / zoom.x;
		}

		private float toGraphY(float y) {
			return y / zoom.y;
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
