package org.solovyev.android.plotter;

import org.solovyev.android.plotter.meshes.Mesh;

import javax.annotation.Nonnull;
import javax.microedition.khronos.opengles.GL11;
import java.util.List;

/**
 * Contains information about functions to be plotted and meshes to be drawn. This class doesn't do plotting but
 * provides all required data to a {@link org.solovyev.android.plotter.PlottingView} which should be connected to it.
 * Note that {@link org.solovyev.android.plotter.PlottingView} might be attached and detached at any time and this
 * doesn't affect neither functions list nor any other plot data stored in this class. This class also makes sure that
 * all meshes (including functions' graphs) are initialized prior to draw.
 */
public interface Plotter {

	void add(@Nonnull Mesh mesh);
	void add(@Nonnull List<Mesh> meshes);

	void add(@Nonnull Function function);
	void add(@Nonnull PlotFunction function);
	void clearFunctions();

	void initGl(@Nonnull GL11 gl, boolean firstTime);
	void draw(@Nonnull GL11 gl);

	void attachView(@Nonnull PlottingView view);
	void detachView(@Nonnull PlottingView view);
}
