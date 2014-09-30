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
	public final View view = new View();

	// current position of camera in graph coordinates
	@Nonnull
	public final Camera camera = new Camera();

	@Nonnull
	public final Graph graph = new Graph();

	@Nonnull
	PointF toGraphCoordinates(float xPxs, float yPxs) {
		return new PointF(scaleXPxs(xPxs) + getXMin(), (getGHeight() - scaleYPxs(yPxs)) + getYMin());
	}

	private float scaleXPxs(float pxs) {
		return pxs * getXGraphToViewScale();
	}

	private float scaleYPxs(float pxs) {
		return pxs * getYGraphToViewScale();
	}

	// X

	public float getXMin() {
		return camera.x - graph.width / 2;
	}

	float getXMax(float minX) {
		return minX + graph.width;
	}

	public float getXMax() {
		return getXMax(getXMin());
	}

	// Y

	public float getYMin() {
		return camera.y - graph.height / 2;
	}

	public float getYMax() {
		return getYMax(getYMin());
	}

	public float getYMax(float yMin) {
		return yMin + graph.height;
	}

	float getXGraphToViewScale() {
		if (view.width != 0) {
			return graph.width / ((float) view.width);
		} else {
			return 0f;
		}
	}

	float getYGraphToViewScale() {
		if (view.height != 0) {
			return graph.height / ((float) view.height);
		} else {
			return 0f;
		}
	}

	private float getViewAspectRatio() {
		if (view.width != 0) {
			return ((float) view.height) / view.width;
		} else {
			return 0f;
		}
	}

	public float getGWidth() {
		return graph.width;
	}

	public float getGHeight() {
		return graph.height;
	}

	public void setXRange(float xMin, float xMax) {
		graph.width = xMax - xMin;
		camera.x = xMin + graph.width / 2;
	}

	public void setYRange(float yMin, float yMax) {
		setYRange0(yMin, yMax);
	}

	private void setYRange0(float yMin, float yMax) {
		graph.height = yMax - yMin;
		camera.y = yMin + graph.height / 2;
	}

	public void setRanges(float xMin, float xMax, float yMin, float yMax) {
		setXRange(xMin, xMax);
		setYRange(yMin, yMax);
	}

	public void setViewDimensions(@Nonnull android.view.View view) {
		this.view.width = view.getWidth();
		this.view.height = view.getHeight();
	}


	public void setGraphDimensions(float width, float height) {
		graph.width = width;
		graph.height = height;
	}

	public void setViewDimensions(int vWidthPxs, int vHeightPxs) {
		view.width = vWidthPxs;
		view.height = vHeightPxs;
	}

	@Nonnull
	public Dimensions copy() {
		return copy(new Dimensions());
	}

	@Nonnull
	public Dimensions copy(@Nonnull Dimensions that) {
		that.view.height = this.view.height;
		that.view.width = this.view.width;
		that.camera.x = this.camera.x;
		that.camera.y = this.camera.y;
		that.graph.width = this.graph.width;
		that.graph.height = this.graph.height;

		return that;
	}

	public static final class Camera {
		public float x = 0;
		public float y = 0;
	}

	public static final class Graph {
		public float width = 20;
		public float height = 20;
	}

	public static final class View {
		public int width;
		public int height;
	}
}
