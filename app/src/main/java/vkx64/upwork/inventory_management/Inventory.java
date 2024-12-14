package vkx64.upwork.inventory_management;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import vkx64.upwork.inventory_management.adapters.InventoryAdapter;
import vkx64.upwork.inventory_management.database.AppDatabase;
import vkx64.upwork.inventory_management.database.InventoryItem;
import vkx64.upwork.inventory_management.database.MyDatabase;
import vkx64.upwork.inventory_management.database.Order;

public class Inventory extends AppCompatActivity {

    RecyclerView rvDataTable;
    Button btnNewOrder;
    private AppDatabase db;
    private InventoryAdapter adapter;

    private final String TAG = "InventoryActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_inventory);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        InitializeObjects();

        db = MyDatabase.getDatabase();
        setupRecyclerView();
    }

    private void InitializeObjects() {
        rvDataTable = findViewById(R.id.rvDataTable);
        btnNewOrder = findViewById(R.id.btnNewOrder);
        btnNewOrder.setOnClickListener(v -> GenerateOrder());
    }

    private void setupRecyclerView() {
        List<InventoryItem> itemList = db.inventoryDao().getAll();
        adapter = new InventoryAdapter(itemList);

        adapter.setOnItemClickListener(item -> {
            Intent intent = new Intent(this, ItemDetails.class);
            intent.putExtra("product_id", item.product_id);
            startActivity(intent);
        });

        rvDataTable.setAdapter(adapter);
        rvDataTable.setLayoutManager(new LinearLayoutManager(this));
    }

    private void GenerateOrder() {
        AppDatabase db = MyDatabase.getDatabase();
        Order newOrder = new Order();

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        Calendar calendar = Calendar.getInstance();
        newOrder.order_date = dateFormat.format(calendar.getTime());

        newOrder.order_name = "default name";

        long newOrderId = db.orderDao().insertOrder(newOrder);
        int intOrderID = (int) newOrderId;

        Intent intent = new Intent(this, OrderActivity.class);
        intent.putExtra("order_id", intOrderID);
        Log.i(TAG, "GenerateOrder: " + intOrderID);
        startActivity(intent);
    }
}