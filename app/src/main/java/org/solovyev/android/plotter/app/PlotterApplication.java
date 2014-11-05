package org.solovyev.android.plotter.app;

import android.app.Application;
import org.solovyev.android.plotter.Function1;
import org.solovyev.android.plotter.Function2;
import org.solovyev.android.plotter.Plot;
import org.solovyev.android.plotter.Plotter;

import javax.annotation.Nonnull;

public class PlotterApplication extends Application {

	@Nonnull
	private static PlotterApplication instance;

	@Nonnull
	private final Plotter plotter = Plot.newPlotter();

	@Nonnull
	public static final String PARABOLOID = "x*x+y*y";

	public PlotterApplication() {
		instance = this;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		/*plotter.add(new Function2("sin(x)+sin(y)-2") {
			@Override
			public float evaluate(float x, float y) {
				return (float) (Math.sin(x) + Math.sin(y)) - 2f;
			}
		});
		plotter.add(Function0.ZERO);*/
		/*final PlotFunction paraboloid = PlotFunction.create(new Function2(PARABOLOID) {
			@Override
			public float evaluate(float x, float y) {
				return x * x + y * y;
			}
		});
		paraboloid.lineStyle.color = Color.RED.toInt();
		plotter.add(paraboloid);*/
		plotter.add(new Function1("x*x") {
			@Override
			public float evaluate(float x) {
				return x * x;
			}
		});

		plotter.add(new Function2("sin(x) + sin(y)") {
			@Override
			public float evaluate(float x, float y) {
				return (float) (Math.sin(x) + Math.sin(y));
			}
		});

		/*plotter.add(new Function2("x+y") {
			@Override
			public float evaluate(float x, float y) {
				return x + y;
			}
		});*/

		/*
		otherMeshes.add(new WireFramePlane(5, 5, 30, 30));
		otherMeshes.add(FunctionGraph.create(new Function2() {
			@Override
			public float evaluate(float x, float y) {
				return x + y;
			}
		}).withColor(Color.BLUE));
		otherMeshes.add(FunctionGraph.create(new Function2() {
			@Override
			public float evaluate(float x, float y) {
				return (float) (Math.sin(x) + Math.cos(x));
			}
		}).withColor(Color.CYAN));
		otherMeshes.add(FunctionGraph.create(new Function2() {
			@Override
			public float evaluate(float x, float y) {
				return x * x + y * y;
			}
		}));
		otherMeshes.add(FunctionGraph.create(new Function2() {
			@Override
			public float evaluate(float x, float y) {
				return -x * x - y * y;
			}
		}).withColor(Color.GREEN));
		otherMeshes.add(FunctionGraph.create(new Function2() {
			@Override
			public float evaluate(float x, float y) {
				return -x * x + y * y;
			}
		}).withColor(Color.RED));
		otherMeshes.add(FunctionGraph.create(5, 5, 30, 30, new Function2() {
			@Override
			public float evaluate(float x, float y) {
				return (float) (Math.sin(x) + Math.sin(y));
			}
		}));*/
		/*meshes.add(FunctionGraph.create(5, 5, 30, 30, new Function2() {
			@Override
			public float evaluate(float x, float y) {
				return (float) (Math.sin(x) + Math.sin(y));
			}
		}));
		background.execute(initializer);*/
	}

	@Nonnull
	public static PlotterApplication get() {
		return instance;
	}

	@Nonnull
	public Plotter getPlotter() {
		return plotter;
	}
}
