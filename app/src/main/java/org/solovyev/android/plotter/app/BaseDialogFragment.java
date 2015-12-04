package org.solovyev.android.plotter.app;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

public abstract class BaseDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public AlertDialog onCreateDialog(Bundle savedInstanceState) {
        final Context context = getActivity();
        final LayoutInflater inflater = LayoutInflater.from(context);
        final View view = onCreateDialogView(context, inflater, savedInstanceState);
        final int spacing = context.getResources().getDimensionPixelSize(R.dimen.dialog_spacing);
        final AlertDialog.Builder b = new AlertDialog.Builder(context);
        b.setView(view, spacing, spacing, spacing, spacing);
        onPrepareDialog(b);
        return b.create();
    }

    protected abstract void onPrepareDialog(@NonNull AlertDialog.Builder builder);
    @NonNull
    protected abstract View onCreateDialogView(@NonNull Context context, @NonNull LayoutInflater inflater, @Nullable Bundle savedInstanceState);
}
