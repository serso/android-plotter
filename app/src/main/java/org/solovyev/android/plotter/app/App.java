package org.solovyev.android.plotter.app;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;

import com.squareup.otto.Bus;

import org.solovyev.android.plotter.Plot;
import org.solovyev.android.plotter.Plotter;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("NullableProblems")
public final class App {

    @NonNull
    private static final App app = new App();
    @NonNull
    private final Handler handler = new Handler(Looper.getMainLooper());
    @NonNull
    private final ExecutorService background = Executors.newCachedThreadPool(new ThreadFactory() {
        @NonNull
        private final AtomicInteger counter = new AtomicInteger();

        @Override
        public Thread newThread(@NonNull Runnable r) {
            return new Thread(r, "Background #" + counter.incrementAndGet());
        }
    });
    @NonNull
    private Application application;
    @NonNull
    private Plotter plotter;
    @NonNull
    private Bus bus;
    @NonNull
    private SharedPreferences preferences;

    private App() {
    }

    @NonNull
    public static Application getApplication() {
        return app.application;
    }

    @NonNull
    public static Plotter getPlotter() {
        return app.plotter;
    }

    @NonNull
    public static Bus getBus() {
        return app.bus;
    }

    @NonNull
    public static Handler getHandler() {
        return app.handler;
    }

    @NonNull
    public static Executor getBackground() {
        return app.background;
    }

    @NonNull
    public static SharedPreferences getPreferences() {
        return app.preferences;
    }

    public static void create(@NonNull Application application) {
        app.application = application;
        app.plotter = Plot.newPlotter(application);
        app.bus = new MainThreadBus(getHandler());
        app.preferences = PreferenceManager.getDefaultSharedPreferences(application);
    }

    private static boolean isMainThread() {
        return Looper.getMainLooper() == Looper.myLooper();
    }

    static void setError(@NonNull TextInputLayout textInput, @NonNull String error) {
        textInput.setError(error);
        textInput.setErrorEnabled(true);
    }

    static void clearError(@NonNull TextInputLayout textInput) {
        textInput.setError(null);
        textInput.setErrorEnabled(false);
    }

    private static class MainThreadBus extends Bus {

        @NonNull
        private final Handler handler;

        private MainThreadBus(@NonNull Handler handler) {
            this.handler = handler;
        }

        @Override
        public void post(final Object event) {
            if (isMainThread()) {
                super.post(event);
            } else {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        MainThreadBus.super.post(event);
                    }
                });
            }
        }
    }
}
