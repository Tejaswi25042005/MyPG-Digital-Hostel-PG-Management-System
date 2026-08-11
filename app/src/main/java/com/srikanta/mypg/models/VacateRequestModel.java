package com.srikanta.mypg.models;

public class VacateRequestModel {

    private String requestId;

    private String tenantId;
    private String name;
    private String mobile;

    private String vacateDate;
    private String paidTill;
    private String reason;

    private long totalDue;
    private long refundAmount;

    private String status;        // PENDING / APPROVED / REJECTED
    private long requestedAt;     // timestamp

    // ✅ ROOM DATA
    private Long roomNo;
    private Long floorNo;
    private Long sharing;

    // ✅ NEW → Tenant request for dues
    private String dueAction;

    // REQUIRED for Firebase
    public VacateRequestModel() {
    }

    // ---------------- GETTERS & SETTERS ----------------

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getName() {
        return name == null ? "" : name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobile() {
        return mobile == null ? "" : mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getVacateDate() {
        return vacateDate == null ? "--" : vacateDate;
    }

    public void setVacateDate(String vacateDate) {
        this.vacateDate = vacateDate;
    }

    public String getPaidTill() {
        return paidTill == null ? "--" : paidTill;
    }

    public void setPaidTill(String paidTill) {
        this.paidTill = paidTill;
    }

    public String getReason() {
        return reason == null || reason.isEmpty() ? "--" : reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public long getTotalDue() {
        return totalDue;
    }

    public void setTotalDue(long totalDue) {
        this.totalDue = totalDue;
    }

    public long getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(long refundAmount) {
        this.refundAmount = refundAmount;
    }

    public String getStatus() {
        return status == null ? "PENDING" : status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(long requestedAt) {
        this.requestedAt = requestedAt;
    }

    // ---------------- ROOM DATA ----------------

    public Long getRoomNo() {
        return roomNo;
    }

    public void setRoomNo(Long roomNo) {
        this.roomNo = roomNo;
    }

    public Long getFloorNo() {
        return floorNo;
    }

    public void setFloorNo(Long floorNo) {
        this.floorNo = floorNo;
    }

    public Long getSharing() {
        return sharing;
    }

    public void setSharing(Long sharing) {
        this.sharing = sharing;
    }

    // ---------------- DUE ACTION ----------------

    public String getDueAction() {
        return dueAction == null ? "NONE" : dueAction;
    }

    public void setDueAction(String dueAction) {
        this.dueAction = dueAction;
    }
}
