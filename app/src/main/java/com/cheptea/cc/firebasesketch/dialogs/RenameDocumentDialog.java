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
import android.view.View;
import android.widget.EditText;

import com.cheptea.cc.firebasesketch.R;
import com.cheptea.cc.firebasesketch.constants.Keys;
import com.cheptea.cc.firebasesketch.models.Document;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by constantin.cheptea on 12/10/16.
 */

public class RenameDocumentDialog extends DialogFragment implements Keys {
	private static final String LOG_TAG = CreateDocumentDialog.class.getSimpleName();

	@BindView(R.id.title)
	EditText title;

	@BindString(R.string.title_new_title)
	String labelRenameDocument;

	private Document document;

	private RenameDocumentDialogListener listener;

	public static RenameDocumentDialog newInstance(Document document) {
		RenameDocumentDialog renameDocumentDialog = new RenameDocumentDialog();
		Bundle args = new Bundle();

		args.putParcelable(KEY_DOCUMENT, document);
		renameDocumentDialog.setArguments(args);

		return renameDocumentDialog;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		document = getArguments().getParcelable(KEY_DOCUMENT);

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		View dialogView = getActivity().getLayoutInflater().inflate(R.layout.dialog_rename_document, null);

		ButterKnife.bind(this, dialogView);

		if (document != null) {
			title.setText(document.getTitle());
		}

		builder
				.setTitle(labelRenameDocument)
				.setView(dialogView)
				.setPositiveButton(R.string.btn_rename, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						listener.onNewDocumentTitle(document, title.getText().toString());
					}
				})
				.setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						RenameDocumentDialog.this.getDialog().cancel();
					}
				});
		return builder.create();
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
			listener = (RenameDocumentDialogListener) getActivity();
		} catch (ClassCastException e) {
			throw new ClassCastException(context.toString() + " must implement RenameDocumentDialogListener");
		}
	}

	public interface RenameDocumentDialogListener {
		void onNewDocumentTitle(Document document, String newTitle);
	}
}
