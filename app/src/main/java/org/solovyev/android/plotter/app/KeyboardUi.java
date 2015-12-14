package org.solovyev.android.plotter.app;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.solovyev.android.drag.DirectionDragButton;
import org.solovyev.android.drag.DragButton;
import org.solovyev.android.drag.DragDirection;
import org.solovyev.android.drag.SimpleDragListener;
import org.solovyev.android.views.LongClickEraser;

import static org.solovyev.android.drag.DirectionDragButton.Direction.down;
import static org.solovyev.android.drag.DirectionDragButton.Direction.up;

public class KeyboardUi {
    @NonNull
    private final ButtonHandler buttonHandler = new ButtonHandler();
    @NonNull
    private final User user;
    @NonNull
    private final SimpleDragListener dragListener;
    private final int textColor;
    private final int textColorSecondary;
    private final int sidePadding;

    @SuppressWarnings("deprecation")
    public KeyboardUi(@NonNull User user) {
        this.user = user;
        this.dragListener = new SimpleDragListener(buttonHandler, user.getContext());
        final Resources resources = user.getResources();
        textColor = resources.getColor(R.color.kb_button_text);
        textColorSecondary = resources.getColor(R.color.kb_button_text_secondary);
        sidePadding = resources.getDimensionPixelSize(R.dimen.button_side_padding);
    }

    public void makeView() {
        LinearLayout row = makeRow();
        addButton(row, 0, "7");
        addButton(row, 0, "8");
        addButton(row, 0, "9").setText("pi", up).setText("e", down);
        addOperationButton(row, R.id.button_multiply, "×").setText("^n", up).setText("^2", down);
        addButton(row, R.id.button_clear, "C");

        row = makeRow();
        addButton(row, 0, "4");
        addButton(row, 0, "5");
        addButton(row, 0, "6");
        addOperationButton(row, R.id.button_divide, "/").setText("%", up).setText("sqrt", down);
        final View backspace = addImageButton(row, R.id.button_backspace, R.drawable.ic_backspace_white_24dp);
        LongClickEraser.createAndAttach(backspace, user.getEditor());

        row = makeRow();
        addButton(row, 0, "1");
        addButton(row, 0, "2");
        addButton(row, 0, "3");
        addOperationButton(row, R.id.button_plus, "+");
        addImageButton(row, R.id.button_space, R.drawable.ic_space_bar_white_24dp);

        row = makeRow();
        addButton(row, R.id.button_brackets, "( )").setText("(", up).setText(")", down);
        addButton(row, 0, "0").setText("00", up).setText("000", down);
        addButton(row, 0, ".").setText(",", up);
        addOperationButton(row, R.id.button_minus, "−");
        addImageButton(row, R.id.button_keyboard, R.drawable.ic_keyboard_white_24dp);

        row = makeRow();
        addButton(row, 0, "x");
        addButton(row, 0, "y");
        addButton(row, R.id.button_functions, "f(x)");
        addButton(row, R.id.button_constants, "π");
        addImageButton(row, R.id.button_close, R.drawable.ic_done_white_24dp);
    }

    @NonNull
    private View addImageButton(@NonNull LinearLayout row, @IdRes int id, @DrawableRes int icon) {
        final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        lp.weight = 1f;
        final View view = makeImageButton(id, icon);
        row.addView(view, lp);
        return view;
    }

    @NonNull
    private DirectionDragButton addOperationButton(@NonNull LinearLayout row, @IdRes int id, @NonNull String text) {
        final DirectionDragButton button = addButton(row, id, text);
        button.setBackgroundResource(R.drawable.kb_material_button_primary);
        button.setTextColor(Color.WHITE);
        button.setDirectionTextColor(Color.WHITE);
        return button;
    }

    @NonNull
    private DirectionDragButton addButton(@NonNull LinearLayout row, @IdRes int id, @NonNull String text) {
        final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        lp.weight = 1f;
        final DirectionDragButton view = makeButton(id, text);
        row.addView(view, lp);
        return view;
    }

    @NonNull
    private LinearLayout makeRow() {
        final LinearLayout row = new LinearLayout(user.getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
        lp.weight = 1f;
        user.getKeyboard().addView(row, lp);
        return row;
    }

    @NonNull
    private DirectionDragButton makeButton(@IdRes int id, @NonNull String text) {
        final DirectionDragButton button = new DirectionDragButton(user.getContext());
        fillButton(button, id);
        button.setText(text);
        button.setTextColor(textColor);
        button.setDirectionTextColor(textColorSecondary);
        button.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 24);
        button.setOnDragListener(dragListener);
        return button;
    }

    private void fillButton(@NonNull View button, @IdRes int id) {
        button.setOnClickListener(buttonHandler);
        button.setId(id);
        button.setBackgroundResource(R.drawable.kb_material_button);
        button.setPadding(sidePadding, 1, sidePadding, 1);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            button.setStateListAnimator(null);
        }
    }

    @NonNull
    private View makeImageButton(@IdRes int id, @DrawableRes int icon) {
        final ImageButton button = new ImageButton(user.getContext());
        fillButton(button, id);
        button.setImageResource(icon);
        button.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        return button;
    }

    interface User {
        @NonNull
        Context getContext();

        @NonNull
        Resources getResources();

        @NonNull
        EditText getEditor();

        @NonNull
        ViewGroup getKeyboard();

        void insertOperator(char operator);

        void insertOperator(@NonNull String operator);

        void showFunctions(@NonNull View v);
        void showConstants(@NonNull View v);

        void insertText(@NonNull CharSequence text, int offset);

        void done();

        void showIme();
    }

    private class ButtonHandler implements View.OnClickListener, SimpleDragListener.DragProcessor {
        @Override
        public void onClick(@NonNull View v) {
            switch (v.getId()) {
                case R.id.button_divide:
                    user.insertOperator('/');
                    break;
                case R.id.button_plus:
                    user.insertOperator('+');
                    break;
                case R.id.button_minus:
                    user.insertOperator('-');
                    break;
                case R.id.button_multiply:
                    user.insertOperator('*');
                    break;
                case R.id.button_functions:
                    user.showFunctions(v);
                    break;
                case R.id.button_constants:
                    user.showConstants(v);
                    break;
                case R.id.button_space:
                    user.insertText(" ", 0);
                    break;
                case R.id.button_keyboard:
                    user.showIme();
                    break;
                case R.id.button_clear:
                    user.getEditor().setText("");
                    user.getEditor().setSelection(0);
                    break;
                case R.id.button_brackets:
                    user.insertText("()", -1);
                    break;
                case R.id.button_close:
                    user.done();
                    break;
                default:
                    onDefaultClick(v);
                    break;
            }
            user.getEditor().requestFocus();
            v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
        }

        private void onDefaultClick(@NonNull View v) {
            user.insertText(((Button) v).getText(), 0);
        }

        @Override
        public boolean processDragEvent(@NonNull DragDirection direction, @NonNull DragButton button, @NonNull PointF startPoint, @NonNull MotionEvent e) {
            switch (button.getId()) {
                default:
                    return onDefaultDrag(button, direction);
            }
        }

        private boolean onDefaultDrag(@NonNull DragButton button, @NonNull DragDirection direction) {
            final String text = ((DirectionDragButton) button).getText(direction);
            if (TextUtils.isEmpty(text)) {
                return false;
            }
            switch (text) {
                case "sqrt":
                    user.insertText("sqrt()", -1);
                    break;
                case ",":
                    user.insertText(", ", 0);
                    break;
                case "^n":
                    user.insertOperator('^');
                    break;
                case "^2":
                    user.insertOperator("^ 2");
                    break;
                case "?":
                case ">":
                case "<":
                case ">=":
                case "<=":
                case ":":
                    user.insertOperator(text);
                    break;
                default:
                    user.insertText(text, 0);
                    break;
            }
            button.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            return true;
        }
    }
}