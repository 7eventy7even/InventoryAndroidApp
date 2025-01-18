package vkx64.android.scanventory.database;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "market",
        foreignKeys = @ForeignKey(
                entity = TableItems.class,
                parentColumns = "item_id",
                childColumns = "item_id",
                onDelete = ForeignKey.CASCADE,
                onUpdate = ForeignKey.CASCADE
        ),
        indices = {@Index("item_id")})
public class TableMarkets {

    @PrimaryKey(autoGenerate = true)
    public int market_id;
    public String item_id;
    public String market_name;
    public int market_quantity;

    public TableMarkets(int market_quantity, String market_name, String item_id) {
        this.market_quantity = market_quantity;
        this.market_name = market_name;
        this.item_id = item_id;
    }

    public int getMarket_id() {
        return market_id;
    }

    public void setMarket_id(int market_id) {
        this.market_id = market_id;
    }

    public String getItem_id() {
        return item_id;
    }

    public void setItem_id(String item_id) {
        this.item_id = item_id;
    }

    public String getMarket_name() {
        return market_name;
    }

    public void setMarket_name(String market_name) {
        this.market_name = market_name;
    }

    public int getMarket_quantity() {
        return market_quantity;
    }

    public void setMarket_quantity(int market_quantity) {
        this.market_quantity = market_quantity;
    }
}
