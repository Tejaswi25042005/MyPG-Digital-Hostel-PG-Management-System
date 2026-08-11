package com.srikanta.mypg.helpers;

import com.google.firebase.database.DatabaseReference;

public class HostelStatsHelper {

    public interface StatsCallback {
        void onStats(int rooms, int beds, int occupied);
    }

    // ================= FLOOR-WISE STATS =================
    public static void observeStatsByFloor(
            DatabaseReference rootRef,
            String hostelId,
            int floorNo,
            StatsCallback callback
    ) {
        rootRef.child("Hostels")
                .child(hostelId)
                .child("rooms")
                .child(String.valueOf(floorNo))
                .addValueEventListener(new com.google.firebase.database.ValueEventListener() {

                    @Override
                    public void onDataChange(
                            com.google.firebase.database.DataSnapshot snapshot
                    ) {

                        int totalRooms = 0;
                        int freeRooms = 0;
                        int totalBeds = 0;
                        int occupiedBeds = 0;

                        for (com.google.firebase.database.DataSnapshot roomSnap
                                : snapshot.getChildren()) {

                            totalRooms++;

                            Integer beds =
                                    roomSnap.child("totalBeds")
                                            .getValue(Integer.class);
                            Integer occ =
                                    roomSnap.child("occupiedBeds")
                                            .getValue(Integer.class);

                            beds = beds == null ? 0 : beds;
                            occ = occ == null ? 0 : occ;

                            totalBeds += beds;
                            occupiedBeds += occ;

                            // ✅ FREE ROOM LOGIC
                            if (beds > 0 && occ == 0) {
                                freeRooms++;
                            }
                        }

                        // 🔥 rooms = totalRooms
                        // 🔥 beds = totalBeds
                        // 🔥 occupied = occupiedBeds
                        // 🔥 freeRooms encoded via callback (see below)

                        callback.onStats(totalRooms, freeRooms, totalBeds - occupiedBeds);
                    }

                    @Override
                    public void onCancelled(
                            com.google.firebase.database.DatabaseError error
                    ) {}
                });
    }

}
