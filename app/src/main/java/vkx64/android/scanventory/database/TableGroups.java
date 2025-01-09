package vkx64.android.scanventory.database;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "groups")
public class TableGroups {
    // Columns
    @PrimaryKey(autoGenerate = false)
    @NonNull
    public String group_id;
    public String group_name;

    @Nullable
    public String group_parent;

    // Constructor
    public TableGroups(@NonNull String group_id, String group_name, @Nullable String group_parent) {
        this.group_id = group_id;
        this.group_name = group_name;
        this.group_parent = group_parent;
    }

    // Getters and Setters
    @NonNull
    public String getGroup_id() {
        return group_id;
    }

    public void setGroup_id(@NonNull String group_id) {
        this.group_id = group_id;
    }

    public String getGroup_name() {
        return group_name;
    }

    public void setGroup_name(String group_name) {
        this.group_name = group_name;
    }

    @Nullable
    public String getGroup_parent() {
        return group_parent;
    }

    public void setGroup_parent(@Nullable String group_parent) {
        this.group_parent = group_parent;
    }
}
