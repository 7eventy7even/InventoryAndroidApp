package vkx64.upwork.inventory_management.database;

import android.app.Application;
import androidx.room.Room;

public class MyDatabase extends Application {
    private static AppDatabase database;

    @Override
    public void onCreate() {
        super.onCreate();
        database = Room.databaseBuilder(
                        getApplicationContext(),
                        AppDatabase.class,
                        "inventory-db"
                )
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();
    }

    public static AppDatabase getDatabase() {
        return database;
    }
}
