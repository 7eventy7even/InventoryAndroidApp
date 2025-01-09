// File: app/src/main/java/vkx64/android/scanventory/MainActivity.java

package vkx64.android.scanventory;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import vkx64.android.scanventory.adapter.BreadcrumbAdapter;
import vkx64.android.scanventory.adapter.MainAdapter;
import vkx64.android.scanventory.database.AppClient;
import vkx64.android.scanventory.database.AppDatabase;
import vkx64.android.scanventory.database.DaoGroups;
import vkx64.android.scanventory.database.TableGroups;
import vkx64.android.scanventory.database.TableItems;
import vkx64.android.scanventory.database.TableOrders;
import vkx64.android.scanventory.dialog.AddGroupDialogFragment;
import vkx64.android.scanventory.dialog.AddItemDialogFragment;
import vkx64.android.scanventory.model.Breadcrumb;
import vkx64.android.scanventory.utilities.FileHelper;
import vkx64.android.scanventory.utilities.SingleImagePicker;

public class MainActivity extends AppCompatActivity {

    // Views
    private RecyclerView rvItemList;
    private SwipeRefreshLayout srItemList;
    private ImageButton ibNewFolder, ibNewItem, ivSettings;
    private EditText etSearch;
    private CardView cvNewOrder, cvOrderHistory;

    // Breadcrumb Adapter
    private RecyclerView rvBreadcrumbs;
    private BreadcrumbAdapter breadcrumbAdapter;
    private final List<Breadcrumb> breadcrumbTrail = new ArrayList<>();
    private static final String DEFAULT_BREADCRUMB = "Home";

    // Adapter
    private MainAdapter mainAdapter;

    // Executor for Thread Operations
    private final Executor executor = Executors.newSingleThreadExecutor();

    // Current group and navigation stack
    private String currentGroupId = null;
    private final Stack<String> navigationStack = new Stack<>();

    private final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews(); // Set up views
        initializeBackPress(); // Handle back navigation

        loadItems(); // Load the root items
    }

    /**
     * Initialize views and listeners.
     */
    private void initializeViews() {
        rvItemList = findViewById(R.id.rvItemList);
        rvItemList.setLayoutManager(new LinearLayoutManager(this));

        srItemList = findViewById(R.id.srItemList);
        srItemList.setOnRefreshListener(this::loadItems);

        ibNewFolder = findViewById(R.id.ibNewFolder);
        ibNewFolder.setOnClickListener(v -> showAddGroupDialog());

        ibNewItem = findViewById(R.id.ibNewItem);
        ibNewItem.setOnClickListener(v -> showAddItemDialog());

        ivSettings = findViewById(R.id.ivSettings);
        ivSettings.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));

        cvNewOrder = findViewById(R.id.cvNewOrder);
        cvNewOrder.setOnClickListener(v -> newOrder());

        cvOrderHistory = findViewById(R.id.cvOrderHistory);
        cvOrderHistory.setOnClickListener(v -> startActivity(new Intent(this, OrderHistoryActivity.class)));

        etSearch = findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action needed before text changes
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                handleSearch(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No action needed after text changes
            }
        });

        rvBreadcrumbs = findViewById(R.id.rvBreadcrumbs);

        // Initialize breadcrumb trail with Home
        breadcrumbTrail.add(new Breadcrumb(DEFAULT_BREADCRUMB, null));

        rvBreadcrumbs.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        breadcrumbAdapter = new BreadcrumbAdapter(breadcrumbTrail, position -> navigateToBreadcrumb(position));
        rvBreadcrumbs.setAdapter(breadcrumbAdapter);
    }

    private void newOrder() {
        Intent intent = new Intent(this, OrderActivity.class);
        startActivity(intent);
    }

    /**
     * Load groups and items for the current group.
     */
    public void loadItems() {
        executor.execute(() -> {
            // Fetch subgroups and items for the current group
            List<TableGroups> groups = fetchSubGroups(currentGroupId);
            List<TableItems> items = fetchItems(currentGroupId);

            // Preload group and item images
            for (TableGroups group : groups) {
                String groupImagePath = FileHelper.getGroupImage(getApplicationContext(), group.getGroup_id());
                if (groupImagePath != null) {
                    Glide.with(this).load(new File(groupImagePath)).preload();
                }
            }

            for (TableItems item : items) {
                List<String> imagePaths = FileHelper.getImagesForItem(getApplicationContext(), item.getItem_id());
                if (!imagePaths.isEmpty()) {
                    Glide.with(this).load(new File(imagePaths.get(0))).preload();
                }
            }

            // Combine groups and items into one list
            List<Object> combinedList = new ArrayList<>();

            // Add "..." to navigate to parent if not at root
            if (currentGroupId != null) {
                combinedList.add(new TableGroups("...", "...", null));
            }

            combinedList.addAll(groups);
            combinedList.addAll(items);

            runOnUiThread(() -> displayItems(combinedList));
        });
    }

    /**
     * Fetch subgroups for the current group ID.
     */
    private List<TableGroups> fetchSubGroups(String parentGroupId) {
        AppDatabase appDatabase = AppClient.getInstance(getApplicationContext()).getAppDatabase();
        if (parentGroupId == null) {
            return appDatabase.daoGroups().getRootGroups();
        } else {
            return appDatabase.daoGroups().getSubGroupsByParentId(parentGroupId);
        }
    }

    /**
     * Fetch items for the current group ID.
     */
    private List<TableItems> fetchItems(String groupId) {
        AppDatabase appDatabase = AppClient.getInstance(getApplicationContext()).getAppDatabase();
        if (groupId == null) {
            // Fetch root items where group_id is NULL
            return appDatabase.daoItems().getRootItems();
        } else {
            // Fetch items by groupId
            return appDatabase.daoItems().getItemsByGroupId(groupId);
        }
    }

    /**
     * Display groups and items in the RecyclerView.
     */
    private void displayItems(List<Object> items) {
        if (mainAdapter != null) {
            mainAdapter.updateItems(items);
            srItemList.setRefreshing(false);
            return;
        }

        mainAdapter = new MainAdapter(this, items, new MainAdapter.MainClickListener() {
            @Override
            public void onGroupClick(TableGroups group) {
                handleGroupClick(group);
            }

            @Override
            public void onProductClick(TableItems item) {
                handleItemClick(item);
            }

            @Override
            public void onGroupLongClick(TableGroups group) {
                confirmGroupDeletion(group);
            }

            @Override
            public void onProductLongClick(TableItems item) {
                confirmItemDeletion(item);
            }

            @Override
            public void onThreeDotMenuClick(TableGroups group) {
                showGroupOptionsDialog(group);
            }
        });

        rvItemList.setAdapter(mainAdapter);
        srItemList.setRefreshing(false);
    }

    private void showGroupOptionsDialog(TableGroups group) {
        new AlertDialog.Builder(this)
                .setTitle("Group Options")
                .setItems(new String[]{"Edit Group", "Delete Group"}, (dialog, which) -> {
                    if (which == 0) {
                        // Edit Group
                        showEditGroupDialog(group);
                    } else if (which == 1) {
                        // Delete Group
                        confirmGroupDeletion(group);
                    }
                })
                .show();
    }

    private void showEditGroupDialog(TableGroups group) {
        AddGroupDialogFragment dialog = new AddGroupDialogFragment((groupId, groupName, imageUri) -> {
            executor.execute(() -> {
                try {
                    // Ensure that groupId remains unchanged
                    // Typically, groupId should not change during an edit
                    // So, avoid modifying groupId here

                    // Update the group's name
                    group.setGroup_name(groupName);

                    // If an image is provided, save it
                    if (imageUri != null) {
                        SingleImagePicker.saveImageToInternalStorage(
                                imageUri, "GroupImages", groupId + ".png", this
                        );
                    }

                    // Update the group in the database
                    AppClient.getInstance(getApplicationContext())
                            .getAppDatabase()
                            .daoGroups()
                            .updateGroup(group);

                    // Refresh the UI on the main thread
                    runOnUiThread(() -> {
                        loadItems();
                        Toast.makeText(this, "Group updated successfully", Toast.LENGTH_SHORT).show();
                    });
                } catch (Exception e) {
                    Log.e(TAG, "Error updating group", e);
                    runOnUiThread(() -> Toast.makeText(this, "Failed to update group", Toast.LENGTH_SHORT).show());
                }
            });
        }, group);

        dialog.show(getSupportFragmentManager(), "EditGroupDialog");
    }


    private void handleSearch(String query) {
        if (query.trim().isEmpty()) {
            loadItems(); // Load default data if search is empty
            return;
        }

        executor.execute(() -> {
            List<TableItems> filteredItems = new ArrayList<>();
            List<TableGroups> filteredGroups = new ArrayList<>();

            if (query.startsWith("#selling >=")) {
                Integer value = parseFilterValue(query, "#selling >=");
                if (value == null) return; // Skip invalid query
                filteredItems = AppClient.getInstance(getApplicationContext())
                        .getAppDatabase()
                        .daoItems()
                        .searchItemsBySellingGreaterThanOrEqual(value);
            } else if (query.startsWith("#selling <=")) {
                Integer value = parseFilterValue(query, "#selling <=");
                if (value == null) return; // Skip invalid query
                filteredItems = AppClient.getInstance(getApplicationContext())
                        .getAppDatabase()
                        .daoItems()
                        .searchItemsBySellingLessThanOrEqual(value);
            } else if (query.startsWith("#storage >=")) {
                Integer value = parseFilterValue(query, "#storage >=");
                if (value == null) return; // Skip invalid query
                filteredItems = AppClient.getInstance(getApplicationContext())
                        .getAppDatabase()
                        .daoItems()
                        .searchItemsByStorageGreaterThanOrEqual(value);
            } else if (query.startsWith("#storage <=")) {
                Integer value = parseFilterValue(query, "#storage <=");
                if (value == null) return; // Skip invalid query
                filteredItems = AppClient.getInstance(getApplicationContext())
                        .getAppDatabase()
                        .daoItems()
                        .searchItemsByStorageLessThanOrEqual(value);
            } else {
                // Search by name for both items and groups
                filteredItems = AppClient.getInstance(getApplicationContext())
                        .getAppDatabase()
                        .daoItems()
                        .searchItemsByName(query);
                filteredGroups = AppClient.getInstance(getApplicationContext())
                        .getAppDatabase()
                        .daoGroups()
                        .searchGroupsByName(query);
            }

            // Combine results and update the UI
            List<Object> combinedResults = new ArrayList<>();
            combinedResults.addAll(filteredGroups);
            combinedResults.addAll(filteredItems);

            runOnUiThread(() -> displayItems(combinedResults));
        });
    }

    // Parse the numeric value from the query
    private Integer parseFilterValue(String query, String prefix) {
        try {
            // Remove the prefix (e.g., "#selling >=") and parse the number
            return Integer.parseInt(query.replace(prefix, "").trim());
        } catch (NumberFormatException e) {
            // Log an error message if parsing fails (optional)
            Log.e("MainActivity", "Invalid number format in query: " + query);
            // Return null to indicate an invalid query
            return null;
        }
    }

    private void confirmGroupDeletion(TableGroups group) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Group")
                .setMessage("Are you sure you want to delete this group? All its subgroups and items will also be deleted.")
                .setPositiveButton("Delete", (dialog, which) -> deleteGroup(group))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmItemDeletion(TableItems item) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Item")
                .setMessage("Are you sure you want to delete this item?")
                .setPositiveButton("Delete", (dialog, which) -> deleteItem(item))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteGroup(TableGroups group) {
        executor.execute(() -> {
            DaoGroups daoGroups = AppClient.getInstance(getApplicationContext()).getAppDatabase().daoGroups();

            // Delete all descendants recursively
            deleteDescendants(daoGroups, group.getGroup_id());

            // Delete the parent group
            daoGroups.deleteGroup(group);

            runOnUiThread(() -> {
                Toast.makeText(this, "Group and all its descendants deleted successfully", Toast.LENGTH_SHORT).show();
                loadItems();
            });
        });
    }

    private void deleteDescendants(DaoGroups daoGroups, String parentGroupId) {
        // Fetch all direct child groups
        List<TableGroups> childGroups = daoGroups.getChildGroups(parentGroupId);

        for (TableGroups childGroup : childGroups) {
            // Recursively delete the children of the current child group
            deleteDescendants(daoGroups, childGroup.getGroup_id());

            // Delete the current child group
            daoGroups.deleteGroup(childGroup);
        }
    }

    private void deleteItem(TableItems item) {
        executor.execute(() -> {
            AppClient.getInstance(getApplicationContext())
                    .getAppDatabase()
                    .daoItems()
                    .deleteItem(item);

            runOnUiThread(() -> {
                Toast.makeText(this, "Item deleted successfully", Toast.LENGTH_SHORT).show();
                loadItems();
            });
        });
    }

    /**
     * Handle click on a group.
     */
    private void handleGroupClick(TableGroups group) {
        if (group == null || "...".equals(group.getGroup_name())) {
            // Navigate to the parent group if "..." is clicked
            if (!navigationStack.isEmpty()) {
                currentGroupId = navigationStack.pop();
                breadcrumbTrail.remove(breadcrumbTrail.size() - 1); // Remove last breadcrumb

                if (breadcrumbTrail.isEmpty()) {
                    breadcrumbTrail.add(new Breadcrumb(DEFAULT_BREADCRUMB, null)); // Reset to default breadcrumb
                }

                breadcrumbAdapter.notifyDataSetChanged();
                loadItems();
            }
            return;
        }

        // Push the current group ID to the navigation stack
        navigationStack.push(currentGroupId);

        // Add the group name and ID to the breadcrumb trail
        breadcrumbTrail.add(new Breadcrumb(group.getGroup_name(), group.getGroup_id()));
        breadcrumbAdapter.notifyDataSetChanged();

        // Update currentGroupId and load its items
        currentGroupId = group.getGroup_id();
        loadItems();

        Log.d(TAG, "Navigating into group: " + group.getGroup_name() + " with ID: " + currentGroupId);
    }

    private void navigateToBreadcrumb(int position) {
        // Log the current state of breadcrumbs and navigation stack
        Log.d(TAG, "navigateToBreadcrumb: Current breadcrumb trail: " + breadcrumbTrail);
        Log.d(TAG, "navigateToBreadcrumb: Selected breadcrumb position: " + position);

        // Remove breadcrumbs and stack elements above the selected breadcrumb
        while (breadcrumbTrail.size() > position + 1) {
            Breadcrumb removedBreadcrumb = breadcrumbTrail.remove(breadcrumbTrail.size() - 1);
            if (!navigationStack.isEmpty()) {
                navigationStack.pop(); // Pop only elements above the selected group
            }
            Log.d(TAG, "navigateToBreadcrumb: Removed breadcrumb: " + removedBreadcrumb.getName());
        }

        // Set currentGroupId to the selected breadcrumb's groupId
        Breadcrumb selectedBreadcrumb = breadcrumbTrail.get(position);
        currentGroupId = selectedBreadcrumb.getGroupId();

        Log.d(TAG, "navigateToBreadcrumb: Updated currentGroupId: " + currentGroupId);

        // Notify the breadcrumb adapter to update the UI
        breadcrumbAdapter.notifyDataSetChanged();

        // Load child items for the selected group
        Log.d(TAG, "navigateToBreadcrumb: Loading items for currentGroupId: " + currentGroupId);
        loadItems();
    }


    /**
     * Handle click on an item.
     */
    private void handleItemClick(TableItems item) {
        //TODO: To Remove Toast
        Intent intent = new Intent(this, ItemDetailsActivity.class);
        intent.putExtra("item_id", item.getItem_id());
        startActivity(intent);
    }

    /**
     * Show the "Add Group" dialog.
     */
    private void showAddGroupDialog() {
        AddGroupDialogFragment dialog = new AddGroupDialogFragment((groupId, groupName, imageUri) -> {
            TableGroups newGroup = new TableGroups(groupId, groupName, currentGroupId);

            executor.execute(() -> {
                if (imageUri != null) {
                    SingleImagePicker.saveImageToInternalStorage(
                            imageUri, "GroupImages", groupId + ".png", this
                    );
                }
                AppClient.getInstance(getApplicationContext())
                        .getAppDatabase()
                        .daoGroups()
                        .insertGroup(newGroup);
                runOnUiThread(this::loadItems);
            });
        });

        dialog.show(getSupportFragmentManager(), "AddGroupDialog");
    }

    /**
     * Show the "Add Item" dialog.
     */
    private void showAddItemDialog() {
        AddItemDialogFragment dialog = new AddItemDialogFragment((itemId, itemName, itemCategory, itemStorage, itemSelling) -> {
            // Create a new item with the currentGroupId as its parent
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy-HH:mm");
            Date currentDate = new Date();
            String formattedDate = dateFormat.format(currentDate);
            TableItems newItem = new TableItems(itemId, itemName, itemCategory, itemStorage, itemSelling, formattedDate, formattedDate, currentGroupId);
            AddNewItem(newItem);
        });

        dialog.show(getSupportFragmentManager(), "AddItemDialog");
    }

    /**
     * Add a new item to the database.
     */
    private void AddNewItem(TableItems newItem) {
        executor.execute(() -> {
            AppClient.getInstance(getApplicationContext())
                    .getAppDatabase()
                    .daoItems()
                    .insertItem(newItem);

            runOnUiThread(this::loadItems);
        });
    }

    /**
     * Handle back navigation for group hierarchy.
     */
    private void initializeBackPress() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (navigationStack.isEmpty()) {
                    finish();
                    return;
                }

                // Navigate to the parent group
                currentGroupId = navigationStack.pop();
                if (!breadcrumbTrail.isEmpty()) {
                    breadcrumbTrail.remove(breadcrumbTrail.size() - 1); // Remove last breadcrumb
                }

                breadcrumbAdapter.notifyDataSetChanged();
                loadItems();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadItems(); // Refresh the RecyclerView
    }
}
