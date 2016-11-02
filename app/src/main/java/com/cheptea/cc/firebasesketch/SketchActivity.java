package com.cheptea.cc.firebasesketch;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.cheptea.cc.firebasesketch.constants.Keys;
import com.cheptea.cc.firebasesketch.listeners.LineTransferListener;
import com.cheptea.cc.firebasesketch.listeners.OnOffViewStateListener;
import com.cheptea.cc.firebasesketch.models.Document;
import com.cheptea.cc.firebasesketch.models.SketchPoint;
import com.cheptea.cc.firebasesketch.ui.SizeF;
import com.cheptea.cc.firebasesketch.widgets.Controls;
import com.cheptea.cc.firebasesketch.widgets.FavouriteButton;
import com.cheptea.cc.firebasesketch.widgets.SketchPad;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SketchActivity extends AppCompatActivity implements
		LineTransferListener,
		OnOffViewStateListener,
		Keys,
		Controls.OnControlSelectedListener {

	private static final String LOG_TAG = SketchActivity.class.getSimpleName();

	@BindView(R.id.toolbar)
	Toolbar toolbar;

	@BindView(R.id.btn_favourite)
	FavouriteButton btnFavourite;

	@BindView(R.id.controls)
	Controls controls;

	@BindView(R.id.pad)
	SketchPad pad;

	Document document;

	private String lineKey;
	private int lineCount = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sketch);
		ButterKnife.bind(this);

		document = getIntent().getParcelableExtra(KEY_DOCUMENT);
		controls.setOnControlSelectedListener(this);
		pad.setLineTransferListener(this);
		pad.setPaperSize(new SizeF(document.getWidth(), document.getHeight()));

		toolbar.setTitle(document.getTitle());

		btnFavourite.setOnOffStateListener(this);
	}

	@Override
	public void onControlSelected(View controlView) {
		switch (controlView.getId()) {
			case R.id.btn_move:
				pad.setControlState(SketchPad.ControlState.MOVE);
				break;
			case R.id.btn_erase_all:
				pad.setControlState(SketchPad.ControlState.ERASE);
				pad.clearDocument();
				break;
			default:
				pad.setControlState(SketchPad.ControlState.DRAW);
		}
	}

	@Override
	public void onViewStateChanged(View view, boolean isOn) {
		switch (view.getId()) {
			case R.id.btn_favourite:
				// handle favourite button
				updateLikesCount(isOn);
				break;
		}
	}

	private void updateLikesCount(final boolean like) {

	}

	@Override
	public void onNewPoint(SketchPoint sketchPoint) {
		// create a new line key if we just started drawing a new line
		if (sketchPoint.getTypeValue() == SketchPoint.Type.START) {
			lineCount++;
			lineKey = "my_line_" + lineCount;
		}

		pad.onPrintNewPoint(lineKey, sketchPoint);
	}
}
