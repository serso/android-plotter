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
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

public final class PlotData {

    @NonNull
    public final List<PlotFunction> functions = new ArrayList<PlotFunction>();
    @NonNull
    public AxisStyle axisStyle = AxisStyle.create();

    private PlotData() {
    }

    @NonNull
    public static PlotData create() {
        Check.isMainThread();
        return new PlotData();
    }

    @NonNull
    public PlotData copy() {
        Check.isMainThread();
        final PlotData copy = create();

        copy.axisStyle = axisStyle.copy();
        for (PlotFunction function : functions) {
            copy.functions.add(function.copy());
        }

        return copy;
    }

    public void add(@NonNull PlotFunction function) {
        Check.isMainThread();
        if (!update(function)) {
            functions.add(function);
        }
    }

    boolean update(@NonNull PlotFunction function) {
        Check.isMainThread();
        for (int i = 0; i < functions.size(); i++) {
            final PlotFunction oldFunction = functions.get(i);
            if (oldFunction.function == function.function) {
                functions.set(i, function);
                return true;
            }
        }
        return false;
    }

    @Nullable
    public PlotFunction get(@NonNull String name) {
        for (PlotFunction function : functions) {
            if (TextUtils.equals(function.function.getName(), name)) {
                return function;
            }
        }
        return null;
    }
}
