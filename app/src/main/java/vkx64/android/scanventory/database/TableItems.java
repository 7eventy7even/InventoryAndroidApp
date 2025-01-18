package vkx64.android.scanventory.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "items",
        foreignKeys = @ForeignKey(
                entity = TableGroups.class,
                parentColumns = "group_id",
                childColumns = "group_id",
                onDelete = ForeignKey.CASCADE,
                onUpdate = ForeignKey.CASCADE
        ),
        indices = {@Index("group_id")})
public class TableItems {
    // Columns
    @PrimaryKey(autoGenerate = false)
    @NonNull
    public String item_id;

    @NonNull
    public String item_name;

    public String item_category;
    public int item_storage;
    public String item_created;
    public String item_updated;
    public String group_id;

    // Constructor
    public TableItems(@NonNull String item_id, @NonNull String item_name, String item_category, int item_storage, String item_created, String item_updated, String group_id) {
        this.item_id = item_id;
        this.item_name = item_name;
        this.item_category = item_category;
        this.item_storage = item_storage;
        this.item_created = item_created;
        this.item_updated = item_updated;
        this.group_id = group_id;
    }

    // Getters and Setters
    @NonNull
    public String getItem_id() {
        return item_id;
    }

    public void setItem_id(@NonNull String item_id) {
        this.item_id = item_id;
    }

    @NonNull
    public String getItem_name() {
        return item_name;
    }

    public void setItem_name(@NonNull String item_name) {
        this.item_name = item_name;
    }

    public String getItem_category() {
        return item_category;
    }

    public void setItem_category(String item_category) {
        this.item_category = item_category;
    }

    public int getItem_storage() {
        return item_storage;
    }

    public void setItem_storage(int item_storage) {
        this.item_storage = item_storage;
    }

    public String getItem_created() {
        return item_created;
    }

    public void setItem_created(String item_created) {
        this.item_created = item_created;
    }

    public String getItem_updated() {
        return item_updated;
    }

    public void setItem_updated(String item_updated) {
        this.item_updated = item_updated;
    }

    public String getGroup_id() {
        return group_id;
    }

    public void setGroup_id(String group_id) {
        this.group_id = group_id;
    }
}
