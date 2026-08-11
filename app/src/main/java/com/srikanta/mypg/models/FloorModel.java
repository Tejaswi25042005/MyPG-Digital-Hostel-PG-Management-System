package com.srikanta.mypg.models;

public class FloorModel {

    public int floorNo;
    public String floorName;

    public FloorModel() {
    }

    public FloorModel(int floorNo, String floorName) {
        this.floorNo = floorNo;
        this.floorName = floorName;
    }

    public int getFloorNo() {
        return floorNo;
    }

    public void setFloorNo(int floorNo) {
        this.floorNo = floorNo;
    }

    public String getFloorName() {
        return floorName;
    }

    public void setFloorName(String floorName) {
        this.floorName = floorName;
    }
}
