package org.solovyev.android.plotter.app;

import android.app.Activity;
import android.os.Bundle;
import android.os.Parcelable;
import org.solovyev.android.plotter.PlotView;

import javax.annotation.Nonnull;

public class MainActivity extends Activity {

	@Nonnull
	private PlotView plotView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		plotView = new PlotView(this);
		plotView.setPlotter(PlotterApplication.get().getPlotter());
		setContentView(plotView);
	}

	@Override
	protected void onSaveInstanceState(@Nonnull Bundle out) {
		super.onSaveInstanceState(out);
		final Parcelable plotViewState = plotView.onSaveInstanceState();
		out.putParcelable("plotview", plotViewState);
	}

	@Override
	protected void onRestoreInstanceState(@Nonnull Bundle in) {
		super.onRestoreInstanceState(in);
		final Parcelable plotviewState = in.getParcelable("plotview");
		if (plotviewState != null) {
			plotView.onRestoreInstanceState(plotviewState);
		}
	}

	@Override
	protected void onPause() {
		plotView.onPause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		plotView.onResume();
	}
}
