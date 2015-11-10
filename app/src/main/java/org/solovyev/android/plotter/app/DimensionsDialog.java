package org.solovyev.android.plotter.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLabel;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.solovyev.android.plotter.Check;
import org.solovyev.android.plotter.Dimensions;
import org.solovyev.android.plotter.Plot;
import org.solovyev.android.plotter.Plotter;
import org.solovyev.android.plotter.RectSizeF;
import org.solovyev.android.plotter.utils.Fragments;

import butterknife.Bind;
import butterknife.ButterKnife;

public class DimensionsDialog extends BaseDialogFragment implements TextView.OnEditorActionListener {
    private static final String ARG_BOUNDS = "arg-bounds";
    private static final String ARG_3D = "arg-3d";
    @NonNull
    private final Plotter plotter = App.getPlotter();

    @Bind(R.id.x_min_edittext)
    EditText xMin;
    @Bind(R.id.x_min_textinput)
    TextInputLabel xMinTextInput;

    @Bind(R.id.x_max_edittext)
    EditText xMax;
    @Bind(R.id.x_max_textinput)
    TextInputLabel xMaxTextInput;

    @Bind(R.id.y_min_edittext)
    EditText yMin;
    @Bind(R.id.y_min_textinput)
    TextInputLabel yMinTextInput;

    @Bind(R.id.y_max_edittext)
    EditText yMax;
    @Bind(R.id.y_max_textinput)
    TextInputLabel yMaxTextInput;

    @Bind(R.id.y_bounds)
    View yBounds;

    @NonNull
    private RectF bounds = new RectF();
    private boolean d3;

    public DimensionsDialog() {
    }

    @NonNull
    public static DimensionsDialog create(@NonNull RectF bounds, boolean d3) {
        final DimensionsDialog dialog = new DimensionsDialog();
        final Bundle args = new Bundle();
        args.putParcelable(ARG_BOUNDS, bounds);
        args.putBoolean(ARG_3D, d3);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle arguments = getArguments();
        Check.isNotNull(arguments);
        bounds = Fragments.getParcelable(arguments, ARG_BOUNDS);
        d3 = arguments.getBoolean(ARG_3D);
    }

    @NonNull
    @Override
    public AlertDialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface d) {
                final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(xMin, InputMethodManager.SHOW_IMPLICIT);

                final Button ok = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        tryClose();
                    }
                });
            }
        });
        return dialog;
    }

    @Override
    protected void onPrepareDialog(@NonNull AlertDialog.Builder builder) {
        builder.setTitle("Dimensions");
        builder.setPositiveButton(android.R.string.ok, null);
    }

    @NonNull
    @Override
    protected View onCreateDialogView(@NonNull Context context, @NonNull LayoutInflater inflater) {
        @SuppressLint("InflateParams") final View view = LayoutInflater.from(context).inflate(R.layout.dialog_dimensions, null);
        ButterKnife.bind(this, view);

        setDimension(xMin, bounds.left);
        setDimension(xMax, bounds.right);
        setDimension(yMin, bounds.top);
        setDimension(yMax, bounds.bottom);
        xMin.addTextChangedListener(new MyTextWatcher(xMinTextInput, true));
        xMax.addTextChangedListener(new MyTextWatcher(xMaxTextInput, true));
        yMin.addTextChangedListener(new MyTextWatcher(yMinTextInput, false));
        yMax.addTextChangedListener(new MyTextWatcher(yMaxTextInput, false));
        if (d3) {
            yBounds.setVisibility(View.GONE);
        }
        return view;
    }

    private void tryClose() {
        if (validate()) {
            applyData();
            dismiss();
        }
    }

    private boolean validate() {
        final RectF bounds = collectData();
        if (!validXBounds(bounds) | !validYBounds(bounds)) {
            return false;
        }
        return true;
    }

    private void clearError(@NonNull TextInputLabel textInput) {
        textInput.setError(null);
        textInput.setErrorEnabled(false);
    }

    private void setError(@NonNull TextInputLabel textInput, @NonNull String error) {
        textInput.setError(error);
        textInput.setErrorEnabled(true);
    }

    private boolean validYBounds(@NonNull RectF bounds) {
        if (validNumbers(this.bounds.top, this.bounds.bottom, yMinTextInput, yMaxTextInput)) {
            return false;
        }
        if (bounds.top >= bounds.bottom) {
            setError(yMinTextInput, " ");
            setError(yMaxTextInput, "max ≯ min");
            return false;
        }
        clearError(yMinTextInput);
        clearError(yMaxTextInput);
        return true;
    }

    private boolean validXBounds(@NonNull RectF bounds) {
        if (validNumbers(bounds.left, bounds.right, xMinTextInput, xMaxTextInput)) {
            return false;
        }
        if (bounds.left >= bounds.right) {
            setError(xMinTextInput, " ");
            setError(xMaxTextInput, "max ≯ min");
            return false;
        }
        clearError(xMinTextInput);
        clearError(xMaxTextInput);
        return true;
    }

    private boolean validNumbers(float l, float r, @NonNull TextInputLabel lInput, @NonNull TextInputLabel rInput) {
        final boolean nanLeft = Float.isNaN(l);
        final boolean nanRight = Float.isNaN(r);
        if (nanLeft || nanRight) {
            if (nanLeft) {
                setError(lInput, " ");
            } else {
                clearError(lInput);
            }
            if (nanRight) {
                setError(rInput, " ");
            } else {
                clearError(rInput);
            }
            return true;
        }
        return false;
    }

    @NonNull
    private RectF collectData() {
        return new RectF(getDimension(xMin), getDimension(yMin), getDimension(xMax), getDimension(yMax));
    }

    private void applyData() {
        final RectF bounds = collectData();
        if (!d3) {
            plotter.updateGraph(null, new RectSizeF(bounds.width(), bounds.height()), new PointF(bounds.centerX(), bounds.centerY()));
        } else {
            final Dimensions dimensions = plotter.getDimensions();
            plotter.updateGraph(null, new RectSizeF(bounds.width(), dimensions.graph.height()), new PointF(bounds.centerX(), dimensions.graph.center.y));
        }
    }

    private void setDimension(@NonNull EditText view, float value) {
        view.setOnEditorActionListener(this);
        view.setText(String.format("%.2f", value));
    }

    private float getDimension(@NonNull EditText view) {
        try {
            return Float.parseFloat(view.getText().toString().replace(",", ".").replace("−", "-"));
        } catch (NumberFormatException e) {
            Log.w(Plot.getTag("MainActivity"), e.getMessage(), e);
            return Float.NaN;
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            tryClose();
            return true;
        }
        return false;
    }

    public static final class ShowEvent {
        @NonNull
        public final RectF bounds;
        public final boolean d3;

        public ShowEvent(@NonNull RectF bounds, boolean d3) {
            this.bounds = bounds;
            this.d3 = d3;
        }
    }

    private class MyTextWatcher implements TextWatcher {
        @NonNull
        private final TextInputLabel input;
        private final boolean x;

        private MyTextWatcher(@NonNull TextInputLabel input, boolean x) {
            this.input = input;
            this.x = x;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (TextUtils.isEmpty(input.getError())) {
                return;
            }

            final RectF bounds = collectData();
            if (x) {
                validXBounds(bounds);
            } else {
                validYBounds(bounds);
            }
        }
    }
}