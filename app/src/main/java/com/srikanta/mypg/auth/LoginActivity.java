package com.srikanta.mypg.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.srikanta.mypg.HomeActivity;
import com.srikanta.mypg.R;

public class LoginActivity extends AppCompatActivity {

    EditText etEmail, etPassword;
    Button btnLogin;
    TextView tvForgotPassword, tvRequestRegister;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Firebase
        mAuth = FirebaseAuth.getInstance();

        // Bind views
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvRequestRegister = findViewById(R.id.tvRequestRegister);

        // Login button
        btnLogin.setOnClickListener(v -> loginOwner());

        // Forgot password
        tvForgotPassword.setOnClickListener(v -> forgotPassword());

        // Request register
        tvRequestRegister.setOnClickListener(v -> {
            openMobileCheckDialog();
        });

    }

    private void loginOwner() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password required");
            return;
        }

        btnLogin.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        String uid = mAuth.getCurrentUser().getUid();

                        DatabaseReference ownerRef = FirebaseDatabase.getInstance()
                                .getReference("Owners")
                                .child(uid)
                                .child("profile");

                        ownerRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                btnLogin.setEnabled(true);

                                if (snapshot.exists()) {
                                    // ✅ Owner profile exists
                                    Toast.makeText(LoginActivity.this,
                                            "Login successful",
                                            Toast.LENGTH_SHORT).show();

                                    startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                                    finish();
                                } else {
                                    // ❌ Logged in but not an owner
                                    Toast.makeText(LoginActivity.this,
                                            "Account not registered as Owner",
                                            Toast.LENGTH_LONG).show();

                                    mAuth.signOut(); // important
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                btnLogin.setEnabled(true);
                                Toast.makeText(LoginActivity.this,
                                        "Database error: " + error.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            }
                        });

                    } else {
                        btnLogin.setEnabled(true);
                        Toast.makeText(LoginActivity.this,
                                "Login failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void forgotPassword() {
        String email = etEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Enter email to reset password");
            return;
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this,
                                "Password reset link sent to email",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this,
                                "Error: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void openMobileCheckDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder =
                new androidx.appcompat.app.AlertDialog.Builder(this);

        android.view.View view = getLayoutInflater()
                .inflate(R.layout.dialog_check_mobile, null);

        EditText etMobile = view.findViewById(R.id.etDialogMobile);
        Button btnContinue = view.findViewById(R.id.btnCheckMobile);

        builder.setView(view);
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();

        btnContinue.setOnClickListener(v -> {
            String mobile = etMobile.getText().toString().trim();

            if (mobile.length() != 10) {
                etMobile.setError("Enter valid mobile number");
                return;
            }

            checkMobileRequest(mobile, dialog);
        });
    }

    private void checkMobileRequest(String mobile,
                                    androidx.appcompat.app.AlertDialog dialog) {

        DatabaseReference requestRef =
                FirebaseDatabase.getInstance()
                        .getReference("OwnerRequests")
                        .child(mobile); // ✅ mobile as key

        requestRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {
                    String status = snapshot.child("status").getValue(String.class);

                    if ("pending".equals(status)) {
                        Toast.makeText(LoginActivity.this,
                                "Your registration request is already under review",
                                Toast.LENGTH_LONG).show();
                        return; // ⛔ block
                    }
                }

                // ✅ No request OR not pending → proceed
                dialog.dismiss();

                Intent intent = new Intent(
                        LoginActivity.this,
                        RequestRegisterActivity.class);
                intent.putExtra("mobile", mobile);
                startActivity(intent);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LoginActivity.this,
                        error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }


}
