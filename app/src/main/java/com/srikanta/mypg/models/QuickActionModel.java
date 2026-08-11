package com.srikanta.mypg.models;

public class QuickActionModel {

    private String id;        // ADD_TENANT, NOTICE, etc.
    private String title;     // UI name
    private int icon;         // drawable
    private boolean isEmpty;  // slot or real action

    // Required empty constructor for Firebase
    public QuickActionModel() {
    }

    public QuickActionModel(String id, String title, int icon, boolean isEmpty) {
        this.id = id;
        this.title = title;
        this.icon = icon;
        this.isEmpty = isEmpty;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getIcon() {
        return icon;
    }

    public boolean isEmpty() {
        return isEmpty;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public void setEmpty(boolean empty) {
        isEmpty = empty;
    }
}
