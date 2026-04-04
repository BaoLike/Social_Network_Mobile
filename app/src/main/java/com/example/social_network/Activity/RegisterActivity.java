package com.example.social_network.Activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.social_network.Config.AppConfig;
import com.example.social_network.R;
import com.example.social_network.Utils.FetchApi;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText etFirstName, etLastName, etEmail;
    private EditText etUsername, etPassword;
    private EditText etGender, etDob, etAddress;
    private final String urlRegister = AppConfig.BASE_URL + "/identity/user/create";
    private static final String TAG = "RegisterActivity";

    private final String[] genderOptions = {"Male", "Female", "Other"};

    // Callback interface vì Volley là async
    interface RegisterCallback {
        void onSuccess();
        void onFailure(String message);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        bindViews();
        setupGenderPicker();
        setupDobPicker();
        setupSignUpButton();
        setupLoginLink();
        setupBackButton();
    }

    private void bindViews() {
        etFirstName = findViewById(R.id.etFirstName);
        etLastName  = findViewById(R.id.etLastName);
        etEmail     = findViewById(R.id.etEmail);
        etUsername  = findViewById(R.id.etUsername);
        etPassword  = findViewById(R.id.etPassword);
        etGender    = findViewById(R.id.etGender);
        etDob       = findViewById(R.id.etDob);
        etAddress   = findViewById(R.id.etAddress);
    }

    private void setupGenderPicker() {
        etGender.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Select Gender")
                    .setItems(genderOptions, (dialog, which) ->
                            etGender.setText(genderOptions[which]))
                    .show();
        });
    }

    private void setupDobPicker() {
        etDob.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            int year  = cal.get(Calendar.YEAR) - 18;
            int month = cal.get(Calendar.MONTH);
            int day   = cal.get(Calendar.DAY_OF_MONTH);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

            DatePickerDialog dialog = new DatePickerDialog(this,
                    (datePicker, y, m, d) -> {
                        Calendar selected = Calendar.getInstance();
                        selected.set(y, m, d);
                        etDob.setText(sdf.format(selected.getTime()));
                    }, year, month, day);

            dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
            dialog.show();
        });
    }

    private void setupSignUpButton() {
        Button btnSignUp = findViewById(R.id.btnSignUp);
        btnSignUp.setOnClickListener(v -> {
            if (!validateFields()) return;
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("userName",  etUsername.getText().toString().trim());
            requestBody.put("password",  etPassword.getText().toString().trim());
            requestBody.put("email",     etEmail.getText().toString().trim());
            requestBody.put("firstName", etFirstName.getText().toString().trim());
            requestBody.put("lastName",  etLastName.getText().toString().trim());
            requestBody.put("gender",    etGender.getText().toString().trim());
            requestBody.put("dob",       etDob.getText().toString().trim());
            requestBody.put("address",   etAddress.getText().toString().trim());

            btnSignUp.setEnabled(false);
            btnSignUp.setText("Signing up...");
            FetchApi fetchApi = new FetchApi();
            fetchApi.postRegister(urlRegister, requestBody,RegisterActivity.this, TAG, new FetchApi.ApiCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(RegisterActivity.this,
                            "Đăng ký tài khoản thành công!", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                    finish();
                }

                @Override
                public void onFailure(String message) {
                    btnSignUp.setEnabled(true);
                    btnSignUp.setText("Sign up");
                    Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_LONG).show();
                }
            });
        });
    }


    private boolean validateFields() {
        if (isEmpty(etFirstName)) return showError(etFirstName, "Please enter your first name");
        if (isEmpty(etLastName))  return showError(etLastName,  "Please enter your last name");
        if (isEmpty(etEmail))     return showError(etEmail,     "Please enter your email");
        if (!isValidEmail())      return showError(etEmail,     "Please enter a valid email");
        if (isEmpty(etUsername))  return showError(etUsername,  "Please enter a username");
        if (isEmpty(etPassword))  return showError(etPassword,  "Password must be at least 6 characters");
        if (etPassword.getText().length() < 6) return showError(etPassword, "Password must be at least 6 characters");
        if (isEmpty(etGender))    return showError(etGender,    "Please select your gender");
        if (isEmpty(etDob))       return showError(etDob,       "Please select your date of birth");
        if (isEmpty(etAddress))   return showError(etAddress,   "Please enter your address");
        return true;
    }

    private boolean isEmpty(EditText et) {
        return et.getText().toString().trim().isEmpty();
    }

    private boolean isValidEmail() {
        String email = etEmail.getText().toString().trim();
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean showError(EditText field, String message) {
        field.setError(message);
        field.requestFocus();
        return false;
    }

    private void setupLoginLink() {
        TextView tvLogin = findViewById(R.id.tvLogin);
        String full = "Already have an account? Log in.";
        SpannableString spannable = new SpannableString(full);
        int start = full.indexOf("Log in.");
        spannable.setSpan(
                new ForegroundColorSpan(0xFF3797EF),
                start, start + "Log in.".length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvLogin.setText(spannable);
        tvLogin.setOnClickListener(v -> finish());
    }

    private void setupBackButton() {
        findViewById(R.id.btnBack).setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
        });
    }
}
