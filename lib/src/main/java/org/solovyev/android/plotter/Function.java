package org.solovyev.android.plotter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public abstract class Function implements SuperFunction {

    @Nullable
    private final String name;

    protected Function() {
        this.name = null;
    }

    protected Function(@Nullable String name) {
        this.name = name;
    }

    @Nullable
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name != null ? name : super.toString();
    }

    @NonNull
    @Override
    public Function copy() {
        return this;
    }
}
