package org.solovyev.android.plotter;

import android.graphics.PointF;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.microedition.khronos.opengles.GL11;

/**
 * Contains information about functions to be plotted and meshes to be drawn. This class doesn't do plotting but
 * provides all required data to a {@link org.solovyev.android.plotter.PlottingView} which should be connected to it.
 * Note that {@link org.solovyev.android.plotter.PlottingView} might be attached and detached at any time and this
 * doesn't affect neither functions list nor any other plot data stored in this class. This class also makes sure that
 * all meshes (including functions' graphs) are initialized prior to draw.
 */
public interface Plotter {
    boolean D3 = true;

    void add(@Nonnull Function function);

    void add(@Nonnull PlotFunction function);

    void clearFunctions();

    void update(@Nonnull PlotFunction function);

    void initGl(@Nonnull GL11 gl, boolean firstTime);

    void draw(@Nonnull GL11 gl, float labelsAlpha);

    @Nonnull
    PlotData getPlotData();

    void attachView(@Nonnull PlottingView view);

    void detachView(@Nonnull PlottingView view);

    /**
     * @return a copy of dimensions
     */
    @Nonnull
    Dimensions getDimensions();

    // should not be modified!
    @Nonnull
    Dimensions.Scene getSceneDimensions();

    void updateScene(@Nullable Object source, @Nonnull Zoom zoom, @Nonnull RectSize viewSize, @Nonnull RectSizeF sceneSize, @Nonnull PointF sceneCenter);

    void updateGraph(@Nullable Object source, @Nonnull RectSizeF graphSize, @Nonnull PointF graphCenter);

    boolean is3d();

    void set3d(boolean d3);

    void showCoordinates(float x, float y);

    void hideCoordinates();

    void onCameraMoved(float dx, float dy);
}
