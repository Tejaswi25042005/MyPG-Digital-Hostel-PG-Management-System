package com.srikanta.mypg.helpers.tenants;

import androidx.annotation.NonNull;

import com.google.firebase.database.*;
import com.srikanta.mypg.models.RoomModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TenantAssignHelper {

    public interface Callback {
        void onSuccess(String assignedAt, String joinedMonth);
        void onFailure(String reason);
    }

    public static void assignTenant(
            DatabaseReference rootRef,
            String hostelId,
            String tenantId,
            String tenantName,
            String tenantMobile,
            RoomModel room,
            String sharing,
            int rent,
            int deposit,
            String assignedAt,
            String joinedMonth,
            Callback callback
    ) {

        DatabaseReference roomRef = rootRef
                .child("Hostels")
                .child(hostelId)
                .child("rooms")
                .child(String.valueOf(room.getFloorNo()))
                .child(String.valueOf(room.getRoomNo()));

        roomRef.runTransaction(new Transaction.Handler() {

            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData data) {
                RoomModel r = data.getValue(RoomModel.class);
                if (r == null) return Transaction.abort();

                if (r.getOccupiedBeds() >= r.getTotalBeds())
                    return Transaction.abort();

                r.setOccupiedBeds(r.getOccupiedBeds() + 1);
                data.setValue(r);
                return Transaction.success(data);
            }

            @Override
            public void onComplete(DatabaseError error, boolean committed, DataSnapshot snapshot) {

                if (!committed) {
                    callback.onFailure("Room is full");
                    return;
                }

                DatabaseReference tenantRef = rootRef
                        .child("Hostels")
                        .child(hostelId)
                        .child("tenants")
                        .child(tenantId);

                tenantRef.child("tenantId").setValue(tenantId);
                tenantRef.child("name").setValue(tenantName);
                tenantRef.child("mobile").setValue(tenantMobile);
                tenantRef.child("roomNo").setValue(room.getRoomNo());
                tenantRef.child("floorNo").setValue(room.getFloorNo());
                tenantRef.child("sharing").setValue(sharing);
                tenantRef.child("rent").setValue(rent);
                tenantRef.child("deposit").setValue(deposit);

                // ✅ Use PROVIDED timestamps
                tenantRef.child("assignedAt").setValue(assignedAt);
                tenantRef.child("joinedMonth").setValue(joinedMonth);

                tenantRef.child("status").setValue("ACTIVE");

                callback.onSuccess(assignedAt, joinedMonth);
            }
        });
    }

}
