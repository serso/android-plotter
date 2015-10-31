package org.solovyev.android.plotter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PointF;
import android.util.Log;
import org.solovyev.android.plotter.meshes.*;
import org.solovyev.android.plotter.text.FontAtlas;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.microedition.khronos.opengles.GL11;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

final class DefaultPlotter implements Plotter {

	@Nonnull
	private static final String TAG = Plot.getTag("Plotter");

	@GuardedBy("lock")
	@Nonnull
	private Dimensions dimensions = new Dimensions();

	@Nonnull
	private final Coordinates coordinates = new Coordinates(dimensions, Color.WHITE);

	@Nonnull
	private final List<DoubleBufferMesh<AxisLabels>> labels = new ArrayList<>(3);

	@Nonnull
	private final DoubleBufferGroup<FunctionGraph> functionMeshes = DoubleBufferGroup.create(FunctionGraphSwapper.INSTANCE);

	@Nonnull
	private final DoubleBufferGroup<Mesh> otherMeshes = DoubleBufferGroup.create(null);

	@Nonnull
	private final Group<Mesh> allMeshes = ListGroup.create(Arrays.<Mesh>asList(functionMeshes, otherMeshes));

	@Nonnull
	private final Pool<FunctionGraph> pool = new ListPool<>(new ListPool.Callback<FunctionGraph>() {
		@Nonnull
		@Override
		public FunctionGraph create() {
			if (is3d()) {
				return FunctionGraph3d.create(Dimensions.empty(), Function0.ZERO);
			} else {
				return FunctionGraph2d.create(Dimensions.empty(), Function0.ZERO);
			}
		}

		@Override
		public void release(@Nonnull FunctionGraph mesh) {
			mesh.setFunction(Function0.ZERO);
		}
	});

	@Nonnull
	private PlotData plotData = PlotData.create();

	@Nonnull
	private final Initializer initializer = new Initializer();

	@Nonnull
	private final MeshConfig config = MeshConfig.create();

	@Nonnull
	private final ExecutorService background = Executors.newFixedThreadPool(Plot.getAvailableProcessors(), new ThreadFactory() {

		@Nonnull
		private final AtomicInteger counter = new AtomicInteger(0);

		@Override
		public Thread newThread(@Nonnull Runnable r) {
			return new Thread(r, "PlotBackground #" + counter.getAndIncrement());
		}
	});

	@Nonnull
	private final Object lock = new Object();

	@GuardedBy("lock")
	@Nonnull
	private final EmptyPlottingView emptyView = new EmptyPlottingView();

	@GuardedBy("lock")
	@Nonnull
	private PlottingView view = emptyView;

	@GuardedBy("lock")
	private boolean d3 = D3;

	@Nonnull
	private final DimensionsChangeNotifier dimensionsChangedRunnable = new DimensionsChangeNotifier();

	@Nonnull
	private final Context context;

	@Nonnull
	private final FontAtlas fontAtlas;

	DefaultPlotter(@Nonnull Context context) {
		this.context = context;
		this.fontAtlas = new FontAtlas(context);
		set3d(false);
	}

	public void add(@Nonnull Mesh mesh) {
		otherMeshes.addMesh(mesh);
		setDirty();
	}

	public void add(@Nonnull DoubleBufferMesh<? extends Mesh> mesh) {
		otherMeshes.add((DoubleBufferMesh<Mesh>) mesh);
		setDirty();
	}

	@Override
	public void initGl(@Nonnull GL11 gl, boolean firstTime) {
		if (firstTime) {
			final Resources resources = context.getResources();
			final int fontSize = resources.getDimensionPixelSize(R.dimen.font_size);
			final int fontSpacing = resources.getDimensionPixelSize(R.dimen.font_spacing);
			fontAtlas.init(gl, "Roboto-Regular.ttf", fontSize, fontSpacing, fontSpacing, plotData.axisStyle.axisLabelsColor);

			// fill the background
			final int bg = plotData.axisStyle.backgroundColor;
			gl.glClearColor(Color.red(bg), Color.green(bg), Color.blue(bg), Color.alpha(bg));
		}
		allMeshes.initGl(gl, config);
		if (existsNotInitializedMesh(allMeshes)) {
			Log.d(TAG, "Exist not initialized meshes after iniGl, invoking init again...");
			setDirty();
		}
	}

	private boolean existsNotInitializedMesh(@Nonnull Group<Mesh> meshes) {
		for (Mesh mesh : meshes) {
			if (mesh instanceof Group) {
				if (existsNotInitializedMesh((Group<Mesh>) mesh)) {
					return true;
				}
				continue;
			}
			if (mesh instanceof DoubleBufferMesh) {
				mesh = ((DoubleBufferMesh) mesh).getNext();
			}

			final Mesh.State state = mesh.getState();
			if (state != Mesh.State.INIT && state != Mesh.State.INIT_GL) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void draw(@Nonnull GL11 gl, float labelsAlpha) {
		for (DoubleBufferMesh<AxisLabels> label : labels) {
			label.setAlpha(labelsAlpha);
		}
		allMeshes.draw(gl);
	}

	private void ensureFunctionsSize() {
		Check.isMainThread();

		final Dimensions dimensions = getDimensions();

		// for each functions we should assign mesh
		// if there are not enough meshes => create new
		// if there are too many meshes => release them
		int i = 0;
		for (PlotFunction function : plotData.functions) {
			if (i < functionMeshes.size()) {
				final DoubleBufferMesh<FunctionGraph> dbm = functionMeshes.get(i);
				final FunctionGraph next = dbm.getNext();
				final FunctionGraph current = dbm.getOther(next);
				next.setFunction(function.function);
				function.meshSpec.applyTo(next);
				next.setDimensions(dimensions);
				function.meshSpec.applyTo(current);
			} else {
				final FunctionGraph mesh = pool.obtain();
				mesh.setFunction(function.function);
				function.meshSpec.applyTo(mesh);
				mesh.setDimensions(dimensions);
				functionMeshes.add(DoubleBufferMesh.wrap(mesh, FunctionGraphSwapper.INSTANCE));
			}
			i++;
		}

		for (int k = functionMeshes.size() - 1; k >= i; k--) {
			final DoubleBufferMesh<FunctionGraph> dbm = functionMeshes.remove(k);
			pool.release(dbm.getNext());
		}
	}

	private void setDirty() {
		background.execute(initializer);
	}

	@Override
	public void add(@Nonnull Function function) {
		plotData.add(PlotFunction.create(function, context));
		onFunctionsChanged();
	}

	private void onDimensionsChanged() {
		Check.isMainThread();

		final Dimensions dimensions = getDimensions();

		for (DoubleBufferMesh<Mesh> dbm : otherMeshes) {
			final Mesh mesh = dbm.getNext();
			if (mesh instanceof DimensionsAware) {
				((DimensionsAware) mesh).setDimensions(dimensions);
			}
		}
		onFunctionsChanged();
	}

	private void onFunctionsChanged() {
		ensureFunctionsSize();
		setDirty();
	}

	@Override
	public void add(@Nonnull PlotFunction function) {
		plotData.add(function.copy());
		onFunctionsChanged();
	}

	@Override
	public void update(@Nonnull PlotFunction function) {
		plotData.update(function.copy());
		onFunctionsChanged();
	}

	@Override
	public void clearFunctions() {
		plotData.functions.clear();
		onFunctionsChanged();
	}

	@Override
	@Nonnull
	public PlotData getPlotData() {
		Check.isMainThread();
		return plotData.copy();
	}

	@Override
	public void attachView(@Nonnull PlottingView newView) {
		Check.isMainThread();
		synchronized (lock) {
			Check.same(emptyView, view);
			view = newView;
			if (emptyView.shouldRender) {
				emptyView.shouldRender = false;
				view.requestRender();
			}

			if (emptyView.shouldUpdateFunctions) {
				emptyView.shouldUpdateFunctions = false;
				dimensionsChangedRunnable.run(emptyView, this);
			}
			view.set3d(d3);
		}
	}

	@Override
	public void detachView(@Nonnull PlottingView oldView) {
		Check.isMainThread();
		synchronized (lock) {
			Check.same(oldView, view);
			emptyView.shouldRender = false;
			emptyView.shouldUpdateFunctions = false;
			view = emptyView;
		}
	}

	private void updateDimensions(@Nonnull Dimensions newDimensions, @Nullable Object source) {
		Check.isAnyThread();
		synchronized (lock) {
			if (!dimensions.equals(newDimensions)) {
				dimensions = newDimensions;
				if (!Plot.isMainThread() || view == emptyView) {
					dimensionsChangedRunnable.post(view, source);
				} else {
					dimensionsChangedRunnable.run(view, source);
				}
			}
		}
	}

	@Override
	public void updateScene(@Nullable Object source, @Nonnull Zoom zoom, @Nonnull RectSize viewSize, @Nonnull RectSizeF sceneSize, @Nonnull PointF sceneCenter) {
		synchronized (lock) {
			final Dimensions newDimensions = dimensions.updateScene(viewSize, sceneSize, sceneCenter);
			if (newDimensions != dimensions) {
				updateDimensions(newDimensions, source);
			}
		}
	}

	@Override
	public void updateGraph(@Nullable Object source, @Nonnull RectSizeF graphSize, @Nonnull PointF graphCenter) {
		synchronized (lock) {
			final Dimensions newDimensions = dimensions.updateGraph(graphSize, graphCenter);
			if (newDimensions != dimensions) {
				updateDimensions(newDimensions, source);
			}
		}
	}

	@Override
	@Nonnull
	public Dimensions getDimensions() {
		synchronized (lock) {
			return dimensions.copy();
		}
	}

	@Nonnull
	@Override
	public Dimensions.Scene getSceneDimensions() {
		synchronized (lock) {
			return dimensions.scene;
		}
	}

	@Override
	public boolean is3d() {
		synchronized (lock) {
			return d3;
		}
	}

	public boolean is2d() {
		return !is3d();
	}

	@Override
	public void set3d(boolean d3) {
		Check.isMainThread();
		if (set3d0(d3)) {
			while (functionMeshes.size() > 0) {
				final DoubleBufferMesh<FunctionGraph> dbm = functionMeshes.remove(functionMeshes.size() - 1);
				pool.release(dbm.getNext());
			}
			pool.clear();
			otherMeshes.clear();
			if (d3) {
				synchronized (lock) {
					final Dimensions newDimensions = dimensions.updateGraph(dimensions.graph.size, new PointF(dimensions.graph.center.x, 0));
					if (newDimensions != dimensions) {
						updateDimensions(newDimensions, this);
					}
				}
			}
			makeSetting(d3);
			onFunctionsChanged();
			synchronized (lock) {
				view.set3d(d3);
			}
		}
	}

	@Override
	public void showCoordinates(float x, float y) {
		/*if (!d3) {
			coordinates.setScreenXY(x, y);
			setDirty();
		}*/
	}

	@Override
	public void hideCoordinates() {
		/*coordinates.clear();
		setDirty();*/
	}

	@Override
	public void onCameraMoved(float dx, float dy) {
		Check.isMainThread();
		for (DoubleBufferMesh<AxisLabels> label : labels) {
			final AxisLabels next = label.getNext();
			next.updateCamera(dx, dy);
		}
	}

	private void makeSetting(boolean d3) {
		otherMeshes.clear();
		labels.clear();
		final Dimensions dimensions = getDimensions();
		//add(new DrawableTexture(context.getResources(), R.drawable.icon));

		final int axisWidth = Math.max(1, MeshSpec.defaultWidth(context) / 2);
		final Color gridColor = Color.create(plotData.axisStyle.gridColor);
		final Color axisColor = Color.create(plotData.axisStyle.axisColor);
		final Color axisLabelsColor = Color.create(plotData.axisStyle.axisLabelsColor);

		add(AxisGrid.xz(dimensions, gridColor, d3).toDoubleBuffer());
		if (d3) {
			add(AxisGrid.xy(dimensions, gridColor, d3).toDoubleBuffer());
			add(AxisGrid.yz(dimensions, gridColor, d3).toDoubleBuffer());
		}
		add(prepareAxis(Axis.x(dimensions, d3), axisColor, axisWidth));
		labels.add(prepareAxisLabels(AxisLabels.x(fontAtlas, dimensions, d3), axisLabelsColor));
		add(prepareAxis(Axis.y(dimensions, d3), axisColor, axisWidth));
		labels.add(prepareAxisLabels(AxisLabels.y(fontAtlas, dimensions, d3), axisLabelsColor));
		if (d3) {
			add(prepareAxis(Axis.z(dimensions, d3), axisColor, axisWidth));
			labels.add(prepareAxisLabels(AxisLabels.z(fontAtlas, dimensions, true), axisLabelsColor));
		}
		for (DoubleBufferMesh<AxisLabels> label : labels) {
			add(label);
		}
		/*final SceneRect sceneRect = new SceneRect(dimensions);
		sceneRect.setColor(MeshSpec.LightColors.GREEN);
		add(DoubleBufferMesh.wrap(sceneRect, DimensionsAwareSwapper.INSTANCE));*/
		/*if(!d3) {
			coordinates.setColor(gridColor);
			add(coordinates);
		}*/
	}

	@Nonnull
	private DoubleBufferMesh<AxisLabels> prepareAxisLabels(@Nonnull AxisLabels labels, @Nonnull Color color) {
		labels.setColor(color);
		return labels.toDoubleBuffer();
	}

	@Nonnull
	private DoubleBufferMesh<Axis> prepareAxis(@Nonnull Axis axis, @Nonnull Color color, int width) {
		axis.setColor(color);
		axis.setWidth(width);
		return axis.toDoubleBuffer();
	}

	private boolean set3d0(boolean d3) {
		synchronized (lock) {
			if (this.d3 != d3) {
				this.d3 = d3;
				return true;
			}
			return false;
		}
	}

	@Nonnull
	public final class Initializer implements Runnable {

		@Override
		public void run() {
			allMeshes.init();
			synchronized (lock) {
				view.requestRender();
			}
		}
	}

	/**
	 * Dummy plotting view which tracks render requests. If the real view is set and this view detected render request
	 * then {@link PlottingView#requestRender()} of the new view will be called.
	 */
	private final class EmptyPlottingView implements PlottingView {
		private boolean shouldRender;
		private boolean shouldUpdateFunctions;

		@Override
		public void requestRender() {
			shouldRender = true;
		}

		@Override
		public void zoom(boolean in) {
		}

		@Override
		public void resetZoom() {
		}

		@Override
		public void resetCamera() {
		}

		@Override
		public boolean removeCallbacks(@Nonnull Runnable runnable) {
			return false;
		}

		@Override
		public boolean post(@Nonnull Runnable runnable) {
			if (!shouldUpdateFunctions) {
				shouldUpdateFunctions = dimensionsChangedRunnable == runnable;
			}
			return true;
		}

		@Override
		public void set3d(boolean d3) {
		}

		@Override
		public void onDimensionChanged(@Nonnull Dimensions dimensions, @Nullable Object source) {
		}
	}

	private class DimensionsChangeNotifier implements Runnable {
		@Nullable
		PlottingView view;
		@Nullable
		private Object source;

		public void run(@Nonnull PlottingView view, @Nullable Object source) {
			Check.isTrue(Thread.holdsLock(lock));
			this.view = view;
			this.source = source;
			run();
		}

		@Override
		public void run() {
			PlottingView view;
			Object source;
			synchronized (lock) {
				view = this.view;
				this.view = null;
				source = this.source;
				this.source = null;
			}
			if (view != null) {
				view.onDimensionChanged(getDimensions(), source);
			}
			onDimensionsChanged();
		}

		public void post(@Nonnull PlottingView view, @Nonnull Object source) {
			Check.isTrue(Thread.holdsLock(lock));
			this.view = view;
			this.source = source;
			view.removeCallbacks(dimensionsChangedRunnable);
			view.post(dimensionsChangedRunnable);
		}
	}
}