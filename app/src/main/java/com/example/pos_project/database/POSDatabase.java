package com.example.pos_project.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.pos_project.dao.ProductDao;
import com.example.pos_project.dao.SaleDao;
import com.example.pos_project.dao.SaleItemDao;
import com.example.pos_project.dao.UserDao;
import com.example.pos_project.model.Product;
import com.example.pos_project.model.Sale;
import com.example.pos_project.model.SaleItem;
import com.example.pos_project.model.User;

@Database(
    entities = {User.class, Product.class, Sale.class, SaleItem.class},
    version = 5,
    exportSchema = false
)
public abstract class POSDatabase extends RoomDatabase {
    
    private static POSDatabase INSTANCE;
    
    public abstract UserDao userDao();
    public abstract ProductDao productDao();
    public abstract SaleDao saleDao();
    public abstract SaleItemDao saleItemDao();
    
    public static synchronized POSDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            try {
                INSTANCE = Room.databaseBuilder(
                    context.getApplicationContext(),
                    POSDatabase.class,
                    "pos_database"
                )
                .fallbackToDestructiveMigration() // Handle schema changes gracefully
                .build();
            } catch (Exception e) {
                // If database creation fails, try to clear and recreate
                try {
                    context.deleteDatabase("pos_database");
                    INSTANCE = Room.databaseBuilder(
                        context.getApplicationContext(),
                        POSDatabase.class,
                        "pos_database"
                    )
                    .fallbackToDestructiveMigration()
                    .build();
                } catch (Exception e2) {
                    throw new RuntimeException("Unable to create database", e2);
                }
            }
        }
        return INSTANCE;
    }

    // Add method to reset database instance
    public static synchronized void resetInstance() {
        if (INSTANCE != null) {
            INSTANCE.close();
            INSTANCE = null;
        }
    }
}