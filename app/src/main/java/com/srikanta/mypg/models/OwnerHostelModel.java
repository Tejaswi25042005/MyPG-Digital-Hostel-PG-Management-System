package com.srikanta.mypg.models;

public class OwnerHostelModel {

    // ================= BASIC INFO =================
    private String hostelId;
    private String name;
    private String address;
    private String status;

    // ================= STATS =================
    private int roomsCount;
    private int bedsCount;
    private int occupiedCount;

    // ⭐ DEFAULT HOSTEL
    private boolean isDefault;

    // 🔹 Required empty constructor for Firebase
    public OwnerHostelModel() {
    }

    // 🔹 Convenience constructor (basic)
    public OwnerHostelModel(
            String hostelId,
            String name,
            String address,
            String status
    ) {
        this.hostelId = hostelId;
        this.name = name;
        this.address = address;
        this.status = status;
    }

    // 🔹 Convenience constructor (with stats)
    public OwnerHostelModel(
            String hostelId,
            String name,
            String address,
            String status,
            int roomsCount,
            int bedsCount,
            int occupiedCount
    ) {
        this.hostelId = hostelId;
        this.name = name;
        this.address = address;
        this.status = status;
        this.roomsCount = roomsCount;
        this.bedsCount = bedsCount;
        this.occupiedCount = occupiedCount;
    }

    // ================= GETTERS & SETTERS =================

    public String getHostelId() {
        return hostelId;
    }

    public void setHostelId(String hostelId) {
        this.hostelId = hostelId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // ================= STATS =================

    public int getRoomsCount() {
        return roomsCount;
    }

    public void setRoomsCount(int roomsCount) {
        this.roomsCount = roomsCount;
    }

    public int getBedsCount() {
        return bedsCount;
    }

    public void setBedsCount(int bedsCount) {
        this.bedsCount = bedsCount;
    }

    public int getOccupiedCount() {
        return occupiedCount;
    }

    public void setOccupiedCount(int occupiedCount) {
        this.occupiedCount = occupiedCount;
    }

    // ================= DEFAULT HOSTEL =================

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    // ================= HELPERS =================

    public int getFreeBeds() {
        return Math.max(bedsCount - occupiedCount, 0);
    }

    public boolean isActive() {
        return "active".equalsIgnoreCase(status);
    }
}
