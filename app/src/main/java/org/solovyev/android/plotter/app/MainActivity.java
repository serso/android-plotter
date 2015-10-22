package org.solovyev.android.plotter.app;

import android.app.Activity;
import android.os.Bundle;

import org.solovyev.android.plotter.Color;
import org.solovyev.android.plotter.PlotData;
import org.solovyev.android.plotter.PlotFunction;
import org.solovyev.android.plotter.Plotter;
import org.solovyev.android.plotter.views.PlotViewFrame;

import javax.annotation.Nonnull;

public class MainActivity extends Activity {

	@Nonnull
	private PlotViewFrame plotView;

	@Nonnull
	private final Plotter plotter = PlotterApplication.get().getPlotter();

	@Nonnull
	private final Runnable colorUpdater = new Runnable() {
		private int direction = -1;
		@Override
		public void run() {
			final PlotData plotData = plotter.getPlotData();
			final PlotFunction function = plotData.get(PlotterApplication.PARABOLOID);
			if(function == null) {
				return;
			}
			final Color color = function.meshSpec.color;
			if (color.equals(Color.BLACK) || color.equals(Color.RED)) {
				direction = -direction;
			}
			function.meshSpec.color = color.add(direction * 0.01f, 0, 0);
			plotter.update(function);
			plotView.postDelayed(this, 10L);
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);
		plotView = (PlotViewFrame) findViewById(R.id.plot_view_frame);
		plotView.setPlotter(plotter);
	}

	@Override
	protected void onSaveInstanceState(@Nonnull Bundle out) {
		super.onSaveInstanceState(out);
		out.putBundle("plotview", plotView.onSaveInstanceState());
	}

	@Override
	protected void onRestoreInstanceState(@Nonnull Bundle in) {
		super.onRestoreInstanceState(in);
		final Bundle plotviewState = in.getBundle("plotview");
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
