package org.solovyev.android.plotter.meshes;


import android.content.Context;
import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;
import org.solovyev.android.plotter.Color;
import org.solovyev.android.plotter.Plot;

public class MeshSpec {
    public static final int MAX_WIDTH = 20;
    public static final int MIN_WIDTH = 1;
    public static final int DEFAULT_POINTS_COUNT = -1;
    @NonNull
    public static final Color COLOR_NO = Color.TRANSPARENT;
    @NonNull
    public static final Color COLOR_DEFAULT = Color.WHITE;
    private static final String JSON_COLOR = "c";
    private static final String JSON_WIDTH = "w";
    private static final String JSON_POINTS_COUNT = "pc";
    @NonNull
    public Color color;
    public int width;
    public int pointsCount = DEFAULT_POINTS_COUNT;

    private MeshSpec(@NonNull JSONObject json) {
        this.color = Color.create(json.optInt(JSON_COLOR, Color.WHITE.toInt()));
        this.width = json.optInt(JSON_WIDTH, MIN_WIDTH);
        this.pointsCount = json.optInt(JSON_POINTS_COUNT, DEFAULT_POINTS_COUNT);
    }

    private MeshSpec(@NonNull Color color, int width, int pointsCount) {
        this.color = color;
        this.width = Math.max(MIN_WIDTH, Math.min(MAX_WIDTH, width));
        this.pointsCount = pointsCount;
    }

    @NonNull
    public static MeshSpec create(@NonNull Color color, int width, int pointsCount) {
        return new MeshSpec(color, width, pointsCount);
    }

    @NonNull
    public static MeshSpec create(@NonNull Color color, int width) {
        return create(color, width, DEFAULT_POINTS_COUNT);
    }

    @NonNull
    public static MeshSpec createDefault(@NonNull Context context) {
        return new MeshSpec(COLOR_DEFAULT, defaultWidth(context), DEFAULT_POINTS_COUNT);
    }

    public static int defaultWidth(@NonNull Context context) {
        return (int) Plot.dpsToPxs(context, 1.5f);
    }

    @NonNull
    public static MeshSpec create(@NonNull JSONObject json) {
        return new MeshSpec(json);
    }

    @NonNull
    public MeshSpec copy() {
        return new MeshSpec(color, width, pointsCount);
    }

    public void applyTo(@NonNull FunctionGraph mesh) {
        mesh.setColor(color);
        mesh.setWidth(width);
        mesh.setPointsCount(pointsCount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MeshSpec)) return false;

        final MeshSpec that = (MeshSpec) o;

        if (width != that.width) return false;
        if (pointsCount != that.pointsCount) return false;
        if (!color.equals(that.color)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = color.hashCode();
        result = 31 * result + width;
        result = 31 * result + pointsCount;
        return result;
    }

    @NonNull
    public JSONObject toJson() throws JSONException {
        final JSONObject json = new JSONObject();
        json.put(JSON_COLOR, color.toInt());
        json.put(JSON_WIDTH, width);
        json.put(JSON_POINTS_COUNT, pointsCount);
        return json;
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
        public static final Color WHITE = Color.WHITE;

        @NonNull
        private static Color[] colors = new Color[]{RED, PINK, PURPLE, DEEP_PURPLE, INDIGO, BLUE, LIGHT_BLUE, CYAN, TEAL, GREEN, LIGHT_GREEN, LIME, YELLOW, AMBER, ORANGE, DEEP_ORANGE, BROWN, GREY, BLUE_GREY, WHITE};
        private static int[] intColors = new int[colors.length];

        static {
            for (int i = 0; i < colors.length; i++) {
                intColors[i] = colors[i].toInt();
            }
        }

        @NonNull
        public static Color[] asArray() {
            return colors;
        }

        @NonNull
        public static int[] asIntArray() {
            return intColors;
        }
    }

}
