package org.solovyev.android.plotter;

import android.os.Parcel;
import android.os.Parcelable;

import javax.annotation.Nonnull;

final class Angle implements Parcelable {

    @Nonnull
    public static final Creator<Angle> CREATOR = new Creator<Angle>() {
        public Angle createFromParcel(@Nonnull Parcel in) {
            return new Angle(in);
        }

        public Angle[] newArray(int size) {
            return new Angle[size];
        }
    };

    @Nonnull
    private final float[] rotation = new float[16];

    @Nonnull
    private final float[] tmp = new float[16];

    public float x;
    public float y;

    Angle(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Angle(@Nonnull Parcel in) {
        this.x = in.readFloat();
        this.y = in.readFloat();
    }

    public void add(@Nonnull Angle angle) {
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
    public void writeToParcel(@Nonnull Parcel out, int flags) {
        out.writeFloat(x);
        out.writeFloat(y);
    }
}
