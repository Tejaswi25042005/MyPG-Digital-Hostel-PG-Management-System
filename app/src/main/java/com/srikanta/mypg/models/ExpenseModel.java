package com.srikanta.mypg.models;

public class ExpenseModel {

    private String id;
    private String title;
    private String category;
    private String subcategory;
    private long amount;
    private long createdAt;

    public ExpenseModel() {}

    public ExpenseModel(String id, String title, String category, String subcategory, long amount, long createdAt) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.subcategory = subcategory;
        this.amount = amount;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSubcategory() {
        return subcategory;
    }

    public void setSubcategory(String subcategory) {
        this.subcategory = subcategory;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
