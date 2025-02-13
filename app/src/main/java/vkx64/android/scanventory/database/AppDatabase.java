package vkx64.android.scanventory.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(
        entities = {
                TableGroups.class,
                TableItems.class,
                TableOrders.class,
                TableOrderItems.class,
                TableMarkets.class
        },
        version = 2,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    public abstract DaoGroups daoGroups();
    public abstract DaoItems daoItems();
    public abstract DaoOrders daoOrders();
    public abstract DaoOrderItems daoOrderItems();
    public abstract ClearDataDao clearDataDao();
    public abstract DaoMarkets daoMarkets();
}
