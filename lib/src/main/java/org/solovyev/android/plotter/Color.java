package org.solovyev.android.plotter;

import javax.annotation.Nonnull;

public final class Color {
    public static final Color BLACK = new Color(0xFF000000);
    public static final Color DKGRAY = new Color(0xFF444444);
    public static final Color GRAY = new Color(0xFF888888);
    public static final Color LTGRAY = new Color(0xFFCCCCCC);
    public static final Color WHITE = new Color(0xFFFFFFFF);
    public static final Color RED = new Color(0xFFFF0000);
    public static final Color GREEN = new Color(0xFF00FF00);
    public static final Color BLUE = new Color(0xFF0000FF);
    public static final Color YELLOW = new Color(0xFFFFFF00);
    public static final Color CYAN = new Color(0xFF00FFFF);
    public static final Color MAGENTA = new Color(0xFFFF00FF);
    public static final Color TRANSPARENT = new Color(0);
    public static final int COMPONENTS = 4;

    public final float red;
    public final float green;
    public final float blue;
    public final float alpha;

    private Color(float red, float green, float blue, float alpha) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
    }

    private Color(int color) {
        this.red = red(color);
        this.green = green(color);
        this.blue = blue(color);
        this.alpha = alpha(color);
    }

    @Nonnull
    public static Color create(int color) {
        return new Color(color);
    }

    @Nonnull
    public static Color create(float red, float green, float blue, float alpha) {
        return new Color(red, green, blue, alpha);
    }

    static float red(int color) {
        return android.graphics.Color.red(color) / 255f;
    }

    static float green(int color) {
        return android.graphics.Color.green(color) / 255f;
    }

    static float blue(int color) {
        return android.graphics.Color.blue(color) / 255f;
    }

    static float alpha(int color) {
        return android.graphics.Color.alpha(color) / 255f;
    }

    private static int red(float red) {
        return ((int) (red * 255f)) << 16;
    }

    private static int green(float green) {
        return ((int) (green * 255f)) << 8;
    }

    private static int blue(float blue) {
        return ((int) (blue * 255f));
    }

    private static int alpha(float alpha) {
        return ((int) (alpha * 255f)) << 24;
    }

    public static void fillVertex(@Nonnull float[] colors, int vertex, @Nonnull Color color) {
        fill(colors, COMPONENTS * vertex, color);
    }

    public static void fill(float[] colors, int index, @Nonnull Color color) {
        colors[index] = color.red;
        colors[index + 1] = color.green;
        colors[index + 2] = color.blue;
        colors[index + 3] = color.alpha;
    }

    public static void fillVertex(@Nonnull float[] colors, int vertex, int color) {
        fill(colors, COMPONENTS * vertex, color);
    }

    public static void fill(float[] colors, int index, int color) {
        colors[index] = red(color);
        colors[index + 1] = green(color);
        colors[index + 2] = blue(color);
        colors[index + 3] = alpha(color);
    }

    public static void fillVertex(float[] colors, int vertex, float red, float green, float blue, float alpha) {
        fill(colors, COMPONENTS * vertex, red, green, blue, alpha);
    }

    public static void fill(float[] colors, int index, float red, float green, float blue, float alpha) {
        colors[index] = red;
        colors[index + 1] = green;
        colors[index + 2] = blue;
        colors[index + 3] = alpha;
    }

    @Nonnull
    public Color transparentCopy(float alpha) {
        return new Color(red, green, blue, alpha);
    }

    public int toInt() {
        return red(red) | green(green) | blue(blue) | alpha(alpha);
    }

    @Nonnull
    public Color add(float value) {
        return add(value, value, value);
    }

    @Nonnull
    public Color add(float red, float green, float blue) {
        return Color.create(add(this.red, red), add(this.green, green), add(this.blue, blue), alpha);
    }

    private float add(float color, float value) {
        final float result = color + value;
        if (result > 1f) return 1f;
        if (result < 0f) return 0f;
        else return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Color color = (Color) o;

        if (Float.compare(color.alpha, alpha) != 0) return false;
        if (Float.compare(color.blue, blue) != 0) return false;
        if (Float.compare(color.green, green) != 0) return false;
        if (Float.compare(color.red, red) != 0) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (red != +0.0f ? Float.floatToIntBits(red) : 0);
        result = 31 * result + (green != +0.0f ? Float.floatToIntBits(green) : 0);
        result = 31 * result + (blue != +0.0f ? Float.floatToIntBits(blue) : 0);
        result = 31 * result + (alpha != +0.0f ? Float.floatToIntBits(alpha) : 0);
        return result;
    }
}
