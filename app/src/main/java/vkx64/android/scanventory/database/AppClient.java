package vkx64.android.scanventory.database;

import android.content.Context;

import androidx.room.Room;

public class AppClient {

    private static AppClient instance;
    private final AppDatabase appDatabase;

    private AppClient(Context context) {
        appDatabase = Room.databaseBuilder(context, AppDatabase.class, "inventory_management_db")
                .fallbackToDestructiveMigration()
                .build();
    }

    public static synchronized AppClient getInstance(Context context) {
        if (instance == null) {
            instance = new AppClient(context);
        }
        return instance;
    }

    public AppDatabase getAppDatabase() {
        return appDatabase;
    }
}
