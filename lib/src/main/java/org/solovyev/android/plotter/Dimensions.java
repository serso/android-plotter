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

	public void updateGraph() {
		graph.update(this);
	}

	public void setZoom(float level) {
		zoom = level;
		graph.update(this);
	}

	public static final class Frustum {
		public final float width;
		public final float height;
		public final float near;
		public final float far;
		public final float distance;

		Frustum(float distance, float aspectRatio) {
			this.distance = distance;
			this.near = distance / 3f;
			this.far = distance * 3f;
			this.width = 2 * near / 5f;
			this.height = width * aspectRatio;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			Frustum frustum = (Frustum) o;

			if (Float.compare(frustum.distance, distance) != 0) return false;
			if (Float.compare(frustum.far, far) != 0) return false;
			if (Float.compare(frustum.height, height) != 0) return false;
			if (Float.compare(frustum.near, near) != 0) return false;
			if (Float.compare(frustum.width, width) != 0) return false;

			return true;
		}

		@Override
		public int hashCode() {
			int result = (width != +0.0f ? Float.floatToIntBits(width) : 0);
			result = 31 * result + (height != +0.0f ? Float.floatToIntBits(height) : 0);
			result = 31 * result + (near != +0.0f ? Float.floatToIntBits(near) : 0);
			result = 31 * result + (far != +0.0f ? Float.floatToIntBits(far) : 0);
			result = 31 * result + (distance != +0.0f ? Float.floatToIntBits(distance) : 0);
			return result;
		}
	}

	public static final class Scene {
		@Nonnull
		public final RectF rect = new RectF();
		@Nonnull
		public Frustum frustum = new Frustum(0, 1);
		@Nonnull
		public final RectF view = new RectF();

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof Scene)) return false;

			Scene scene = (Scene) o;

			if (!frustum.equals(scene.frustum)) return false;
			if (!rect.equals(scene.rect)) return false;
			if (!view.equals(scene.view)) return false;

			return true;
		}

		@Override
		public int hashCode() {
			int result = rect.hashCode();
			result = 31 * result + frustum.hashCode();
			result = 31 * result + view.hashCode();
			return result;
		}

		@Nonnull
		public Frustum setFrustumDistance(float distance) {
			if (frustum.distance != distance) {
				if (frustum.distance != 0) {
					rect.right *= distance / frustum.distance;
					rect.bottom *= distance / frustum.distance;
				}
				frustum = new Frustum(distance, rect.height() / rect.width());
			}
			return frustum;
		}

		public boolean isEmpty() {
			return rect.isEmpty();
		}

		public void setViewDimensions(int width, int height) {
			this.view.right = width;
			this.view.bottom = height;

			final float aspectRatio = (float) height / (float) width;
			this.frustum = new Frustum(this.frustum.distance, aspectRatio);
			this.rect.right = 1.5f;
			this.rect.bottom = this.rect.width() * aspectRatio;
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
			if (this.rect.right != width || this.rect.bottom != height) {
				this.rect.right = width;
				this.rect.bottom = height;
				return true;
			}

			return false;
		}

		public boolean isEmpty() {
			return this.rect.isEmpty();
		}

		public void update(@Nonnull Dimensions dimensions) {
			final float requestedWidth = 20;
			final float requestedHeight = 20;
			final float aspectRatio = dimensions.scene.getAspectRatio();
			rect.right = requestedWidth * dimensions.zoom;
			rect.bottom = requestedHeight * dimensions.zoom * aspectRatio;
			zoom.x = dimensions.zoom / width();
			zoom.y = dimensions.zoom / height();
		}

		public float getXMin() {
			return -width() / 2;
		}

		public float getYMin() {
			return -height() / 2;
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
	}
}
