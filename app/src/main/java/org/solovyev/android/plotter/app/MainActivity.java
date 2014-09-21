package org.solovyev.android.plotter.app;

import android.app.Activity;
import android.os.Bundle;
import org.solovyev.android.plotter.Function0;
import org.solovyev.android.plotter.Function1;
import org.solovyev.android.plotter.Function2;
import org.solovyev.android.plotter.PlotView;

import javax.annotation.Nonnull;

public class MainActivity extends Activity {

	@Nonnull
	private PlotView plotView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		plotView = new PlotView(this);
		plotView.plot(new Function0() {
			@Override
			public float evaluate() {
				return 10;
			}
		});
		plotView.plot(new Function1() {
			@Override
			public float evaluate(float x) {
				return x;
			}
		});
		plotView.plot(new Function2() {
			@Override
			public float evaluate(float x, float y) {
				return x * x + y * y;
			}
		});
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
