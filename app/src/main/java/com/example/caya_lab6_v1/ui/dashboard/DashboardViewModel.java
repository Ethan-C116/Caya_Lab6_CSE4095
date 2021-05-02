package com.example.caya_lab6_v1.ui.dashboard;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.caya_lab6_v1.Data.Stock;
import com.example.caya_lab6_v1.Data.StockDatabase;

import java.util.List;

public class DashboardViewModel extends AndroidViewModel {
    private static final String TAG = "PORT_VIEW_MODEl";
    private static LiveData<List<Stock>> stockList;
    private static StockDatabase stockDatabase;
    private static Stock currentStock;

    public DashboardViewModel(@NonNull Application application) {
        super(application);

        stockDatabase = StockDatabase.getInstance(application);
        stockList = stockDatabase.stockDAO().getAllStocks();
    }

    public static LiveData<List<Stock>> getStockList() {
        return stockList;
    }

    public static void setStockList(List<Stock> stocks) {
        stockList = new MutableLiveData<List<Stock>>(stocks);
    }

    public static StockDatabase getStockDatabase() {
        return stockDatabase;
    }

    public static void setStockDatabase(StockDatabase stockDatabase) {
        DashboardViewModel.stockDatabase = stockDatabase;
    }


    public static Stock getCurrentStock() {
        return currentStock;
    }

    public static void setCurrentStock(Stock currentStock) {
        DashboardViewModel.currentStock = currentStock;
    }
}