package com.cheptea.cc.firebasesketch;

import com.cheptea.cc.firebasesketch.models.SketchPoint;

/**
 * Interface used for transferring points form source to network
 * Created by constantin.cheptea on 19/09/16.
 */
public interface LineTransferListener {

	void onNewPoint(SketchPoint sketchPoint);
}
