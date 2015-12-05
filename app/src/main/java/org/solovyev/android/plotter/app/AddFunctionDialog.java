package org.solovyev.android.plotter.app;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;

import org.solovyev.android.plotter.PlotFunction;
import org.solovyev.android.plotter.math.ExpressionFunction;

public class AddFunctionDialog extends FunctionDialog {
    @NonNull
    public static AddFunctionDialog create() {
        return new AddFunctionDialog();
    }

    protected void applyData() {
        final ExpressionFunction function = ExpressionFunction.createNamed(getName(), getBody(), "x", "y");
        App.getPlotter().add(PlotFunction.create(function, applyMeshSpec()));
    }

    @NonNull
    @Override
    protected View onCreateDialogView(@NonNull Context context, @NonNull LayoutInflater inflater, Bundle savedInstanceState) {
        final View view = super.onCreateDialogView(context, inflater, savedInstanceState);
        if (savedInstanceState == null) {
            colorPicker.setSelectedColorPosition(0);
        }
        return view;
    }

    public static class ShowEvent {
    }
}
