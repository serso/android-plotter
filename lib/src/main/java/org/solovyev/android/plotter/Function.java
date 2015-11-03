package org.solovyev.android.plotter;

import javax.annotation.Nullable;

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
}
