package com.srikanta.mypg.helpers;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

public class TenantHelper {

    public interface TenantCountCallback {
        void onCount(int count);
    }

    public static void observeTenantsByFloor(
            DatabaseReference rootRef,
            String hostelId,
            int floorNo,
            TenantCountCallback callback
    ) {

        rootRef.child("Hostels")
                .child(hostelId)
                .child("tenants")
                .addValueEventListener(new com.google.firebase.database.ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot snapshot) {

                        int count = 0;

                        for (DataSnapshot tenantSnap : snapshot.getChildren()) {

                            Integer tFloor =
                                    tenantSnap.child("floorNo")
                                            .getValue(Integer.class);

                            if (tFloor != null && tFloor == floorNo) {
                                count++;
                            }
                        }

                        callback.onCount(count);
                    }

                    @Override
                    public void onCancelled(
                            com.google.firebase.database.DatabaseError error
                    ) {}
                });
    }
}
