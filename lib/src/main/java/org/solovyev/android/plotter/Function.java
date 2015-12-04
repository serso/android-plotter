package org.solovyev.android.plotter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class Function implements SuperFunction {
    public static final int NO_ID = -1;

    @NonNull
    private static final AtomicInteger counter = new AtomicInteger(0);

    private final int id;

    @Nullable
    private final String name;

    protected Function() {
        this(null);
    }

    protected Function(@Nullable String name) {
        this.name = name;
        this.id = counter.getAndIncrement();
    }

    @Nullable
    public String getName() {
        return name;
    }

    public boolean hasName() {
        return !TextUtils.isEmpty(name);
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

    @Override
    public int getId() {
        return id;
    }
}
