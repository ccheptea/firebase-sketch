package com.cheptea.cc.firebasesketch.ui;

import android.util.DisplayMetrics;

/**
 * Created by constantin.cheptea on 05/09/16.
 */
public class DimensionsUtil {

	public static FloatPoint convertPointToInches(DisplayMetrics dm, FloatPoint point) {
		return new FloatPoint(point.x / dm.xdpi, point.y / dm.ydpi);
	}

	public static FloatPoint convertPointToPixels(DisplayMetrics dm, FloatPoint point) {
		return new FloatPoint(point.x * dm.xdpi, point.y * dm.ydpi);
	}
}
