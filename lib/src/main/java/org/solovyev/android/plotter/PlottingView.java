package org.solovyev.android.plotter;

import javax.annotation.Nonnull;

public interface PlottingView {

	void requestRender();

	void zoom(boolean in);

	void resetZoom();
	void resetCamera();

	boolean post(@Nonnull Runnable runnable);

	void set3d(boolean d3);
}
