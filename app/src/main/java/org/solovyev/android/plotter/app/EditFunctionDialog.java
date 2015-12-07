package org.solovyev.android.plotter.app;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;

import org.solovyev.android.plotter.Check;
import org.solovyev.android.plotter.Function;
import org.solovyev.android.plotter.PlotData;
import org.solovyev.android.plotter.PlotFunction;
import org.solovyev.android.plotter.math.ExpressionFunction;
import org.solovyev.android.plotter.meshes.MeshSpec;

public class EditFunctionDialog extends FunctionDialog {
    private static final String ARGS_FUNCTION = "function";
    private PlotFunction plotFunction;

    @NonNull
    public static EditFunctionDialog create(@NonNull PlotFunction function) {
        final EditFunctionDialog fragment = new EditFunctionDialog();
        final Bundle args = new Bundle();
        args.putInt(ARGS_FUNCTION, function.function.getId());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle args = getArguments();
        final int functionId = args.getInt(ARGS_FUNCTION, Function.NO_ID);
        final PlotData plotData = App.getPlotter().getPlotData();
        if (functionId != Function.NO_ID) {
            plotFunction = plotData.get(functionId);
        }

        if (plotFunction == null) {
            dismiss();
        }
    }

    @NonNull
    @Override
    protected View onCreateDialogView(@NonNull Context context, @NonNull LayoutInflater inflater, @Nullable Bundle savedInstanceState) {
        final View view = super.onCreateDialogView(context, inflater, savedInstanceState);
        if (savedInstanceState == null) {
            final ExpressionFunction function = getFunction();
            nameEditText.setText(function.hasName() ? function.getName() : null);
            bodyEditText.setText(function.getExpressionString());
            final MeshSpec meshSpec = plotFunction.meshSpec;
            final int color = meshSpec.color.toInt();
            final int[] colors = colorPicker.getColors();
            final int i = indexOf(colors, color);
            colorPicker.setSelectedColorPosition(Math.max(0, i));
            lineWidthSeekBar.setProgress(meshSpec.width - MeshSpec.MIN_WIDTH);
            iconView.setMeshSpec(plotFunction.meshSpec);
        }
        return view;
    }

    private static int indexOf(int[] itegers, int integer) {
        for (int i = 0; i < itegers.length; i++) {
            if(itegers[i] == integer) {
                return i;
            }
        }
        return -1;
    }

    @NonNull
    private ExpressionFunction getFunction() {
        return (ExpressionFunction) plotFunction.function;
    }

    protected void applyData() {
        final ExpressionFunction function = ExpressionFunction.createNamed(getName(), getBody(), "x", "y");
        App.getPlotter().update(getFunction().getId(), PlotFunction.create(function, applyMeshSpec()));
    }

    public static class ShowEvent {
        @NonNull
        public final PlotFunction function;

        private ShowEvent(@NonNull PlotFunction function) {
            Check.isTrue(function.function instanceof ExpressionFunction);
            this.function = function;
        }

        @Nullable
        public static ShowEvent tryCreate(@NonNull PlotFunction function) {
            if (function.function instanceof ExpressionFunction) {
                return new ShowEvent(function);
            }
            return null;
        }
    }
}
