package com.cheptea.cc.firebasesketch;

import android.app.Activity;
import android.util.DisplayMetrics;

import com.cheptea.cc.firebasesketch.ui.FloatPoint;

/**
 * Created by constantin.cheptea on 05/09/16.
 */
public class DimensionsUtil {

	public static FloatPoint convertPointToInches(DisplayMetrics dm, FloatPoint point){
		FloatPoint resultPoint = new FloatPoint(point.x / dm.xdpi, point.y / dm.ydpi);
		return resultPoint;
	}

	public static FloatPoint convertPointToPixels(DisplayMetrics dm, FloatPoint point){
		FloatPoint resultPoint = new FloatPoint(point.x * dm.xdpi, point.y * dm.ydpi);
		return resultPoint;
	}
}
