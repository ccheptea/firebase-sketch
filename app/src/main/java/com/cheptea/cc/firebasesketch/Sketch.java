package com.cheptea.cc.firebasesketch;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.cheptea.cc.firebasesketch.models.SketchPoint;
import com.google.firebase.FirebaseApp;

public class Sketch extends AppCompatActivity implements View.OnClickListener, LineTransferListener {

	private static final String LOG_TAG = Sketch.class.getSimpleName();

	Button btnMove;
	Button btnDraw;

	Pad pad;

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
		pad.onPrintNewPoint(sketchPoint);
	}
	// endregion bridge

}
