package org.solovyev.android.plotter.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.EditText;
import org.solovyev.android.plotter.*;
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
				final DimensionsDialog dialog = new DimensionsDialog(this, plotter.getDimensions());
				dialog.show();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private class DimensionsDialog {

		@Nonnull
		protected final View view;
		@Nonnull
		protected final EditText xMin;
		@Nonnull
		protected final EditText xMax;
		@Nonnull
		protected final EditText yMin;
		@Nonnull
		protected final EditText yMax;

		protected DimensionsDialog(@Nonnull Context context, @Nonnull Dimensions dimensions) {
			this.view = LayoutInflater.from(context).inflate(R.layout.dialog_dimensions, null);
			this.xMin = (EditText) view.findViewById(R.id.x_min_edittext);
			this.xMax = (EditText) view.findViewById(R.id.x_max_edittext);
			this.yMin = (EditText) view.findViewById(R.id.y_min_edittext);
			this.yMax = (EditText) view.findViewById(R.id.y_max_edittext);

			setDimension(xMin, dimensions.graph.xMin());
			setDimension(xMax, dimensions.graph.xMax());
			setDimension(yMin, dimensions.graph.yMin());
			setDimension(yMax, dimensions.graph.yMax());
		}

		private void setDimension(@Nonnull EditText view, float value) {
			view.setText(String.format("%.2f", value));
		}

		public void show() {
			final AlertDialog.Builder b = new AlertDialog.Builder(view.getContext());
			b.setView(view);
			b.setCancelable(true);
			b.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					final RectF graph = new RectF(getDimension(xMin), getDimension(yMin), getDimension(xMax), getDimension(yMax));
					plotter.updateGraph(null, new RectSizeF(graph.width(), graph.height()), new PointF(graph.centerX(), graph.centerY()));
				}
			});
			b.show();
		}

		private float getDimension(@Nonnull EditText view) {
			try {
				return Float.parseFloat(view.getText().toString().replace(",", ".").replace("−", "-"));
			} catch (NumberFormatException e) {
				Log.e(Plot.getTag("MainActivity"), e.getMessage(), e);
				return 0f;
			}
		}
	}
}
