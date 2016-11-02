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

import com.cheptea.cc.firebasesketch.adapters.DocumentsAdapter;
import com.cheptea.cc.firebasesketch.constants.Keys;
import com.cheptea.cc.firebasesketch.dialogs.CreateDocumentDialog;
import com.cheptea.cc.firebasesketch.dialogs.RenameDocumentDialog;
import com.cheptea.cc.firebasesketch.models.Document;

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
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
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
		onDocumentInserted(document);
	}

	@Override
	public void onNewDocumentTitle(Document document, String newTitle) {
		document.setTitle(newTitle);
		onDocumentChanged(document);
	}

	private void removeDocument(Document document) {
		onDocumentRemoved(document);
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
