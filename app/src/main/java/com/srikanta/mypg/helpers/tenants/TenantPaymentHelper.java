package com.srikanta.mypg.helpers.tenants;

import com.google.firebase.database.DatabaseReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TenantPaymentHelper {

    public static void createJoiningPayment(
            DatabaseReference rootRef,
            String hostelId,
            String tenantId,
            String monthKey,
            int rent,
            int deposit
    ) {

        DatabaseReference payRef = rootRef
                .child("Hostels")
                .child(hostelId)
                .child("tenants")
                .child(tenantId)
                .child("payments")
                .child(monthKey);

        payRef.child("rentPaid").setValue(rent);
        payRef.child("depositPaid").setValue(deposit);
        payRef.child("paidOn").setValue(
                new SimpleDateFormat(
                        "ddMMyyyyHHmm",
                        Locale.getDefault()
                ).format(new Date())
        );

    }
}
