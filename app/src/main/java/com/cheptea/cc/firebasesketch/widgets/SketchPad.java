package com.cheptea.cc.firebasesketch.widgets;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

import com.cheptea.cc.firebasesketch.listeners.LinePrinterListener;
import com.cheptea.cc.firebasesketch.listeners.LineTransferListener;
import com.cheptea.cc.firebasesketch.models.SketchPoint;
import com.cheptea.cc.firebasesketch.ui.DimensionsUtil;
import com.cheptea.cc.firebasesketch.ui.FloatPoint;
import com.cheptea.cc.firebasesketch.ui.SizeF;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A very simple drawing pad.
 * <p/>
 * Created by constantin.cheptea on 17/07/16.
 */
public class SketchPad extends SurfaceView implements SurfaceHolder.Callback, View.OnTouchListener, LinePrinterListener {

	private static final String LOG_TAG = SketchPad.class.getSimpleName();

	private static final long FRAME_RATE = 1000 / 36;
	private static final float LINE_STROKE_WIDTH = 0.02f; // inch
	private final Map<String, Path> paths = new HashMap<>();
	private final List<String> pathsToRemove = new ArrayList<>();
	private final float paperMargin = 0.1f; // inch
	private SizeF paperSize = new SizeF(5, 5);
	private Paint grayLayerPaint;
	private Paint whiteLayerPaint;
	private Paint drawPaint;
	private PadThread padThread;
	private DisplayMetrics displayMetrics;
	private LineTransferListener lineTransferListener;
	private float xOffset = 0;
	private float yOffset = 0;
	private float oldXOffset = 0;
	private float oldYOffset = 0;
	private float xMoveLast;
	private float yMoveLast;
	private float xCenter;
	private float yCenter;
	private Bitmap cacheBitmap;
	private Canvas cacheCanvas;

	private ControlState controlState = ControlState.DRAW;

	private float cacheBitmapXPos;
	private float cacheBitmapYPos;

	public SketchPad(Context context) {
		super(context);
		init();
	}

	public SketchPad(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public SketchPad(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public SketchPad(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init();
	}

	private void init() {
		displayMetrics = new DisplayMetrics();
		WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
		windowManager.getDefaultDisplay().getMetrics(displayMetrics);

		float strokeWidth = DimensionsUtil.convertPointToPixels(displayMetrics, new FloatPoint(LINE_STROKE_WIDTH, LINE_STROKE_WIDTH)).x;

		grayLayerPaint = new Paint();
		grayLayerPaint.setColor(0xffeeeeee);
		grayLayerPaint.setStyle(Paint.Style.FILL);

		whiteLayerPaint = new Paint();
		whiteLayerPaint.setColor(0xffffffff);
		whiteLayerPaint.setStyle(Paint.Style.FILL);

		drawPaint = new Paint();
		drawPaint.setColor(Color.BLACK);
		drawPaint.setStrokeWidth(strokeWidth);
		drawPaint.setStyle(Paint.Style.STROKE);

		createCacheBitmap();

		setOnTouchListener(this);

		getHolder().addCallback(this);
	}

	private void createCacheBitmap() {
		FloatPoint paperDimenPixels = DimensionsUtil.convertPointToPixels(displayMetrics, new FloatPoint(paperSize.getWidth(), paperSize.getHeight()));
		FloatPoint paperMargins = DimensionsUtil.convertPointToPixels(displayMetrics, new FloatPoint(paperMargin, paperMargin));

		cacheBitmap = Bitmap.createBitmap((int) (paperDimenPixels.x + paperMargins.x * 2), (int) (paperDimenPixels.y + paperMargins.y * 2), Bitmap.Config.RGB_565);

		RectF grayLayer = new RectF(0, 0, paperDimenPixels.x + paperMargins.x * 2, paperDimenPixels.y + paperMargins.y * 2);
		RectF whiteLayer = new RectF(paperMargins.x, paperMargins.y, grayLayer.right - paperMargins.x, grayLayer.bottom - paperMargins.y);

		cacheCanvas = new Canvas(cacheBitmap);
		cacheCanvas.drawRect(grayLayer, grayLayerPaint);
		cacheCanvas.drawRect(whiteLayer, whiteLayerPaint);
	}

	public void setControlState(ControlState controlState) {
		Log.d(LOG_TAG, controlState + " control selected");
		this.controlState = controlState;
	}

	public void setLineTransferListener(LineTransferListener lineTransferListener) {
		this.lineTransferListener = lineTransferListener;
	}

	/**
	 * Recreates the entire paper. Make sure to call it only once and before drawing anything;
	 *
	 * @param paperSize
	 */
	public void setPaperSize(SizeF paperSize) {
		this.paperSize = paperSize;
		createCacheBitmap();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		cacheBitmapXPos = xCenter - cacheBitmap.getWidth() / 2 + xOffset;
		cacheBitmapYPos = yCenter - cacheBitmap.getHeight() / 2 + yOffset;

		canvas.drawBitmap(cacheBitmap, cacheBitmapXPos, cacheBitmapYPos, null);

		for (Path path : paths.values()) {
			path.offset(xOffset - oldXOffset, yOffset - oldYOffset);
			canvas.drawPath(path, drawPaint);
		}

		cacheIfNeeded();

		oldXOffset = xOffset;
		oldYOffset = yOffset;
	}

	public void cacheIfNeeded() {
		if (pathsToRemove.size() > 0) {
			// cache lines
			synchronized (pathsToRemove) {
				for (String lineKey : pathsToRemove) {
					Path path = paths.get(lineKey);
					path.offset(-cacheBitmapXPos, -cacheBitmapYPos);
					cacheCanvas.drawPath(path, drawPaint);
					paths.remove(lineKey);
				}
				pathsToRemove.clear();
			}
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		xCenter = w / 2;
		yCenter = h / 2;
	}

	@Override
	public boolean onTouch(View view, MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				if (controlState == ControlState.MOVE) {
					xMoveLast = event.getX();
					yMoveLast = event.getY();
				} else {
					createAndSendSketchPoint(event, SketchPoint.Type.START);
				}
				break;
			case MotionEvent.ACTION_MOVE:
				if (controlState == ControlState.MOVE) {
					if (moveWithinHorizontalBounds(event)) {
						xOffset += event.getX() - xMoveLast;
						xMoveLast = event.getX();
					}
					if (moveWithinVerticalBounds(event)) {
						yOffset += event.getY() - yMoveLast;
						yMoveLast = event.getY();
					}
				} else {
					createAndSendSketchPoint(event, SketchPoint.Type.JOINT);
				}
				break;
			case MotionEvent.ACTION_UP:
				if (controlState == ControlState.DRAW) {
					createAndSendSketchPoint(event, SketchPoint.Type.END);
				}
				break;
		}
		return true;
	}

	private void createAndSendSketchPoint(MotionEvent event, SketchPoint.Type type) {
		// real distance = distance from center to event + move offset
		float xPx = event.getX() - xCenter - xOffset;
		float yPx = event.getY() - yCenter - yOffset;
		if (lineTransferListener != null) {
			FloatPoint point = DimensionsUtil.convertPointToInches(displayMetrics, new FloatPoint(xPx, yPx));
			SketchPoint sketchPoint = new SketchPoint(point.x, point.y, type);

			lineTransferListener.onNewPoint(sketchPoint);
		}
	}

	private boolean moveWithinHorizontalBounds(MotionEvent motionEvent) {
		float xOffset = this.xOffset + motionEvent.getX() - xMoveLast;
		return Math.abs(xOffset) < (cacheBitmap.getWidth() - getWidth()) / 2;
	}

	private boolean moveWithinVerticalBounds(MotionEvent motionEvent) {
		float yOffset = this.yOffset + motionEvent.getY() - yMoveLast;
		return Math.abs(yOffset) < (cacheBitmap.getHeight() - getHeight()) / 2;
	}

	@Override
	public void onPrintNewPoint(String lineKey, SketchPoint sketchPoint) {
		FloatPoint pointIn = new FloatPoint(sketchPoint.getX(), sketchPoint.getY());
		FloatPoint pointPx = DimensionsUtil
				.convertPointToPixels(displayMetrics, pointIn)
				.offset(xCenter + xOffset, yCenter + yOffset);

		switch (sketchPoint.getTypeValue()) {
			case START:
				onBeginLine(lineKey, pointPx);
				break;
			case JOINT:
				onContinueLine(lineKey, pointPx);
				break;
			case END:
				onEndLine(lineKey, pointPx);
				break;
			default:
				Log.e(LOG_TAG, "No Print Command " + sketchPoint.getType() + " Found");
		}
	}

	private void onBeginLine(String lineKey, FloatPoint pointPixels) {
		Path path = new Path();
		path.moveTo(pointPixels.x, pointPixels.y);
		paths.put(lineKey, path);
	}

	private void onContinueLine(String lineKey, FloatPoint pointPixels) {
		paths.get(lineKey).lineTo(pointPixels.x, pointPixels.y);
	}

	private void onEndLine(String lineKey, FloatPoint pointPixels) {
		paths.get(lineKey).lineTo(pointPixels.x, pointPixels.y);
		synchronized (pathsToRemove) {
			pathsToRemove.add(lineKey);
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder surfaceHolder) {
		padThread = new PadThread(this);
		padThread.start();
	}

	// region surface

	@Override
	public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
		synchronized (padThread.okToDraw) {
			padThread.okToDraw = false;
		}
	}

	public enum ControlState {MOVE, DRAW}

	// endregion surface

	//region classes

	private static class PadThread extends Thread {

		SketchPad pad;
		private Boolean okToDraw = true;

		PadThread(SketchPad pad) {
			this.pad = pad;
		}

		@Override
		public void run() {
			while (okToDraw) {
				Canvas canvas = pad.getHolder().lockCanvas();
				synchronized (pad.getHolder()) {
					pad.postInvalidate();
				}
				if (okToDraw) {
					pad.getHolder().unlockCanvasAndPost(canvas);
				}

				try {
					sleep(FRAME_RATE);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	//endregion classes
}
