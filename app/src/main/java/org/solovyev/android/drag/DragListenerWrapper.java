package org.solovyev.android.drag;


import android.support.annotation.NonNull;

/**
 * User: serso
 * Date: 10/26/11
 * Time: 10:37 PM
 */
public class DragListenerWrapper implements DragListener {

    @NonNull
    private final DragListener dragListener;

    public DragListenerWrapper(@NonNull DragListener dragListener) {
        this.dragListener = dragListener;
    }

    @Override
    public boolean isSuppressOnClickEvent() {
        return this.dragListener.isSuppressOnClickEvent();
    }

    @Override
    public boolean onDrag(@NonNull DragButton dragButton, @NonNull DragEvent event) {
        return this.dragListener.onDrag(dragButton, event);
    }
}
