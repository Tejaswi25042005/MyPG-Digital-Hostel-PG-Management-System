package com.srikanta.mypg.helpers;

import android.content.Context;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.srikanta.mypg.models.FloorModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FloorHelper {

    public interface FloorCallback {
        void onFloorsLoaded(List<FloorModel> floors);
    }

    public static void loadFloors(
            DatabaseReference rootRef,
            String hostelId,
            FloorCallback callback
    ) {
        rootRef.child("Hostels")
                .child(hostelId)
                .child("floors")
                .get()
                .addOnSuccessListener(snapshot -> {

                    List<FloorModel> list = new ArrayList<>();

                    for (var snap : snapshot.getChildren()) {
                        Integer no = snap.child("floorNo").getValue(Integer.class);
                        String name = snap.child("floorName").getValue(String.class);

                        if (no != null && name != null) {
                            list.add(new FloorModel(no, name));
                        }
                    }

                    Collections.sort(list,
                            (a, b) -> Integer.compare(a.floorNo, b.floorNo));

                    callback.onFloorsLoaded(list);
                });
    }

    public static void addFloor(
            Context context,
            DatabaseReference rootRef,
            String hostelId,
            int floorNo
    ) {
        DatabaseReference ref = rootRef
                .child("Hostels")
                .child(hostelId)
                .child("floors")
                .child(String.valueOf(floorNo));

        ref.child("floorNo").setValue(floorNo);
        ref.child("floorName").setValue("Floor " + floorNo);

        Toast.makeText(context,
                "Floor " + floorNo + " added",
                Toast.LENGTH_SHORT).show();
    }
}
