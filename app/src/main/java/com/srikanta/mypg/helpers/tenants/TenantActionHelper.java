package com.srikanta.mypg.helpers.tenants;

import com.google.firebase.database.DatabaseReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TenantActionHelper {

    public static void logTenantAdded(
            DatabaseReference rootRef,
            String hostelId,
            String tenantId,
            String tenantName,
            int roomNo,
            int floorNo,
            String assignedAt   // ddMMyyyyHHmm
    ) {

        Date assignedDate;
        try {
            assignedDate = new SimpleDateFormat(
                    "ddMMyyyyHHmm",
                    Locale.getDefault()
            ).parse(assignedAt);
        } catch (ParseException e) {
            // fallback to now (never crash action logging)
            assignedDate = new Date();
        }

        // ✅ Month bucket (yyyy-MM)
        String monthYear = new SimpleDateFormat(
                "yyyy-MM",
                Locale.getDefault()
        ).format(assignedDate);

        DatabaseReference ref = rootRef
                .child("Hostels")
                .child(hostelId)
                .child("actions")
                .child(monthYear)
                .push();

        ref.child("actionType").setValue("TENANT_ADDED");
        ref.child("category").setValue("TENANT");
        ref.child("title").setValue("Tenant Added");
        ref.child("description")
                .setValue(tenantName + " joined Room " + roomNo);
        ref.child("tenantId").setValue(tenantId);
        ref.child("tenantName").setValue(tenantName);
        ref.child("roomNumber").setValue(roomNo);
        ref.child("floorNo").setValue(floorNo);

        ref.child("timestamp").setValue(assignedAt);
        ref.child("timestampMillis").setValue(assignedDate.getTime());
        ref.child("triggeredBy").setValue("ADMIN");

    }
}
