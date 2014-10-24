package org.solovyev.android.plotter;

import javax.annotation.Nonnull;
import javax.microedition.khronos.opengles.GL11;

/**
 * Contains information about functions to be plotted and meshes to be drawn. This class doesn't do plotting but
 * provides all required data to a {@link org.solovyev.android.plotter.PlottingView} which should be connected to it.
 * Note that {@link org.solovyev.android.plotter.PlottingView} might be attached and detached at any time and this
 * doesn't affect neither functions list nor any other plot data stored in this class. This class also makes sure that
 * all meshes (including functions' graphs) are initialized prior to draw.
 */
public interface Plotter {

	void add(@Nonnull Function function);
	void add(@Nonnull PlotFunction function);
	void clearFunctions();

	void update(@Nonnull PlotFunction function);


	void initGl(@Nonnull GL11 gl, boolean firstTime);
	void draw(@Nonnull GL11 gl);

	@Nonnull
	PlotData getPlotData();

	void attachView(@Nonnull PlottingView view);
	void detachView(@Nonnull PlottingView view);

	void setDimensions(@Nonnull Dimensions dimensions);
	@Nonnull
	Dimensions getDimensions();

	void updateDimensions(float zoom);

	boolean is3d();

	void set3d(boolean d3);
}
