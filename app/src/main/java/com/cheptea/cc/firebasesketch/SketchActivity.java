package com.cheptea.cc.firebasesketch;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.cheptea.cc.firebasesketch.constants.Keys;
import com.cheptea.cc.firebasesketch.listeners.ChildEventListenerAdapter;
import com.cheptea.cc.firebasesketch.listeners.LineTransferListener;
import com.cheptea.cc.firebasesketch.listeners.OnOffViewStateListener;
import com.cheptea.cc.firebasesketch.models.Document;
import com.cheptea.cc.firebasesketch.models.SketchPoint;
import com.cheptea.cc.firebasesketch.ui.SizeF;
import com.cheptea.cc.firebasesketch.widgets.Controls;
import com.cheptea.cc.firebasesketch.widgets.FavouriteButton;
import com.cheptea.cc.firebasesketch.widgets.SketchPad;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

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

	DatabaseReference documentLinesRef;
	DatabaseReference myLineReference = null;

	HashMap<String, DatabaseReference> lineListeners = new HashMap<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sketch);
		ButterKnife.bind(this);

		document = getIntent().getParcelableExtra(KEY_DOCUMENT);
		controls.setOnControlSelectedListener(this);
		pad.setLineTransferListener(this);
		pad.setPaperSize(new SizeF(document.getWidth(), document.getHeight()));

		documentLinesRef = FirebaseDatabase.getInstance().getReference("lines").child(document.getKey());

		documentLinesRef.addChildEventListener(new ChildEventListenerAdapter() {
			@Override
			public void onChildAdded(DataSnapshot dataSnapshot, String s) {
				Log.d(LOG_TAG, "New line created with key " + dataSnapshot.getKey());
				listenNewLine(dataSnapshot.getKey());
			}
		});

		toolbar.setTitle(document.getTitle());

		btnFavourite.setOnOffStateListener(this);
	}

	private void listenNewLine(final String lineKey) {
		DatabaseReference newLineRef = documentLinesRef.child(lineKey);
		lineListeners.put(lineKey, newLineRef);
		newLineRef.addChildEventListener(new ChildEventListenerAdapter() {
			@Override
			public void onChildAdded(DataSnapshot dataSnapshot, String s) {
				SketchPoint point = dataSnapshot.getValue(SketchPoint.class);
				pad.onPrintNewPoint(lineKey, point);
				if (point.getTypeValue() == SketchPoint.Type.END) {
					lineListeners.remove(lineKey);
				}
			}
		});
	}

	@Override
	public void onControlSelected(View controlView) {
		switch (controlView.getId()) {
			case R.id.btn_move:
				pad.setControlState(SketchPad.ControlState.MOVE);
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
				break;
		}
	}

	// region bridge
	@Override
	public void onNewPoint(SketchPoint sketchPoint) {
//		pad.onPrintNewPoint(sketchPoint);
		if (sketchPoint.getTypeValue() == SketchPoint.Type.START) {
			String newLineKey = documentLinesRef.push().getKey();
			myLineReference = documentLinesRef.child(newLineKey);
		}

		if (myLineReference != null) {
			myLineReference.push().setValue(sketchPoint);
		}

	}
	// endregion bridge


}
