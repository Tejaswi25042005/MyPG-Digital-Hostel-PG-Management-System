package com.srikanta.mypg.helpers.tenants;

import android.content.Context;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class TenantAccountHelper {

    public interface Callback {
        void onSuccess(String tenantUid);
        void onFailure(String error);
    }

    public static void createTenantAccount(
            Context context,
            String email,
            String tempPassword,
            Callback callback
    ) {

        FirebaseApp tenantApp;

        try {
            tenantApp = FirebaseApp.initializeApp(
                    context,
                    FirebaseOptions.fromResource(context),
                    "TenantApp"
            );
        } catch (IllegalStateException e) {
            tenantApp = FirebaseApp.getInstance("TenantApp");
        }

        FirebaseAuth tenantAuth = FirebaseAuth.getInstance(tenantApp);

        tenantAuth.createUserWithEmailAndPassword(email, tempPassword)
                .addOnSuccessListener(result -> {
                    FirebaseUser user = result.getUser();
                    if (user != null) {
                        callback.onSuccess(user.getUid());
                    } else {
                        callback.onFailure("User creation failed");
                    }
                })
                .addOnFailureListener(e ->
                        callback.onFailure(e.getMessage())
                );
    }
}
