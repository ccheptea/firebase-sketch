package com.cheptea.cc.firebasesketch.widgets;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cheptea.cc.firebasesketch.R;
import com.cheptea.cc.firebasesketch.dialogs.CreateDocumentDialog;

import java.util.List;

import butterknife.BindColor;
import butterknife.BindDrawable;
import butterknife.BindViews;
import butterknife.ButterKnife;

/**
 * Convenience view to hold the controls
 * Created by constantin.cheptea on 07/10/16.
 */

public class Controls extends RelativeLayout implements View.OnClickListener {

	private static final String LOG_TAG = CreateDocumentDialog.class.getSimpleName();

	@BindViews({R.id.btn_draw, R.id.btn_move, R.id.btn_erase_all})
	List<TextView> tools;

	@BindDrawable(R.drawable.tool_background_selected)
	Drawable toolBackgroundSelected;

	@BindDrawable(R.drawable.tool_background_normal)
	Drawable toolBackgroundNormal;

	@BindColor(R.color.tool_selected_text)
	int toolSelectedText;

	@BindColor(R.color.tool_normal_text)
	int toolNormalText;

	OnControlSelectedListener onControlSelectedListener;
	OnClickListener toolClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			for (TextView textView : tools) {
				textView.setTextColor(toolNormalText);
				textView.setBackgroundResource(R.drawable.tool_background_normal);
			}

			((TextView) v).setTextColor(toolSelectedText);
			v.setBackgroundResource(R.drawable.tool_background_selected);

			if (onControlSelectedListener != null) {
				onControlSelectedListener.onControlSelected(v);
			}
		}
	};

	public Controls(Context context) {
		super(context);
	}

	public Controls(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public Controls(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	public Controls(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	public void init() {
		ButterKnife.bind(this, this);
		for (View view : tools) {
			view.setOnClickListener(toolClickListener);
		}
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		init();
	}

	@Override
	public void onClick(View v) {
		if (onControlSelectedListener != null) {
			onControlSelectedListener.onControlSelected(v);
		}
	}

	public void setOnControlSelectedListener(OnControlSelectedListener onControlSelectedListener) {
		this.onControlSelectedListener = onControlSelectedListener;
	}

	public interface OnControlSelectedListener {
		void onControlSelected(View controlView);
	}

}
