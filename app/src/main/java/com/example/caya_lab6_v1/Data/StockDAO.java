package com.example.caya_lab6_v1.Data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface StockDAO {

    @Insert
    void insert(Stock stock);

    @Delete
    void delete(Stock stock);

    @Update
    void update(Stock stock);

    /**
     *
     * @param query - the name or symbol of the stock to find
     * @return - List<Stock> whose name or symbol match the query
     */
    @Query("SELECT * FROM stock_table WHERE symbol OR name LIKE :query")
    List<Stock> getStock(String query);

    @Query("SELECT * FROM stock_table WHERE symbol LIKE :symbol")
    List<Stock> getStockBySymbol(String symbol);

    /**
     * @return a LiveData list of all stocks ordered by symbol
     */
    @Query("SELECT * FROM stock_table ORDER BY isSaved DESC, symbol ASC")
    LiveData<List<Stock>> getAllStocks();

    @Query("SELECT * FROM stock_table WHERE ID LIKE :id")
    Stock getStockByID(int id);
}
