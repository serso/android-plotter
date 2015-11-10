package org.solovyev.android.plotter.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import butterknife.Bind;
import butterknife.ButterKnife;

public class NewFunctionDialog extends BaseDialogFragment implements KeyboardUi.User {

    @Bind(R.id.editor)
    EditText editor;
    @Bind(R.id.keyboard)
    ViewGroup keyboard;


    public NewFunctionDialog() {
    }

    @NonNull
    public static NewFunctionDialog create() {
        return new NewFunctionDialog();
    }

    public static int clampSelection(int selection) {
        return selection < 0 ? 0 : selection;
    }

    @Override
    protected void onPrepareDialog(@NonNull AlertDialog.Builder builder) {
        builder.setTitle("Function");
        builder.setPositiveButton(android.R.string.ok, null);
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    protected View onCreateDialogView(@NonNull Context context, @NonNull LayoutInflater inflater) {
        final View view = inflater.inflate(R.layout.dialog_new_function, null);
        ButterKnife.bind(this, view);
        new KeyboardUi(this).makeView();
        return view;
    }

    @NonNull
    @Override
    public EditText getEditor() {
        return editor;
    }

    @NonNull
    @Override
    public ViewGroup getKeyboard() {
        return keyboard;
    }

    @Override
    public void insertOperator(char operator) {
        insertOperator(String.valueOf(operator));
    }

    @Override
    public void insertOperator(@NonNull String operator) {
        final int start = clampSelection(editor.getSelectionStart());
        final int end = clampSelection(editor.getSelectionEnd());
        final Editable e = editor.getText();
        e.replace(start, end, getOperator(start, end, e, operator));
    }

    @NonNull
    private String getOperator(int start, int end, @NonNull Editable e, @NonNull CharSequence operator) {
        boolean spaceBefore = true;
        boolean spaceAfter = true;
        if (start > 0 && Character.isSpaceChar(e.charAt(start - 1))) {
            spaceBefore = false;
        }
        if (end < e.length() && Character.isSpaceChar(e.charAt(end))) {
            spaceAfter = false;
        }

        if (spaceBefore && spaceAfter) {
            return " " + operator + " ";
        }
        if (spaceBefore) {
            return " " + operator;
        }
        if (spaceAfter) {
            return operator + " ";
        }
        return String.valueOf(operator);
    }

    @Override
    public void openContextMenu(@NonNull View view) {
        view.showContextMenu();
    }

    @Override
    public void insertText(@NonNull CharSequence text, int selectionOffset) {
        final int start = clampSelection(editor.getSelectionStart());
        final int end = clampSelection(editor.getSelectionEnd());
        final Editable e = editor.getText();
        e.replace(start, end, text);
        if (selectionOffset != 0) {
            final int selection = clampSelection(editor.getSelectionEnd());
            final int newSelection = selection + selectionOffset;
            if (newSelection >= 0 && newSelection < e.length()) {
                editor.setSelection(newSelection);
            }
        }
    }
}
