package com.cheptea.cc.firebasesketch.ui;

/**
 * Created by constantin.cheptea on 06/10/16.
 */

public class SizeF {
	private final float width;
	private final float height;

	public SizeF(float width, float height) {
		this.width = width;
		this.height = height;
	}

	public float getWidth() {
		return width;
	}

	public float getHeight() {
		return height;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		SizeF sizeF = (SizeF) o;

		if (Float.compare(sizeF.width, width) != 0) return false;
		return Float.compare(sizeF.height, height) == 0;

	}

	@Override
	public int hashCode() {
		int result = (width != +0.0f ? Float.floatToIntBits(width) : 0);
		result = 31 * result + (height != +0.0f ? Float.floatToIntBits(height) : 0);
		return result;
	}
}
