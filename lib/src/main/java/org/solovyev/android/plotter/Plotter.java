package org.solovyev.android.plotter;

import org.solovyev.android.plotter.meshes.Mesh;

import javax.annotation.Nonnull;
import javax.microedition.khronos.opengles.GL11;
import java.util.List;

public interface Plotter {

	void add(@Nonnull Mesh mesh);
	void add(@Nonnull List<Mesh> meshes);

	void add(@Nonnull Function function);
	void add(@Nonnull PlotFunction function);
	void clearFunctions();

	void initGl(@Nonnull GL11 gl, boolean firstTime);
	void draw(@Nonnull GL11 gl);

	void setDirty();

	void attachView(@Nonnull PlottingView view);
	void detachView(@Nonnull PlottingView view);
}
