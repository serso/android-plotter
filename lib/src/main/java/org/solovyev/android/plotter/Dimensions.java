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

	public void update(@Nonnull Zoom zoom, int viewWidth, int viewHeight, @Nonnull PointF center) {
		if (shouldUpdate(zoom, viewWidth, viewHeight, center)) {
			final boolean cameraChanged = centerChanged(center);
			final boolean viewChanged = scene.setViewDimensions(viewWidth, viewHeight);
			final Zoom zoomChange = setZoom(zoom);
			final boolean zoomChanged = !zoomChange.isOne();
			if (viewChanged) {
				scaleRect(zoom.level * zoomChange.x, zoom.level * zoomChange.y);
			} else if (zoomChanged) {
				scaleRect(1f / (zoomChange.level * zoomChange.x), 1f / (zoomChange.level * zoomChange.y));
			}

			// camera can be changed independently from view/zoom
			if (cameraChanged) {
				scene.rect.offset(center.x - scene.rect.centerX(), center.y - scene.rect.centerY());
			}

			graph.update(scene.rect.centerX(), scene.rect.centerY(), scene.getAspectRatio(), zoom);
		}
	}

	private boolean centerChanged(@Nonnull PointF center) {
		return scene.rect.centerX() != center.x || scene.rect.centerY() != center.y;
	}

	private void scaleRect(float zoomX, float zoomY) {
		final float cx = scene.rect.centerX();
		final float cy = scene.rect.centerY();
		final float width = scene.rect.width() * zoomX;
		final float height = scene.rect.height() * zoomY;

		scene.rect.left = cx - width / 2f;
		scene.rect.right = cx + width / 2f;
		scene.rect.top = cy - height / 2f;
		scene.rect.bottom = cy + height / 2f;
	}

	boolean shouldUpdate(@Nonnull Zoom zoom, int viewWidth, int viewHeight, @Nonnull PointF center) {
		return !this.zoom.equals(zoom) || this.scene.view.width() != viewWidth || this.scene.view.height() != viewHeight || centerChanged(center);
	}

	public boolean isZero() {
		return graph.isEmpty() || scene.isEmpty();
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
			return -rect.centerX() + x * rect.width() / view.width() - rect.width() / 2;
		}

		public float toSceneDx(float dx) {
			return dx * rect.width() / view.width();
		}

		public float toSceneY(float y) {
			return -rect.centerY() + -(y * rect.height() / view.height() - rect.height() / 2);
		}

		public float toSceneDy(float dy) {
			return dy * rect.height() / view.height();
		}

		@Override
		public String toString() {
			return "Scene{" +
					"rect=" + Dimensions.toString(rect) +
					", view=" + Dimensions.toString(view) +
					'}';
		}

		public float centerXForStep(float step) {
			return rect.centerX() - rect.centerX() % step;
		}

		public float centerYForStep(float step) {
			return rect.centerY() - rect.centerY() % step;
		}
	}

	public static final class Graph {
		@Nonnull
		public final PointF zoom = new PointF(1f, 1f);

		@Nonnull
		public final RectF rect = new RectF();

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

		private boolean set(float width, float height, float x, float y) {
			if (rect.width() != width || rect.height() != height || rect.centerX() != x || rect.centerY() != y) {
				rect.left = x - width / 2;
				rect.right = width + rect.left;
				rect.top = y - height / 2;
				rect.bottom = height + rect.top;
				return true;
			}

			return false;
		}

		public boolean isEmpty() {
			return rect.isEmpty();
		}

		public void update(float x, float y, float aspectRatio, @Nonnull Zoom zoom) {
			final float requestedWidth = 20;
			final float requestedHeight = 20;
			final float width = requestedWidth * zoom.level;
			final float height = requestedHeight * zoom.level * aspectRatio;
			this.zoom.x = 1f / requestedWidth;
			this.zoom.y = 1f / (requestedHeight * aspectRatio);
			set(width, height, scaleToGraphX(-x), scaleToGraphY(-y));
		}

		public float toGraphX(float x) {
			return /*rect.centerX()*/ + scaleToGraphX(x);
		}

		public float scaleToGraphX(float x) {
			return x / zoom.x;
		}

		public float toGraphY(float y) {
			return /*rect.centerY()*/ + scaleToGraphY(y);
		}

		public float scaleToGraphY(float y) {
			return y / zoom.y;
		}

		public float toGraphZ(float z) {
			return toGraphY(z);
		}

		public float toScreenX(float x) {
			return /*scaleToScreenX(-rect.centerX())*/ + scaleToScreenX(x);
		}

		public float scaleToScreenX(float x) {
			return x * zoom.x;
		}

		public float toScreenY(float y) {
			return /*scaleToScreenY(-rect.centerY())*/ + scaleToScreenY(y);
		}

		public float scaleToScreenY(float y) {
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
					"zoom=" + Dimensions.toString(zoom) +
					", rect=" + Dimensions.toString(rect) +
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

	@Nonnull
	static String toString(@Nonnull PointF point) {
		return "(x=" + point.x + ", y=" + point.y + ")";
	}

	@Nonnull
	static String toString(@Nonnull RectF rect) {
		return "[x=" + rect.left + ", y=" + rect.top + ", w=" + rect.width() + ", h=" + rect.height() + "]";
	}
}
