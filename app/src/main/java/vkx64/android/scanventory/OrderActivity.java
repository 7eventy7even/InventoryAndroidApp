package vkx64.android.scanventory;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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
import java.util.Map;
import java.util.concurrent.Executors;

import vkx64.android.scanventory.adapter.ScannedItemsAdapter;
import vkx64.android.scanventory.database.AppClient;
import vkx64.android.scanventory.database.AppDatabase;
import vkx64.android.scanventory.database.DaoItems;
import vkx64.android.scanventory.database.DaoOrderItems;
import vkx64.android.scanventory.database.TableItems;
import vkx64.android.scanventory.database.TableOrderItems;
import vkx64.android.scanventory.database.TableOrders;
import vkx64.android.scanventory.utilities.QRScanner;

public class OrderActivity extends AppCompatActivity implements QRScanner.QRScannerCallback  {

    private int orderId;
    private TextView tvHeader;
    private Map<String, Integer> scannedItems = new HashMap<>();
    QRScanner qrScanner;

    private RecyclerView rvItemList;
    private ScannedItemsAdapter adapter;

    private CardView cvCompleteOrder;

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
        generateOrderId();
        initializeViews();

        // Initialize QRScanner
        DecoratedBarcodeView bsScanner = findViewById(R.id.bsScanner);
        qrScanner = new QRScanner(this, findViewById(R.id.bsScanner), this);
        qrScanner.initialize();
    }

    // Initialize RecyclerView
    private void initializeRecyclerView() {
        rvItemList = findViewById(R.id.rvItemList);
        rvItemList.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ScannedItemsAdapter(this, scannedItems);
        rvItemList.setAdapter(adapter);
    }

    private void initializeViews() {
        tvHeader = findViewById(R.id.tvHeader);
        tvHeader.setText("Order ID: " + orderId);

        cvCompleteOrder = findViewById(R.id.cvCompleteOrder);
        cvCompleteOrder.setOnClickListener(v -> completeOrder());
    }

    private void generateOrderId() {
        Executors.newSingleThreadExecutor().execute(() -> {
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
        new Thread(() -> {
            AppDatabase db = AppClient.getInstance(getApplicationContext()).getAppDatabase();
            DaoOrderItems daoOrderItems = db.daoOrderItems();
            DaoItems daoItems = db.daoItems();

            // Save scanned items to database
            for (Map.Entry<String, Integer> entry : scannedItems.entrySet()) {
                String itemId = entry.getKey();
                int quantity = entry.getValue();

                // Save order item
                TableOrderItems orderItem = new TableOrderItems(orderId, itemId, quantity);
                daoOrderItems.insertOrderItem(orderItem);

                // Update item quantities
                TableItems item = daoItems.getItemById(itemId);
                if (item != null) {
                    item.setItem_selling(item.getItem_selling() - quantity);
                    item.setItem_storage(item.getItem_storage() - quantity);
                    daoItems.updateItem(item);
                }
            }

            runOnUiThread(() -> {
                Toast.makeText(this, "Order Completed", Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }

    @Override
    public void onScanned(String scannedValue) {
        if (scannedValue == null || scannedValue.isEmpty()) {
            Toast.makeText(this, "Invalid QR code", Toast.LENGTH_SHORT).show();
            return;
        }

        // Increment scanned item quantity
        scannedItems.put(scannedValue, scannedItems.getOrDefault(scannedValue, 0) + 1);

        // Notify the adapter to refresh the list
        adapter.refreshData();

        Toast.makeText(this, "Scanned: " + scannedValue, Toast.LENGTH_SHORT).show();
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        qrScanner.handlePermissionResult(requestCode, grantResults);
    }
}