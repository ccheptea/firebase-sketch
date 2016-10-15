package com.cheptea.cc.firebasesketch.dialogs;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.cheptea.cc.firebasesketch.R;
import com.cheptea.cc.firebasesketch.constants.Keys;
import com.cheptea.cc.firebasesketch.models.Document;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Create document dialog fragment.
 * Created by constantin.cheptea on 06/10/16.
 */

public class CreateDocumentDialog extends DialogFragment implements Keys {

	private static final String LOG_TAG = CreateDocumentDialog.class.getSimpleName();

	@BindView(R.id.title)
	EditText title;

	@BindView(R.id.width_spinner)
	Spinner widthSpinner;

	@BindView(R.id.height_spinner)
	Spinner heightSpinner;

	@BindString(R.string.title_create_document)
	String labelCreateDocument;

	@BindString(R.string.title_default_document_title)
	String defaultDocumentTitle;
	FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
	private CreateDocumentDialogListener listener;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Log.d(LOG_TAG, "CreateDocumentDialog created");

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		View dialogView = getActivity().getLayoutInflater().inflate(R.layout.dialog_create_document, null);

		ButterKnife.bind(this, dialogView);

		setDocumentSizes();

		builder
				.setTitle(labelCreateDocument)
				.setView(dialogView)
				.setPositiveButton(R.string.btn_create, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						listener.onCreateDocument(createDocument());
					}
				})
				.setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						CreateDocumentDialog.this.getDialog().cancel();
					}
				});
		return builder.create();
	}

	private void setDocumentSizes() {
		String[] sizes = remoteConfig.getString(REMOTE_CONFIG_DOCUMENT_SIZES).split(",");
		ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, sizes);

		widthSpinner.setAdapter(adapter);
		heightSpinner.setAdapter(adapter);

		widthSpinner.setSelection((int) remoteConfig.getLong(REMOTE_CONFIG_DOCUMENT_SIZE_ITEM_SELECTED));
		heightSpinner.setSelection((int) remoteConfig.getLong(REMOTE_CONFIG_DOCUMENT_SIZE_ITEM_SELECTED));
	}

	private Document createDocument() {
		Document document = new Document();

		document.setTitle(TextUtils.isEmpty(title.getText()) ? defaultDocumentTitle : title.getText().toString());
		document.setWidth(Integer.valueOf(widthSpinner.getSelectedItem().toString()));
		document.setHeight(Integer.valueOf(heightSpinner.getSelectedItem().toString()));
		document.setDate(System.currentTimeMillis());

		return document;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
			onAttachContext(activity);
		}
	}

	@TargetApi(23)
	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		onAttachContext(context);
	}

	private void onAttachContext(Context context) {
		try {
			listener = (CreateDocumentDialogListener) getActivity();
		} catch (ClassCastException e) {
			throw new ClassCastException(context.toString() + " must implement CreateDocumentDialogListener");
		}
	}

	public interface CreateDocumentDialogListener {
		void onCreateDocument(Document document);
	}
}
