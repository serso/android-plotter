package org.solovyev.android.plotter.app;

import android.app.Activity;
import android.os.Bundle;
import org.solovyev.android.plotter.*;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends Activity {

	@Nonnull
	private final List<Function> functions = Arrays.asList(
			new Function0() {
				@Override
				public float evaluate() {
					return 0;
				}
			},
			new Function2() {
				@Override
				public float evaluate(float x, float y) {
					return x * x + y * y;
				}
			},
			new Function1() {
				@Override
				public float evaluate(float x) {
					return x * x;
				}
			}
	);

	private final class MyFunction extends Function {

		@Nonnull
		private volatile Function function = functions.get(0);

		@Override
		public int getArity() {
			return function.getArity();
		}

		@Override
		public float evaluate() {
			return function.evaluate();
		}

		@Override
		public float evaluate(float x) {
			return function.evaluate(x);
		}

		@Override
		public float evaluate(float x, float y) {
			return function.evaluate(x, y);
		}
	}

	@Nonnull
	private PlotView plotView;
	@Nonnull
	private final MyFunction function = new MyFunction();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		plotView = new PlotView(this);
		setContentView(plotView);
		plotView.plot(function);
		plotView.post(new FunctionSwitcher());
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

	private class FunctionSwitcher implements Runnable {
		private int position = -1;

		@Override
		public void run() {
			position++;
			if (position >= functions.size()) {
				position = 0;
			}
			function.function = functions.get(position);
			plotView.setDirty();
			plotView.postDelayed(this, 5000L);
		}
	}
}
