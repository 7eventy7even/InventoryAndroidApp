package vkx64.upwork.inventory_management.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;
import java.util.List;

@Dao
public interface InventoryDao {
    @Query("SELECT * FROM inventory")
    List<InventoryItem> getAll();

    @Query("SELECT * FROM inventory WHERE product_id = :id LIMIT 1")
    InventoryItem findById(int id);

    @Query("DELETE FROM inventory")
    void deleteAllItems();

    @Insert
    void insertAll(List<InventoryItem> items);

    @Insert
    void insertItem(InventoryItem item);

    @Update
    void updateItem(InventoryItem item);

    @Delete
    void deleteItem(InventoryItem item);
}
