package org.solovyev.android.plotter.app;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.RectF;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.squareup.leakcanary.LeakCanary;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.solovyev.android.io.FileLoader;
import org.solovyev.android.io.FileSaver;
import org.solovyev.android.plotter.BasePlotterListener;
import org.solovyev.android.plotter.Plot;
import org.solovyev.android.plotter.PlotData;
import org.solovyev.android.plotter.PlotFunction;
import org.solovyev.android.plotter.Plotter;
import org.solovyev.android.plotter.PlottingView;
import org.solovyev.android.plotter.RectSize;
import org.solovyev.android.plotter.math.ExpressionFunction;
import org.solovyev.android.plotter.meshes.MeshSpec;

import java.io.File;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class PlotterApplication extends Application {

    public static final String PREFS_VERSION_CODE = "version.code";
    public static final String PREFS_VERSION_NAME = "version.name";
    @NonNull
    private static final Object SOURCE = new Object();

    @NonNull
    private static File getFunctionsFile(@NonNull Context context) {
        return new File(context.getFilesDir(), "functions");
    }

    @NonNull
    private static File getPlotterFile(@NonNull Context context) {
        return new File(context.getFilesDir(), "plotter");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        LeakCanary.install(this);

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

        App.getBackground().execute(new PlotterStateLoader(plotter));
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

    private static class PlotterListener extends BasePlotterListener {
        @NonNull
        private final Plotter plotter;
        @NonNull
        private final Runnable functionsChangedRunnable = new Runnable() {
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
        };
        @NonNull
        private final Runnable plotterChangedRunnable = new Runnable() {
            @Override
            public void run() {
                final PlotterState plotterState = new PlotterState(plotter.is3d(), plotter.getDimensions().graph.makeBounds());
                App.getBackground().execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            final JSONObject json = plotterState.toJson();
                            FileSaver.save(getPlotterFile(App.getApplication()), json.toString());
                        } catch (JSONException e) {
                            Log.e("PlotterApplication", e.getMessage(), e);
                        }
                    }
                });
            }
        };

        public PlotterListener(@NonNull Plotter plotter) {
            this.plotter = plotter;
        }

        @Override
        public void onFunctionsChanged() {
            scheduleWrite(functionsChangedRunnable);
        }

        @Override
        public void onDimensionsChanged(@Nullable Object source) {
            if (source == SOURCE) {
                return;
            }
            scheduleWrite(plotterChangedRunnable);
        }

        @Override
        public void on3dChanged(boolean d3) {
            scheduleWrite(plotterChangedRunnable);
        }

        private void scheduleWrite(@NonNull Runnable runnable) {
            final Handler handler = App.getHandler();
            handler.removeCallbacks(runnable);
            handler.postDelayed(runnable, 500L);
        }
    }

    private static class PlotterStateLoader extends BasePlotterListener implements Runnable, PlottingView.Listener {
        @NonNull
        private final Plotter plotter;
        @NonNull
        private final CountDownLatch latch = new CountDownLatch(1);

        public PlotterStateLoader(@NonNull Plotter plotter) {
            this.plotter = plotter;
            this.plotter.addListener(this);
        }

        @Override
        public void run() {
            final Context context = App.getApplication();
            final FileLoader fileLoader = FileLoader.create(context, getPlotterFile(context));
            final CharSequence jsonString = fileLoader.load();
            if (TextUtils.isEmpty(jsonString)) {
                return;
            }
            try {
                final PlotterState plotterState = new PlotterState(new JSONObject(jsonString.toString()));
                App.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        plotter.set3d(plotterState.is3d());
                    }
                });
                try {
                    // we must wait while PlotView is initialized as we don't know yet screen
                    // dimensions and applying graph bounds now will cause wrong position of the
                    // camera (as scene width/height will be changed)
                    latch.await(2000L, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    Log.w("PlotterApplication", e.getMessage(), e);
                }
                App.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        plotter.removeListener(PlotterStateLoader.this);
                        final RectF bounds = plotterState.getBounds();
                        Plot.setGraphBounds(SOURCE, plotter, bounds, plotterState.is3d());
                    }
                });
            } catch (JSONException e) {
                Log.e("PlotterApplication", e.getMessage(), e);
            }
        }

        @Override
        public void onViewAttached(@NonNull PlottingView view) {
            view.addListener(this);
        }

        @Override
        public void onViewDetached(@NonNull PlottingView view) {
            view.removeListener(this);
        }

        public void onTouchStarted() {
        }

        @Override
        public void onSizeChanged(@NonNull RectSize viewSize) {
            latch.countDown();
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
