package vkx64.android.scanventory.database;

import androidx.room.Dao;
import androidx.room.Query;

@Dao
public interface ClearDataDao {

    @Query("DELETE FROM orders")
    void clearOrders();

    @Query("DELETE FROM order_items")
    void clearOrderItems();

    @Query("DELETE FROM items")
    void clearItems();

    @Query("DELETE FROM groups")
    void clearGroups();

    @Query("DELETE FROM market")
    void clearMarket();

    @Query("DELETE FROM sqlite_sequence")
    void resetAutoIncrement();
}
