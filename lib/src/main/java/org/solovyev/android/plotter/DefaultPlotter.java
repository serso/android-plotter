package org.solovyev.android.plotter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.solovyev.android.plotter.meshes.Axis;
import org.solovyev.android.plotter.meshes.AxisGrid;
import org.solovyev.android.plotter.meshes.AxisLabels;
import org.solovyev.android.plotter.meshes.Coordinates;
import org.solovyev.android.plotter.meshes.DimensionsAware;
import org.solovyev.android.plotter.meshes.DoubleBufferGroup;
import org.solovyev.android.plotter.meshes.DoubleBufferMesh;
import org.solovyev.android.plotter.meshes.FunctionGraph;
import org.solovyev.android.plotter.meshes.FunctionGraph2d;
import org.solovyev.android.plotter.meshes.FunctionGraph3d;
import org.solovyev.android.plotter.meshes.FunctionGraphSwapper;
import org.solovyev.android.plotter.meshes.Group;
import org.solovyev.android.plotter.meshes.ListGroup;
import org.solovyev.android.plotter.meshes.ListPool;
import org.solovyev.android.plotter.meshes.Mesh;
import org.solovyev.android.plotter.meshes.MeshSpec;
import org.solovyev.android.plotter.meshes.Pool;
import org.solovyev.android.plotter.text.FontAtlas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.concurrent.GuardedBy;
import javax.microedition.khronos.opengles.GL11;

final class DefaultPlotter implements Plotter {

    @NonNull
    private static final String TAG = Plot.getTag("Plotter");
    @NonNull
    private final List<DoubleBufferMesh<AxisLabels>> labels = new CopyOnWriteArrayList<>();
    @NonNull
    private final DoubleBufferGroup<FunctionGraph> functionMeshes = DoubleBufferGroup.create(FunctionGraphSwapper.INSTANCE);
    @NonNull
    private final DoubleBufferGroup<Mesh> otherMeshesBefore = DoubleBufferGroup.create(null);
    @NonNull
    private final DoubleBufferGroup<Mesh> otherMeshesAfter = DoubleBufferGroup.create(null);
    @NonNull
    private final Group<Mesh> allMeshes = ListGroup.create(Arrays.<Mesh>asList(otherMeshesBefore, functionMeshes, otherMeshesAfter));
    @NonNull
    private final Initializer initializer = new Initializer();
    @NonNull
    private final MeshConfig config = MeshConfig.create();
    @NonNull
    private final ExecutorService background = Executors.newFixedThreadPool(Plot.getAvailableProcessors(), new ThreadFactory() {

        @NonNull
        private final AtomicInteger counter = new AtomicInteger(0);

        @Override
        public Thread newThread(@NonNull Runnable r) {
            return new Thread(r, "PlotBackground #" + counter.getAndIncrement());
        }
    });
    @NonNull
    private final Object lock = new Object();
    @GuardedBy("lock")
    @NonNull
    private final EmptyPlottingView emptyView = new EmptyPlottingView();
    @NonNull
    private final DimensionsChangeNotifier dimensionsChangedRunnable = new DimensionsChangeNotifier();
    @NonNull
    private final Context context;
    @NonNull
    private final FontAtlas fontAtlas;
    @GuardedBy("lock")
    @NonNull
    private Dimensions dimensions = new Dimensions();
    @NonNull
    private final Coordinates coordinates = new Coordinates(dimensions, Color.WHITE);
    @NonNull
    private PlotData plotData = PlotData.create();
    @GuardedBy("lock")
    @NonNull
    private PlottingView view = emptyView;
    @GuardedBy("lock")
    private boolean d3 = D3;
    @NonNull
    private final Pool<FunctionGraph> pool = new ListPool<>(new ListPool.Callback<FunctionGraph>() {
        @NonNull
        @Override
        public FunctionGraph create() {
            if (is3d()) {
                return FunctionGraph3d.create(Dimensions.empty(), Function0.ZERO, MeshSpec.DEFAULT_POINTS_COUNT);
            } else {
                return FunctionGraph2d.create(Dimensions.empty(), Function0.ZERO, MeshSpec.DEFAULT_POINTS_COUNT);
            }
        }

        @Override
        public void release(@NonNull FunctionGraph mesh) {
            mesh.setFunction(Function0.ZERO);
        }
    });
    // main thread only
    @NonNull
    private final List<Listener> listeners = new ArrayList<>();

    DefaultPlotter(@NonNull Context context) {
        this.context = context;
        this.fontAtlas = new FontAtlas(context);
        set3d(false);
    }

    public void add(@NonNull Mesh mesh) {
        otherMeshesBefore.addMesh(mesh);
        setDirty();
    }

    public void addAfter(@NonNull DoubleBufferMesh<? extends Mesh> mesh) {
        otherMeshesAfter.add((DoubleBufferMesh<Mesh>) mesh);
        setDirty();
    }

    public void add(@NonNull DoubleBufferMesh<? extends Mesh> mesh) {
        otherMeshesBefore.add((DoubleBufferMesh<Mesh>) mesh);
        setDirty();
    }

    @Override
    public void initGl(@NonNull GL11 gl, boolean firstTime) {
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

    private boolean existsNotInitializedMesh(@NonNull Group<Mesh> meshes) {
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
    public void draw(@NonNull GL11 gl, float labelsAlpha) {
        for (DoubleBufferMesh<AxisLabels> label : labels) {
            label.setAlpha(labelsAlpha);
        }
        allMeshes.draw(gl);
    }

    private void updateFunctions() {
        Check.isMainThread();

        final Dimensions dimensions = getDimensions();

        // for each functions we should assign mesh
        // if there are not enough meshes => create new
        // if there are too many meshes => release them
        int i = 0;
        for (PlotFunction function : plotData.functions) {
            if (!function.visible) {
                continue;
            }
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
    public void add(@NonNull Function function) {
        add(PlotFunction.create(function, context));
    }

    @Override
    public void remove(@NonNull Function function) {
        final PlotFunction removedFunction = plotData.remove(function);
        if (removedFunction != null) {
            for (Listener listener : listeners) {
                listener.onFunctionRemoved(removedFunction);
            }
            onFunctionsChanged();
        }
    }

    private void onDimensionsChanged() {
        Check.isMainThread();

        final Dimensions dimensions = getDimensions();
        updateDimensions(dimensions, otherMeshesAfter);
        updateDimensions(dimensions, otherMeshesBefore);
        updateFunctions();
        setDirty();
    }

    private void updateDimensions(@NonNull Dimensions dimensions, @NonNull DoubleBufferGroup<Mesh> meshes) {
        for (DoubleBufferMesh<Mesh> dbm : meshes) {
            final Mesh mesh = dbm.getNext();
            if (mesh instanceof DimensionsAware) {
                ((DimensionsAware) mesh).setDimensions(dimensions);
            }
        }
    }

    private void onFunctionsChanged() {
        updateFunctions();
        setDirty();
        for (Listener listener : listeners) {
            listener.onFunctionsChanged();
        }
    }

    @Override
    public void add(@NonNull PlotFunction function) {
        plotData.add(function.copy());
        for (Listener listener : listeners) {
            listener.onFunctionAdded(function);
        }
        onFunctionsChanged();
    }

    @Override
    public void addAll(@NonNull List<PlotFunction> functions) {
        for (PlotFunction function : functions) {
            plotData.add(function.copy());
            for (Listener listener : listeners) {
                listener.onFunctionAdded(function);
            }
        }
        onFunctionsChanged();
    }

    @Override
    public void remove(@NonNull PlotFunction function) {
        if (plotData.remove(function)) {
            for (Listener listener : listeners) {
                listener.onFunctionRemoved(function);
            }
            onFunctionsChanged();
        }
    }

    @Override
    public void update(int id, @NonNull PlotFunction function) {
        if (plotData.update(id, function.copy())) {
            for (Listener listener : listeners) {
                listener.onFunctionUpdated(id, function);
            }
            onFunctionsChanged();
        }
    }

    @Override
    @NonNull
    public PlotData getPlotData() {
        Check.isMainThread();
        return plotData.copy();
    }

    @Override
    public void attachView(@NonNull PlottingView newView) {
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
                dimensionsChangedRunnable.run(view, this);
            }
            view.set3d(d3);
            for (Listener listener : listeners) {
                listener.onViewAttached(view);
            }
        }
    }

    @Override
    public void detachView(@NonNull PlottingView oldView) {
        Check.isMainThread();
        synchronized (lock) {
            Check.same(oldView, view);
            emptyView.shouldRender = false;
            emptyView.shouldUpdateFunctions = false;
            view = emptyView;
            for (Listener listener : listeners) {
                listener.onViewDetached(oldView);
            }
        }
    }

    private void updateDimensions(@NonNull Dimensions newDimensions, @Nullable Object source) {
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
    public void updateScene(@Nullable Object source, @NonNull Zoom zoom, @NonNull RectSize viewSize, @NonNull RectSizeF sceneSize, @NonNull PointF sceneCenter) {
        synchronized (lock) {
            final Dimensions newDimensions = dimensions.updateScene(viewSize, sceneSize, sceneCenter);
            if (newDimensions != dimensions) {
                updateDimensions(newDimensions, source);
            }
        }
    }

    @Override
    public void updateGraph(@Nullable Object source, @NonNull RectSizeF graphSize, @NonNull PointF graphCenter) {
        synchronized (lock) {
            final Dimensions newDimensions = dimensions.updateGraph(graphSize, graphCenter);
            if (newDimensions != dimensions) {
                updateDimensions(newDimensions, source);
            }
        }
    }

    @Override
    @NonNull
    public Dimensions getDimensions() {
        synchronized (lock) {
            return dimensions.copy();
        }
    }

    @NonNull
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

    @Override
    public void set3d(boolean d3) {
        Check.isMainThread();
        if (set3d0(d3)) {
            while (functionMeshes.size() > 0) {
                final DoubleBufferMesh<FunctionGraph> dbm = functionMeshes.remove(functionMeshes.size() - 1);
                pool.release(dbm.getNext());
            }
            pool.clear();
            otherMeshesAfter.clear();
            otherMeshesBefore.clear();
            if (d3) {
                synchronized (lock) {
                    final Dimensions newDimensions = dimensions.updateGraph(new RectSizeF(dimensions.graph.size.width, Dimensions.Graph.SIZE), new PointF(dimensions.graph.center.x, 0));
                    if (newDimensions != dimensions) {
                        updateDimensions(newDimensions, this);
                    }
                }
            }
            makeSetting(d3);
            updateFunctions();
            setDirty();
            synchronized (lock) {
                view.set3d(d3);
            }
            for (Listener listener : listeners) {
                listener.on3dChanged(d3);
            }
        }
    }

    @Override
    public void addListener(@NonNull Listener listener) {
        Check.isMainThread();
        Check.isTrue(!listeners.contains(listener));
        listeners.add(listener);
    }

    @Override
    public void removeListener(@NonNull Listener listener) {
        Check.isMainThread();
        Check.isTrue(listeners.contains(listener));
        listeners.remove(listener);
    }

    public boolean is2d() {
        return !is3d();
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
        otherMeshesAfter.clear();
        otherMeshesBefore.clear();
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
            addAfter(label);
        }
        //add(new Square(dimensions, new PointF(1, 1)));
        //add(new Circle(dimensions, new PointF(0, 0), 1));
        /*final SceneRect sceneRect = new SceneRect(dimensions);
        sceneRect.setColor(MeshSpec.LightColors.GREEN);
        add(DoubleBufferMesh.wrap(sceneRect, DimensionsAwareSwapper.INSTANCE));*/
        /*if (!d3) {
            coordinates.setColor(gridColor);
            add(coordinates);
        }*/
    }

    @NonNull
    private DoubleBufferMesh<AxisLabels> prepareAxisLabels(@NonNull AxisLabels labels, @NonNull Color color) {
        labels.setColor(color);
        return labels.toDoubleBuffer();
    }

    @NonNull
    private DoubleBufferMesh<Axis> prepareAxis(@NonNull Axis axis, @NonNull Color color, int width) {
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
        public boolean removeCallbacks(@NonNull Runnable runnable) {
            return false;
        }

        @Override
        public boolean post(@NonNull Runnable runnable) {
            if (!shouldUpdateFunctions) {
                shouldUpdateFunctions = dimensionsChangedRunnable == runnable;
            }
            return true;
        }

        @Override
        public void set3d(boolean d3) {
        }

        @Override
        public void onDimensionChanged(@NonNull Dimensions dimensions, @Nullable Object source) {
        }

        @Override
        public void onSizeChanged(@NonNull RectSize viewSize) {
            Check.isTrue(false);
        }

        @Override
        public void addListener(@NonNull Listener listener) {
            Check.isTrue(false);
        }

        @Override
        public void removeListener(@NonNull Listener listener) {
            Check.isTrue(false);
        }
    }

    private class DimensionsChangeNotifier implements Runnable {
        @Nullable
        PlottingView view;
        @Nullable
        private Object source;

        public void run(@NonNull PlottingView view, @Nullable Object source) {
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
            for (Listener listener : listeners) {
                listener.onDimensionsChanged(source);
            }
        }

        public void post(@NonNull PlottingView view, @Nullable Object source) {
            Check.isTrue(Thread.holdsLock(lock));
            this.view = view;
            this.source = source;
            view.removeCallbacks(dimensionsChangedRunnable);
            view.post(dimensionsChangedRunnable);
        }
    }
}