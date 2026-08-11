package com.srikanta.mypg.helpers;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SubscriptionChecker {

    public interface SubscriptionCallback {
        void onResult(boolean isActive, String plan, String endDate);
    }

    public static void check(String hostelId,
                             @NonNull SubscriptionCallback callback) {

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("Hostels")
                .child(hostelId)
                .child("subscription");

        ref.get().addOnSuccessListener(snapshot -> {

            if (!snapshot.exists()) {
                callback.onResult(false, "NONE", "");
                return;
            }

            String plan = snapshot.child("plan").getValue(String.class);
            String endDate = snapshot.child("endDate").getValue(String.class);

            boolean active = isActive(endDate);

            // OPTIONAL → update DB status
            ref.child("status").setValue(active ? "active" : "expired");

            callback.onResult(active, plan, endDate);
        });
    }

    private static boolean isActive(String endDate) {

        try {
            SimpleDateFormat sdf =
                    new SimpleDateFormat("ddMMyyyy", Locale.getDefault());

            Date today = new Date();
            Date expiry = sdf.parse(endDate);

            return !today.after(expiry);

        } catch (Exception e) {
            return false;
        }
    }
}
