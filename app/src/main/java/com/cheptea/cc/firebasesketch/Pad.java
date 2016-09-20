package com.cheptea.cc.firebasesketch;

import android.annotation.TargetApi;
import android.content.Context;
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

import com.cheptea.cc.firebasesketch.models.SketchPoint;
import com.cheptea.cc.firebasesketch.ui.FloatPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * A very simple drawing pad.
 * <p/>
 * Created by constantin.cheptea on 17/07/16.
 */
public class Pad extends SurfaceView implements SurfaceHolder.Callback, View.OnTouchListener, LinePrinterListener {

	private static final String LOG_TAG = Pad.class.getSimpleName();

	private static final float RADIUS = 30;
	private static final long FRAME_RATE = 1000 / 36;

	private static final float PAPER_WIDTH = 1; // inch
	private static final float PAPER_HEIGHT = 1; // inch

	private List<Path> paths = new ArrayList<>();
	private Path path = null;


	private Paint backgroundPaint;
	private Paint paperPaint;
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

	private RectF background = new RectF();
	private RectF paper = new RectF();

	private FloatPoint paperDimenPixels;

	private final int backgroundMargin = 30;

	private ControlState controlState = ControlState.DRAW;

	public enum ControlState {MOVE, DRAW}

	public Pad(Context context) {
		super(context);
		init();
	}

	public Pad(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public Pad(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public Pad(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init();
	}

	private void init() {
		backgroundPaint = new Paint();
		backgroundPaint.setColor(0xffeeeeee);
		backgroundPaint.setStyle(Paint.Style.FILL);

		paperPaint = new Paint();
		paperPaint.setColor(0xffffffff);
		paperPaint.setStyle(Paint.Style.FILL);

		drawPaint = new Paint();
		drawPaint.setColor(Color.BLACK);
		drawPaint.setStrokeWidth(10);
		drawPaint.setStyle(Paint.Style.STROKE);

		displayMetrics = new DisplayMetrics();
		WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
		windowManager.getDefaultDisplay().getMetrics(displayMetrics);

		paperDimenPixels = DimensionsUtil.convertPointToPixels(displayMetrics, new FloatPoint(PAPER_WIDTH, PAPER_HEIGHT));

		setOnTouchListener(this);

		getHolder().addCallback(this);
	}

	public void setControlState(ControlState controlState) {
		Log.d(LOG_TAG, controlState + " control selected");
		this.controlState = controlState;
	}

	public void setLineTransferListener(LineTransferListener lineTransferListener) {
		this.lineTransferListener = lineTransferListener;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		background.offset(xOffset - oldXOffset, yOffset - oldYOffset);
		paper.offset(xOffset - oldXOffset, yOffset - oldYOffset);

		canvas.drawRect(background, backgroundPaint);
		canvas.drawRect(paper, paperPaint);
		canvas.drawCircle(xCenter + xOffset, yCenter + yOffset, 20, drawPaint);

		for (Path path : paths) {
			path.offset(xOffset - oldXOffset, yOffset - oldYOffset);
			canvas.drawPath(path, drawPaint);
		}

		oldXOffset = xOffset;
		oldYOffset = yOffset;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		xCenter = w / 2;
		yCenter = h / 2;

		background.set(
				xCenter - paperDimenPixels.x / 2 - backgroundMargin,
				yCenter - paperDimenPixels.y / 2 - backgroundMargin,
				xCenter + paperDimenPixels.x / 2 + backgroundMargin,
				yCenter + paperDimenPixels.y / 2 + backgroundMargin);
		paper.set(
				xCenter - paperDimenPixels.x / 2,
				yCenter - paperDimenPixels.y / 2,
				xCenter + paperDimenPixels.x / 2,
				yCenter + paperDimenPixels.y / 2);

		if (paper.top >= backgroundMargin) background.top = 0;
		if (paper.left >= backgroundMargin) background.left = 0;
		if (paper.right <= w - backgroundMargin) background.right = w;
		if (paper.bottom <= h - backgroundMargin) background.bottom = h;
	}

	@Override
	public boolean onTouch(View view, MotionEvent event) {

		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				if (controlState == ControlState.MOVE) {
					xMoveLast = event.getX();
					yMoveLast = event.getY();
				} else {
					createAndSendSketchPoint(event.getX() + xOffset, event.getY() + yOffset, SketchPoint.Type.START);
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
					createAndSendSketchPoint(event.getX() + xOffset, event.getY() + yOffset, SketchPoint.Type.JOINT);
				}
				break;
			case MotionEvent.ACTION_UP:
				if (controlState == ControlState.DRAW) {
					createAndSendSketchPoint(event.getX() + xOffset, event.getY() + yOffset, SketchPoint.Type.END);
				}
				break;
		}
		return true;
	}

	private void createAndSendSketchPoint(float xPx, float yPx, SketchPoint.Type type) {
		if (lineTransferListener != null) {
			FloatPoint point = DimensionsUtil.convertPointToInches(displayMetrics, new FloatPoint(xPx, yPx));
			SketchPoint sketchPoint = new SketchPoint(point.x, point.y, type);

			lineTransferListener.onNewPoint(sketchPoint);
		}
	}

	private boolean moveWithinHorizontalBounds(MotionEvent motionEvent) {
		float xOffset = this.xOffset + motionEvent.getX() - xMoveLast;
		float backgroundWidth = background.right - background.left;

		return Math.abs(xOffset) < (backgroundWidth - getWidth()) / 2;
	}

	private boolean moveWithinVerticalBounds(MotionEvent motionEvent) {
		float yOffset = this.yOffset + motionEvent.getY() - yMoveLast;
		float backgroundHeight = background.bottom - background.top;
		return Math.abs(yOffset) < (backgroundHeight - getHeight()) / 2;
	}

	@Override
	public void onPrintNewPoint(SketchPoint sketchPoint) {
		FloatPoint pointIn = new FloatPoint(sketchPoint.getX(), sketchPoint.getY());
		FloatPoint pointPx = DimensionsUtil.convertPointToPixels(displayMetrics, pointIn);

		switch (sketchPoint.getType()) {
			case START:
				onBeginLine(pointPx);
				break;
			case JOINT:
				onContinueLine(pointPx);
				break;
			case END:
				onEndLine(pointPx);
			default:
				Log.e(LOG_TAG, "No Print Command " + sketchPoint.getType() + " Found");
		}
	}

	private void onBeginLine(FloatPoint pointPixels) {
		path = new Path();
		path.moveTo(pointPixels.x - xOffset, pointPixels.y - yOffset);
		paths.add(path);
	}

	private void onContinueLine(FloatPoint pointPixels) {
		path.lineTo(pointPixels.x - xOffset, pointPixels.y - yOffset);
	}

	private void onEndLine(FloatPoint pointPixels) {
		path.lineTo(pointPixels.x, pointPixels.y);
	}

	// region surface

	@Override
	public void surfaceCreated(SurfaceHolder surfaceHolder) {
		padThread = new PadThread(this);
		padThread.start();
	}

	@Override
	public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
		padThread.stop = true;
	}

	// endregion surface

	//region classes

	private static class PadThread extends Thread {

		private boolean stop = false;

		SurfaceView pad;

		public PadThread(SurfaceView pad) {
			this.pad = pad;
		}

		@Override
		public void run() {
			while (!stop) {
				Canvas canvas = pad.getHolder().lockCanvas();
				synchronized (pad.getHolder()) {
					pad.postInvalidate();
				}
				try {
					sleep(FRAME_RATE);
					pad.getHolder().unlockCanvasAndPost(canvas);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				//Log.d("PadThread", "New Frame at " + System.currentTimeMillis());
			}
		}
	}

	//endregion classes
}
