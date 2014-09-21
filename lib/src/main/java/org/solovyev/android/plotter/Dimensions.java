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
import android.view.View;

import javax.annotation.Nonnull;

public final class Dimensions {

	public interface Listener {
		void onChanged();
	}

	//                    |<--------------gWidth-------------->|
	//                   xMin                                xMax
	// -------------------|------------------------------------|--------------------
	//                    |<-------------vWidthPxs------------>|
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
	private Listener listener;

	// view width and height in pixels
	private int vWidthPxs;
	private int vHeightPxs;

	// current position of camera in graph coordinates
	private float x0;
	private float y0;

	// graph width and height in function units (NOT screen pixels)
	private float gWidth = 20;
	private float gHeight = 20;

	public Dimensions(@Nonnull Listener listener) {
		this.listener = listener;
	}

	/*
	**********************************************************************
	*
	*                           METHODS
	*
	**********************************************************************
	*/

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
		return x0 - gWidth / 2;
	}

	float getXMax(float minX) {
		return minX + gWidth;
	}

	public float getXMax() {
		return getXMax(getXMin());
	}

	// Y

	public float getYMin() {
		return y0 - gHeight / 2;
	}

	public float getYMax() {
		return getYMax(getYMin());
	}

	public float getYMax(float yMin) {
		return yMin + gHeight;
	}

	float getXGraphToViewScale() {
		if (vWidthPxs != 0) {
			return gWidth / ((float) vWidthPxs);
		} else {
			return 0f;
		}
	}

	float getYGraphToViewScale() {
		if (vHeightPxs != 0) {
			return gHeight / ((float) vHeightPxs);
		} else {
			return 0f;
		}
	}

	private float getViewAspectRatio() {
		if (vWidthPxs != 0) {
			return ((float) vHeightPxs) / vWidthPxs;
		} else {
			return 0f;
		}
	}

	public int getVWidthPxs() {
		return vWidthPxs;
	}

	public int getVHeightPxs() {
		return vHeightPxs;
	}

	public float getX0() {
		return x0;
	}

	public float getY0() {
		return y0;
	}

	public float getGWidth() {
		return gWidth;
	}

	public float getGHeight() {
		return gHeight;
	}

	/*
	**********************************************************************
	*
	*                           SETTERS
	*
	**********************************************************************
	*/

	public void setXRange(float xMin, float xMax) {
		setXRange0(xMin, xMax);

		listener.onChanged();
	}

	private void setXRange0(float xMin, float xMax) {
		this.gWidth = xMax - xMin;
		this.x0 = xMin + gWidth / 2;
	}

	public void setYRange(float yMin, float yMax) {
		setYRange0(yMin, yMax);

		listener.onChanged();
	}

	private void setYRange0(float yMin, float yMax) {
		this.gHeight = yMax - yMin;
		this.y0 = yMin + gHeight / 2;
	}

	public void setRanges(float xMin, float xMax, float yMin, float yMax) {
		setXRange0(xMin, xMax);
		setYRange0(yMin, yMax);

		listener.onChanged();
	}

	public void setViewDimensions(@Nonnull View view) {
		this.vWidthPxs = view.getWidth();
		this.vHeightPxs = view.getHeight();

		listener.onChanged();
	}


	public void setGraphDimensions(float gWidth, float gHeight) {
		this.gWidth = gWidth;
		this.gHeight = gHeight;

		listener.onChanged();
	}

	public void setViewDimensions(int vWidthPxs, int vHeightPxs) {
		this.vWidthPxs = vWidthPxs;
		this.vHeightPxs = vHeightPxs;

		listener.onChanged();
	}

	void setXY(float x0, float y0) {
		this.x0 = x0;
		this.y0 = y0;
	}

	public void increaseXY(float dx, float dy) {
		this.x0 += dx;
		this.y0 += dy;
	}

	@Nonnull
	public Dimensions copy() {
		return copy(new Dimensions(listener));
	}

	@Nonnull
	public Dimensions copy(@Nonnull Dimensions that) {
		that.vWidthPxs = this.vWidthPxs;
		that.vHeightPxs = this.vHeightPxs;
		that.x0 = this.x0;
		that.y0 = this.y0;
		that.gWidth = this.gWidth;
		that.gHeight = this.gHeight;

		return that;
	}
}
