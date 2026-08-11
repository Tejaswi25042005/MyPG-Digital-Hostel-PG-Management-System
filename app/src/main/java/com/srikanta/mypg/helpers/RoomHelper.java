package com.srikanta.mypg.helpers;

import android.content.Context;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.srikanta.mypg.models.RoomModel;

import java.util.ArrayList;
import java.util.List;

public class RoomHelper {

    public interface RoomCallback {
        void onRoomsLoaded(List<Object> displayList);
    }

    public static void loadRoomsByFloor(
            DatabaseReference rootRef,
            String hostelId,
            int floorNo,
            RoomCallback callback
    ) {
        rootRef.child("Hostels")
                .child(hostelId)
                .child("rooms")
                .child(String.valueOf(floorNo))
                .get()
                .addOnSuccessListener(snapshot -> {

                    List<Object> list = new ArrayList<>();

                    for (var roomSnap : snapshot.getChildren()) {
                        Integer roomNo = roomSnap.child("roomNo").getValue(Integer.class);
                        Integer beds = roomSnap.child("totalBeds").getValue(Integer.class);
                        Integer occ = roomSnap.child("occupiedBeds").getValue(Integer.class);

                        if (roomNo != null) {
                            list.add(new RoomModel(
                                    roomNo,
                                    beds == null ? 0 : beds,
                                    occ == null ? 0 : occ
                            ));
                        }
                    }

                    list.add("ADD_ROOM");
                    callback.onRoomsLoaded(list);
                });
    }

    public static void addRoom(
            Context context,
            DatabaseReference rootRef,
            String hostelId,
            int floorNo,
            int roomNo,
            int beds
    ) {
        DatabaseReference ref = rootRef
                .child("Hostels")
                .child(hostelId)
                .child("rooms")
                .child(String.valueOf(floorNo))
                .child(String.valueOf(roomNo));

        ref.child("roomNo").setValue(roomNo);
        ref.child("totalBeds").setValue(beds);
        ref.child("occupiedBeds").setValue(0);

        Toast.makeText(context,
                "Room " + roomNo + " added",
                Toast.LENGTH_SHORT).show();
    }
}
