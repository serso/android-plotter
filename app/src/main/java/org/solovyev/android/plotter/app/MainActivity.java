package org.solovyev.android.plotter.app;

import android.app.Activity;
import android.os.Bundle;
import org.solovyev.android.plotter.PlotView;

import javax.annotation.Nonnull;

public class MainActivity extends Activity {

	@Nonnull
	private PlotView plotView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		plotView = new PlotView(this);
		setContentView(plotView);
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
