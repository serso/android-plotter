package org.solovyev.android.plotter.app;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import org.solovyev.android.plotter.Dimensions;
import org.solovyev.android.plotter.PlotViewFrame;
import org.solovyev.android.plotter.Plotter;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends FragmentActivity implements PlotViewFrame.Listener {

    @NonNull
    private final Plotter plotter = App.getPlotter();
    @NonNull
    private final EventHandler eventHandler = new EventHandler();
    @Bind(R.id.plot_view_frame)
    PlotViewFrame plotView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
        ButterKnife.bind(this);
        App.getBus().register(eventHandler);

        plotView.addControlView(R.id.plot_add_function);
        plotView.addControlView(R.id.plot_functions);
        plotView.addControlView(R.id.plot_dimensions);
        plotView.setPlotter(plotter);
        plotView.setListener(this);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle out) {
        super.onSaveInstanceState(out);
        out.putBundle("plotview", plotView.onSaveInstanceState());
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle in) {
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
    protected void onDestroy() {
        App.getBus().unregister(eventHandler);
        super.onDestroy();
    }

    @Override
    public boolean onButtonPressed(@IdRes int id) {
        if (id == R.id.plot_dimensions) {
            final Dimensions dimensions = plotter.getDimensions();
            App.getBus().post(new DimensionsDialog.ShowEvent(dimensions.graph.makeBounds(), plotter.is3d()));
            return true;
        } else if (id == R.id.plot_functions) {
            App.getBus().post(new FunctionsDialog.ShowEvent());
            return true;
        } else if (id == R.id.plot_add_function) {
            App.getBus().post(new AddFunctionDialog.ShowEvent());
            return true;
        }
        return false;
    }

    @Override
    public void unableToZoom(boolean in) {
        Toast.makeText(this, "Can't zoom anymore", Toast.LENGTH_SHORT).show();
    }

    public class EventHandler {

        @NonNull
        private final MainActivity activity = MainActivity.this;

        @Subscribe
        public void onShowAddFunction(@NonNull AddFunctionDialog.ShowEvent e) {
            AddFunctionDialog.create().show(getSupportFragmentManager(), null);
        }

        @Subscribe
        public void onShowEditFunction(@NonNull EditFunctionDialog.ShowEvent e) {
            EditFunctionDialog.create(e.function).show(getSupportFragmentManager(), null);
        }

        @Subscribe
        public void onShowDimensionsDialog(@NonNull DimensionsDialog.ShowEvent e) {
            DimensionsDialog.create(e.bounds, e.d3).show(getSupportFragmentManager(), null);
        }

        @Subscribe
        public void onShowFunctionsDialog(@NonNull FunctionsDialog.ShowEvent e) {
            FunctionsDialog.create().show(getSupportFragmentManager(), null);
        }
    }
}
