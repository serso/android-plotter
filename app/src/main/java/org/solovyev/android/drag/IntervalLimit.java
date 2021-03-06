package org.solovyev.android.drag;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

class IntervalLimit<T extends Comparable<T>> {

    public static final Integer BOTH_NULLS_CONST = 0;
    @NonNull
    private final Type type;
    @Nullable
    private T value;
    private boolean closed;

    private IntervalLimit(@NonNull Type type) {
        this.type = type;
    }

    @NonNull
    public static <T extends Comparable<T>> IntervalLimit<T> create(@NonNull T value, boolean closed) {
        final IntervalLimit<T> result = new IntervalLimit<>(Type.between);

        result.value = value;
        result.closed = closed;

        return result;
    }

    @NonNull
    public static <T extends Comparable<T>> IntervalLimit<T> newLowest() {
        return create(Type.lowest);
    }

    @NonNull
    public static <T extends Comparable<T>> IntervalLimit<T> newHighest() {
        return create(Type.highest);
    }

    @NonNull
    private static <T extends Comparable<T>> IntervalLimit<T> create(@NonNull Type type) {
        final IntervalLimit<T> result = new IntervalLimit<>(type);

        result.value = null;
        result.closed = false;

        return result;
    }

    public static int compare(Object value1, Object value2) {
        Integer result = compareOnNullness(value1, value2);

        if (result == null) {
            if (value1 instanceof Comparable && value2 instanceof Comparable) {
                //noinspection unchecked
                result = ((Comparable) value1).compareTo(value2);
            } else {
                result = 0;
            }
        }
        return result;
    }

    @Nullable
    public static Integer compareOnNullness(Object o1, Object o2) {
        Integer result;

        if (o1 == null && o2 == null) {
            result = BOTH_NULLS_CONST;
        } else if (o1 == null) {
            result = -1;
        } else if (o2 == null) {
            result = 1;
        } else {
            //both not nulls
            result = null;
        }

        return result;
    }

    @Nullable
    public T getValue() {
        return this.value;
    }

    public boolean isClosed() {
        return this.closed;
    }

    public boolean isOpened() {
        return !this.closed;
    }

    public boolean isLowest() {
        return this.type == Type.lowest;
    }

    public boolean isHighest() {
        return this.type == Type.highest;
    }

    public boolean isLowerOrEqualsThan(@NonNull T that) {
        if (this.isLowest()) {
            return true;
        } else if (this.isHighest()) {
            return false;
        } else {
            if (this.isClosed()) {
                assert this.value != null;
                return this.value.compareTo(that) <= 0;
            } else {
                assert this.value != null;
                return this.value.compareTo(that) < 0;
            }
        }
    }

    public boolean isLowerOrEqualsThan(@NonNull IntervalLimit<T> that) {
        if (this.isLowest()) {
            return that.isLowest();
        } else if (this.isHighest()) {
            return that.isHighest();
        } else {
            if (this.isClosed()) {
                return this.compareTo(that) <= 0;
            } else {
                return this.compareTo(that) < 0;
            }
        }
    }

    public boolean isHigherOrEqualsThan(@NonNull T that) {
        if (this.isHighest()) {
            return true;
        } else if (this.isLowest()) {
            return false;
        } else {
            assert this.value != null;
            if (this.isClosed()) {
                return this.value.compareTo(that) >= 0;
            } else {
                return this.value.compareTo(that) > 0;
            }
        }
    }

    public boolean isHigherOrEqualsThan(@NonNull IntervalLimit<T> that) {
        if (this.isHighest()) {
            return that.isHighest();
        } else if (this.isLowest()) {
            return that.isLowest();
        } else {
            if (this.isClosed()) {
                return this.compareTo(that) >= 0;
            } else {
                return this.compareTo(that) > 0;
            }
        }
    }

    public int compareTo(@NonNull IntervalLimit<T> that) {
        if (this == that) {
            return 0;
        }

        if (this.isLowest()) {
            if (that.isLowest()) {
                return 0;
            } else {
                return -1;
            }
        } else if (this.isHighest()) {
            if (that.isHighest()) {
                return 0;
            } else {
                return 1;
            }
        }

        return compare(this.value, that.getValue());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof IntervalLimit)) {
            return false;
        }

        IntervalLimit that = (IntervalLimit) o;

        if (closed != that.closed) {
            return false;
        }
        if (type != that.type) {
            return false;
        }
        if (value != null ? !value.equals(that.value) : that.value != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = value != null ? value.hashCode() : 0;
        result = 31 * result + (closed ? 1 : 0);
        result = 31 * result + type.hashCode();
        return result;
    }

    @Override
    public String toString() {
        if (this.isLowest()) {
            return "-Inf";
        } else if (this.isHighest()) {
            return "Inf";
        } else {
            return String.valueOf(this.value);
        }
    }

    public enum Type {
        lowest,
        between,
        highest
    }
}
