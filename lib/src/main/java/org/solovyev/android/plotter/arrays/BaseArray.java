package org.solovyev.android.plotter.arrays;

import org.solovyev.android.plotter.Check;

abstract class BaseArray {
    public int size;

    public final void allocate(int capacity) {
        if (capacity == this.size) {
            return;
        }
        if (capacity < this.size) {
            truncate(capacity);
            return;
        }
        if (capacity <= arrayLength()) {
            // do nothing as array is big enough and size is smaller than capacity
            return;
        }
        reallocate(capacity);
    }

    protected abstract void reallocate(int capacity);

    protected abstract int arrayLength();

    public final void truncate(int size) {
        Check.isTrue(size <= this.size);
        this.size = size;
    }
}
