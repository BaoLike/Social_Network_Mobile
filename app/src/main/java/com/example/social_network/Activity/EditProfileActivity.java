package com.example.social_network.Activity;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.social_network.Model.UserProfile;
import com.example.social_network.Network.ProfileApiService;
import com.example.social_network.R;
import com.google.android.material.button.MaterialButton;

public class EditProfileActivity extends AppCompatActivity {

    private TextView tvUsername;
    private TextView tvPhone;
    private EditText etLastName;
    private EditText etFirstName;
    private EditText etDob;
    private EditText etAddress;
    private MaterialButton btnSave;

    private final ProfileApiService profileApi = new ProfileApiService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        tvUsername = findViewById(R.id.tvEditProfileUsername);
        tvPhone = findViewById(R.id.tvEditProfilePhone);
        etLastName = findViewById(R.id.etEditLastName);
        etFirstName = findViewById(R.id.etEditFirstName);
        etDob = findViewById(R.id.etEditDob);
        etAddress = findViewById(R.id.etEditAddress);
        btnSave = findViewById(R.id.btnSaveProfile);

        findViewById(R.id.btnEditProfileBack).setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveProfile());

        loadProfile();
    }

    private void loadProfile() {
        btnSave.setEnabled(false);
        profileApi.getMyProfile(this, new ProfileApiService.ProfileCallback() {
            @Override
            public void onSuccess(UserProfile profile) {
                runOnUiThread(() -> {
                    tvUsername.setText(profile.getUserName() != null && !profile.getUserName().isEmpty()
                            ? profile.getUserName() : "—");
                    tvPhone.setText(profile.getPhone() != null && !profile.getPhone().isEmpty()
                            ? profile.getPhone() : "—");
                    etLastName.setText(profile.getLastName() != null ? profile.getLastName() : "");
                    etFirstName.setText(profile.getFirstName() != null ? profile.getFirstName() : "");
                    etDob.setText(profile.getDob() != null ? profile.getDob() : "");
                    etAddress.setText(profile.getAddress() != null ? profile.getAddress() : "");
                    btnSave.setEnabled(true);
                });
            }

            @Override
            public void onFailure(String message) {
                runOnUiThread(() -> {
                    Toast.makeText(EditProfileActivity.this, message, Toast.LENGTH_LONG).show();
                    btnSave.setEnabled(true);
                });
            }
        });
    }

    private void saveProfile() {
        String last = etLastName.getText().toString().trim();
        String first = etFirstName.getText().toString().trim();
        String dob = etDob.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        btnSave.setEnabled(false);
        profileApi.updateMyProfile(this, first, last, dob, address, new ProfileApiService.ProfileCallback() {
            @Override
            public void onSuccess(UserProfile profile) {
                runOnUiThread(() -> {
                    Toast.makeText(EditProfileActivity.this, "Đã cập nhật hồ sơ", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                });
            }

            @Override
            public void onFailure(String message) {
                runOnUiThread(() -> {
                    Toast.makeText(EditProfileActivity.this, message, Toast.LENGTH_LONG).show();
                    btnSave.setEnabled(true);
                });
            }
        });
    }
}
