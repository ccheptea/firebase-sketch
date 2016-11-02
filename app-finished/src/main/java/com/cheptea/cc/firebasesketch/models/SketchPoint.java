package com.cheptea.cc.firebasesketch.models;

import android.support.annotation.NonNull;

import com.google.firebase.database.Exclude;

/**
 * Created by constantin.cheptea on 19/09/16.
 */
public class SketchPoint {

	private float x;
	private float y;
	private Type type;

	public SketchPoint() {
		// needed for Firebase
	}

	public SketchPoint(float x, float y, @NonNull Type type) {
		this.x = x;
		this.y = y;
		this.type = type;
	}

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}

	public String getType() {
		return this.type.name();
	}

	@Exclude
	public void setType(Type type) {
		this.type = type;
	}

	public void setType(String typeString) {
		if (typeString == null)
			this.type = null;
		else
			this.type = Type.valueOf(typeString);
	}

	@Exclude
	public Type getTypeValue() {
		return type;
	}

	@Override
	public String toString() {
		return "SketchPoint{" +
				"x=" + x +
				", y=" + y +
				", type=" + type +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		SketchPoint that = (SketchPoint) o;

		if (Float.compare(that.x, x) != 0) return false;
		if (Float.compare(that.y, y) != 0) return false;
		return type == that.type;

	}

	@Override
	public int hashCode() {
		int result = (x != +0.0f ? Float.floatToIntBits(x) : 0);
		result = 31 * result + (y != +0.0f ? Float.floatToIntBits(y) : 0);
		result = 31 * result + (type != null ? type.hashCode() : 0);
		return result;
	}

	public enum Type {
		START, JOINT, END
	}
}
