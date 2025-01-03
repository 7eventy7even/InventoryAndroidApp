package vkx64.android.scanventory.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DaoOrderItems {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrderItem(TableOrderItems orderItem);

    @Query("SELECT * FROM order_items WHERE order_id = :orderId")
    List<TableOrderItems> getOrderItemsByOrderId(int orderId);

}
