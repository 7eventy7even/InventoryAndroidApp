package vkx64.android.scanventory.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "order_items",
        foreignKeys = {
                @ForeignKey(
                        entity = TableOrders.class,
                        parentColumns = "order_id",
                        childColumns = "order_id",
                        onDelete = ForeignKey.CASCADE,
                        onUpdate = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = TableItems.class,
                        parentColumns = "item_id",
                        childColumns = "item_id",
                        onDelete = ForeignKey.CASCADE,
                        onUpdate = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index(value = "order_id"),
                @Index(value = "item_id")
        }
)
public class TableOrderItems {
    @PrimaryKey(autoGenerate = true)
    public int order_items_id;

    @NonNull
    public int order_id;

    @NonNull
    public String item_id;

    public int order_items_quantity;

    // Constructor
    public TableOrderItems(int order_id, @NonNull String item_id, int order_items_quantity) {
        this.order_id = order_id;
        this.item_id = item_id;
        this.order_items_quantity = order_items_quantity;
    }

    // Getters and Setters
    public int getOrder_items_id() {
        return order_items_id;
    }

    public void setOrder_items_id(int order_items_id) {
        this.order_items_id = order_items_id;
    }

    @NonNull
    public int getOrder_id() {
        return order_id;
    }

    public void setOrder_id(@NonNull int order_id) {
        this.order_id = order_id;
    }

    @NonNull
    public String getItem_id() {
        return item_id;
    }

    public void setItem_id(@NonNull String item_id) {
        this.item_id = item_id;
    }

    public int getOrder_items_quantity() {
        return order_items_quantity;
    }

    public void setOrder_items_quantity(int order_items_quantity) {
        this.order_items_quantity = order_items_quantity;
    }
}
