package org.solovyev.android.plotter.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import org.solovyev.android.plotter.Color;
import org.solovyev.android.plotter.Dimensions;
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_dimensions:
				final Dimensions dimensions = plotter.getDimensions();
				final View view = LayoutInflater.from(this).inflate(R.layout.dialog_dimensions, null);
				final EditText xMin = (EditText) view.findViewById(R.id.x_min_edittext);
				xMin.setText(String.format("%.2f", dimensions.graph.rect.left));
				final EditText xMax = (EditText) view.findViewById(R.id.x_max_edittext);
				xMax.setText(String.format("%.2f", dimensions.graph.rect.right));
				final EditText yMin = (EditText) view.findViewById(R.id.y_min_edittext);
				yMin.setText(String.format("%.2f", dimensions.graph.rect.top));
				final EditText yMax = (EditText) view.findViewById(R.id.y_max_edittext);
				yMax.setText(String.format("%.2f", dimensions.graph.rect.bottom));

				final AlertDialog.Builder b = new AlertDialog.Builder(this);
				b.setView(view);
				b.setCancelable(true);
				b.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				});
				b.show();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
}
