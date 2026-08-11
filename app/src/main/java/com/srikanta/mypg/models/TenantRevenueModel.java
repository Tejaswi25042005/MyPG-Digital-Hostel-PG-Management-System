package com.srikanta.mypg.models;

public class TenantRevenueModel {

    // ---------- BASIC ----------
    private String tenantId;
    private String name;
    private int floorNo;
    private int roomNo;

    // ---------- LEDGER DATA ----------
    private int rentAmount;        // rent
    private int rentPaidAmount;    // rentPaid (ONLY rent)
    private int depositAmount;     // deposit (joining month only)
    private int totalPaidAmount;   // rentPaid + deposit

    private String status;         // PAID / DUE
    private String tenantType;     // NEW / REGULAR

    // ---------- CONSTRUCTOR ----------
    public TenantRevenueModel() {}

    public TenantRevenueModel(
            String tenantId,
            String name,
            int floorNo,
            int roomNo,
            int rentAmount,
            int rentPaidAmount,
            int depositAmount,
            int totalPaidAmount,
            String status,
            String tenantType
    ) {
        this.tenantId = tenantId;
        this.name = name;
        this.floorNo = floorNo;
        this.roomNo = roomNo;
        this.rentAmount = rentAmount;
        this.rentPaidAmount = rentPaidAmount;
        this.depositAmount = depositAmount;
        this.totalPaidAmount = totalPaidAmount;
        this.status = status;
        this.tenantType = tenantType;
    }

    // ---------- GETTERS & SETTERS ----------

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

    // ---------- RENT ----------
    public int getRentAmount() {
        return rentAmount;
    }

    public void setRentAmount(int rentAmount) {
        this.rentAmount = rentAmount;
    }

    // ---------- RENT PAID ----------
    public int getRentPaidAmount() {
        return rentPaidAmount;
    }

    public void setRentPaidAmount(int rentPaidAmount) {
        this.rentPaidAmount = rentPaidAmount;
    }

    // ---------- DEPOSIT ----------
    public int getDepositAmount() {
        return depositAmount;
    }

    public void setDepositAmount(int depositAmount) {
        this.depositAmount = depositAmount;
    }

    // ---------- TOTAL PAID ----------
    public int getTotalPaidAmount() {
        return totalPaidAmount;
    }

    public void setTotalPaidAmount(int totalPaidAmount) {
        this.totalPaidAmount = totalPaidAmount;
    }

    // ---------- STATUS ----------
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // ---------- TYPE ----------
    public String getTenantType() {
        return tenantType;
    }

    public void setTenantType(String tenantType) {
        this.tenantType = tenantType;
    }
}
