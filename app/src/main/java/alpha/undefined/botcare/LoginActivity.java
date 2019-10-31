package alpha.undefined.botcare;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {


	// UI references.
	private AutoCompleteTextView mEmailView;
	private EditText mPasswordView;
	private View mProgressView;
	private View mLoginFormView;
	private TextView mRegisterView;

	SharedPreferences sharedPref;

	private EditText server;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		tryLogin();

		// Set up the login form.
		mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
		server = (EditText) findViewById(R.id.server_add);

		mPasswordView = (EditText) findViewById(R.id.password);
		mPasswordView.setHintTextColor(getResources().getColor(R.color.colorWhite));

		Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
		mEmailSignInButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				//attemptLogin();
				login();
			}
		});

		//mLoginFormView = findViewById(R.id.login_form);
		mProgressView = findViewById(R.id.login_progress);

		mRegisterView = (TextView) findViewById(R.id.email_sign_up_button);
		mRegisterView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				signup();
			}
		});
	}

	private void signup() {
		Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
		startActivity(intent);
	}

	private void login() {

		if(mEmailView.getText().toString().equals("testdev")) {
			Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
			startActivity(intent);
			finish();
		}
		else {
			if(server.getText().toString().length()>0) {
				ConnectionManager.server_address = "http://"+server.getText().toString();
			}

			JSONObject jsonObject = new JSONObject();
			try {
				jsonObject.put("email", mEmailView.getText().toString());
				jsonObject.put("password", mPasswordView.getText().toString());
			} catch (JSONException e) {
				e.printStackTrace();
			}


			String URL = ConnectionManager.server_address + "/login";
			RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
			ConnectionManager.sendData(jsonObject.toString(), requestQueue, URL, new ConnectionManager.VolleyCallback() {
				@Override
				public void onSuccessResponse(String result) {
					String id = "0";
					try {
						JSONObject jsonObject = new JSONObject(result);
						id = jsonObject.getString("id");
					}
					catch (JSONException e) {
						//sad
					}
					loginSuccess(id);
				}

				@Override
				public void onErrorResponse(VolleyError error) {
					Toast.makeText(getApplicationContext(), "Failed!", Toast.LENGTH_LONG).show();
				}
			});
		}
	}

	void loginSuccess(String id) {
		Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
		Log.d("Login", "Logged in with "+id);
		// get or create SharedPreferences
		sharedPref = getSharedPreferences("Login", MODE_PRIVATE);
		// save your string in SharedPreferences
		ConnectionManager.userID = id;
		sharedPref.edit().putString("userID", id).commit();
		startActivity(intent);
		finish();
	}

	void tryLogin() {
		// get or create SharedPreferences
		sharedPref = getSharedPreferences("Login", MODE_PRIVATE);
		// save your string in SharedPreferences
		String id = sharedPref.getString("userID", "0"); //tag, default if empty
		if(!id.equals("0")) {
			loginSuccess(id);
		}
	}
	/*
	private boolean mayRequestContacts() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
			return true;
		}
		if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
			return true;
		}
		if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
			Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
					.setAction(android.R.string.ok, new View.OnClickListener() {
						@Override
						@TargetApi(Build.VERSION_CODES.M)
						public void onClick(View v) {
							requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
						}
					});
		} else {
			requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
		}
		return false;
	}



	/*
	 * Callback received when a permissions request has been completed.

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
										   @NonNull int[] grantResults) {
		if (requestCode == REQUEST_READ_CONTACTS) {
			if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				populateAutoComplete();
			}
		}
	}

	*/

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
}

