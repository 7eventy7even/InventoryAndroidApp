package vkx64.upwork.inventory_management.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {InventoryItem.class, Order.class, OrderList.class}, version = 3, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract InventoryDao inventoryDao();
    public abstract OrderDao orderDao();
    public abstract OrderListDao orderListDao();
}
