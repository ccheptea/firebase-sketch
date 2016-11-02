package com.cheptea.cc.firebasesketch;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.cheptea.cc.firebasesketch.adapters.DocumentsAdapter;
import com.cheptea.cc.firebasesketch.constants.Keys;
import com.cheptea.cc.firebasesketch.dialogs.CreateDocumentDialog;
import com.cheptea.cc.firebasesketch.dialogs.RenameDocumentDialog;
import com.cheptea.cc.firebasesketch.listeners.ChildEventListenerAdapter;
import com.cheptea.cc.firebasesketch.models.Document;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * The activity for managing sketch documents
 * <p>
 * Created by constantin.cheptea on 03/10/16.
 */

public class DocumentsActivity extends AppCompatActivity implements
		CreateDocumentDialog.CreateDocumentDialogListener,
		RenameDocumentDialog.RenameDocumentDialogListener,
		DocumentsAdapter.OnDocumentOptionSelected,
		Keys {

	private static final String LOG_TAG = DocumentsActivity.class.getSimpleName();

	@BindView(R.id.documents_list)
	RecyclerView documentsListView;

	@BindView(R.id.toolbar)
	Toolbar toolbar;

	DocumentsAdapter documentsAdapter;
	List<Document> documents = new ArrayList<>();

	DatabaseReference documentsRef = FirebaseDatabase.getInstance().getReference("documents");
	DatabaseReference linesRef = FirebaseDatabase.getInstance().getReference("lines");
	DatabaseReference userDocumentsRef;

	FirebaseAuth auth = FirebaseAuth.getInstance();

	ChildEventListenerAdapter documentsListener = new ChildEventListenerAdapter() {
		@Override
		public void onChildAdded(DataSnapshot dataSnapshot, String s) {
			Document document = dataSnapshot.getValue(Document.class);
			document.setKey(dataSnapshot.getKey());
			onDocumentInserted(document);
		}

		@Override
		public void onChildRemoved(DataSnapshot dataSnapshot) {
			Document document = dataSnapshot.getValue(Document.class);
			document.setKey(dataSnapshot.getKey());
			onDocumentRemoved(document);
		}

		@Override
		public void onChildChanged(DataSnapshot dataSnapshot, String s) {
			Document document = dataSnapshot.getValue(Document.class);
			document.setKey(dataSnapshot.getKey());
			onDocumentChanged(document);
		}
	};

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_documents);
		ButterKnife.bind(this);

		setSupportActionBar(toolbar);

		documentsAdapter = new DocumentsAdapter(documents);

		documentsListView.setHasFixedSize(true);
		documentsListView.setLayoutManager(new LinearLayoutManager(this));
		documentsListView.setAdapter(documentsAdapter);

		documentsRef.addChildEventListener(documentsListener);

		initRemoteConfig();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		documentsRef.removeEventListener(documentsListener);
	}

	private void onDocumentInserted(Document remoteDocument) {
		documents.add(remoteDocument);
		documentsAdapter.notifyItemInserted(documents.size() - 1);
	}

	private void onDocumentRemoved(Document remoteDocument) {
		int documentIndex = findDocumentIndexByKey(remoteDocument.getKey());
		documentsAdapter.notifyItemRemoved(documentIndex);
		documents.remove(documentIndex);
	}

	private void onDocumentChanged(Document remoteDocument) {
		int documentIndex = findDocumentIndexByKey(remoteDocument.getKey());
		documents.set(documentIndex, remoteDocument);
		documentsAdapter.notifyItemChanged(documentIndex);
	}

	private int findDocumentIndexByKey(String key) {
		for (int i = 0; i < documents.size(); i++) {
			if (documents.get(i).getKey().equals(key)) return i;
		}
		return -1;
	}

	private void initRemoteConfig() {
		FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
		FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
				.setDeveloperModeEnabled(BuildConfig.DEBUG)
				.build();
		remoteConfig.setConfigSettings(configSettings);

		remoteConfig.setDefaults(R.xml.remote_firesketch_defaults);
		remoteConfig.fetch();
		remoteConfig.activateFetched();

		Log.d(LOG_TAG, remoteConfig.getString(REMOTE_CONFIG_TEST));
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (auth.getCurrentUser() != null) {
			userDocumentsRef = FirebaseDatabase
					.getInstance()
					.getReference("users")
					.child(auth.getCurrentUser().getUid())
					.child("created_documents");
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_item_sign_in:
				Intent openSignInActivityIntent = new Intent(this, AccountActivity.class);
				startActivity(openSignInActivityIntent);
				return true;
			case R.id.menu_item_new_document:
				promptCreateDocumentDialog();
				return true;
		}
		return false;
	}

	@Override
	public void onDocumentOptionSelected(Document document, int optionId) {
		switch (optionId) {
			case R.id.menu_item_delete_document:
				removeDocument(document);
				break;
			case R.id.menu_item_rename_document:
				promptRenameDocumentDialog(document);
				break;
		}
		Log.d(LOG_TAG, document.toString());
	}

	@Override
	public void onCreateDocument(Document document) {
		if (auth.getCurrentUser() != null) {
			DatabaseReference newDocumentRef = documentsRef.push();
			newDocumentRef.setValue(document);
			userDocumentsRef.child(newDocumentRef.getKey()).setValue(true);
		} else {
			Toast.makeText(this, "Only logged users can create documents", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onNewDocumentTitle(Document document, String newTitle) {
		document.setTitle(newTitle);
		documentsRef.child(document.getKey()).setValue(document);
	}

	private void removeDocument(Document document) {
		documentsRef.child(document.getKey()).setValue(null);
		linesRef.child(document.getKey()).setValue(null);
	}

	private void promptCreateDocumentDialog() {
		CreateDocumentDialog createDocumentDialog = new CreateDocumentDialog();
		createDocumentDialog.show(getFragmentManager(), CreateDocumentDialog.class.getSimpleName());
	}

	private void promptRenameDocumentDialog(Document document) {
		RenameDocumentDialog renameDocumentDialog = RenameDocumentDialog.newInstance(document);
		renameDocumentDialog.show(getFragmentManager(), RenameDocumentDialog.class.getSimpleName());
	}
}
