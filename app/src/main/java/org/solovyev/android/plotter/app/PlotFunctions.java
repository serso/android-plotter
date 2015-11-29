package org.solovyev.android.plotter.app;

import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.solovyev.android.plotter.PlotFunction;
import org.solovyev.android.plotter.math.ExpressionFunction;
import org.solovyev.android.plotter.meshes.MeshSpec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PlotFunctions {
    private static final String JSON_FUNCTION = "f";
    private static final String JSON_CONFIGURATION = "c";
    private static final String JSON_VISIBLE = "v";

    @NonNull
    private final List<PlotFunction> list;

    public PlotFunctions(@NonNull List<PlotFunction> list) {
        this.list = list;
    }

    public PlotFunctions(@NonNull JSONArray jsonArray) {
        this.list = new ArrayList<>(jsonArray.length());
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                final JSONObject json = jsonArray.getJSONObject(i);
                final ExpressionFunction function = ExpressionFunction.create(json.getJSONObject(JSON_FUNCTION));
                final MeshSpec meshSpec = MeshSpec.create(json.getJSONObject(JSON_CONFIGURATION));
                final PlotFunction plotFunction = PlotFunction.create(function, meshSpec);
                plotFunction.visible = json.optBoolean(JSON_VISIBLE, true);
                this.list.add(plotFunction);
            } catch (JSONException e) {
                Log.e("PlotFunctions", e.getMessage(), e);
            }
        }
    }

    @NonNull
    public JSONArray toJson() throws JSONException {
        final JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < list.size(); i++) {
            final PlotFunction plotFunction = list.get(i);
            if (!(plotFunction.function instanceof ExpressionFunction)) {
                continue;
            }
            final ExpressionFunction function = (ExpressionFunction) plotFunction.function;
            final JSONObject json = new JSONObject();
            json.put(JSON_FUNCTION, function.toJson());
            json.put(JSON_CONFIGURATION, plotFunction.meshSpec.toJson());
            json.put(JSON_VISIBLE, plotFunction.visible);
            jsonArray.put(i, json);
        }
        return jsonArray;
    }

    @NonNull
    public List<PlotFunction> asList() {
        return Collections.unmodifiableList(list);
    }
}
