package com.srikanta.mypg.models;

public class RecentActionModel {

    private String actionType;
    private String category;
    private String title;
    private String description;

    private long timestampMillis;   // ✅ ONLY timestamp field

    private String tenantId;
    private String tenantName;
    private int roomNumber;
    private int floorNo;
    private String triggeredBy;
    private int amount; // optional for payment actions

    public RecentActionModel() {}

    // -------- GETTERS --------
    public String getActionType() { return actionType; }
    public String getCategory() { return category; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }

    public long getTimestampMillis() { return timestampMillis; }

    public String getTenantId() { return tenantId; }
    public String getTenantName() { return tenantName; }
    public int getRoomNumber() { return roomNumber; }
    public int getFloorNo() { return floorNo; }
    public String getTriggeredBy() { return triggeredBy; }
    public int getAmount() { return amount; }

    // -------- UI HELPER --------
    public String getFormattedDate() {
        return new java.text.SimpleDateFormat(
                "dd MMM yyyy",
                java.util.Locale.getDefault()
        ).format(new java.util.Date(timestampMillis));
    }
}
