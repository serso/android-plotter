package org.solovyev.android.plotter.app;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.solovyev.android.io.FileLoader;
import org.solovyev.android.io.FileSaver;
import org.solovyev.android.plotter.PlotData;
import org.solovyev.android.plotter.PlotFunction;
import org.solovyev.android.plotter.Plotter;
import org.solovyev.android.plotter.math.ExpressionFunction;
import org.solovyev.android.plotter.meshes.MeshSpec;

import java.io.File;
import java.util.List;

public class PlotterApplication extends Application {

    public static final String PREFS_VERSION_CODE = "version.code";
    public static final String PREFS_VERSION_NAME = "version.name";

    @NonNull
    private static File getFunctionsFile(@NonNull Context context) {
        return new File(context.getFilesDir(), "functions");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        App.create(this);
        final boolean newInstall = checkAppVersion();

        final Plotter plotter = App.getPlotter();
        plotter.addListener(new PlotterListener(plotter));

        if (newInstall) {
            final int meshWidth = MeshSpec.defaultWidth(this);
            plotter.add(PlotFunction.create(ExpressionFunction.create("-x", "x"), MeshSpec.create(MeshSpec.LightColors.INDIGO, meshWidth)));
            plotter.add(PlotFunction.create(ExpressionFunction.create("tan(x)", "x"), MeshSpec.create(MeshSpec.LightColors.AMBER, meshWidth)));
            plotter.add(PlotFunction.create(ExpressionFunction.create("sin(1/x)", "x"), MeshSpec.create(MeshSpec.LightColors.PURPLE, meshWidth)));
            plotter.add(PlotFunction.create(ExpressionFunction.create("1/(x + 1)/(x - 1)^6", "x"), MeshSpec.create(MeshSpec.LightColors.PINK, meshWidth)));
            plotter.add(PlotFunction.create(ExpressionFunction.create("sin(x) + sin(y)", "x", "y"), MeshSpec.create(MeshSpec.LightColors.GREEN, meshWidth)));
            plotter.add(PlotFunction.create(ExpressionFunction.create("x * x + y * y", "x", "y"), MeshSpec.create(MeshSpec.LightColors.RED, meshWidth)));
        }

        App.getBackground().execute(new FunctionsLoader(plotter));
    }

    private boolean checkAppVersion() {
        final SharedPreferences preferences = App.getPreferences();
        final int oldVersionCode = preferences.getInt(PREFS_VERSION_CODE, 0);
        final int newVersionCode = BuildConfig.VERSION_CODE;
        final String oldVersionName = preferences.getString(PREFS_VERSION_NAME, "");
        final String newVersionName = BuildConfig.VERSION_NAME;

        final boolean newInstall = oldVersionCode == 0;
        if (oldVersionCode != newVersionCode || !TextUtils.equals(oldVersionName, newVersionName)) {
            final SharedPreferences.Editor editor = preferences.edit();
            editor.putInt(PREFS_VERSION_CODE, newVersionCode);
            editor.putString(PREFS_VERSION_NAME, newVersionName);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                editor.apply();
            } else {
                editor.commit();
            }
        }
        return newInstall;
    }

    private static class PlotterListener implements Plotter.Listener, Runnable {
        @NonNull
        private final Plotter plotter;

        public PlotterListener(@NonNull Plotter plotter) {
            this.plotter = plotter;
        }

        @Override
        public void onFunctionsChanged() {
            final Handler handler = App.getHandler();
            handler.removeCallbacks(this);
            handler.postDelayed(this, 500L);
        }

        @Override
        public void run() {
            final PlotData plotData = plotter.getPlotData();
            App.getBackground().execute(new Runnable() {
                @Override
                public void run() {
                    final PlotFunctions plotFunctions = new PlotFunctions(plotData.functions);
                    try {
                        final JSONArray jsonArray = plotFunctions.toJson();
                        FileSaver.save(getFunctionsFile(App.getApplication()), jsonArray.toString());
                    } catch (JSONException e) {
                        Log.e("PlotterApplication", e.getMessage(), e);
                    }
                }
            });
        }
    }

    private static class FunctionsLoader implements Runnable {
        @NonNull
        private final Plotter plotter;

        public FunctionsLoader(@NonNull Plotter plotter) {
            this.plotter = plotter;
        }

        @Override
        public void run() {
            final Context context = App.getApplication();
            final FileLoader fileLoader = FileLoader.create(context, getFunctionsFile(context));
            final CharSequence jsonString = fileLoader.load();
            if (TextUtils.isEmpty(jsonString)) {
                return;
            }
            try {
                final PlotFunctions plotFunctions = new PlotFunctions(new JSONArray(jsonString.toString()));
                final List<PlotFunction> functions = plotFunctions.asList();
                if (functions.isEmpty()) {
                    return;
                }
                App.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        for (PlotFunction function : functions) {
                            plotter.add(function);
                        }
                    }
                });
            } catch (JSONException e) {
                Log.e("PlotterApplication", e.getMessage(), e);
            }
        }
    }
}
