package com.sns.wanderwise.checklist;

import android.os.Parcel;
import android.os.Parcelable;

public class ChecklistItem {
    private String name;
    private int weight;
    private int quantity;
    private String weightUnit; 

    public ChecklistItem(String name, int weight, int quantity, String weightUnit) {
        this.name = name;
        this.weight = weight;
        this.quantity = quantity;
        this.weightUnit = weightUnit;
    }

    public ChecklistItem(String name, int weight, int quantity) {
        this.name = name;
        this.weight = weight;
        this.quantity = quantity;
        this.weightUnit = "kg"; 
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getWeightUnit() {
        return weightUnit;
    }

    public void setWeightUnit(String weightUnit) {
        this.weightUnit = weightUnit;
    }
}