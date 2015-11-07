package android.support.design.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;

import java.lang.reflect.Field;

public class TextInputLabel extends TextInputLayout {
    private static final String TAG = TextInputLabel.class.getSimpleName();
    private static final Field FIELD_INDICATOR_AREA;

    static {
        Field fieldIndicatorArea = null;
        try {
            fieldIndicatorArea = TextInputLayout.class.getDeclaredField("mIndicatorArea");
            fieldIndicatorArea.setAccessible(true);
        } catch (NoSuchFieldException e) {
            Log.w(TAG, e);
        }

        FIELD_INDICATOR_AREA = fieldIndicatorArea;
    }

    public TextInputLabel(Context context) {
        super(context);
    }

    public TextInputLabel(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TextInputLabel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setErrorEnabled(boolean enabled) {
        super.setErrorEnabled(enabled);

        if (!enabled)
            removeIndicatorFix();
    }

    @Override
    public void setCounterEnabled(boolean enabled) {
        super.setCounterEnabled(enabled);

        if (!enabled)
            removeIndicatorFix();
    }

    private void removeIndicatorFix() {
        if (FIELD_INDICATOR_AREA == null)
            return;

        try {
            final Object object = FIELD_INDICATOR_AREA.get(this);
            if (!(object instanceof ViewGroup))
                return;
            if (((ViewGroup) object).getChildCount() == 0)
                FIELD_INDICATOR_AREA.set(this, null);
        } catch (IllegalAccessException e) {
            Log.w(TAG, e);
        }
    }
}
