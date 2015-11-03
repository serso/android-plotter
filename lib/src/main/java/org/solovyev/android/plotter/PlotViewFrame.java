package org.solovyev.android.plotter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

public class PlotViewFrame extends FrameLayout implements PlotView.Listener, View.OnClickListener {
    private static final float ALPHA = 0.8f;
    @NonNull
    private final Handler handler = new Handler();
    @NonNull
    private final List<View> controlViews = new ArrayList<>();
    @NonNull
    private final Runnable controlsShowTimeout = new Runnable() {
        @Override
        public void run() {
            hideControlViews();
        }
    };
    @NonNull
    private PlotView plotView;
    @Nullable
    private Plotter plotter;
    @Nullable
    private Listener listener;

    public PlotViewFrame(Context context) {
        super(context);
    }

    public PlotViewFrame(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public PlotViewFrame(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PlotViewFrame(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        plotView = (PlotView) findViewById(R.id.plot_view);
        Check.isNotNull(plotView);

        addControlView(R.id.plot_zoom_in_button);
        addControlView(R.id.plot_zoom_out_button);
        addControlView(R.id.plot_zoom_reset_button);
        addControlView(R.id.plot_3d_button);
        addControlView(R.id.plot_dimensions);
        addControlView(R.id.plot_functions);

        plotView.addListener(this);
    }

    public void setPlotter(@NonNull Plotter plotter) {
        this.plotter = plotter;
        this.plotView.setPlotter(plotter);
    }

    @Nullable
    protected View addControlView(@IdRes int viewId) {
        final View view = findViewById(viewId);
        if (view == null) {
            return null;
        }
        view.setOnClickListener(this);
        view.setVisibility(GONE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            view.setAlpha(0f);
        }
        controlViews.add(view);
        return view;
    }

    @Override
    public Bundle onSaveInstanceState() {
        final Bundle state = new Bundle();
        state.putParcelable("super", super.onSaveInstanceState());
        state.putParcelable("plotview", plotView.onSaveInstanceState());
        return state;
    }

    @Override
    public void onRestoreInstanceState(@android.support.annotation.Nullable Parcelable in) {
        if (in instanceof Bundle) {
            final Bundle state = (Bundle) in;
            in = state.getParcelable("super");
            plotView.onRestoreInstanceState(state.getParcelable("plotview"));
        }

        super.onRestoreInstanceState(in);
    }

    @Override
    public void onTouchStarted() {
        prolongControlsViewShow();
        showControlViews();
    }

    private void prolongControlsViewShow() {
        handler.removeCallbacks(controlsShowTimeout);
        handler.postDelayed(controlsShowTimeout, 5000L);
    }

    private void showControlViews() {
        for (View controlView : controlViews) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
                fadeInControl(controlView);
            } else {
                controlView.setVisibility(VISIBLE);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    private void fadeInControl(@NonNull View view) {
        view.animate().alpha(ALPHA).setListener(null);
        view.setVisibility(VISIBLE);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    private void fadeOutControl(@NonNull final View view) {
        view.animate().alpha(0f).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                view.setVisibility(GONE);
            }
        });
    }

    private void hideControlViews() {
        for (View controlView : controlViews) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
                fadeOutControl(controlView);
            } else {
                controlView.setVisibility(GONE);
            }
        }
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        prolongControlsViewShow();
        if (listener != null) {
            if (listener.onButtonPressed(id)) {
                return;
            }
        }
        if (id == R.id.plot_zoom_in_button) {
            plotView.zoom(true);
        } else if (id == R.id.plot_zoom_out_button) {
            plotView.zoom(false);
        } else if (id == R.id.plot_zoom_reset_button) {
            plotView.resetCamera();
            plotView.resetZoom();
        } else if (id == R.id.plot_3d_button) {
            if (plotter == null) {
                return;
            }
            plotter.set3d(!plotter.is3d());
        }
    }

    public void onPause() {
        plotView.onPause();
    }

    public void onResume() {
        plotView.onResume();
    }

    public void setListener(@Nullable Listener listener) {
        this.listener = listener;
    }

    public interface Listener {
        boolean onButtonPressed(@IdRes int id);
    }
}
