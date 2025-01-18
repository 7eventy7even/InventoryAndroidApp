package vkx64.android.scanventory.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface DaoMarkets {

    @Insert
    long insertMarket(TableMarkets market);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdateMarket(TableMarkets markets);

    @Delete
    void deleteMarket(TableMarkets market);

    @Update
    void updateMarkets(List<TableMarkets> markets);

    // Optionally, if you prefer to delete by ID
    @Query("DELETE FROM market WHERE item_id = :marketId")
    void deleteMarketById(int marketId);

    @Query("SELECT market_name FROM market WHERE item_id = :itemId")
    List<String> getMarketNamesByItemId(String itemId);

    @Update
    void updateMarket(TableMarkets market);

    @Query("SELECT * FROM market WHERE item_id = :itemId")
    List<TableMarkets> getMarketsByItemId(String itemId);

    @Query("SELECT DISTINCT market_name FROM market")
    List<String> getAllUniqueMarketNames();

    @Query("SELECT * FROM market WHERE item_id = :itemId AND market_name = :marketName LIMIT 1")
    TableMarkets getMarketByItemIdAndMarketName(String itemId, String marketName);

    @Query("UPDATE market SET market_quantity = :newQuantity WHERE item_id = :itemId AND market_name = :marketName")
    void updateMarketQuantity(String itemId, String marketName, int newQuantity);

    @Query("SELECT DISTINCT market_name FROM market")
    List<String> getAllUniqueMarkets();
}
