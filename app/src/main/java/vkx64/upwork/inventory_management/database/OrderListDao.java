package vkx64.upwork.inventory_management.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

@Dao
public interface OrderListDao {
    @Insert
    long insertOrderList(OrderList orderList);

    @Transaction
    @Query("SELECT * FROM inventory WHERE product_id IN (SELECT product_id FROM order_list WHERE order_id = :orderId)")
    List<InventoryItem> getInventoryItemsByOrderId(int orderId);

    @Query("SELECT * FROM order_list WHERE order_id = :orderId")
    List<OrderList> getOrderListByOrderId(int orderId);
}
