package org.solovyev.android.plotter;

import android.opengl.Matrix;
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

	Angle() {
	}

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

		if (x >= 180) {
			x -= 360;
		}

		if (x < -180) {
			x += 360;
		}

		if (y >= 180) {
			y -= 360;
		}

		if (y < -180) {
			y += 360;
		}
	}

	@Nonnull
	public float[] getMatrix() {
		final float[] matrix = new float[16];
		rotateTo(matrix);
		return matrix;
	}

	public void rotateTo(@Nonnull float[] matrix) {
		Matrix.setIdentityM(matrix, 0);
		Matrix.rotateM(matrix, 0, x, 1, 0, 0);
		Matrix.rotateM(matrix, 0, y, 0, 1, 0);
	}

	public void rotateBy(@Nonnull float[] matrix) {
		Matrix.setIdentityM(rotation, 0);
		Matrix.rotateM(rotation, 0, x, 1, 0, 0);
		Matrix.rotateM(rotation, 0, y, 0, 1, 0);
		Matrix.multiplyMM(tmp, 0, rotation, 0, matrix, 0);
		System.arraycopy(tmp, 0, matrix, 0, 16);
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

	public void apply(@Nonnull Angle from) {
		this.x = from.x;
		this.y = from.y;
	}
}
