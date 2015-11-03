package org.solovyev.android.plotter;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

final class Angle implements Parcelable {

    @NonNull
    public static final Creator<Angle> CREATOR = new Creator<Angle>() {
        public Angle createFromParcel(@NonNull Parcel in) {
            return new Angle(in);
        }

        public Angle[] newArray(int size) {
            return new Angle[size];
        }
    };

    @NonNull
    private final float[] rotation = new float[16];

    @NonNull
    private final float[] tmp = new float[16];

    public float x;
    public float y;

    Angle(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Angle(@NonNull Parcel in) {
        this.x = in.readFloat();
        this.y = in.readFloat();
    }

    public void add(@NonNull Angle angle) {
        x += angle.x;
        y += angle.y;

        x = normalize(x);
        y = normalize(y);
    }

    private float normalize(float angle) {
        if (angle < 0) {
            return angle + 360f;
        } else if (angle >= 360f) {
            return angle - 360f;
        }
        return angle;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel out, int flags) {
        out.writeFloat(x);
        out.writeFloat(y);
    }
}
