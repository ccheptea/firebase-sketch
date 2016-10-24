package com.cheptea.cc.firebasesketch.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cheptea.cc.firebasesketch.R;
import com.cheptea.cc.firebasesketch.SketchActivity;
import com.cheptea.cc.firebasesketch.constants.Keys;
import com.cheptea.cc.firebasesketch.models.Document;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * RecyclerView adapter for documents
 * <p>
 * Created by constantin.cheptea on 03/10/16.
 */

public class DocumentsAdapter extends RecyclerView.Adapter<DocumentsAdapter.DocumentViewHolder> implements Keys {

	private static final String LOG_TAG = DocumentsAdapter.class.getSimpleName();
	DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy 'at' HH:mm:ss", Locale.getDefault());
	private List<Document> documents;

	public DocumentsAdapter(List<Document> documents) {
		this.documents = documents;
	}

	@Override
	public DocumentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_document, parent, false);

		return new DocumentViewHolder(this, view);
	}

	@Override
	public void onBindViewHolder(DocumentViewHolder holder, int position) {
		Document document = documents.get(position);
		Context context = holder.size.getContext();

		holder.title.setText(documents.get(position).getTitle());
		holder.size.setText(context.getString(R.string.template_size, document.getWidth(), document.getHeight()));
		holder.likes.setText(context.getString(R.string.template_likes, document.getLikes()));
		holder.date.setText(dateFormat.format(document.getDate()));
	}

	@Override
	public int getItemCount() {
		return documents.size();
	}

	public Document getItemAtPosition(int position) {
		return documents.get(position);
	}

	public interface OnDocumentOptionSelected {
		void onDocumentOptionSelected(Document document, int optionId);
	}

	protected static class DocumentViewHolder extends RecyclerView.ViewHolder {
		@BindView(R.id.title)
		TextView title;

		@BindView(R.id.size)
		TextView size;

		@BindView(R.id.likes)
		TextView likes;

		@BindView(R.id.date)
		TextView date;

		private DocumentsAdapter documentsAdapter;

		public DocumentViewHolder(DocumentsAdapter documentsAdapter, View itemView) {
			super(itemView);
			ButterKnife.bind(this, itemView);
			this.documentsAdapter = documentsAdapter;
		}

		@OnClick(R.id.card_content)
		public void onCardClick(View documentView) {
			Log.d(LOG_TAG, "Clicked the Card");

			Intent intent = new Intent(documentView.getContext(), SketchActivity.class);
			intent.putExtra(KEY_DOCUMENT, documentsAdapter.getItemAtPosition(getAdapterPosition()));
			documentView.getContext().startActivity(intent);
		}

		@OnClick(R.id.btn_options)
		public void onCardOptionsClick(final View optionsView) {
			PopupMenu popup = new PopupMenu(optionsView.getContext(), optionsView);
			popup.getMenuInflater().inflate(R.menu.document_menu, popup.getMenu());

			popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
				public boolean onMenuItemClick(MenuItem item) {
					((OnDocumentOptionSelected) (optionsView.getContext()))
							.onDocumentOptionSelected(documentsAdapter
									.getItemAtPosition(getAdapterPosition()), item.getItemId());
					return true;
				}
			});

			popup.show();
		}
	}
}
