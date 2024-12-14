package vkx64.upwork.inventory_management.database;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "order_list",
        foreignKeys = {
                @ForeignKey(
                        entity = Order.class,
                        parentColumns = "order_id",
                        childColumns = "order_id",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = InventoryItem.class,
                        parentColumns = "product_id",
                        childColumns = "product_id",
                        onDelete = ForeignKey.CASCADE
                )
        }
)
public class OrderList {
    @PrimaryKey(autoGenerate = true)
    public int orderList_id;

    public int order_id;
    public int product_id;
    public int quantity;
}
