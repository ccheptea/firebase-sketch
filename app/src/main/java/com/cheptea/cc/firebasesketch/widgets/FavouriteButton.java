package com.cheptea.cc.firebasesketch.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.cheptea.cc.firebasesketch.R;
import com.cheptea.cc.firebasesketch.listeners.OnOffViewStateListener;
import com.joanzapata.iconify.widget.IconTextView;

import butterknife.BindColor;
import butterknife.ButterKnife;

/**
 * Created by constantin.cheptea on 12/10/16.
 */

public class FavouriteButton extends IconTextView implements View.OnClickListener {

	private final String iconTextOn = "{fa-thumbs-up}";
	private final String iconTextOff = "{fa-thumbs-o-up}";
	@BindColor(R.color.favourite_on_default)
	int colorOn;
	@BindColor(R.color.favourite_off_default)
	int colorOff;
	OnOffViewStateListener listener;
	private boolean isOn = false;

	public FavouriteButton(Context context) {
		super(context);
		init();
	}

	public FavouriteButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public FavouriteButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		ButterKnife.bind(this, this);
		setOnClickListener(this);
		setOn(false);
	}

	public void setOn(boolean isOn) {
		this.isOn = isOn;
		if (isOn) {
			setText(iconTextOn);
			setTextColor(colorOn);
		} else {
			setText(iconTextOff);
			setTextColor(colorOff);
		}
	}

	public void setOnOffStateListener(OnOffViewStateListener listener) {
		this.listener = listener;
	}

	@Override
	public void onClick(View v) {
		setOn(!this.isOn);
		if (listener != null) {
			listener.onViewStateChanged(this, this.isOn);
		}
	}
}
