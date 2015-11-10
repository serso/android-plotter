package org.solovyev.android.drag;


import android.support.annotation.NonNull;

public final class Intervals {

    private Intervals() {
        throw new AssertionError();
    }

    @NonNull
    public static <T extends Comparable<T>> Interval<T> newPoint(@NonNull T point) {
        return newInstance(IntervalLimit.create(point, true), IntervalLimit.create(point, true));
    }

    @NonNull
    public static <T extends Comparable<T>> Interval<T> newInterval(@NonNull T left, boolean leftClosed, @NonNull T right, boolean rightClosed) {
        return newInstance(newLimit(left, leftClosed), newLimit(right, rightClosed));
    }

    @NonNull
    public static <T extends Comparable<T>> Interval<T> newClosedInterval(@NonNull T left, @NonNull T right) {
        return newInstance(newClosedLimit(left), newClosedLimit(right));
    }

    @NonNull
    public static <T extends Comparable<T>> IntervalLimit<T> newClosedLimit(@NonNull T value) {
        return newLimit(value, true);
    }

    @NonNull
    public static <T extends Comparable<T>> IntervalLimit<T> newLimit(@NonNull T value, boolean closed) {
        return IntervalLimit.create(value, closed);
    }

    @NonNull
    public static <T extends Comparable<T>> IntervalLimit<T> newOpenedLimit(@NonNull T value) {
        return newLimit(value, false);
    }

    @NonNull
    public static <T extends Comparable<T>> Interval<T> newInstance(@NonNull IntervalLimit<T> left,
                                                                    @NonNull IntervalLimit<T> right) {
        return Interval.create(left, right);
    }

    @NonNull
    public static <T extends Comparable<T>> IntervalLimit<T> newLowestLimit() {
        return IntervalLimit.newLowest();
    }

    @NonNull
    public static <T extends Comparable<T>> IntervalLimit<T> newHighestLimit() {
        return IntervalLimit.newHighest();
    }
}
