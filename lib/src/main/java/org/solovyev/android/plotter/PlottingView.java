package org.solovyev.android.plotter;

import javax.annotation.Nonnull;

public interface PlottingView {

	void requestRender();

	void zoom(boolean in);

	boolean post(@Nonnull Runnable runnable);
}
