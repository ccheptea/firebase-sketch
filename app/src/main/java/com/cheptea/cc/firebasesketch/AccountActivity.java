package com.cheptea.cc.firebasesketch;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Screen to manage the account.
 * <p>
 * Created by constantin.cheptea on 13/10/16.
 */

public class AccountActivity extends AppCompatActivity implements FirebaseAuth.AuthStateListener {

	@BindView(R.id.toolbar)
	Toolbar toolbar;

	@BindView(R.id.account_info)
	ViewGroup containerAccountInfo;

	@BindView(R.id.display_name)
	TextView displayNameView;

	@BindView(R.id.login_form)
	ViewGroup containerLoginForm;

	@BindView(R.id.input_email)
	EditText emailInputView;

	@BindView(R.id.input_password)
	EditText passwordInputView;

	ProgressDialog progressDialog;

	FirebaseAuth auth = FirebaseAuth.getInstance();

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_account);

		ButterKnife.bind(this);

		setSupportActionBar(toolbar);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);

		setDebugValues();

	}

	private void setDebugValues() {
		if (BuildConfig.DEBUG) {
			emailInputView.setText("your.email@example.com");
			passwordInputView.setText("qwerty");
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		auth.addAuthStateListener(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		auth.removeAuthStateListener(this);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@OnClick(R.id.btn_sign_up)
	public void signUp() {
		if (fieldsAreValid()) {
			popSignInProgressDialog();
			auth.createUserWithEmailAndPassword(emailInputView.getText().toString(), passwordInputView.getText().toString())
					.addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
						@Override
						public void onComplete(@NonNull Task<AuthResult> task) {
							closeProgressDialog();
							if (task.isSuccessful()) {
								Toast.makeText(AccountActivity.this, "Sign up successful", Toast.LENGTH_SHORT).show();
							} else {
								Toast.makeText(AccountActivity.this, "Sign up failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
							}
						}
					});
		} else {
			Toast.makeText(AccountActivity.this, "Email & Password cannot be empty!", Toast.LENGTH_SHORT).show();
		}
	}

	@OnClick(R.id.btn_sign_in)
	public void signIn() {
		if (fieldsAreValid()) {
			popSignInProgressDialog();
			auth.signInWithEmailAndPassword(emailInputView.getText().toString(), passwordInputView.getText().toString())
					.addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
						@Override
						public void onComplete(@NonNull Task<AuthResult> task) {
							closeProgressDialog();
							if (task.isSuccessful()) {
								Toast.makeText(AccountActivity.this, "Sign in successful", Toast.LENGTH_SHORT).show();
							} else {
								Toast.makeText(AccountActivity.this, "Sign in failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
							}
						}
					});
		} else {
			Toast.makeText(AccountActivity.this, "Email & Password cannot be empty!", Toast.LENGTH_SHORT).show();
		}
	}

	private boolean fieldsAreValid() {
		return !TextUtils.isEmpty(emailInputView.getText()) && !TextUtils.isEmpty(passwordInputView.getText());
	}

	@OnClick(R.id.btn_sign_out)
	public void signOut() {
		auth.signOut();
	}

	@Override
	public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
		FirebaseUser user = firebaseAuth.getCurrentUser();
		if (user == null) {
			updateUIAfterSignOut();
		} else {
			updateUIAfterSignIn(user);
		}
	}

	private void updateUIAfterSignOut() {
		containerLoginForm.setVisibility(View.VISIBLE);
		containerAccountInfo.setVisibility(View.GONE);
	}

	private void updateUIAfterSignIn(FirebaseUser user) {
		displayNameView.setText(user.getEmail());

		containerLoginForm.setVisibility(View.GONE);
		containerAccountInfo.setVisibility(View.VISIBLE);
	}

	private void popSignInProgressDialog() {
		popProgressDialog(getString(R.string.title_progress), getString(R.string.label_sign_in_progress));
	}

	private void popProgressDialog(String title, String message) {
		if (progressDialog == null) {
			progressDialog = new ProgressDialog(this);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		}

		progressDialog.setTitle(title);
		progressDialog.setMessage(message);

		progressDialog.show();
	}

	private void closeProgressDialog() {
		if (progressDialog != null)
			progressDialog.cancel();
	}

}
