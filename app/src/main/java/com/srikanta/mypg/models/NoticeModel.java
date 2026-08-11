package com.srikanta.mypg.models;

public class NoticeModel {

    public String noticeId;
    public String title;
    public String message;
    public String type;
    public boolean active;
    public long createdAt;

    // Required empty constructor
    public NoticeModel() {}

    public NoticeModel(String noticeId, String title, String message, String type, boolean active, long createdAt) {
        this.noticeId = noticeId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.active = active;
        this.createdAt = createdAt;
    }

    public String getNoticeId() {
        return noticeId;
    }

    public void setNoticeId(String noticeId) {
        this.noticeId = noticeId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
