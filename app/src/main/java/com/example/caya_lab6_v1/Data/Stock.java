package com.example.caya_lab6_v1.Data;

import android.util.Log;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "stock_table")
public class Stock {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private double price;
    private String name;
    private String symbol;
    public boolean isSaved = false;
    private static final String TAG = "STOCK";

    public Stock(String symbol, String name, double price) {
        this.price = price;
        this.name = name;
        this.symbol = symbol;
        Log.d("STOCK", String.format("Stock Created: %s %s %f", symbol, name, price));
    }

    public int getId() {
        return id;
    }

    public double getPrice() {
        return price;
    }

    public String getName() {
        return name;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public void setId(int id){
        this.id = id;
    }

    public boolean getIsSaved(){
        return this.isSaved;
    }

    public void setSaved(boolean saved) {
        isSaved = saved;
    }

    public void toggleSave() {
        if(isSaved){
            isSaved = false;
            Log.d(TAG, "toggleSave: was save, now not");
        }
        else{
            isSaved = true;
            Log.d(TAG, "toggleSave: was not, now saved");
        }
    }
}
