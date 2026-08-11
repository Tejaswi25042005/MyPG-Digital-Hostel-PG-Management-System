package com.srikanta.mypg.models;

public class TenantModel {

    public String tenantId;
    public String name;
    public String mobile;
    public int floorNo;
    public int roomNo;

    public TenantModel() {
        // Firebase required
    }

    public TenantModel(
            String tenantId,
            String name,
            String mobile,
            int floorNo,
            int roomNo
    ) {
        this.tenantId = tenantId;
        this.name = name;
        this.mobile = mobile;
        this.floorNo = floorNo;
        this.roomNo = roomNo;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public int getFloorNo() {
        return floorNo;
    }

    public void setFloorNo(int floorNo) {
        this.floorNo = floorNo;
    }

    public int getRoomNo() {
        return roomNo;
    }

    public void setRoomNo(int roomNo) {
        this.roomNo = roomNo;
    }
}
