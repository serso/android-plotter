package org.solovyev.android.drag;

import android.support.annotation.NonNull;

import java.util.EventListener;


public interface DragListener extends EventListener {

    /**
     * @return 'true': if drag event has taken place (i.e. onDrag() method returned true) then click action will be suppresed
     */
    boolean isSuppressOnClickEvent();

    /**
     * @param dragButton drag button object for which onDrag listener was set
     * @param event      drag event
     * @return 'true' if drag event occurred, 'false' otherwise
     */
    boolean onDrag(@NonNull DragButton dragButton, @NonNull DragEvent event);

}
