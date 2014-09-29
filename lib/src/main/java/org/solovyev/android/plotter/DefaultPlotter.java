package org.solovyev.android.plotter;

import org.solovyev.android.plotter.meshes.*;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import java.util.ArrayList;
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
	private final List<FunctionGraph> pool = new ArrayList<FunctionGraph>();

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

	public void ensureFunctionsSize() {
		Check.isMainThread();

		// for each functions we should assign mesh
		// if there are not enough meshes => create new
		// if there are too many meshes => release them
		int i = 0;
		for (PlotFunction function : plotData.functions) {
			if (i < functionMeshes.size()) {
				final DoubleBufferMesh<FunctionGraph> dbm = functionMeshes.get(i);
				final FunctionGraph mesh = dbm.getNext();
				mesh.setFunction(function.function);
				mesh.setColor(function.lineStyle.color);
			} else {
				final int poolSize = pool.size();
				final FunctionGraph mesh;
				if (poolSize > 0) {
					mesh = pool.remove(poolSize - 1);
					mesh.setFunction(function.function);
				} else {
					mesh = FunctionGraph.create(5, 5, 30, 30, function.function);
				}
				mesh.setColor(function.lineStyle.color);
				functionMeshes.add(DoubleBufferMesh.wrap(mesh));
			}
			i++;
		}

		for (int k = functionMeshes.size() - 1; k >= i; k--) {
			final DoubleBufferMesh<FunctionGraph> dbm = functionMeshes.remove(k);
			final FunctionGraph mesh = dbm.getNext();
			mesh.setFunction(Function0.ZERO);
			pool.add(mesh);
		}
	}

	@Override
	public void setDirty() {
		background.execute(initializer);
	}

	@Override
	public void add(@Nonnull Function function) {
		plotData.add(function);
		setDirtyFunctions();
	}

	private void setDirtyFunctions() {
		ensureFunctionsSize();
		setDirty();
	}

	@Override
	public void add(@Nonnull PlotFunction function) {
		plotData.add(function.copy());
		setDirtyFunctions();
	}

	@Override
	public void clearFunctions() {
		plotData.functions.clear();
		setDirtyFunctions();
	}

	@Override
	public void attachView(@Nonnull PlottingView view) {
		Check.isMainThread();
		synchronized (lock) {
			Check.same(emptyView, this.view);
			this.view = view;
			if (emptyView.requested) {
				emptyView.requested = false;
				this.view.requestRender();
			}
		}
	}

	@Override
	public void detachView(@Nonnull PlottingView view) {
		Check.isMainThread();
		synchronized (lock) {
			Check.same(view, this.view);
			emptyView.requested = false;
			this.view = emptyView;
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
	private final static class EmptyPlottingView implements PlottingView {
		private boolean requested;

		@Override
		public void requestRender() {
			requested = true;
		}
	}
}

