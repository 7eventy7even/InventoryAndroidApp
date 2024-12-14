package vkx64.upwork.inventory_management.database;


import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "orders")
public class Order {
    @PrimaryKey(autoGenerate = true)
    public int order_id;

    public String order_date;
    public String order_name;

}