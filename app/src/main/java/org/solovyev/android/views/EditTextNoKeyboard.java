package org.solovyev.android.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.EditText;

import java.lang.reflect.Method;

public class EditTextNoKeyboard extends EditText {

    private Method setShowSoftInputOnFocusMethod;

    public EditTextNoKeyboard(Context context) {
        super(context);
        init();
    }

    public EditTextNoKeyboard(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EditTextNoKeyboard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public EditTextNoKeyboard(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setShowSoftInputOnFocusCompat(false);
    }

    public void setShowSoftInputOnFocusCompat(boolean show) {
        if (Build.VERSION.SDK_INT >= 21) {
            setShowSoftInputOnFocus(show);
        } else {
            try {
                if (setShowSoftInputOnFocusMethod == null) {
                    setShowSoftInputOnFocusMethod = EditText.class.getMethod("setShowSoftInputOnFocus", boolean.class);
                    setShowSoftInputOnFocusMethod.setAccessible(true);
                }
                setShowSoftInputOnFocusMethod.invoke(this, show);
            } catch (Exception e) {
                // ignore
            }
        }
    }
}
