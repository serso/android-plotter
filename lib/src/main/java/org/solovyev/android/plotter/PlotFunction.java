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

public final class PlotFunction {

	@Nonnull
	public Function function;

	@Nonnull
	public LineStyle lineStyle;

	public boolean visible = true;

	private PlotFunction(@Nonnull Function function) {
		this.function = function;
		this.lineStyle = LineStyle.create();
	}

	private PlotFunction(@Nonnull Function function,
						 @Nonnull LineStyle lineStyle) {
		this.function = function;
		this.lineStyle = lineStyle;
	}

	@Nonnull
	public static PlotFunction create(@Nonnull Function function) {
		return new PlotFunction(function);
	}

	@Nonnull
	public static PlotFunction create(@Nonnull Function function,
									  @Nonnull LineStyle lineStyle) {
		return new PlotFunction(function, lineStyle);
	}

	@Nonnull
	public PlotFunction copy() {
		final PlotFunction copy = create(this.function, this.lineStyle.copy());

		copy.visible = this.visible;

		return copy;
	}
}
