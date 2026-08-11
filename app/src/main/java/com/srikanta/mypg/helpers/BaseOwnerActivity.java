package com.srikanta.mypg.helpers;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.srikanta.mypg.auth.LoginActivity;

public abstract class BaseOwnerActivity extends AppCompatActivity {

    private boolean ownerVerified = false; // 🔒 prevents repeat checks

    @Override
    protected void onStart() {
        super.onStart();

        // 1️⃣ Firebase Auth check
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            redirectToLogin();
            return;
        }

        // 2️⃣ Verify owner ONLY ONCE
        if (ownerVerified) return;

        String ownerUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseDatabase.getInstance()
                .getReference("Owners")
                .child(ownerUid)
                .child("profile") // ✅ FIXED (lowercase)
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (!snapshot.exists()) {
                        // ❌ Owner profile missing → block access
                        redirectToLogin();
                    } else {
                        // ✅ Owner verified
                        ownerVerified = true;
                    }
                })
                .addOnFailureListener(e -> {
                    // Network / permission issue → do NOT sign out
                    // Just block access safely
                    redirectToLogin();
                });
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );
        startActivity(intent);
        finish();
    }
}
