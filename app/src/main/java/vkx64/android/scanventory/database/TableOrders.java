package vkx64.android.scanventory.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "orders")
public class TableOrders {

    @PrimaryKey(autoGenerate = true)
    public int order_id;

    public String order_date;

    // Constructor
    public TableOrders(int order_id, String order_date) {
        this.order_id = order_id;
        this.order_date = order_date;
    }

    // Getters and Setters
    @NonNull
    public int getOrder_id() {
        return order_id;
    }

    public void setOrder_id(@NonNull int order_id) {
        this.order_id = order_id;
    }

    public String getOrder_date() {
        return order_date;
    }

    public void setOrder_date(String order_date) {
        this.order_date = order_date;
    }
}

