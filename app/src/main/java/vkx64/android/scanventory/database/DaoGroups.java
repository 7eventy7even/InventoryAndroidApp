package vkx64.android.scanventory.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DaoGroups {

    @Query("SELECT * FROM groups")
    List<TableGroups> getAllGroups();

    @Insert
    void insertGroup(TableGroups group);

    @Delete
    void deleteGroup(TableGroups group);

    @Query("SELECT * FROM groups WHERE group_parent = :parentId")
    List<TableGroups> getSubGroupsByParentId(String parentId);

    @Query("SELECT * FROM groups WHERE group_name LIKE '%' || :query || '%'")
    List<TableGroups> searchGroupsByName(String query);

    // Insert or update a group (used during import)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdateGroup(TableGroups group);

    // Get a group by ID (used to validate parent group relationships during import)
    @Query("SELECT * FROM groups WHERE group_id = :groupId LIMIT 1")
    TableGroups getGroupById(String groupId);
}
