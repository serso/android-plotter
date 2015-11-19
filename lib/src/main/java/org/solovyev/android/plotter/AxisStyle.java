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

import android.support.annotation.NonNull;

public final class AxisStyle {

    public int axisColor = 0xFFEEEEEE;
    public int axisLabelsColor = 0xFFEEEEEE;
    public int gridColor = 0x99424242;
    public int backgroundColor = 0xFF212121;

    private AxisStyle() {
    }

    @NonNull
    public static AxisStyle create() {
        return new AxisStyle();
    }

    @NonNull
    public AxisStyle copy() {
        final AxisStyle copy = new AxisStyle();
        copy.axisColor = axisColor;
        copy.axisLabelsColor = axisLabelsColor;
        copy.gridColor = gridColor;
        copy.backgroundColor = backgroundColor;
        return copy;
    }
}
