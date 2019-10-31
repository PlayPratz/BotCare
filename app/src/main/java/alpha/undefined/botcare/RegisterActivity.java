package alpha.undefined.botcare;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

public class RegisterActivity extends AppCompatActivity {

    private EditText mName, mEmail, mPass, mConfirmPass, mDob, mAddress;
    private Button send;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mName = (EditText) findViewById(R.id.name);
        mEmail = (EditText) findViewById(R.id.email);
        mPass = (EditText) findViewById(R.id.password);
        mConfirmPass = (EditText) findViewById(R.id.confirmpassword);
        mDob = (EditText) findViewById(R.id.dob);
        mAddress = (EditText) findViewById(R.id.address);

        mDob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                datePick();
            }
        });

        send = (Button) findViewById(R.id.email_sign_up_button);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkError();
            }
        });

    }

    void checkError() {
        int flag=0;
        if(mPass.getText().toString().length()<8) {
            mPass.setError("Password must require minimum 8 characters");
            flag=1;
        }
        if(!mPass.getText().toString().equals(mConfirmPass.getText().toString())) {
            mPass.setError("Password does not match Confirm Password");
            mConfirmPass.setError("Password does not match Confirm Password");
            flag=1;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(mEmail.getText().toString()).matches()) {
            mEmail.setError("Must be a valid E-mail");
            flag=1;
        }
        if(mAddress.getText().toString().length()==0) {
            mAddress.setError("Must not be empty");
            flag=1;
        }
        if(mDob.getText().toString().length()==0) {
            mDob.setError("Must not be empty");
            flag=1;
        }
        if(flag==0) {
            register();
        }
    }

    private void register() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("name", mName.getText());
            jsonObject.put("address", mAddress.getText());
            jsonObject.put("age", mDob.getText());
            jsonObject.put("email", mEmail.getText());
            jsonObject.put("contact", "9921673212");
            jsonObject.put("password", mPass.getText());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestQueue requestQueue = Volley.newRequestQueue(RegisterActivity.this);
        String URL = ConnectionManager.server_address+"/register";

        ConnectionManager.sendData(jsonObject.toString(), requestQueue, URL, new ConnectionManager.VolleyCallback() {
            @Override
            public void onSuccessResponse(String result) {
                finish();
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Works * -1", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void datePick() {
        int mYear, mMonth, mDay, mHour, mMinute;
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(RegisterActivity.this, R.style.DialogTheme,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {

                        String year1 = Integer.toString(year);
                        String month1 = Integer.toString(monthOfYear+1);
                        String day1 = Integer.toString(dayOfMonth);

                        mDob.setText(day1+"/"+month1+"/"+year1);
                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }
}
