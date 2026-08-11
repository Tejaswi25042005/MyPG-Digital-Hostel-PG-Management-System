package com.srikanta.mypg.helpers;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.srikanta.mypg.SubscriptionPlansActivity;

public class SubscriptionPurchaseHelper {

    private final Activity activity;
    private final String hostelId;

    public SubscriptionPurchaseHelper(@NonNull Activity activity,
                                      @NonNull String hostelId) {
        this.activity = activity;
        this.hostelId = hostelId;
    }

    /**
     * Entry point from anywhere in the app
     * Example → Expired dialog / Profile / Banner / Dashboard
     */
    public void startPurchase() {

        if (hostelId == null || hostelId.isEmpty()) {
            Toast.makeText(activity,
                    "Hostel not selected",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(activity, SubscriptionPlansActivity.class);
        intent.putExtra("hostelId", hostelId);

        activity.startActivity(intent);
    }
}
