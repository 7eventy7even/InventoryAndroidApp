package vkx64.android.scanventory.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface DaoItems {

    @Query("SELECT * FROM items WHERE item_id = :itemId LIMIT 1")
    TableItems getItemById(String itemId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertItem(TableItems item);

    @Delete
    void deleteItem(TableItems item);

    @Query("SELECT * FROM items WHERE group_id = :groupId")
    List<TableItems> getItemsByGroupId(String groupId);

    @Update
    void updateItem(TableItems item);

    @Query("SELECT * FROM items WHERE item_name LIKE '%' || :query || '%'")
    List<TableItems> searchItemsByName(String query);

    @Query("SELECT * FROM items WHERE item_selling >= :value")
    List<TableItems> searchItemsBySellingGreaterThanOrEqual(int value);

    @Query("SELECT * FROM items WHERE item_selling <= :value")
    List<TableItems> searchItemsBySellingLessThanOrEqual(int value);

    @Query("SELECT * FROM items WHERE item_storage >= :value")
    List<TableItems> searchItemsByStorageGreaterThanOrEqual(int value);

    @Query("SELECT * FROM items WHERE item_storage <= :value")
    List<TableItems> searchItemsByStorageLessThanOrEqual(int value);

    // Insert or update an item (used during import)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdateItem(TableItems item);

    // Get all items (used during export)
    @Query("SELECT * FROM items")
    List<TableItems> getAllItems();

    @Query("SELECT * FROM items WHERE group_id IS NULL")
    List<TableItems> getRootItems();

}
