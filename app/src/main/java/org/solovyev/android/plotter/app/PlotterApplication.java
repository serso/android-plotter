package org.solovyev.android.plotter.app;

import android.app.Application;

import org.solovyev.android.plotter.Color;
import org.solovyev.android.plotter.Function1;
import org.solovyev.android.plotter.Plot;
import org.solovyev.android.plotter.PlotFunction;
import org.solovyev.android.plotter.Plotter;
import org.solovyev.android.plotter.meshes.MeshSpec;

import javax.annotation.Nonnull;

public class PlotterApplication extends Application {

	@Nonnull
	public static final String PARABOLOID = "x * x + y * y";

	@Nonnull
	private static PlotterApplication instance;

	@Nonnull
	private Plotter plotter;

	public PlotterApplication() {
		instance = this;
	}

	@Nonnull
	public static PlotterApplication get() {
		return instance;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		plotter = Plot.newPlotter(this);
		plotter.add(PlotFunction.create(new Function1("x * x / 2") {
			@Override
			public float evaluate(float x) {
				return x * x / 2;
			}
		}, MeshSpec.create(Color.BLUE, MeshSpec.defaultWidth(this))));

/*
		plotter.add(new Function1("tan(x)") {
			@Override
			public float evaluate(float x) {
				return (float) Math.tan(x);
			}
		});
*/

/*		plotter.add(new Function1("sin(1/x)") {
			@Override
			public float evaluate(float x) {
				if (x == 0) {
					return 0;
				}
				return (float) Math.sin(1 / x);
			}
		});*/

/*		plotter.add(new Function1("1/(x + 1)/(x - 1)^6") {
			@Override
			public float evaluate(float x) {
				if (x == 1 || x == -1) {
					return Float.MAX_VALUE;
				}
				return (float) (1 / (x + 1) / Math.pow(x - 1, 6));
			}
		});*/

/*		plotter.add(new Function2("sin(x) + sin(y)") {
			@Override
			public float evaluate(float x, float y) {
				return (float) (Math.sin(x) + Math.sin(y));
			}
		});

		final PlotFunction paraboloid = PlotFunction.create(new Function2(PARABOLOID) {
			@Override
			public float evaluate(float x, float y) {
				return x * x + y * y;
			}
		});
		paraboloid.meshSpec.color = Color.RED;
		plotter.add(paraboloid);*/
	}

	@Nonnull
	public Plotter getPlotter() {
		return plotter;
	}
}
