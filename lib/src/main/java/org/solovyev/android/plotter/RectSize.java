package org.solovyev.android.plotter;

import android.support.annotation.NonNull;

public final class RectSize {
    public int width;
    public int height;

    private RectSize() {
    }

    private RectSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @NonNull
    public static RectSize empty() {
        return new RectSize();
    }

    @NonNull
    public static RectSize create(int width, int height) {
        return new RectSize(width, height);
    }

    public void setEmpty() {
        width = 0;
        height = 0;
    }

    public void multiplyBy(float value) {
        width *= value;
        height *= value;
    }

    @Override
    public String toString() {
        return "Size" + stringSize();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final RectSize that = (RectSize) o;

        if (width != that.width) return false;
        return height == that.height;

    }

    @Override
    public int hashCode() {
        int result = width;
        result = 31 * result + height;
        return result;
    }

    public void set(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void set(@NonNull RectSize that) {
        set(that.width, that.height);
    }

    @NonNull
    public String stringSize() {
        return "[w=" + width + ", h=" + height + "]";
    }

    public boolean isEmpty() {
        return width == 0 || height == 0;
    }

    float aspectRatio() {
        if (isEmpty()) {
            return 0;
        }
        return (float) width / (float) height;
    }
}
