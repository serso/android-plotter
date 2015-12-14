package org.solovyev.android.drag;

import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

public class Maths {

    public static final float MIN_AMOUNT = 0.05f;

    /**
     * @param nominal nominal
     * @param value   value
     * @return nearest value to specified value that can be divided by nominal value without remainder
     */
    public static double getRoundedAmount(double nominal, double value) {
        double result;
        int numberOfTimes = (int) (value / nominal);
        result = numberOfTimes * nominal;
        return result;
    }

    /**
     * @param l    first number
     * @param sign sign
     * @param r    second number
     * @return sum or difference of two numbers (supposed: null = 0)
     */
    public static double sumUp(Double l, int sign, Double r) {
        double result = 0d;
        if (l != null && r != null) {
            result = l + sign * r;
        } else if (l != null) {
            result = l;
        } else if (r != null) {
            result = sign * r;
        }
        return result;
    }

    /**
     * @param l first number
     * @param r second number
     * @return sum of tow numbers (supposed: null = 0)
     */
    public static double sumUp(Double l, Double r) {
        return sumUp(l, 1, r);
    }

    /**
     * @param l fist number
     * @param r second number
     * @return difference of two numbers (supposed: null = 0)
     */
    public static double subtract(Double l, Double r) {
        return sumUp(l, -1, r);
    }

    /**
     * Method compares two double values with specified precision
     *
     * @param d1        first value to compare
     * @param d2        second value for compare
     * @param precision number of digits after dot
     * @return 'true' if values are equal with specified precision
     */
    public static boolean equals(double d1, double d2, int precision) {
        return Math.abs(d1 - d2) < getMaxPreciseAmount(precision);
    }

    /**
     * Method tests if first value is less than second with specified precision
     *
     * @param d1        first value to compare
     * @param d2        second value for compare
     * @param precision number of digits after dot
     * @return 'true' if first value is less than second with specified precision
     */
    public static boolean less(double d1, double d2, int precision) {
        return d1 < d2 - getMaxPreciseAmount(precision);
    }

    /**
     * Method tests if first value is more than second with specified precision
     *
     * @param d1        first value to compare
     * @param d2        second value for compare
     * @param precision number of digits after dot
     * @return 'true' if first value is more than second with specified precision
     */
    public static boolean more(double d1, double d2, int precision) {
        return d1 > d2 + getMaxPreciseAmount(precision);
    }

    private static double getMaxPreciseAmount(int precision) {
        return Math.pow(0.1d, precision) / 2;
    }

    public static double getNotNull(@Nullable Double value) {
        return value != null ? value : 0d;
    }

    public static double round(@NonNull Double value, int precision) {
        double factor = Math.pow(10, precision);
        return ((double) Math.round(value * factor)) / factor;
    }

    public static float getDistance(@NonNull PointF startPoint,
                                    @NonNull PointF endPoint) {
        return getNorm(subtract(endPoint, startPoint));
    }

    public static PointF subtract(@NonNull PointF p1, @NonNull PointF p2) {
        return new PointF(p1.x - p2.x, p1.y - p2.y);
    }

    public static PointF sum(@NonNull PointF p1, @NonNull PointF p2) {
        return new PointF(p1.x + p2.x, p1.y + p2.y);
    }

    public static float getNorm(@NonNull PointF point) {
        return (float) Math.pow(Math.pow(point.x, 2) + Math.pow(point.y, 2), 0.5);
    }

    public static float getAngle(@NonNull PointF startPoint,
                                 @NonNull PointF axisEndPoint,
                                 @NonNull PointF endPoint,
                                 @Nullable MutableObject<Boolean> left) {
        final PointF axisVector = subtract(axisEndPoint, startPoint);
        final PointF vector = subtract(endPoint, startPoint);

        double a_2 = Math.pow(getDistance(vector, axisVector), 2);
        double b = getNorm(vector);
        double b_2 = Math.pow(b, 2);
        double c = getNorm(axisVector);
        double c_2 = Math.pow(c, 2);

        if (left != null) {
            left.setObject(axisVector.x * vector.y - axisVector.y * vector.x < 0);
        }

        return (float) Math.acos((-a_2 + b_2 + c_2) / (2 * b * c));
    }

    public static double countMean(@NonNull List<Double> objects) {

        double sum = 0d;
        for (Double object : objects) {
            sum += object;
        }

        return objects.size() == 0 ? 0d : (sum / objects.size());
    }

    public static double countStandardDeviation(@NonNull Double mean, @NonNull List<Double> objects) {
        double sum = 0d;

        for (Double object : objects) {
            sum += Math.pow(object - mean, 2);
        }

        return objects.size() == 0 ? 0d : Math.sqrt(sum / objects.size());
    }

    public static StatData getStatData(@NonNull List<Double> objects) {

        final double mean = countMean(objects);
        final double standardDeviation = countStandardDeviation(mean, objects);

        return new StatData(mean, standardDeviation);
    }

    private static <T extends Comparable<T>> boolean earlier(@Nullable T t1,
                                                             boolean isNegativeInf1,
                                                             @Nullable T t2,
                                                             boolean isNegativeInf2) {
        boolean result;

        if (t1 == null && t2 == null && (isNegativeInf1 == isNegativeInf2)) {
            // -inf and -inf or +inf and +inf
            result = false;
        } else if (t1 == null) {
            // anything bigger then -inf if left
            result = isNegativeInf1;
        } else if (t2 == null) {
            // anything lower then +inf if right
            result = !isNegativeInf2;
        } else {
            result = t1.compareTo(t2) < 0;
        }

        return result;
    }

    static int pow(int value, int exponent) {
        if (exponent == 0) {
            return 1;
        }

        if (exponent == 1) {
            return value;
        }

        if (exponent % 2 == 0) {
            return pow(value * value, exponent / 2);
        } else {
            return value * pow(value * value, (exponent - 1) / 2);
        }
    }

    public static enum ComparisonType {
        min,
        max
    }

    public static class StatData {

        private final double mean;

        private final double standardDeviation;

        public StatData(double mean, double standardDeviation) {
            this.mean = mean;
            this.standardDeviation = standardDeviation;
        }

        public double getMean() {
            return mean;
        }

        public double getStandardDeviation() {
            return standardDeviation;
        }

    }
}

