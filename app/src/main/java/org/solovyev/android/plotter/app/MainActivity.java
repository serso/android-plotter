package org.solovyev.android.plotter.app;

import android.app.Activity;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import org.solovyev.android.plotter.*;

import javax.annotation.Nonnull;

public class MainActivity extends Activity {

	@Nonnull
	private PlotView plotView;

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
		plotView = (PlotView) findViewById(R.id.plotview);
		plotView.setPlotter(plotter);

		final View zoomOutButton = findViewById(R.id.zoom_out_button);
		zoomOutButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				plotView.zoom(false);
			}
		});
		final View zoom0Button = findViewById(R.id.zoom_0_button);
		zoom0Button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				plotView.resetCamera();
				plotView.resetZoom();
			}
		});
		final View zoomInButton = findViewById(R.id.zoom_in_button);
		zoomInButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				plotView.zoom(true);
			}
		});
		final View plotModeButton = findViewById(R.id.plot_mode_button);
		plotModeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				plotter.set3d(!plotter.is3d());
			}
		});


		//plotView.post(colorUpdater);
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
