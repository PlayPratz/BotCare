package alpha.undefined.botcare;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import java.util.Calendar;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import static android.app.Notification.EXTRA_NOTIFICATION_ID;

public class ChatActivity extends AppCompatActivity {

	//File
	private final String filename = "encrypted_messages.bcm";
	String CHANNEL_ID1 = "Dosage_Channel";
	String CHANNEL_ID2 = "Doctor_Channel";

	//Server details
	//String server_address = "http://192.168.43.24:8081";
	Socket socket;

	//Message type declaration
	public static final int message_query = 1;
	public static final int message_response = 2;
	public static final int message_doctor = 3;
	public static final int message_dose = 4;
	public static final int message_appointment = 5;
	public static final int message_image_query = 6;

	static final int REQUEST_IMAGE_CAPTURE = 1;

	ImageButton sendButton, attachButton;
	Button settingsButton, profileButton, deleteButton;
	EditText editText;
	RecyclerView messageView;

	ArrayList<Message> messageList = new ArrayList<>();
	MessageAdapter messageAdapter;
	LinearLayoutManager linearLayoutManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		loadMessages();
		socketMaker();
		createNotificationChannel();

	//	messageList.add(new Message("Chalti Hai Kya 9 se 12?", 1, new Date()));

		editText = (EditText) findViewById(R.id.editQuery);
		sendButton = (ImageButton) findViewById(R.id.sendButton);
		attachButton = (ImageButton) findViewById(R.id.attachButton);
		settingsButton = (Button) findViewById(R.id.settingsButton);
		deleteButton = (Button) findViewById(R.id.deleteButton);
		//profileButton = (Button) findViewById(R.id.profileButton);

		messageView = (RecyclerView) findViewById(R.id.messagerecyclerview);

		messageAdapter = new MessageAdapter(messageList);
		linearLayoutManager = new LinearLayoutManager(this);
		linearLayoutManager.setStackFromEnd(true);
		messageView.setLayoutManager(linearLayoutManager);
		messageView.setAdapter(messageAdapter);

		refresh();

		sendButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(getMessage().length()>0){
					sendMessage();
				}
			}
		});

		attachButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				takePhoto();
				askForPermission(Manifest.permission.CAMERA,0);
			}
		});

		settingsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
				//startActivity(intent);
				new AlertDialog.Builder(ChatActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_DARK)
						.setTitle("Log Out")
						.setMessage("Do you really want to log out? This will clear your chat history.")
						.setIcon(android.R.drawable.ic_dialog_alert)
						.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								deleteMessages();
								logOut();
							}})
						.setNegativeButton(android.R.string.no, null).show();

			}
		});

		deleteButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				new AlertDialog.Builder(ChatActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_DARK)
						.setTitle("Clear History")
						.setMessage("Do you really want to delete your entire chat history?")
						.setIcon(android.R.drawable.ic_dialog_alert)
						.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								deleteMessages();
							}})
						.setNegativeButton(android.R.string.no, null).show();
			}
		});



	}

	private void askForPermission(String permission, Integer requestCode) {
		if (ContextCompat.checkSelfPermission(getApplicationContext(), permission) != PackageManager.PERMISSION_GRANTED) {

			// Should we show an explanation?
			if (ActivityCompat.shouldShowRequestPermissionRationale(ChatActivity.this, permission)) {

				//This is called if user has denied the permission before
				//In this case I am just asking the permission again
				ActivityCompat.requestPermissions(ChatActivity.this, new String[]{permission}, requestCode);

			} else {

				ActivityCompat.requestPermissions(ChatActivity.this, new String[]{permission}, requestCode);
			}
		} else {
			//Toast.makeText(this, "" + permission + " is already granted.", Toast.LENGTH_SHORT).show();
		}
	}

	private void takePhoto() {
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
			startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);

		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
			Bundle extras = data.getExtras();
			Bitmap imageBitmap = (Bitmap) extras.get("data");
			sendPhoto(imageBitmap);
		}
	}

	private void sendPhoto(Bitmap bitmap) {
		messageList.add(new Message(bitmap, message_image_query));
		refresh();
	}
	private String getMessage(){
		return editText.getText().toString();
	}

	private void sendMessage() {
		String message = getMessage();
		editText.getText().clear();
		messageList.add(new Message(message, message_query));
		refresh();
		socket.emit("newMessage", message);
		refresh();
		Log.i("Messages", messageList.get(messageList.size()-1).getText());
	}

	private void refresh() {
		saveMessages();
		messageAdapter.notifyDataSetChanged();
		messageView.scrollToPosition(messageList.size()-1);
	}

	private void socketMaker() {

		try {
			socket = IO.socket(ConnectionManager.server_address);
			socket.connect();

			//Establish session
			socket.emit("newID", ConnectionManager.userID);

			socket.on("newRegular", new Emitter.Listener() {
				@Override
				public void call(final Object... args) {
					// Create an explicit intent for an Activity in your app

					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							messageList.add(new Message(args[0].toString(), message_response));
							refresh();
						}
					});
				}
			});

			socket.on("newAlert", new Emitter.Listener() {
				@Override
				public void call(final Object... args) {
					// Create an explicit intent for an Activity in your app
					messageNotifications(getString(R.string.alert_dosage), args[0].toString());
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							messageList.add(new Message(args[0].toString(), message_response));
							refresh();
						}
					});
				}
			});

			socket.on("newDoctorResponse", new Emitter.Listener() {
				@Override
				public void call(final Object... args) {
					// Create an explicit intent for an Activity in your app
					messageNotifications(getString(R.string.alert_doctorresponse), args[0].toString());
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							messageList.add(new Message(args[0].toString(), message_response));
							refresh();
						}
					});
				}
			});

			socket.on("newDoctor", new Emitter.Listener() {
				@Override
				public void call(final Object... args) {;

					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							try {
								JSONObject jsonObject = new JSONObject(args[0].toString());
								messageList.add(new Message(jsonObject.getString("name"),
										jsonObject.getString("spec"),
										jsonObject.getString("contact"),
										message_doctor));
								refresh();
							} catch (JSONException e) {
								e.printStackTrace();
							}

						}
					});
				}
			});

			socket.on("newDose", new Emitter.Listener() {
				@Override
				public void call(final Object... args) {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							try {
								JSONObject jsonObject = new JSONObject(args[0].toString());
								messageList.add(new Message(jsonObject.getString("drug"),
										jsonObject.getString("unit"),
										jsonObject.getString("time"),
										message_dose));
								refresh();
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					});
				}
			});

			socket.on("newAppointment", new Emitter.Listener() {
				@Override
				public void call(final Object... args) {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							try {
								JSONObject jsonObject = new JSONObject(args[0].toString());
								messageList.add(new Message(jsonObject.getString("name"),
										jsonObject.getString("date"),
										jsonObject.getString("type"),
										message_appointment));
								refresh();
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					});
				}
			});

			socket.on("changeAppointment", new Emitter.Listener() {
				@Override
				public void call(final Object... args) {
					// Create an explicit intent for an Activity in your app
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							datePick();
							refresh();
						}
					});
				}

			});

		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	private void saveMessages() {
		try {
			FileOutputStream fileOutputStream = openFileOutput(filename, Context.MODE_PRIVATE);
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
			objectOutputStream.writeObject(messageList);
			objectOutputStream.close();
		} catch (FileNotFoundException e){
			e.printStackTrace();
			Log.e("savemessage", "File Not Found");
		} catch (IOException e) {
			e.printStackTrace();
			Log.e("savemessage", e.toString());
		}
	}

	private void loadMessages() {
		try {
			FileInputStream fileInputStream = openFileInput(filename);
			ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
			messageList.clear();
			messageList = (ArrayList<Message>) objectInputStream.readObject();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	void deleteMessages() {
		messageList.clear();
		saveMessages();
		refresh();
	}

	void logOut() {
		SharedPreferences sharedPref = getSharedPreferences("Login", MODE_PRIVATE);
		sharedPref.edit().putString("userID", "0").commit();
		Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
		startActivity(intent);
		finish();
	}

	private void createNotificationChannel() {
		// Create the NotificationChannel, but only on API 26+ because
		// the NotificationChannel class is new and not in the support library
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

			NotificationChannel dosageChannel = new NotificationChannel(CHANNEL_ID1,
					getString(R.string.alert_channel_dosage),
					NotificationManager.IMPORTANCE_DEFAULT);
			dosageChannel.setDescription(getString(R.string.alert_channel_dosage_description));

			NotificationChannel doctorChannel = new NotificationChannel(CHANNEL_ID2,
					getString(R.string.alert_channel_doctorresponse),
					NotificationManager.IMPORTANCE_DEFAULT);
			doctorChannel.setDescription(getString(R.string.alert_channel_doctorresponse_description));
			// Register the channel with the system; you can't change the importance
			// or other notification behaviors after this
			NotificationManager notificationManager = getSystemService(NotificationManager.class);
			notificationManager.createNotificationChannel(dosageChannel);
			notificationManager.createNotificationChannel(doctorChannel);
		}
	}

	private void messageNotifications(String title, String message) {

		Intent intent = new Intent(ChatActivity.this, ChatActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		PendingIntent pendingIntent = PendingIntent.getActivity(ChatActivity.this, 0, intent, 0);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(ChatActivity.this, CHANNEL_ID1)
				.setSmallIcon(R.drawable.ic_alarm_add_black_24dp)
				.setContentTitle(title)
				.setContentText(message)
				.setPriority(NotificationCompat.PRIORITY_DEFAULT)
				// Set the intent that will fire when the user taps the notification
				.setContentIntent(pendingIntent)
				.setAutoCancel(true)
				.setOngoing(true);
		NotificationManagerCompat notificationManager = NotificationManagerCompat.from(ChatActivity.this);
		notificationManager.notify(1, builder.build());
	}

	public void datePick() {
		int mYear, mMonth, mDay, mHour, mMinute;
		final Calendar c = Calendar.getInstance();
		mYear = c.get(Calendar.YEAR);
		mMonth = c.get(Calendar.MONTH);
		mDay = c.get(Calendar.DAY_OF_MONTH);
		DatePickerDialog datePickerDialog = new DatePickerDialog(ChatActivity.this, R.style.DialogTheme,
				new DatePickerDialog.OnDateSetListener() {

					@Override
					public void onDateSet(DatePicker view, int year,
					                      int monthOfYear, int dayOfMonth) {
						String newDate = year + "-" + String.format("%02d", monthOfYear+1) + "-" + String.format("%02d", dayOfMonth);
						socket.emit("changedAppointment", newDate );
						Log.d("AppointmentChange", newDate);
					}
				}, mYear, mMonth, mDay);
		datePickerDialog.show();
	}


}
