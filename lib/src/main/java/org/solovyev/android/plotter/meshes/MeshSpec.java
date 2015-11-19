package org.solovyev.android.plotter.meshes;


import android.content.Context;
import android.support.annotation.NonNull;

import org.solovyev.android.plotter.Color;
import org.solovyev.android.plotter.Plot;

public class MeshSpec {
    @NonNull
    public static final Color COLOR_NO = Color.TRANSPARENT;
    @NonNull
    public static final Color COLOR_DEFAULT = Color.WHITE;
    @NonNull
    public Color color;
    public int width;
    private MeshSpec(@NonNull Color color, int width) {
        this.color = color;
        this.width = width;
    }

    @NonNull
    public static MeshSpec create(@NonNull Color color, int width) {
        return new MeshSpec(color, width);
    }

    @NonNull
    public static MeshSpec createDefault(@NonNull Context context) {
        return new MeshSpec(COLOR_DEFAULT, defaultWidth(context));
    }

    public static int defaultWidth(@NonNull Context context) {
        return (int) Plot.dpsToPxs(context, 1.5f);
    }

    @NonNull
    public MeshSpec copy() {
        return this;
    }

    public void applyTo(@NonNull DimensionsAware mesh) {
        mesh.setColor(color);
        mesh.setWidth(width);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final MeshSpec that = (MeshSpec) o;

        if (width != that.width) return false;
        return color.equals(that.color);

    }

    @Override
    public int hashCode() {
        int result = color.hashCode();
        result = 31 * result + width;
        return result;
    }

    public static final class LightColors {
        public static final Color RED = Color.create(0xFFE57373);
        public static final Color PINK = Color.create(0xFFF06292);
        public static final Color PURPLE = Color.create(0xFFBA68C8);
        public static final Color DEEP_PURPLE = Color.create(0xFF9575CD);
        public static final Color INDIGO = Color.create(0xFF7986CB);
        public static final Color BLUE = Color.create(0xFF64B5F6);
        public static final Color LIGHT_BLUE = Color.create(0xFF4FC3F7);
        public static final Color CYAN = Color.create(0xFF4DD0E1);
        public static final Color TEAL = Color.create(0xFF4DB6AC);
        public static final Color GREEN = Color.create(0xFF81C784);
        public static final Color LIGHT_GREEN = Color.create(0xFFAED581);
        public static final Color LIME = Color.create(0xFFDCE775);
        public static final Color YELLOW = Color.create(0xFFFFF176);
        public static final Color AMBER = Color.create(0xFFFFD54F);
        public static final Color ORANGE = Color.create(0xFFFFB74D);
        public static final Color DEEP_ORANGE = Color.create(0xFFFF8A65);
        public static final Color BROWN = Color.create(0xFFA1887F);
        public static final Color GREY = Color.create(0xFFE0E0E0);
        public static final Color BLUE_GREY = Color.create(0xFF90A4AE);
    }
}
