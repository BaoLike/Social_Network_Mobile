package com.example.social_network.Activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.social_network.Config.AppConfig;
import com.example.social_network.R;
import com.example.social_network.Utils.FetchApi;
import com.example.social_network.Utils.FcmRegistrationHelper;
import com.example.social_network.Utils.TokenManager;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String COLOR_BLUE    = "#3797EF";
    private static final String COLOR_DISABLED = "#9acaf6";
    private final String urlLogin = AppConfig.BASE_URL + "/identity/auth/login";
    private static final String TAG = "MainActivity";
    private EditText etUsername;
    private EditText etPassword;
    private Button   btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {

                    if (!task.isSuccessful()) {
                        Log.w("FCM", "Fetching FCM token failed", task.getException());
                        return;
                    }

                    String token = task.getResult();
                    Log.d("FCM_TOKEN", token);
                    FcmRegistrationHelper.register(MainActivity.this, token);
                });

    // Nếu đã đăng nhập trước đó thì bỏ qua màn login
        if (TokenManager.isLoggedIn(this)) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
            return;

        }

        setupSignUpText();
        setupBackButton();
        setupLoginButton();

    }

    private void setupLoginButton() {
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin   = findViewById(R.id.btnLogin);
        btnLogin.setEnabled(false);

        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                boolean filled = etUsername.getText().length() > 0
                              && etPassword.getText().length() > 0;

                btnLogin.setEnabled(filled);

                GradientDrawable bg = (GradientDrawable) btnLogin.getBackground().mutate();
                bg.setColor(Color.parseColor(filled ? COLOR_BLUE : COLOR_DISABLED));
            }
        };

        etUsername.addTextChangedListener(watcher);
        etPassword.addTextChangedListener(watcher);
        btnLogin.setOnClickListener(v -> {
            FetchApi fetchApi = new FetchApi();
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("userName", etUsername.getText().toString().trim());
            requestBody.put("password", etPassword.getText().toString().trim());
            btnLogin.setEnabled(false);
            btnLogin.setText("Logging ...");
            fetchApi.postLogin(urlLogin, requestBody, MainActivity.this, TAG, new FetchApi.ApiCallback() {

                @Override
                public void onSuccess() {
                    FcmRegistrationHelper.registerWithCurrentToken(MainActivity.this);
                    startActivity(new Intent(MainActivity.this, HomeActivity.class));
                    finish();
                }

                @Override
                public void onFailure(String message) {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Login");
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private void setupSignUpText() {
        TextView tvSignUp = findViewById(R.id.tvSignUp);
        String fullText = "Don't have an account? Sign up.";
        SpannableString spannable = new SpannableString(fullText);

        int start = fullText.indexOf("Sign up.");
        int end   = start + "Sign up.".length();
        spannable.setSpan(
                new ForegroundColorSpan(Color.parseColor(COLOR_BLUE)),
                start, end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        tvSignUp.setText(spannable);
        tvSignUp.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void setupBackButton() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> onBackPressed());
    }



}