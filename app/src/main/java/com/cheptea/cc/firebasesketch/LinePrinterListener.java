package com.cheptea.cc.firebasesketch;

import com.cheptea.cc.firebasesketch.models.SketchPoint;

/**
 * Created by constantin.cheptea on 19/09/16.
 */
public interface LinePrinterListener {
	void onPrintNewPoint(String lineKey, SketchPoint sketchPoint);
}
