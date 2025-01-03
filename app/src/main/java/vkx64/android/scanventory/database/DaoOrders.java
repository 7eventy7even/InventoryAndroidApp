package vkx64.android.scanventory.database;

import androidx.room.Dao;
import androidx.room.Insert;

@Dao
public interface DaoOrders {

    @Insert
    long insertOrder(TableOrders order);

}
