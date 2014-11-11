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

import javax.annotation.Nonnull;

public final class Dimensions {

	//                    |<--------------gw-------------->|
	//                   xMin                                xMax
	// -------------------|------------------------------------|--------------------
	//                    |<-------------vwPxs------------>|
	//
	/*
	*
	*
	*        yMax   ------0------------------------------------|--> xPxs
	*               ^     |
	*               |     |
	*               v     |                  y
	*               H     |                  ^
	*               e     |                  |
	*               i     |                  |
	*               g     |                  |
	*               h     |------------------0-----------------|--> x
	*               t     |                  |
	*               |     |                  |
	*               |     |                  |
	*               v     |                  |
	*        yMin   -------                  -
	*                     |                  |
	*                     v
	*                    yPxs
	*
	* */


	@Nonnull
	public final Camera camera = new Camera();

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
		that.camera.x = this.camera.x;
		that.camera.y = this.camera.y;
		that.graph.width = this.graph.width;
		that.graph.height = this.graph.height;
		that.graph.zoom.x = this.graph.zoom.x;
		that.graph.zoom.y = this.graph.zoom.y;
		that.scene.width = this.scene.width;
		that.scene.height = this.scene.height;

		return that;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Dimensions)) return false;

		Dimensions that = (Dimensions) o;

		if (Float.compare(that.zoom, zoom) != 0) return false;
		if (!camera.equals(that.camera)) return false;
		if (!graph.equals(that.graph)) return false;
		if (!scene.equals(that.scene)) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = camera.hashCode();
		result = 31 * result + graph.hashCode();
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

	public static final class Camera {
		static final float DISTANCE = 4f;
		public float x = 0;
		public float y = 0;

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			Camera that = (Camera) o;

			if (Float.compare(that.x, x) != 0) return false;
			if (Float.compare(that.y, y) != 0) return false;

			return true;
		}

		@Override
		public int hashCode() {
			int result = (x != +0.0f ? Float.floatToIntBits(x) : 0);
			result = 31 * result + (y != +0.0f ? Float.floatToIntBits(y) : 0);
			return result;
		}
	}

	public static final class Zoom {
		public float x = 1f;
		public float y = 1f;

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof Zoom)) return false;

			Zoom zoom = (Zoom) o;

			if (Float.compare(zoom.x, x) != 0) return false;
			if (Float.compare(zoom.y, y) != 0) return false;

			return true;
		}

		@Override
		public int hashCode() {
			int result = (x != +0.0f ? Float.floatToIntBits(x) : 0);
			result = 31 * result + (y != +0.0f ? Float.floatToIntBits(y) : 0);
			return result;
		}

		public float getLevel() {
			return x;
		}

		void setLevel(float level) {
			float ratio = level / getLevel();
			x = level;
			y = y * ratio;
		}
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
		public float width;
		public float height;
		@Nonnull
		public Frustum frustum = new Frustum(0, 1);

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof Scene)) return false;

			Scene scene = (Scene) o;

			if (Float.compare(scene.height, height) != 0) return false;
			if (Float.compare(scene.width, width) != 0) return false;
			if (!frustum.equals(scene.frustum)) return false;

			return true;
		}

		@Override
		public int hashCode() {
			int result = (width != +0.0f ? Float.floatToIntBits(width) : 0);
			result = 31 * result + (height != +0.0f ? Float.floatToIntBits(height) : 0);
			result = 31 * result + frustum.hashCode();
			return result;
		}

		@Nonnull
		public Frustum setFrustumDistance(float distance) {
			if (frustum.distance != distance) {
				if (frustum.distance != 0) {
					width *= distance / frustum.distance;
					height *= distance / frustum.distance;
				}
				frustum = new Frustum(distance, height / width);
			}
			return frustum;
		}

		public boolean isEmpty() {
			return width == 0 || height == 0;
		}

		public void setViewDimensions(int width, int height) {
			final float aspectRatio = (float) height / (float) width;
			this.frustum = new Frustum(this.frustum.distance, aspectRatio);
			this.width = 1.5f;
			this.height = this.width * aspectRatio;
		}

		private float getAspectRatio() {
			return height / width;
		}
	}

	public static final class Graph {
		@Nonnull
		public final Zoom zoom = new Zoom();

		public float width;
		public float height;

		public void multiplyBy(float w, float h) {
			width *= w;
			height *= h;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof Graph)) return false;

			Graph graph = (Graph) o;

			if (Float.compare(graph.height, height) != 0) return false;
			if (Float.compare(graph.width, width) != 0) return false;
			if (!zoom.equals(graph.zoom)) return false;

			return true;
		}

		@Override
		public int hashCode() {
			int result = zoom.hashCode();
			result = 31 * result + (width != +0.0f ? Float.floatToIntBits(width) : 0);
			result = 31 * result + (height != +0.0f ? Float.floatToIntBits(height) : 0);
			return result;
		}

		public boolean set(float width, float height) {
			if (this.width != width || this.height != height) {
				this.width = width;
				this.height = height;
				return true;
			}

			return false;
		}

		public boolean isEmpty() {
			return width == 0f || height == 0f;
		}

		public void update(@Nonnull Dimensions dimensions) {
			final float requestedWidth = 20;
			final float requestedHeight = 20;
			final float aspectRatio = dimensions.scene.getAspectRatio();
			width = requestedWidth * dimensions.zoom;
			height = requestedHeight * dimensions.zoom * aspectRatio;
			zoom.x = dimensions.zoom / width;
			zoom.y = dimensions.zoom / height;
		}

		public float getXMin(@Nonnull Camera camera) {
			return toGraphX(camera.x) - width / 2;
		}

		private float toGraphX(float x) {
			return x / zoom.x;
		}

		private float toGraphY(float y) {
			return y / zoom.y;
		}

		public float getYMin(@Nonnull Camera camera) {
			return toGraphY(camera.y) - height / 2;
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
	}
}
