package org.solovyev.android.plotter;

import android.support.annotation.NonNull;

interface SuperFunction {

    int getArity();

    float evaluate();

    float evaluate(float x);

    float evaluate(float x, float y);

    @NonNull
    SuperFunction copy();
}
