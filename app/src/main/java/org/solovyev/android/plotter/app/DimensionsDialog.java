package org.solovyev.android.plotter.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import org.solovyev.android.plotter.Dimensions;
import org.solovyev.android.plotter.Plot;
import org.solovyev.android.plotter.Plotter;
import org.solovyev.android.plotter.RectSizeF;

import javax.annotation.Nonnull;

import butterknife.Bind;
import butterknife.ButterKnife;

public class DimensionsDialog implements TextView.OnEditorActionListener {
	@Nonnull
	private final Plotter plotter = App.getPlotter();
	@Nonnull
	private final AlertDialog dialog;
	private final boolean d3;
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
	protected DimensionsDialog(@Nonnull final Context context, @Nonnull RectF graph, final boolean d3) {
		this.d3 = d3;
		final View view = LayoutInflater.from(context).inflate(R.layout.dialog_dimensions, null);
		ButterKnife.bind(this, view);

		setDimension(xMin, graph.left);
		setDimension(xMax, graph.right);
		setDimension(yMin, graph.top);
		setDimension(yMax, graph.bottom);
		if (d3) {
			yBounds.setVisibility(View.GONE);
		}

		final AlertDialog.Builder b = new AlertDialog.Builder(context);
		b.setView(view);
		b.setCancelable(true);
		b.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				close(d3);
			}
		});
		dialog = b.create();
		dialog.setOnShowListener(new DialogInterface.OnShowListener() {
			@Override
			public void onShow(DialogInterface dialog) {
				final InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(xMin, InputMethodManager.SHOW_IMPLICIT);
			}
		});
	}

	private void close(boolean d3) {
		final RectF graph = new RectF(getDimension(xMin), getDimension(yMin), getDimension(xMax), getDimension(yMax));
		if (graph.isEmpty()) {
			App.getBus().post(new ShowEvent(graph, d3));
			return;
		}
		if (!d3) {
			plotter.updateGraph(null, new RectSizeF(graph.width(), graph.height()), new PointF(graph.centerX(), graph.centerY()));
		} else {
			final Dimensions dimensions = plotter.getDimensions();
			plotter.updateGraph(null, new RectSizeF(graph.width(), dimensions.graph.height()), new PointF(graph.centerX(), dimensions.graph.center.y));
		}
	}

	private void setDimension(@Nonnull EditText view, float value) {
		view.setOnEditorActionListener(this);
		view.setText(String.format("%.2f", value));
	}

	public void show() {
		dialog.show();
	}

	private float getDimension(@Nonnull EditText view) {
		try {
			return Float.parseFloat(view.getText().toString().replace(",", ".").replace("−", "-"));
		} catch (NumberFormatException e) {
			Log.e(Plot.getTag("MainActivity"), e.getMessage(), e);
			return 0f;
		}
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if (actionId == EditorInfo.IME_ACTION_DONE) {
			dialog.dismiss();
			close(d3);
			return true;
		}
		return false;
	}

	public static final class ShowEvent {
		@Nonnull
		public final RectF graph;
		public final boolean d3;

		public ShowEvent(@Nonnull RectF graph, boolean d3) {
			this.graph = graph;
			this.d3 = d3;
		}
	}
}
