package org.solovyev.android.plotter.utils;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import org.solovyev.android.plotter.Check;

public final class Fragments {

    private Fragments() {
    }

    @NonNull
    public static <P extends Parcelable> P getParcelable(@NonNull Bundle bundle, @NonNull String key) {
        final P parcelable = bundle.getParcelable(key);
        Check.isNotNull(parcelable);
        return parcelable;
    }
}
