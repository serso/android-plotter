package org.solovyev.android.plotter.arrays;

import android.support.annotation.NonNull;

public final class ShortArray extends BaseArray {
    @NonNull
    public short[] array;

    public ShortArray(int capacity) {
        this.array = new short[capacity];
    }

    protected int arrayLength() {
        return this.array.length;
    }

    protected void reallocate(int capacity) {
        final short[] newArray = new short[capacity];
        System.arraycopy(array, 0, newArray, 0, this.size);
        this.array = newArray;
    }
}
