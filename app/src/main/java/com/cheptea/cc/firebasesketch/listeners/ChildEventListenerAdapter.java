package com.cheptea.cc.firebasesketch.listeners;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

/**
 * Created by constantin.cheptea on 24/09/16.
 */
public class ChildEventListenerAdapter implements ChildEventListener {
	@Override
	public void onChildAdded(DataSnapshot dataSnapshot, String s) {

	}

	@Override
	public void onChildChanged(DataSnapshot dataSnapshot, String s) {

	}

	@Override
	public void onChildRemoved(DataSnapshot dataSnapshot) {

	}

	@Override
	public void onChildMoved(DataSnapshot dataSnapshot, String s) {

	}

	@Override
	public void onCancelled(DatabaseError databaseError) {

	}
}
