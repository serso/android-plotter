package org.solovyev.android.plotter.app;

import android.app.Application;
import org.solovyev.android.plotter.*;
import org.solovyev.android.plotter.meshes.Mesh;
import org.solovyev.android.plotter.meshes.WireFrameCube;

import javax.annotation.Nonnull;
import java.util.Arrays;

public class PlotterApplication extends Application {

	@Nonnull
	private static PlotterApplication instance;

	@Nonnull
	private final Plotter plotter = Plot.newPlotter();

	public PlotterApplication() {
		instance = this;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		plotter.add(new Function2("sin(x)+sin(y)-2") {
			@Override
			public float evaluate(float x, float y) {
				return (float) (Math.sin(x) + Math.sin(y)) - 2f;
			}
		});
		plotter.add(Function0.ZERO);
		plotter.add(new Function2("x*x+y*y") {
			@Override
			public float evaluate(float x, float y) {
				return x * x + y * y;
			}
		});
		plotter.add(new Function1("x*x") {
			@Override
			public float evaluate(float x) {
				return x * x;
			}
		});

		final int size = Dimensions.GRAPH_SIZE;
		plotter.add(Arrays.<Mesh>asList(new WireFrameCube(size, size, size)));
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
