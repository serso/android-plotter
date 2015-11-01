package org.solovyev.android.plotter.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import butterknife.Bind;
import butterknife.ButterKnife;
import org.solovyev.android.plotter.*;
import org.solovyev.android.plotter.views.PlotViewFrame;

import javax.annotation.Nonnull;

public class MainActivity extends Activity implements PlotViewFrame.Listener {

	@Bind(R.id.plot_view_frame)
	PlotViewFrame plotView;

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
		ButterKnife.bind(this);

		plotView.setPlotter(plotter);
		plotView.setListener(this);
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
	public boolean onButtonPressed(@IdRes int id) {
		if (id == R.id.plot_dimensions) {
			final Dimensions dimensions = plotter.getDimensions();
			final DimensionsDialog dialog = new DimensionsDialog(this, dimensions.graph.makeBounds(), plotter.is3d());
			dialog.show();
			return true;
		} else if (id == R.id.plot_functions) {
			final FunctionsDialog dialog = new FunctionsDialog(this, plotter.getPlotData());
			dialog.show();
			return true;
		}
		return false;
	}

	public class DimensionsDialog {

		@Nonnull
		protected final View view;
		@Bind(R.id.x_min_edittext)
		EditText xMin;
		@Bind(R.id.x_max_edittext)
		EditText xMax;
		@Bind(R.id.y_min_edittext)
		EditText yMin;
		@Bind(R.id.y_max_edittext)
		EditText yMax;
		@Bind(R.id.y_bounds)
		View yBounds;
		private final boolean d3;

		protected DimensionsDialog(@Nonnull Context context, @Nonnull RectF graph, boolean d3) {
			this.d3 = d3;
			view = LayoutInflater.from(context).inflate(R.layout.dialog_dimensions, null);
			ButterKnife.bind(this, view);

			setDimension(xMin, graph.left);
			setDimension(xMax, graph.right);
			setDimension(yMin, graph.top);
			setDimension(yMax, graph.bottom);
			if (d3) {
				yBounds.setVisibility(View.GONE);
			}
		}

		private void setDimension(@Nonnull EditText view, float value) {
			view.setText(String.format("%.2f", value));
		}

		public void show() {
			final AlertDialog.Builder b = new AlertDialog.Builder(view.getContext());
			b.setView(view);
			b.setCancelable(true);
			b.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					final RectF graph = new RectF(getDimension(xMin), getDimension(yMin), getDimension(xMax), getDimension(yMax));
					if (graph.isEmpty()) {
						new DimensionsDialog(MainActivity.this, graph, d3).show();
						return;
					}
					if (!d3) {
						plotter.updateGraph(null, new RectSizeF(graph.width(), graph.height()), new PointF(graph.centerX(), graph.centerY()));
					} else {
						final Dimensions dimensions = plotter.getDimensions();
						plotter.updateGraph(null, new RectSizeF(graph.width(), dimensions.graph.height()), new PointF(graph.centerX(), dimensions.graph.center.y));
					}
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
