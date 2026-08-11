package com.srikanta.mypg.models;

public class ServiceRequestModel {

    // Firebase key
    public String requestId;
    public int createdDate;

    // Issue info
    private String title;
    private String description;
    private String category;

    // Status: OPEN / IN_PROGRESS / RESOLVED
    private String status;

    // Tenant info
    private String tenantId;
    private String tenantName;
    private String roomNo;

    // Time
    private String createdAt;        // ddMMyyyyHHmm (display)
    private long createdAtMillis;    // sorting / filtering

    // Required empty constructor for Firebase
    public ServiceRequestModel() { }

    // -------- GETTERS --------


    public ServiceRequestModel(String requestId, int createdDate, String title, String description, String category, String status, String tenantId, String tenantName, String roomNo, String createdAt, long createdAtMillis) {
        this.requestId = requestId;
        this.createdDate = createdDate;
        this.title = title;
        this.description = description;
        this.category = category;
        this.status = status;
        this.tenantId = tenantId;
        this.tenantName = tenantName;
        this.roomNo = roomNo;
        this.createdAt = createdAt;
        this.createdAtMillis = createdAtMillis;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public int getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(int createdDate) {
        this.createdDate = createdDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public String getRoomNo() {
        return roomNo;
    }

    public void setRoomNo(String roomNo) {
        this.roomNo = roomNo;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public long getCreatedAtMillis() {
        return createdAtMillis;
    }

    public void setCreatedAtMillis(long createdAtMillis) {
        this.createdAtMillis = createdAtMillis;
    }
}
