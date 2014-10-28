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


	// current position of camera in graph coordinates
	@Nonnull
	public final Camera camera = new Camera();

	@Nonnull
	public final Graph graph = new Graph();

	@Nonnull
	public final View view = new View();

	public float zoom = 1f;

	// X

	public float getXMin() {
		return camera.x - graph.width / 2;
	}

	// Y

	public float getYMin() {
		return camera.y - graph.height / 2;
	}

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
		that.view.width = this.view.width;
		that.view.height = this.view.height;

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
		if (!view.equals(that.view)) return false;

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
		graph.update(view);
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

	public static final class Frustum {
		public final float width;
		public final float height;
		public final float near;
		public final float far;
		public final float distance;

		Frustum(float distance, @Nonnull View dimensions) {
			this.distance = distance;
			this.near = distance / 3f;
			this.far = distance * 3f;
			this.width = 2 * near / 5f;
			this.height = this.width * dimensions.height / dimensions.width;
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

	public static final class View {
		public int width;
		public int height;
		@Nonnull
		public Frustum frustum = new Frustum(0, this);

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof View)) return false;

			View view = (View) o;

			if (height != view.height) return false;
			if (width != view.width) return false;
			if (!frustum.equals(view.frustum)) return false;

			return true;
		}

		@Override
		public int hashCode() {
			int result = width;
			result = 31 * result + height;
			return result;
		}

		@Nonnull
		public Frustum setFrustumDistance(float distance) {
			if (frustum.distance != distance) {
				frustum = new Frustum(distance, this);
			}
			return frustum;
		}

		public boolean isEmpty() {
			return width == 0 || height == 0;
		}

		public void set(int width, int height) {
			this.width = width;
			this.height = height;
			this.frustum = new Frustum(this.frustum.distance, this);
		}
	}

	public static final class Graph {
		public float width;
		public float height;

		public void multiplyBy(float value) {
			multiplyBy(value, value);
		}

		public void multiplyBy(float w, float h) {
			width *= w;
			height *= h;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			Graph that = (Graph) o;

			if (Float.compare(that.height, height) != 0) return false;
			if (Float.compare(that.width, width) != 0) return false;

			return true;
		}

		@Override
		public int hashCode() {
			int result = (width != +0.0f ? Float.floatToIntBits(width) : 0);
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

		public void update(@Nonnull View view) {
			final float ratio = view.frustum.near / view.frustum.far;
			width = 1;//view.width * ratio;
			height = 1;//view.height * ratio;
		}
	}
}
