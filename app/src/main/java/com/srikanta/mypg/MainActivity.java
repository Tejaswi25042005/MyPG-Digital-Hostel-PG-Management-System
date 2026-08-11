package com.srikanta.mypg;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.srikanta.mypg.auth.LoginActivity;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    DatabaseReference ownerDetailsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {

            String uid = currentUser.getUid();

            ownerDetailsRef = FirebaseDatabase.getInstance()
                    .getReference("Owners")
                    .child(uid)
                    .child("profile");

            ownerDetailsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    if (snapshot.exists()) {
                        // Owner profile exists → go to home
                        startActivity(new Intent(MainActivity.this, HomeActivity.class));
                    } else {
                        // Logged in but profile missing
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    }
                    finish();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                }
            });

        } else {
            // Not logged in
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }
    }
}
