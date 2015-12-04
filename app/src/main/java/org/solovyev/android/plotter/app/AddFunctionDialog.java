package org.solovyev.android.plotter.app;

import android.support.annotation.NonNull;

import org.solovyev.android.plotter.math.ExpressionFunction;

public class AddFunctionDialog extends FunctionDialog {
    @NonNull
    public static AddFunctionDialog create() {
        return new AddFunctionDialog();
    }

    protected void applyData() {
        App.getPlotter().add(ExpressionFunction.createNamed(getName(), getBody(), "x", "y"));
    }

    public static class ShowEvent {
    }
}
