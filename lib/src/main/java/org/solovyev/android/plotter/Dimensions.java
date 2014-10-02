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
	public static final int GRAPH_SIZE = 1;

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

	public float zoom = 1f;

	// X

	public float getXMin() {
		return camera.x - graph.width / 2;
	}

	// Y

	public float getYMin() {
		return camera.y - graph.height / 2;
	}

	public boolean setGraphDimensions(float width, float height) {
		if (graph.width != width || graph.height != height) {
			graph.width = width;
			graph.height = height;
			return true;
		}

		return false;
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

		return true;
	}

	@Override
	public int hashCode() {
		int result = camera.hashCode();
		result = 31 * result + graph.hashCode();
		result = 31 * result + (zoom != +0.0f ? Float.floatToIntBits(zoom) : 0);
		return result;
	}

	public static final class Camera {
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

	public static final class Graph {
		public float width = GRAPH_SIZE;
		public float height = GRAPH_SIZE;

		public void multiplyBy(float value) {
			width *= value;
			height *= value;
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
	}
}
