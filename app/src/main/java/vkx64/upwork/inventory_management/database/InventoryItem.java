package vkx64.upwork.inventory_management.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "inventory")
public class InventoryItem {
    @PrimaryKey
    public int product_id;

    public String product_category;
    public String product_name;
    public int storage_quantity;
    public int selling_quantity;
    public String date_created;
    public String date_updated;
}
