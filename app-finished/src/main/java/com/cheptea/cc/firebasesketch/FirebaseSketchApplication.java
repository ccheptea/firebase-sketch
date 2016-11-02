package com.cheptea.cc.firebasesketch;

import android.app.Application;

import com.cheptea.cc.firebasesketch.constants.Keys;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeModule;

/**
 * Created by constantin.cheptea on 06/10/16.
 */

public class FirebaseSketchApplication extends Application implements Keys {

	private static final String LOG_TAG = FirebaseSketchApplication.class.getSimpleName();

	@Override
	public void onCreate() {
		super.onCreate();

		Iconify.with(new FontAwesomeModule());
	}
}
