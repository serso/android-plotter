package org.solovyev.android.plotter.app;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import com.squareup.otto.Bus;

import org.solovyev.android.plotter.Plot;
import org.solovyev.android.plotter.Plotter;

@SuppressWarnings("NullableProblems")
public final class App {

	@NonNull
	private static final App app = new App();
	@NonNull
	private final Handler handler = new Handler(Looper.getMainLooper());
	@NonNull
	private Application application;
	@NonNull
	private Plotter plotter;
	@NonNull
	private Bus bus;

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

	public static void create(@NonNull Application application) {
		app.application = application;
		app.plotter = Plot.newPlotter(application);
		app.bus = new MainThreadBus(getHandler());
	}

	private static boolean isMainThread() {
		return Looper.getMainLooper() == Looper.myLooper();
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
