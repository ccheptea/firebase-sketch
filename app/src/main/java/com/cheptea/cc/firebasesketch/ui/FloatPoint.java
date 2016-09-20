package com.cheptea.cc.firebasesketch.ui;

/**
 * A class representing a point with float values as coordinates.
 *
 * Created by constantin.cheptea on 06/09/16.
 */
public class FloatPoint {
	public final float x;
	public final float y;

	public FloatPoint(float x, float y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		FloatPoint that = (FloatPoint) o;

		if (Float.compare(that.x, x) != 0) return false;
		return Float.compare(that.y, y) == 0;

	}

	@Override
	public int hashCode() {
		int result = (x != +0.0f ? Float.floatToIntBits(x) : 0);
		result = 31 * result + (y != +0.0f ? Float.floatToIntBits(y) : 0);
		return result;
	}

	@Override
	public String toString() {
		return "FloatPoint{" +
				"x=" + x +
				", y=" + y +
				'}';
	}
}
