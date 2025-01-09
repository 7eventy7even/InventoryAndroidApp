package vkx64.android.scanventory.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(
        entities = {
                TableGroups.class,
                TableItems.class,
                TableOrders.class,
                TableOrderItems.class
        },
        version = 1,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    public abstract DaoGroups daoGroups();
    public abstract DaoItems daoItems();
    public abstract DaoOrders daoOrders();
    public abstract DaoOrderItems daoOrderItems();

    // Add this
    public abstract ClearDataDao clearDataDao();
}
