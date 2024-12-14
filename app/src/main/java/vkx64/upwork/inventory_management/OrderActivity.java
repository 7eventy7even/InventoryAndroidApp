package vkx64.upwork.inventory_management;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.BarcodeView;

import java.util.List;

import vkx64.upwork.inventory_management.adapters.ItemAdapter;
import vkx64.upwork.inventory_management.adapters.OrderAdapter;
import vkx64.upwork.inventory_management.database.AppDatabase;
import vkx64.upwork.inventory_management.database.InventoryItem;
import vkx64.upwork.inventory_management.database.MyDatabase;
import vkx64.upwork.inventory_management.database.OrderList;

public class OrderActivity extends AppCompatActivity {

    private ItemAdapter adapter;
    private Button btnStartScan;
    private RecyclerView rvOrderList;
    private AppDatabase db;
    private SwipeRefreshLayout swipeRefreshLayout;

    private int orderID;

    private final String TAG = "OrderActivity";
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1;

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

        // Check if the camera permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Request permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            // Permission already granted, initialize scanner
            InitializeObjects();
        }

        orderID = getIntent().getIntExtra("order_id", -1);
        Log.i(TAG, "IntentRecieved: " + orderID);

        InitializeObjects();
        db = MyDatabase.getDatabase();
    }

    private void InitializeObjects() {
        btnStartScan = findViewById(R.id.btnStartScan);
        btnStartScan.setOnClickListener(v -> showQRScannerPopup());

        rvOrderList = findViewById(R.id.rvOrderList);

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadOrderItems();
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void loadOrderItems() {
        // Query inventory items linked to the given order ID
        List<InventoryItem> itemList = db.orderListDao().getInventoryItemsByOrderId(orderID);

        // Pass the filtered list to the adapter
        adapter = new ItemAdapter(itemList);
        rvOrderList.setAdapter(adapter);

        if (rvOrderList.getLayoutManager() == null) {
            rvOrderList.setLayoutManager(new LinearLayoutManager(this));
        }

        adapter.setOnItemClickListener(item -> {
            // Handle item click (optional)
            Toast.makeText(this, "Clicked on: " + item.product_name, Toast.LENGTH_SHORT).show();
        });
    }

    private void showQRScannerPopup() {
        // Create a dialog for the QR scanner
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.qr_scanner);
        dialog.setCancelable(true);

        // Initialize BarcodeView
        BarcodeView barcodeView = dialog.findViewById(R.id.barcodeView);
        barcodeView.resume(); // Start camera preview

        // Handle QR scan results
        barcodeView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (result != null) {
                    String scannedContent = result.getText();
                    Toast.makeText(OrderActivity.this, "Scanned: " + scannedContent, Toast.LENGTH_SHORT).show();

                    playBeepSound();

                    // Example: Update inventory (replace this logic with your app's behavior)
                    InventoryItem item = db.inventoryDao().findById(Integer.parseInt(scannedContent));
                    if (item != null) {
                        item.selling_quantity--;
                        item.storage_quantity--;
                        db.inventoryDao().updateItem(item);

                        OrderList orderList = new OrderList();
                        orderList.order_id = orderID;
                        orderList.product_id = Integer.parseInt(scannedContent);
                        orderList.quantity = 1;
                        db.orderListDao().insertOrderList(orderList);

                        loadOrderItems();
                    }

                    // Pause scanning briefly to prevent duplicates
                    barcodeView.pause();

                    // Resume the scanner after a short delay (e.g., 1 second)
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            barcodeView.resume();  // Resume scanning
                        }
                    }, 1000);
                }
            }

            @Override
            public void possibleResultPoints(List<ResultPoint> resultPoints) {
                // Optional: Handle visual feedback for possible QR code positions
            }
        });

        dialog.show();
    }

    // Method to play the beep sound
    private void playBeepSound() {
        // MediaPlayer to play a beep sound
        MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.beep);
        if (mediaPlayer != null) {
            mediaPlayer.start();  // Play the beep sound
            mediaPlayer.setOnCompletionListener(mp -> mp.release());
        }
    }

    // Handle the permission request response
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, initialize the scanner
                InitializeObjects();
            } else {
                // Permission denied, show a message to the user
                Toast.makeText(this, "Camera permission is required to scan QR codes",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}