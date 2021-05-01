package com.example.caya_lab6_v1.Data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;

@Database(entities = {Stock.class}, version = 2)
public abstract class StockDatabase extends RoomDatabase {

    //singleton instance of database
    private static StockDatabase INSTANCE;
    //StockDAO class used to access DAO functions
    public abstract StockDAO stockDAO();

    public static synchronized StockDatabase getInstance(Context context) {
        //if no database existing create one
        if (null == INSTANCE) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                    StockDatabase.class, "stock_database")
                    .fallbackToDestructiveMigration()
                    .addCallback(roomCallback)
                    .build();
        }
        return INSTANCE;
    }

    private static RoomDatabase.Callback roomCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            //new PopulateDbAsyncTask(INSTANCE).execute();
        }
    };

}
