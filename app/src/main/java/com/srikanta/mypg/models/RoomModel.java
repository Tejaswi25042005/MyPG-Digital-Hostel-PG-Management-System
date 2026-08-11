package com.srikanta.mypg.models;

public class RoomModel {

    private int roomNo;
    private int totalBeds;
    private int occupiedBeds;
    private int floorNo;

    // Required empty constructor for Firebase
    public RoomModel() {}

    public RoomModel(int roomNo, int totalBeds, int occupiedBeds) {
        this.roomNo = roomNo;
        this.totalBeds = totalBeds;
        this.occupiedBeds = occupiedBeds;
    }

    public int getRoomNo() {
        return roomNo;
    }

    public void setRoomNo(int roomNo) {
        this.roomNo = roomNo;
    }

    public int getTotalBeds() {
        return totalBeds;
    }

    public void setTotalBeds(int totalBeds) {
        this.totalBeds = totalBeds;
    }

    public int getOccupiedBeds() {
        return occupiedBeds;
    }

    public void setOccupiedBeds(int occupiedBeds) {
        this.occupiedBeds = occupiedBeds;
    }

    public int getFloorNo() {
        return floorNo;
    }

    public void setFloorNo(int floorNo) {
        this.floorNo = floorNo;
    }

    // ✅ ADD THIS
    public int getFreeBeds() {
        return Math.max(totalBeds - occupiedBeds, 0);
    }
}
