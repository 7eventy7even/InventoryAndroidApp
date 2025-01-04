package vkx64.android.scanventory.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DaoOrders {

    @Insert
    long insertOrder(TableOrders order);

    @Query("SELECT * FROM orders")
    List<TableOrders> getAllOrderHistory();

}
