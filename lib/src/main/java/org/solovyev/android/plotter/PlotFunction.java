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

import android.content.Context;
import android.support.annotation.NonNull;

import org.solovyev.android.plotter.meshes.MeshSpec;

public final class PlotFunction {

    @NonNull
    public Function function;

    @NonNull
    public MeshSpec meshSpec;

    public boolean visible = true;

    private PlotFunction(@NonNull Function function,
                         @NonNull MeshSpec meshSpec) {
        this.function = function;
        this.meshSpec = meshSpec;
    }

    @NonNull
    public static PlotFunction create(@NonNull Function function, @NonNull Context context) {
        return new PlotFunction(function, MeshSpec.createDefault(context));
    }

    @NonNull
    public static PlotFunction create(@NonNull Function function,
                                      @NonNull MeshSpec meshSpec) {
        return new PlotFunction(function, meshSpec);
    }

    @NonNull
    public PlotFunction copy() {
        final PlotFunction copy = create(this.function.copy(), this.meshSpec.copy());

        copy.visible = this.visible;

        return copy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final PlotFunction that = (PlotFunction) o;

        if (!function.equals(that.function)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return function.hashCode();
    }
}
