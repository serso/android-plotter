package org.solovyev.android.plotter.app;

import android.graphics.RectF;
import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PlotterState {
    private static final String JSON_3D = "3d";
    private static final String JSON_BOUNDS = "b";

    private final boolean d3;
    @NonNull
    private final RectF bounds;

    public PlotterState(boolean d3, @NonNull RectF bounds) {
        this.d3 = d3;
        this.bounds = bounds;
    }

    public PlotterState(@NonNull JSONObject json) throws JSONException {
        d3 = json.getBoolean(JSON_3D);
        final JSONArray jsonArray = json.getJSONArray(JSON_BOUNDS);
        bounds = new RectF(getFloat(jsonArray, 0), getFloat(jsonArray, 1), getFloat(jsonArray, 2), getFloat(jsonArray, 3));
    }

    private float getFloat(@NonNull JSONArray array, int index) throws JSONException {
        return (float) array.getDouble(index);
    }

    @NonNull
    public JSONObject toJson() throws JSONException {
        final JSONObject json = new JSONObject();
        json.put(JSON_3D, d3);
        final JSONArray jsonBounds = new JSONArray();
        jsonBounds.put(0, bounds.left);
        jsonBounds.put(1, bounds.top);
        jsonBounds.put(2, bounds.right);
        jsonBounds.put(3, bounds.bottom);
        json.put(JSON_BOUNDS, jsonBounds);
        return json;
    }

    public boolean is3d() {
        return d3;
    }

    @NonNull
    public RectF getBounds() {
        return bounds;
    }
}
