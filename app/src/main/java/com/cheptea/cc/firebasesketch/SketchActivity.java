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
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

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
	DatabaseReference documentReference;
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

		documentReference = FirebaseDatabase.getInstance().getReference("documents").child(document.getKey());
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
					lineListeners.get(lineKey).removeEventListener(this);
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
			case R.id.btn_erase_all:
				pad.setControlState(SketchPad.ControlState.ERASE);
				pad.clearDocument();
				documentLinesRef.setValue(null);
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
		documentReference.runTransaction(new Transaction.Handler() {
			@Override
			public Transaction.Result doTransaction(MutableData mutableData) {
				Document document = mutableData.getValue(Document.class);

				if (document == null) {
					return Transaction.success(mutableData);
				}

				if (like) {
					document.setLikes(document.getLikes() + 1);
				} else {
					if (document.getLikes() > 0) {
						document.setLikes(document.getLikes() - 1);
					}
				}
				mutableData.setValue(document);
				return Transaction.success(mutableData);
			}

			@Override
			public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

			}
		});
	}

	@Override
	public void onNewPoint(SketchPoint sketchPoint) {
//		pad.onPrintNewPoint(sketchPoint);

		// create a new line node if we just started drawing a new line
		if (sketchPoint.getTypeValue() == SketchPoint.Type.START) {
			String newLineKey = documentLinesRef.push().getKey();
			myLineReference = documentLinesRef.child(newLineKey);
		}
		// add the new point to the line
		if (myLineReference != null) {
			myLineReference.push().setValue(sketchPoint);
		}

	}
}
