package org.solovyev.android.plotter.arrays;

import android.support.annotation.NonNull;

public final class FloatArray extends BaseArray {
    @NonNull
    public float[] array;

    public FloatArray(int capacity) {
        this.array = new float[capacity];
    }

    public final void append(@NonNull FloatArray that) {
        System.arraycopy(that.array, 0, array, size, that.size);
        size += that.size;
    }

    protected int arrayLength() {
        return this.array.length;
    }

    protected void reallocate(int capacity) {
        final float[] newArray = new float[capacity];
        System.arraycopy(array, 0, newArray, 0, this.size);
        this.array = newArray;
    }
}
