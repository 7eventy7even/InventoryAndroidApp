package vkx64.android.scanventory;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
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

import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import vkx64.android.scanventory.adapter.ScannedItemsAdapter;
import vkx64.android.scanventory.database.AppClient;
import vkx64.android.scanventory.database.AppDatabase;
import vkx64.android.scanventory.database.DaoItems;
import vkx64.android.scanventory.database.DaoMarkets;
import vkx64.android.scanventory.database.DaoOrderItems;
import vkx64.android.scanventory.database.DaoOrders;
import vkx64.android.scanventory.database.TableItems;
import vkx64.android.scanventory.database.TableMarkets;
import vkx64.android.scanventory.database.TableOrderItems;
import vkx64.android.scanventory.database.TableOrders;
import vkx64.android.scanventory.utilities.QRScanner;

public class OrderActivity extends AppCompatActivity implements QRScanner.QRScannerCallback  {

    private int orderId;
    private Map<String, Integer> scannedItems = new HashMap<>();
    QRScanner qrScanner;

    private RecyclerView rvItemList;
    private ScannedItemsAdapter adapter;

    private ImageButton ibLeftButton;

    private CardView cvCompleteOrder, cvCancelOrder;

    private Spinner spinnerMarkets;
    private String selectedMarketName = null;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeRecyclerView();
        initializeViews();

        // Initialize QRScanner
        DecoratedBarcodeView bsScanner = findViewById(R.id.bsScanner);
        qrScanner = new QRScanner(this, findViewById(R.id.bsScanner), this);
        qrScanner.initialize();

        // Disable scanning until marketplace is selected
        qrScanner.pauseScanner();
    }

    // Initialize RecyclerView
    private void initializeRecyclerView() {
        rvItemList = findViewById(R.id.rvItemList);
        rvItemList.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ScannedItemsAdapter(this, scannedItems, selectedMarketName);
        rvItemList.setAdapter(adapter);
    }

    private void initializeViews() {
        cvCancelOrder = findViewById(R.id.cvCancelOrder);
        cvCancelOrder.setOnClickListener(v -> {
            // Optionally, confirm cancellation with the user
            new AlertDialog.Builder(this)
                    .setTitle("Cancel Order")
                    .setMessage("Are you sure you want to cancel this order?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Clear scanned items
                        scannedItems.clear();
                        adapter.refreshData();
                        finish();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        ibLeftButton = findViewById(R.id.ibLeftButton);
        ibLeftButton.setOnClickListener(v -> finish());

        cvCompleteOrder = findViewById(R.id.cvCompleteOrder);
        cvCompleteOrder.setOnClickListener(v -> {
            if (scannedItems.isEmpty()) {
                Toast.makeText(this, "Order is Empty!", Toast.LENGTH_SHORT).show();
            } else {
                completeOrder();
            }
        });

        spinnerMarkets = findViewById(R.id.spinnerMarkets);
        populateMarketSpinner();
    }

    private void populateMarketSpinner() {
        executor.execute(() -> {
            AppDatabase db = AppClient.getInstance(getApplicationContext()).getAppDatabase();
            DaoMarkets daoMarkets = db.daoMarkets();

            // Fetch all unique market names
            List<String> marketNames = daoMarkets.getAllUniqueMarketNames();

            runOnUiThread(() -> {
                if (marketNames != null && !marketNames.isEmpty()) {
                    // Create an ArrayAdapter using the default spinner layout
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, marketNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerMarkets.setAdapter(adapter);

                    // Set the selection listener
                    spinnerMarkets.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            selectedMarketName = parent.getItemAtPosition(position).toString();
                            Toast.makeText(OrderActivity.this, "Selected Market: " + selectedMarketName, Toast.LENGTH_SHORT).show();

                            // Update the adapter with the new selected market
                            OrderActivity.this.adapter.setSelectedMarketName(selectedMarketName);

                            // Enable scanning
                            qrScanner.resumeScanner();
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                            selectedMarketName = null;

                            // Update the adapter to clear the selected market
                            OrderActivity.this.adapter.setSelectedMarketName(null);

                            // Disable scanning
                            qrScanner.pauseScanner();
                        }
                    });

                    // Optionally, set a default selection or prompt
                    spinnerMarkets.setSelection(0); // Select the first marketplace by default
                } else {
                    Toast.makeText(this, "No marketplaces found.", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void generateOrderId() {
        executor.execute(() -> {
            // Get the database instance from AppClient
            AppDatabase appDatabase = AppClient.getInstance(getApplicationContext()).getAppDatabase();

            // Create a new order
            String currentDate = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new Date());
            TableOrders newOrder = new TableOrders(0, currentDate);

            // Insert the order and retrieve the generated ID
            long generatedId = appDatabase.daoOrders().insertOrder(newOrder);
            orderId = (int) generatedId;
        });
    }

    private void completeOrder() {
        // Ensure a marketplace is selected
        if (selectedMarketName == null) {
            Toast.makeText(this, "Please select a marketplace before completing the order.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Ensure there are scanned items
        if (scannedItems.isEmpty()) {
            Toast.makeText(this, "No items scanned to complete the order.", Toast.LENGTH_SHORT).show();
            return;
        }

        executor.execute(() -> {
            AppDatabase db = AppClient.getInstance(getApplicationContext()).getAppDatabase();
            DaoOrderItems daoOrderItems = db.daoOrderItems();
            DaoMarkets daoMarkets = db.daoMarkets();
            DaoOrders daoOrders = db.daoOrders();
            DaoItems daoItems = db.daoItems();

            try {
                db.runInTransaction(() -> {
                    // Create a new order
                    String currentDate = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new Date());
                    TableOrders newOrder = new TableOrders(0, currentDate);

                    // Insert the order and retrieve the generated ID
                    long generatedId = daoOrders.insertOrder(newOrder);
                    orderId = (int) generatedId;

                    // Save scanned items to database
                    for (Map.Entry<String, Integer> entry : scannedItems.entrySet()) {
                        String itemId = entry.getKey();
                        int quantity = entry.getValue();

                        // Fetch the corresponding market entry for the selected marketplace and item
                        TableMarkets marketEntry = daoMarkets.getMarketByItemIdAndMarketName(itemId, selectedMarketName);

                        if (marketEntry == null) {
                            // This should not happen as we validated during scanning, but handle it just in case
                            runOnUiThread(() -> Toast.makeText(this, "Market entry not found for item: " + itemId + " in marketplace: " + selectedMarketName, Toast.LENGTH_SHORT).show());
                            continue;
                        }

                        // **Re-validate Quantity to Handle Concurrent Orders**
                        if (marketEntry.getMarket_quantity() < quantity) {
                            runOnUiThread(() -> Toast.makeText(this, "Insufficient quantity for item: " + itemId + " in marketplace: " + selectedMarketName, Toast.LENGTH_SHORT).show());
                            continue;
                        }

                        // Save order item
                        TableOrderItems orderItem = new TableOrderItems(orderId, itemId, quantity);
                        daoOrderItems.insertOrderItem(orderItem);

                        // Update market quantities
                        marketEntry.setMarket_quantity(marketEntry.getMarket_quantity() - quantity);
                        daoMarkets.updateMarket(marketEntry);

                        TableItems itemEntry = daoItems.getItemById(itemId);
                        itemEntry.setItem_storage(itemEntry.getItem_storage() - quantity);
                        daoItems.updateItem(itemEntry);
                    }
                });

                // Update UI on the main thread
                runOnUiThread(() -> {
                    Toast.makeText(this, "Order Completed Successfully!", Toast.LENGTH_SHORT).show();
                    // Clear scanned items
                    scannedItems.clear();
                    adapter.refreshData();
                    finish();
                });
            } catch (Exception e) {
                Log.e("OrderActivity", "Failed to complete order", e);
                runOnUiThread(() -> Toast.makeText(this, "Failed to complete order. Please try again.", Toast.LENGTH_SHORT).show());
            }
        });
    }

    @Override
    public void onScanned(String scannedValue) {
        if (scannedValue == null || scannedValue.isEmpty()) {
            runOnUiThread(() -> Toast.makeText(this, "Invalid QR code", Toast.LENGTH_SHORT).show());
            return;
        }

        // Ensure a marketplace is selected
        if (selectedMarketName == null) {
            runOnUiThread(() -> Toast.makeText(this, "Please select a marketplace before scanning items.", Toast.LENGTH_SHORT).show());
            return;
        }

        executor.execute(() -> {
            AppDatabase db = AppClient.getInstance(getApplicationContext()).getAppDatabase();
            DaoItems daoItems = db.daoItems();
            DaoMarkets daoMarkets = db.daoMarkets();

            // Check if the item exists in the selected marketplace
            TableMarkets marketEntry = daoMarkets.getMarketByItemIdAndMarketName(scannedValue, selectedMarketName);

            if (marketEntry == null) {
                // Item does not exist in the selected marketplace
                runOnUiThread(() -> Toast.makeText(this, "Item '" + scannedValue + "' does not exist in '" + selectedMarketName + "'.", Toast.LENGTH_SHORT).show());
                return;
            }

            if (marketEntry.getMarket_quantity() <= 0) {
                // No available quantity for the item in the selected marketplace
                runOnUiThread(() -> Toast.makeText(this, "No available quantity for item '" + scannedValue + "' in '" + selectedMarketName + "'.", Toast.LENGTH_SHORT).show());
                return;
            }

            // Fetch the item details
            TableItems item = daoItems.getItemById(scannedValue);

            if (item == null) {
                // Item does not exist in the items table
                runOnUiThread(() -> Toast.makeText(this, "Item '" + scannedValue + "' does not exist in the inventory.", Toast.LENGTH_SHORT).show());
                return;
            }

            // **Validation: Prevent Scanning More Than Available Quantity**
            if (scannedItems.containsKey(scannedValue)) {
                int alreadyScanned = scannedItems.get(scannedValue);
                if (alreadyScanned >= marketEntry.getMarket_quantity()) {
                    runOnUiThread(() -> Toast.makeText(this, "Cannot scan more of '" + scannedValue + "' than available in '" + selectedMarketName + "'.", Toast.LENGTH_SHORT).show());
                    return;
                }
            }

            // Increment scanned item quantity
            scannedItems.put(scannedValue, scannedItems.getOrDefault(scannedValue, 0) + 1);

            // Notify the adapter to refresh the list
            runOnUiThread(() -> {
                adapter.refreshData();
                Toast.makeText(this, "Scanned: " + scannedValue, Toast.LENGTH_SHORT).show();
            });
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        qrScanner.pauseScanner();
    }

    @Override
    protected void onResume() {
        super.onResume();
        qrScanner.resumeScanner();
    }

    // Shutdown the executor when the activity is destroyed to avoid leaks
    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        qrScanner.handlePermissionResult(requestCode, grantResults);
    }
}