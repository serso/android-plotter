package org.solovyev.android.plotter.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.ValidationResult;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class NewFunctionDialog extends BaseDialogFragment implements View.OnFocusChangeListener, View.OnClickListener {

    @Bind(R.id.fn_name_input)
    TextInputLayout nameInput;
    @Bind(R.id.fn_name_edittext)
    EditText name;
    @Bind(R.id.fn_body_input)
    TextInputLayout bodyInput;
    @Bind(R.id.fn_body_edittext)
    EditText body;
    @Nullable
    PopupWindow keyboardWindow;

    public NewFunctionDialog() {
    }

    @NonNull
    public static NewFunctionDialog create() {
        return new NewFunctionDialog();
    }

    public static int clampSelection(int selection) {
        return selection < 0 ? 0 : selection;
    }

    private static void hideIme(@NonNull View view) {
        final IBinder token = view.getWindowToken();
        if (token != null) {
            InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(token, 0);
        }
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
        body.setOnFocusChangeListener(this);
        body.setOnClickListener(this);
        return view;
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
                imm.showSoftInput(name, InputMethodManager.SHOW_IMPLICIT);

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

    private void tryClose() {
        if (validate()) {
            applyData();
            dismiss();
        }
    }

    private void applyData() {
    }

    private boolean validate() {
        final String body = this.body.getText().toString();
        if (TextUtils.isEmpty(body)) {
            App.setError(bodyInput, "Empty");
            return false;
        }
        final Expression expression = new ExpressionBuilder(body).variables("x", "y").build();
        final ValidationResult validateResult = expression.validate(false);
        if(!validateResult.isValid()) {
            final List<String> errors = validateResult.getErrors();
            App.setError(bodyInput, errors.isEmpty() ? " " : errors.get(0));
            return false;
        }
        App.clearError(bodyInput);
        return true;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (v.getId() == R.id.fn_body_edittext) {
            if (hasFocus) {
                showKeyboard();
            } else {
                hideKeyboard();
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.fn_body_edittext) {
            showKeyboard();
        }
    }

    private void hideKeyboard() {
        if (keyboardWindow == null) {
            return;
        }
        keyboardWindow.dismiss();
        keyboardWindow = null;
    }

    private void showKeyboard() {
        if (keyboardWindow != null) {
            return;
        }
        hideIme(body);
        final LinearLayout view = new LinearLayout(getActivity());
        view.setOrientation(LinearLayout.VERTICAL);
        final int buttonSize = getResources().getDimensionPixelSize(R.dimen.button_size);
        final int keyboardSize = 5 * buttonSize;
        keyboardWindow = new PopupWindow(view, keyboardSize, keyboardSize);
        keyboardWindow.setClippingEnabled(false);
        keyboardWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                keyboardWindow = null;
            }
        });
        // see http://stackoverflow.com/a/4713487/720489
        body.post(new Runnable() {
            @Override
            public void run() {
                if (keyboardWindow == null) {
                    return;
                }
                if (body.getWindowToken() != null) {
                    final int inputWidth = body.getWidth();
                    final int xOff = (inputWidth - keyboardSize) / 2;
                    keyboardWindow.setWidth(keyboardSize);
                    keyboardWindow.showAsDropDown(body, xOff, 0);
                } else {
                    body.postDelayed(this, 50);
                }
            }
        });
        new KeyboardUi(new KeyboardUser()).makeView();
    }

    public static class ShowEvent {
    }

    private class KeyboardUser implements KeyboardUi.User {
        @NonNull
        @Override
        public Context getContext() {
            return getActivity();
        }

        @NonNull
        @Override
        public Resources getResources() {
            return NewFunctionDialog.this.getResources();
        }

        @NonNull
        @Override
        public EditText getEditor() {
            return body;
        }

        @NonNull
        @Override
        public ViewGroup getKeyboard() {
            return (ViewGroup) keyboardWindow.getContentView();
        }

        @Override
        public void insertOperator(char operator) {
            insertOperator(String.valueOf(operator));
        }

        @Override
        public void insertOperator(@NonNull String operator) {
            final int start = clampSelection(body.getSelectionStart());
            final int end = clampSelection(body.getSelectionEnd());
            final Editable e = body.getText();
            e.replace(start, end, getOperator(start, end, e, operator));
        }

        @Override
        public void registerForContextMenu(@NonNull View view) {

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
            final int start = clampSelection(body.getSelectionStart());
            final int end = clampSelection(body.getSelectionEnd());
            final Editable e = body.getText();
            e.replace(start, end, text);
            if (selectionOffset != 0) {
                final int selection = clampSelection(body.getSelectionEnd());
                final int newSelection = selection + selectionOffset;
                if (newSelection >= 0 && newSelection < e.length()) {
                    body.setSelection(newSelection);
                }
            }
        }

        @Override
        public void done() {
            hideKeyboard();
            tryClose();
        }

        @Override
        public void showIme() {
            final InputMethodManager keyboard = (InputMethodManager)
                    getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            keyboard.showSoftInput(getEditor(), InputMethodManager.SHOW_FORCED);
            hideKeyboard();
        }
    }
}