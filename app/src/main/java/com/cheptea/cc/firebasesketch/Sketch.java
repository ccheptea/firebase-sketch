package com.cheptea.cc.firebasesketch;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.cheptea.cc.firebasesketch.models.SketchPoint;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class Sketch extends AppCompatActivity implements View.OnClickListener, LineTransferListener {

	private static final String LOG_TAG = Sketch.class.getSimpleName();

	Button btnMove;
	Button btnDraw;

	Pad pad;

	DatabaseReference dbLinesRef = FirebaseDatabase.getInstance().getReference("lines");
	DatabaseReference myLineReference = null;

	HashMap<String, DatabaseReference> lineListeners = new HashMap<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sketch);

		pad = (Pad) findViewById(R.id.pad);
		btnDraw = (Button) findViewById(R.id.btn_draw);
		btnMove = (Button) findViewById(R.id.btn_move);

		btnMove.setOnClickListener(this);
		btnDraw.setOnClickListener(this);

		pad.setLineTransferListener(this);

		dbLinesRef.addChildEventListener(new ChildEventListenerAdapter() {
			@Override
			public void onChildAdded(DataSnapshot dataSnapshot, String s) {
				Log.d(LOG_TAG, "New line created with key " + dataSnapshot.getKey());
				listenNewLine(dataSnapshot.getKey());
			}
		});
	}

	private void listenNewLine(final String lineKey) {
		DatabaseReference newLineRef = dbLinesRef.child(lineKey);
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
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.btn_move:
				pad.setControlState(Pad.ControlState.MOVE);
				break;
			default:
				pad.setControlState(Pad.ControlState.DRAW);
		}
	}

	// region bridge
	@Override
	public void onNewPoint(SketchPoint sketchPoint) {
		// send to Firebase
//		pad.onPrintNewPoint(sketchPoint);
		if (sketchPoint.getTypeValue() == SketchPoint.Type.START) {
			String newLineKey = dbLinesRef.push().getKey();
			myLineReference = dbLinesRef.child(newLineKey);
		}

		if (myLineReference != null) {
			myLineReference.push().setValue(sketchPoint);
		}

	}
	// endregion bridge

}
