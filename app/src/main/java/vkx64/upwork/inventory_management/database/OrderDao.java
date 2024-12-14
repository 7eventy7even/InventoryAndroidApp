package vkx64.upwork.inventory_management.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;
import java.util.List;

@Dao
public interface OrderDao {
    @Insert
    long insertOrder(Order order);

    @Query("SELECT * FROM orders")
    List<Order> getAllOrders();

    @Query("SELECT * FROM orders WHERE order_id = :id LIMIT 1")
    Order findOrderById(int id);

    @Delete
    void deleteOrder(Order order);
}
