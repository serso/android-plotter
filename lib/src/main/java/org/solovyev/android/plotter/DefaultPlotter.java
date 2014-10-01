package org.solovyev.android.plotter;

import org.solovyev.android.plotter.meshes.*;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

final class DefaultPlotter implements Plotter {

	@Nonnull
	private final DoubleBufferGroup<FunctionGraph> functionMeshes = DoubleBufferGroup.create();

	@Nonnull
	private final DoubleBufferGroup<Mesh> otherMeshes = DoubleBufferGroup.create();

	@Nonnull
	private final Group<Mesh> allMeshes = ListGroup.create(Arrays.<Mesh>asList(functionMeshes, otherMeshes));

	@Nonnull
	private final Pool<FunctionGraph> pool = new ListPool<FunctionGraph>(new ListPool.Callback<FunctionGraph>() {
		@Nonnull
		@Override
		public FunctionGraph create() {
			return FunctionGraph.create(5, 5, 30, 30, Function0.ZERO);
		}

		@Override
		public void release(@Nonnull FunctionGraph mesh) {
			mesh.setFunction(Function0.ZERO);
		}
	});

	@Nonnull
	private PlotData plotData = PlotData.create();

	@Nonnull
	private final Initializer initializer = new Initializer(allMeshes);

	@Nonnull
	private final MeshConfig config = MeshConfig.create();

	@Nonnull
	private final Executor background = Executors.newFixedThreadPool(4, new ThreadFactory() {

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
	@Nonnull
	private Dimensions dimensions = new Dimensions();

	@Nonnull
	private final Runnable dimensionsChangedRunnable = new Runnable() {
		@Override
		public void run() {
			onFunctionsChanged();
		}
	};

	@Override
	public void add(@Nonnull Mesh mesh) {
		otherMeshes.addMesh(mesh);
		setDirty();
	}

	@Override
	public void add(@Nonnull List<Mesh> meshes) {
		for (Mesh mesh : meshes) {
			otherMeshes.addMesh(mesh);
		}
		setDirty();
	}

	@Override
	public void initGl(@Nonnull GL11 gl, boolean firstTime) {
		if (firstTime) {
			// fill the background
			final int bg = plotData.axisStyle.backgroundColor;
			gl.glClearColor(Color.red(bg), Color.green(bg), Color.blue(bg), Color.alpha(bg));

			if (config.alpha) {
				gl.glEnable(GL10.GL_BLEND);
				gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
			}

		}
		allMeshes.initGl(gl, config);
	}

	@Override
	public void draw(@Nonnull GL11 gl) {
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
				final FunctionGraph first = dbm.getFirst();
				first.setFunction(function.function);
				first.setColor(function.lineStyle.color);
				first.setDimensions(dimensions);
				final FunctionGraph second = dbm.getSecond();
				second.setFunction(function.function);
				second.setColor(function.lineStyle.color);
				second.setDimensions(dimensions);
			} else {
				final FunctionGraph mesh = pool.obtain();
				mesh.setFunction(function.function);
				mesh.setColor(function.lineStyle.color);
				mesh.setDimensions(dimensions);
				functionMeshes.add(DoubleBufferMesh.wrap(mesh));
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
		plotData.add(function);
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
	public void clearFunctions() {
		plotData.functions.clear();
		onFunctionsChanged();
	}

	@Override
	public void attachView(@Nonnull PlottingView view) {
		Check.isMainThread();
		synchronized (lock) {
			Check.same(emptyView, this.view);
			this.view = view;
			if (emptyView.shouldRender) {
				emptyView.shouldRender = false;
				this.view.requestRender();
			}

			if (emptyView.shouldUpdateFunctions) {
				emptyView.shouldUpdateFunctions = false;
				this.view.post(dimensionsChangedRunnable);
			}
		}
	}

	@Override
	public void detachView(@Nonnull PlottingView view) {
		Check.isMainThread();
		synchronized (lock) {
			Check.same(view, this.view);
			emptyView.shouldRender = false;
			emptyView.shouldUpdateFunctions = false;
			this.view = emptyView;
		}
	}

	@Override
	public void setDimensions(@Nonnull Dimensions newDimensions) {
		synchronized (lock) {
			if (!dimensions.equals(newDimensions)) {
				dimensions = newDimensions;
				view.post(dimensionsChangedRunnable);
			}
		}
	}

	@Nonnull
	public Dimensions getDimensions() {
		synchronized (dimensions) {
			return dimensions;
		}
	}

	@Nonnull
	public final class Initializer implements Runnable {

		@Nonnull
		private final Group<Mesh> group;

		public Initializer(@Nonnull Group<Mesh> group) {
			this.group = group;
		}

		@Override
		public void run() {
			group.init();
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
		public boolean post(@Nonnull Runnable runnable) {
			shouldUpdateFunctions = dimensionsChangedRunnable == runnable;
			return true;
		}
	}
}

